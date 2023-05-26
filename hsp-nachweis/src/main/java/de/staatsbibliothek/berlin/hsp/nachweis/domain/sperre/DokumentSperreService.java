/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre;

import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp.IN_TRANSACTION;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp.MANUAL;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.jta.CommonXAResource.getTransactionId;
import static javax.transaction.Status.STATUS_ACTIVE;

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
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import lombok.extern.slf4j.Slf4j;

/**
 * Provide functionality required in the multiuser environment for parallel document processing
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 17.08.21
 */
@ApplicationScoped
@Slf4j
public class DokumentSperreService implements DokumentSperreBoundary, Serializable {

  private static final Map<String, SperreAccessXAResource> ACTIVE_TRANSACTIONS = new ConcurrentHashMap<>();
  private static final long serialVersionUID = 6437463908874191911L;

  private final SperreRepository sperreRepository;
  private final TransactionManager transactionManager;

  @Inject
  public DokumentSperreService(TransactionManager transactionManager,
      SperreRepository sperreRepository) {
    this.transactionManager = transactionManager;
    this.sperreRepository = sperreRepository;
  }

  @Override
  @Transactional
  public Sperre acquireSperre(Bearbeiter bearbeiter, SperreTyp sperreTyp, String sperreGrund,
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

    switch (sperreTyp) {
      case IN_TRANSACTION:
        return acquireSperreInTransaction(bearbeiter, sperreGrund,
            sperreEintraege);
      case MANUAL:
        return acquireSperreManual(bearbeiter, sperreGrund,
            sperreEintraege);
      default:
        throw new DokumentSperreException("SperreTyp not supported: " + sperreTyp);
    }
  }

  @Transactional
  Sperre acquireSperreInTransaction(Bearbeiter bearbeiter, String sperreGrund,
      SperreEintrag... sperreEintraege)
      throws DokumentSperreException, SperreAlreadyExistException {

    final SperreTyp sperreTyp = IN_TRANSACTION;

    try {
      String transactionId = getTransactionId(transactionManager.getTransaction());

      List<Sperre> conflictSperren = findConflictSperrenForTransaction(transactionId,
          sperreEintraege);
      if (conflictSperren != null && !conflictSperren.isEmpty()) {
        throw new SperreAlreadyExistException(
            "One or more of the provided document entries are already locked!"
                + "\nFor SperreTyp='" + sperreTyp
                + "',\nbenutzer='" + bearbeiter.getId()
                + "',\nsperreGrund='" + sperreGrund
                + "',\nsperreEintraege=" + sperreEintraege.length,
            conflictSperren);
      }

      Sperre sperre;
      SperreBuilder sperreBuilder = Sperre.newBuilder()
          .withSperreTyp(sperreTyp)
          .withSperreGrund(sperreGrund)
          .withBearbeiter(bearbeiter)
          .withTxId(transactionId);

      Collection<Sperre> activeSperreInCurrentTransaction = sperreRepository.findByTransactionId(
          transactionId);
      if (activeSperreInCurrentTransaction.isEmpty()) {
        Stream.of(sperreEintraege).forEach(sperreBuilder::addSperreEintrag);
        sperre = sperreBuilder.build();
        saveSperreInNewTransaction(sperre);
        enlistSperre(sperre);
        log.info("Register Sperre({}) for bearbeiter = {} in TransactionId {} : {}", sperreTyp,
            bearbeiter.getId(),
            transactionId, sperreEintraege.length);
      } else {
        Set<SperreEintrag> sperreEintragSet = getNotLockedSperreEintraege(
            activeSperreInCurrentTransaction,
            sperreEintraege);
        if (sperreEintragSet.isEmpty()) {
          sperre = (Sperre) activeSperreInCurrentTransaction.toArray()[0];
          log.info("Requested documents already locked in the current transaction");
        } else {
          sperreBuilder.withSperreEintraege(sperreEintragSet);
          sperre = sperreBuilder.build();
          saveSperreInNewTransaction(sperre);
          enlistSperre(sperre);
          log.info("Register Sperre({}) for bearbeiter = {} in TransactionId {} : {}", sperreTyp,
              bearbeiter,
              transactionId, sperreEintraege);
        }
      }
      return sperre;
    } catch (SperreAlreadyExistException sae) {
      throw sae;
    } catch (Exception ex) {
      throw new DokumentSperreException(
          "Unable to acquire lock for " + Arrays.toString(sperreEintraege) + " due to "
              + ex.getMessage(), ex);
    }
  }

