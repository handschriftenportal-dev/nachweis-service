package de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre;

import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp.IN_TRANSACTION;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp.MANUAL;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.jta.CommonXAResource.getTransactionId;
import static jakarta.transaction.Status.STATUS_ACTIVE;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre.SperreBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreRepositoryException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Provide functionality required in the multiuser environment for parallel document processing
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 17.08.21
 */
@ApplicationScoped
@Slf4j
public class DokumentSperreService implements DokumentSperreBoundary, Serializable {

  private static final Map<String, SperreAccessXAResource> ACTIVE_TRANSACTIONS =
      new ConcurrentHashMap<>();
  @Serial
  private static final long serialVersionUID = 6437463908874191911L;

  private final SperreRepository sperreRepository;
  private final TransactionManager transactionManager;

  private final int expirationMinutes;

  @Inject
  public DokumentSperreService(
      TransactionManager transactionManager,
      SperreRepository sperreRepository,
      @ConfigProperty(name = "sperre.cleanup.expirationMinutes", defaultValue = "30")
      int expirationMinutes) {
    this.transactionManager = transactionManager;
    this.sperreRepository = sperreRepository;
    this.expirationMinutes = expirationMinutes;
  }

  @Override
  @Transactional
  public Sperre acquireSperre(
      Bearbeiter bearbeiter,
      SperreTyp sperreTyp,
      String sperreGrund,
      SperreEintrag... sperreEintraege)
      throws DokumentSperreException, SperreAlreadyExistException {

    if (bearbeiter == null || bearbeiter.getBearbeitername() == null) {
      throw new DokumentSperreException(
          "Required parameter 'bearbeiter' should be not null or empty.");
    }

    if (sperreEintraege == null || sperreEintraege.length == 0) {
      throw new DokumentSperreException(
          "Required parameter 'sperreEintraege' should be not null or empty.");
    }

    return switch (sperreTyp) {
      case IN_TRANSACTION -> acquireSperreInTransaction(bearbeiter, sperreGrund, sperreEintraege);
      case MANUAL -> acquireSperreManual(bearbeiter, sperreGrund, sperreEintraege);
      case APPROVAL -> acquireSperreApproval(bearbeiter, sperreGrund, sperreEintraege);
    };
  }

  /**
   * Acquires an {@code IN_TRANSACTION} Sperre that is automatically released when the outer JTA
   * transaction commits or rolls back.
   *
   * <p>Performs a pre-flight check that the calling thread is bound to an active JTA transaction
   * and aborts with {@link DokumentSperreException} if not — persisting a Sperre row without
   * subsequently enlisting it via {@link SperreAccessXAResource} would leave an orphan row in the
   * database, since persist and enlist must be atomic for the cleanup-on-rollback contract to
   * hold.
   *
   * @throws DokumentSperreException     if no active JTA transaction is bound to the calling
   *                                     thread, or on infrastructure failures during persistence.
   * @throws SperreAlreadyExistException if any of the provided {@link SperreEintrag}s is already
   *                                     locked by another transaction.
   */
  @Transactional
  Sperre acquireSperreInTransaction(
      Bearbeiter bearbeiter, String sperreGrund, SperreEintrag... sperreEintraege)
      throws DokumentSperreException, SperreAlreadyExistException {

    final SperreTyp sperreTyp = IN_TRANSACTION;

    try {
      Transaction jtaTransaction = transactionManager.getTransaction();
      if (jtaTransaction == null || jtaTransaction.getStatus() != STATUS_ACTIVE) {
        int status = jtaTransaction == null ? -1 : jtaTransaction.getStatus();
        throw new DokumentSperreException(
            "Cannot acquire IN_TRANSACTION Sperre — outer JTA transaction not active (status="
                + status + "). Persist+enlist must be atomic; aborting before persist.");
      }
      String transactionId = getTransactionId(jtaTransaction);

      List<Sperre> conflictSperren =
          findConflictSperrenForTransaction(transactionId, sperreEintraege);
      if (conflictSperren != null && !conflictSperren.isEmpty()) {
        throw new SperreAlreadyExistException(
            "One or more of the provided document entries are already locked!"
                + "\nFor SperreTyp='"
                + sperreTyp
                + "',\nbenutzer='"
                + bearbeiter.getId()
                + "',\nsperreGrund='"
                + sperreGrund
                + "',\nsperreEintraege="
                + sperreEintraege.length
                + "',\n=conflicts"
                + conflictSperren.stream()
                .map(s -> s.getTxId() + " " + s.getSperreGrund())
                .collect(Collectors.joining()),
            conflictSperren);
      }

      Sperre sperre;
      SperreBuilder sperreBuilder =
          Sperre.newBuilder()
              .withSperreTyp(sperreTyp)
              .withSperreGrund(sperreGrund)
              .withBearbeiter(bearbeiter)
              .withTxId(transactionId);

      Collection<Sperre> activeSperreInCurrentTransaction =
          sperreRepository.findByTransactionId(transactionId);
      if (activeSperreInCurrentTransaction.isEmpty()) {
        Stream.of(sperreEintraege).forEach(sperreBuilder::addSperreEintrag);
        sperre = sperreBuilder.build();
        saveSperreInNewTransaction(sperre);
        enlistSperre(sperre);
        log.info(
            "Register Sperre({}) for bearbeiter = {} in TransactionId {} : {}",
            sperreTyp,
            bearbeiter.getId(),
            transactionId,
            sperreEintraege.length);
      } else {
        Set<SperreEintrag> sperreEintragSet =
            getNotLockedSperreEintraege(activeSperreInCurrentTransaction, sperreEintraege);
        if (sperreEintragSet.isEmpty()) {
          sperre = (Sperre) activeSperreInCurrentTransaction.toArray()[0];
          log.info("Requested documents already locked in the current transaction");
        } else {
          sperreBuilder.withSperreEintraege(sperreEintragSet);
          sperre = sperreBuilder.build();
          saveSperreInNewTransaction(sperre);
          enlistSperre(sperre);
          log.info(
              "Register Sperre({}) for bearbeiter = {} in TransactionId {} : {}",
              sperreTyp,
              bearbeiter,
              transactionId,
              sperreEintraege);
        }
      }
      return sperre;
    } catch (SperreAlreadyExistException | DokumentSperreException e) {
      throw e;
    } catch (Exception ex) {
      throw new DokumentSperreException(
          "Unable to acquire lock for "
              + Arrays.toString(sperreEintraege)
              + " due to "
              + ex.getMessage(),
          ex);
    }
  }