  @Transactional
  Sperre acquireSperreManual(Bearbeiter bearbeiter, String sperreGrund,
      SperreEintrag... sperreEintraege)
      throws DokumentSperreException, SperreAlreadyExistException {

    final SperreTyp sperreTyp = MANUAL;
    try {
      List<Sperre> conflictSperren = findConflictSperrenForBearbeiter(
          bearbeiter.getBearbeitername(), sperreEintraege);
      if (conflictSperren != null && !conflictSperren.isEmpty()) {
        throw new SperreAlreadyExistException(
            "One or more of the provided document entries are already locked!"
                + "\nFor SperreTyp='" + sperreTyp
                + "',\nbenutzer='" + bearbeiter.getId()
                + "',\nsperreGrund='" + sperreGrund
                + "',\nsperreEintraege=" + sperreEintraege.length,
            conflictSperren);
      }

      Sperre sperre;
      SperreBuilder sperreBuilder = Sperre.newBuilder()
          .withSperreTyp(sperreTyp)
          .withSperreGrund(sperreGrund)
          .withBearbeiter(bearbeiter);

      Collection<Sperre> activeSperrenForBearbeiter = sperreRepository.findByBearbeiter(
          bearbeiter.getBearbeitername());
      if (activeSperrenForBearbeiter.isEmpty()) {
        sperreBuilder.withSperreEintraege(Set.of(sperreEintraege));
        sperre = sperreBuilder.build();
        saveSperreInNewTransaction(sperre);
        log.info("Register manual Sperre({}) for bearbeiter = {} : {}", sperreTyp,
            bearbeiter.getId(),
            sperreEintraege.length);
      } else {
        Set<SperreEintrag> sperreEintragSet = getNotLockedSperreEintraege(
            activeSperrenForBearbeiter, sperreEintraege);
        if (sperreEintragSet.isEmpty()) {
          sperre = (Sperre) activeSperrenForBearbeiter.toArray()[0];
          log.info("Requested documents already locked for the current bearbeiter");
        } else {
          sperreBuilder.withSperreEintraege(sperreEintragSet);
          sperre = sperreBuilder.build();
          saveSperreInNewTransaction(sperre);
          log.info("Register manual Sperre({}) for bearbeiter = {}: {}", sperreTyp, bearbeiter,
              sperreEintraege);
        }
      }

      return sperre;
    } catch (SperreAlreadyExistException sae) {
      throw sae;
    } catch (Exception ex) {
      throw new DokumentSperreException(
          "Unable to acquire lock for " + Arrays.toString(sperreEintraege) + " due to "
              + ex.getMessage(), ex);
    }
  }

  @Override
  public boolean releaseSperre(Sperre sperreToRelease) {
    boolean result = false;
    try {
      result = deleteSperreInNewTransaction(sperreToRelease.getId());
    } catch (Exception ex) {
      log.warn("Unable to release SperreId= {} due to {}", sperreToRelease.getId(), ex.getMessage(),
          ex);
    } finally {
      deregiesterXAResource(sperreToRelease);
    }
    return result;
  }

  @Override
  @Transactional
  public List<Sperre> findConflictSperrenForTransaction(String transactionId,
      SperreEintrag... sperreEintraege)
      throws DokumentSperreException {
    try {
      return sperreRepository.findConflictSperrenForTransaction(transactionId, sperreEintraege);
    } catch (DokumentSperreRepositoryException ex) {
      throw new DokumentSperreException(ex);
    }
  }

  @Override
  @Transactional
  public List<Sperre> findConflictSperrenForBearbeiter(String bearbeitername,
      SperreEintrag... sperreEintraege)
      throws DokumentSperreException {
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

  @Transactional
  void enlistSperre(Sperre sperre) {
    try {
      Transaction jtaTransaction = transactionManager.getTransaction();
      String sperreTransactionId = getTransactionId(jtaTransaction);
      if (!ACTIVE_TRANSACTIONS.containsKey(sperreTransactionId)) {
        if (jtaTransaction.getStatus() == STATUS_ACTIVE) {
          ACTIVE_TRANSACTIONS.put(sperreTransactionId,
              new SperreAccessXAResource(this, sperre));
          jtaTransaction.enlistResource(ACTIVE_TRANSACTIONS.get(sperreTransactionId));
        } else {
          log.warn(
              "Unable to register SperreXAResource for transactioSBnID= {} due to transaction status={}",
              sperreTransactionId, jtaTransaction.getStatus());
        }
      }
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
    return sperreRepository.deleteById(sperrePK);
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
  public Set<SperreEintrag> createSperreEintraege(Set<KulturObjektDokument> kulturObjektDokumente,
      Set<Beschreibung> beschreibungen) {
    Set<SperreEintrag> result = createSperreEintraege(
        kulturObjektDokumente.toArray(new KulturObjektDokument[0]));
    result.addAll(createSperreEintraege(beschreibungen.toArray(new Beschreibung[0])));
    return result;
  }

  private Set<SperreEintrag> getNotLockedSperreEintraege(Collection<Sperre> activeSperres,
      SperreEintrag[] sperreEintraege) {
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