  @Transactional
  Sperre acquireSperreManual(
      Bearbeiter bearbeiter, String sperreGrund, SperreEintrag... sperreEintraege)
      throws DokumentSperreException, SperreAlreadyExistException {

    final SperreTyp sperreTyp = MANUAL;
    try {
      List<Sperre> conflictSperren =
          findConflictSperrenForBearbeiter(bearbeiter.getBearbeitername(), sperreEintraege);
      if (conflictSperren != null && !conflictSperren.isEmpty()) {
        throw new SperreAlreadyExistException(
            "One or more of the provided document entries are already locked!"
                + "\nFor SperreTyp='"
                + sperreTyp
                + "',\nbenutzer='"
                + bearbeiter.getId()
                + "',\nsperreGrund='"
                + sperreGrund
                + "',\nsperreEintraege="
                + sperreEintraege.length,
            conflictSperren);
      }

      Sperre sperre;
      SperreBuilder sperreBuilder =
          Sperre.newBuilder()
              .withSperreTyp(sperreTyp)
              .withSperreGrund(sperreGrund)
              .withBearbeiter(bearbeiter);

      Collection<Sperre> activeSperrenForBearbeiter =
          sperreRepository.findByBearbeiter(bearbeiter.getBearbeitername());
      if (activeSperrenForBearbeiter.isEmpty()) {
        sperreBuilder.withSperreEintraege(Set.of(sperreEintraege));
        sperre = sperreBuilder.build();
        saveSperreInNewTransaction(sperre);
        log.info(
            "Register manual Sperre({}) for bearbeiter = {} : {}",
            sperreTyp,
            bearbeiter.getId(),
            sperreEintraege.length);
      } else {
        Set<SperreEintrag> sperreEintragSet =
            getNotLockedSperreEintraege(activeSperrenForBearbeiter, sperreEintraege);
        if (sperreEintragSet.isEmpty()) {
          sperre = (Sperre) activeSperrenForBearbeiter.toArray()[0];
          log.info("Requested documents already locked for the current bearbeiter");
        } else {
          sperreBuilder.withSperreEintraege(sperreEintragSet);
          sperre = sperreBuilder.build();
          saveSperreInNewTransaction(sperre);
          log.info(
              "Register manual Sperre({}) for bearbeiter = {}: {}",
              sperreTyp,
              bearbeiter,
              sperreEintraege);
        }
      }

      return sperre;
    } catch (SperreAlreadyExistException sae) {
      throw sae;
    } catch (Exception ex) {
      throw new DokumentSperreException(
          "Unable to acquire lock for "
              + Arrays.toString(sperreEintraege)
              + " due to "
              + ex.getMessage(),
          ex);
    }
  }

  @Transactional
  Sperre acquireSperreApproval(
      Bearbeiter bearbeiter, String sperreGrund, SperreEintrag... sperreEintraege)
      throws DokumentSperreException, SperreAlreadyExistException {

    final SperreTyp sperreTyp = SperreTyp.APPROVAL;
    try {
      List<Sperre> existingSperren =
          sperreRepository.findBySperreEintraege(sperreEintraege);
      if (existingSperren != null && !existingSperren.isEmpty()) {
        throw new SperreAlreadyExistException(
            "One or more of the provided document entries are already locked!"
                + "\nFor SperreTyp='"
                + sperreTyp
                + "',\nbenutzer='"
                + bearbeiter.getId()
                + "',\nsperreGrund='"
                + sperreGrund
                + "',\nsperreEintraege="
                + sperreEintraege.length,
            existingSperren);
      }

      Sperre sperre = Sperre.newBuilder()
          .withSperreTyp(sperreTyp)
          .withSperreGrund(sperreGrund)
          .withBearbeiter(bearbeiter)
          .withSperreEintraege(Set.of(sperreEintraege))
          .build();
      sperreRepository.persistAndFlush(sperre);
      log.info("Register approval Sperre for bearbeiter = {} : {}",
          bearbeiter.getId(), sperreEintraege.length);
      return sperre;
    } catch (SperreAlreadyExistException sae) {
      throw sae;
    } catch (Exception ex) {
      throw new DokumentSperreException(
          "Unable to acquire approval lock for "
              + Arrays.toString(sperreEintraege)
              + " due to "
              + ex.getMessage(),
          ex);
    }
  }

  @Override
  public boolean releaseSperre(Sperre sperreToRelease) {
    boolean result = false;
    try {
      result = deleteSperreInNewTransaction(sperreToRelease.getId());
    } catch (Exception ex) {
      log.warn(
          "Unable to release SperreId= {} due to {}", sperreToRelease.getId(), ex.getMessage(), ex);
    } finally {
      deregiesterXAResource(sperreToRelease);
    }
    return result;
  }

  @Override
  public boolean releaseSperreInCurrentTransaction(Sperre sperreToRelease)
      throws DokumentSperreException {
    try {
      return sperreRepository.deleteByIdAndFlush(sperreToRelease.getId());
    } catch (Exception ex) {
      throw new DokumentSperreException(
          "Unable to release SperreId=" + sperreToRelease.getId(), ex);
    }
  }

  @Override
  @Transactional
  public List<Sperre> findConflictSperrenForTransaction(
      String transactionId, SperreEintrag... sperreEintraege) throws DokumentSperreException {
    try {
      return sperreRepository.findConflictSperrenForTransaction(transactionId, sperreEintraege);
    } catch (DokumentSperreRepositoryException ex) {
      throw new DokumentSperreException(ex);
    }
  }

  @Override
  @Transactional
  public List<Sperre> findConflictSperrenForBearbeiter(
      String bearbeitername, SperreEintrag... sperreEintraege) throws DokumentSperreException {
    try {
      return sperreRepository.findConflictSperrenForBearbeiter(bearbeitername, sperreEintraege);
    } catch (DokumentSperreRepositoryException ex) {
      throw new DokumentSperreException(ex);
    }
  }

  @Override
  @Transactional
  public List<Sperre> findBySperreEintraege(SperreEintrag... sperreEintraege)
      throws DokumentSperreException {
    try {
      return sperreRepository.findBySperreEintraege(sperreEintraege);
    } catch (DokumentSperreRepositoryException ex) {
      throw new DokumentSperreException(ex);
    }
  }

  @Override
  public List<Sperre> findAll() throws DokumentSperreException {
    return sperreRepository.listAll(true);
  }

  /**
   * Enlists a previously persisted Sperre with the calling thread's JTA transaction so that
   * commit/rollback drives the matching cleanup via {@link SperreAccessXAResource}.
   *
   * <p>Defense-in-depth: if the outer JTA transaction is no longer active by the time enlistment
   * is attempted (e.g. the row was just persisted in {@code REQUIRES_NEW} but the caller's
   * transaction has since rolled back or completed), the orphan row is deleted in a separate
   * transaction and a {@link DokumentSperreException} is thrown — this prevents stale Sperre rows
   * from accumulating in the database when persist and enlist cannot remain atomic.
   *
   * <p>If an {@code XAResource} for the current transaction is already registered the call is a
   * no-op (each transaction registers exactly one resource that aggregates all Sperren acquired
   * within it).
   *
   * @throws DokumentSperreException if the outer JTA transaction is not active or enlistment fails
   *                                 for an infrastructure reason.
   */
  @Transactional
  void enlistSperre(Sperre sperre) throws DokumentSperreException {
    try {
      Transaction jtaTransaction = transactionManager.getTransaction();
      if (jtaTransaction == null || jtaTransaction.getStatus() != STATUS_ACTIVE) {
        int status = jtaTransaction == null ? -1 : jtaTransaction.getStatus();
        log.error(
            "Cannot acquire SperreId={} — JTA transaction status={}. Deleting orphan row.",
            sperre.getId(), status);
        deleteSperreInNewTransaction(sperre.getId());
        throw new DokumentSperreException(
            "Failed to acquire SperreId=" + sperre.getId()
                + " — outer JTA transaction not active (status=" + status
                + "). Persisted row was rolled back.");
      }
      String sperreTransactionId = getTransactionId(jtaTransaction);
      SperreAccessXAResource existing = ACTIVE_TRANSACTIONS.get(sperreTransactionId);
      if (existing == null) {
        SperreAccessXAResource resource = new SperreAccessXAResource(this, sperre);
        existing = ACTIVE_TRANSACTIONS.putIfAbsent(sperreTransactionId, resource);
        if (existing == null) {
          jtaTransaction.enlistResource(resource);
        }
      }
    } catch (DokumentSperreException dse) {
      throw dse;
    } catch (Exception e) {
      throw new RuntimeException("Unable to enlistResource " + e.getMessage(), e);
    }
  }

  @Transactional(value = TxType.REQUIRES_NEW, rollbackOn = Exception.class)
  void saveSperreInNewTransaction(Sperre sperre) {
    sperreRepository.persistAndFlush(sperre);
  }

  @Transactional(value = TxType.REQUIRES_NEW)
  boolean deleteSperreInNewTransaction(String sperrePK) {
    return sperreRepository.deleteByIdAndFlush(sperrePK);
  }

  private String deregiesterXAResource(Sperre sperreToRelease) {
    String transactionIdToConsider = sperreToRelease.getTxId();
    if (transactionIdToConsider != null) {
      ACTIVE_TRANSACTIONS.remove(transactionIdToConsider);
    }
    return transactionIdToConsider;
  }

  @Override
  public Set<SperreEintrag> createSperreEintraege(Beschreibung... beschreibungen) {
    Set<SperreEintrag> sperreEintraege = new LinkedHashSet<>(beschreibungen.length);
    for (Beschreibung beschreibung : beschreibungen) {
      sperreEintraege.add(new SperreEintrag(beschreibung.getId(), SperreDokumentTyp.BESCHREIBUNG));
    }
    return sperreEintraege;
  }

  @Override
  public Set<SperreEintrag> createSperreEintraege(KulturObjektDokument... kulturObjektDokumente) {
    Set<SperreEintrag> sperreEintraege = new LinkedHashSet<>(kulturObjektDokumente.length);
    for (KulturObjektDokument kulturObjektDokument : kulturObjektDokumente) {
      sperreEintraege.add(new SperreEintrag(kulturObjektDokument.getId(), SperreDokumentTyp.KOD));
    }
    return sperreEintraege;
  }

  @Override
  public Set<SperreEintrag> createSperreEintraege(
      Set<KulturObjektDokument> kulturObjektDokumente, Set<Beschreibung> beschreibungen) {
    Set<SperreEintrag> result =
        createSperreEintraege(kulturObjektDokumente.toArray(new KulturObjektDokument[0]));
    result.addAll(createSperreEintraege(beschreibungen.toArray(new Beschreibung[0])));
    return result;
  }

  @Override
  public List<Sperre> findExpiredSperren() {
    return sperreRepository.findExpiredSperren(expirationMinutes);
  }

  @Override
  public Sperre findById(String sperreId) {
    return sperreRepository.findById(sperreId);
  }

  private Set<SperreEintrag> getNotLockedSperreEintraege(
      Collection<Sperre> activeSperres, SperreEintrag[] sperreEintraege) {
    Set<SperreEintrag> dokumentEintragsSet = new LinkedHashSet<>(sperreEintraege.length);
    Collections.addAll(dokumentEintragsSet, sperreEintraege);
    for (Sperre alreadyRegistredSperre : activeSperres) {
      for (SperreEintrag sperreEintrag : alreadyRegistredSperre.getSperreEintraege()) {
        if (dokumentEintragsSet.remove(sperreEintrag)) {
          log.info("Document {} already locked in the same context .. skipped", sperreEintrag);
        }
      }
    }
    return dokumentEintragsSet;
  }
}
