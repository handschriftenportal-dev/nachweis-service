package de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages APPROVAL-type Sperren on a Beschreibung during the publication workflow.
 *
 * <p>Should only be used by {@link PublikationsService}.
 *
 * <p><b>Transaction contract:</b> {@link #releaseApprovalSperre(String)} is annotated
 * {@link TxType#MANDATORY} because {@code DokumentSperreBoundary.releaseSperreInCurrentTransaction}
 * is deliberately <em>not</em> {@code @Transactional} — it must run inside the caller's active
 * transaction so the Sperre delete commits atomically with the surrounding approve/reject. Callers
 * must invoke this from within an existing transaction.
 */
@Slf4j
@ApplicationScoped
public class BeschreibungApprovalSperrenService {

  private BeschreibungsBoundary beschreibungsService;
  private DokumentSperreBoundary dokumentSperreBoundary;
  private BearbeiterBoundary bearbeiterBoundary;

  protected BeschreibungApprovalSperrenService() {
  }

  @Inject
  public BeschreibungApprovalSperrenService(
      BeschreibungsBoundary beschreibungsService,
      DokumentSperreBoundary dokumentSperreBoundary,
      BearbeiterBoundary bearbeiterBoundary) {
    this.beschreibungsService = beschreibungsService;
    this.dokumentSperreBoundary = dokumentSperreBoundary;
    this.bearbeiterBoundary = bearbeiterBoundary;
  }

  /**
   * Fails with a {@link PublikationException} if the Beschreibung is currently locked by any Sperre.
   *
   * @throws PublikationException if the Beschreibung is locked or the lock check fails
   */
  public void checkHasSperre(String beschreibungId) {
    try {
      if (beschreibungsService.isBeschreibungGesperrt(beschreibungId)) {
        throw new PublikationException(
            "publikation_error_beschreibung_locked", beschreibungId);
      }
    } catch (DokumentSperreException e) {
      throw new PublikationException(
          "publikation_error_sperre_check", beschreibungId, e);
    }
  }

  /**
   * Acquires an APPROVAL Sperre on the Beschreibung for the currently logged-in Bearbeiter.
   *
   * <p><b>Audit note:</b> {@code bearbeiterBoundary.getLoggedBearbeiter()} falls back to the
   * {@code unbekannterBearbeiter} placeholder when no HTTP session, no authentication, or any
   * lookup exception is present (see {@code BearbeiterService.getLoggedBearbeiter}). That means
   * Sperren acquired from background jobs, scheduled tasks, or REST endpoints without a security
   * context will be recorded against the unknown user, not the real triggering actor. If a new
   * caller needs a different attribution, it must resolve the Bearbeiter itself and use a
   * different entry point.
   */
  public void acquireApprovalSperre(String beschreibungId)
      throws DokumentSperreException, SperreAlreadyExistException {
    var bearbeiter = bearbeiterBoundary.getLoggedBearbeiter();
    beschreibungsService.beschreibungSperrenForApproval(bearbeiter, beschreibungId);
  }

  /**
   * Releases any APPROVAL Sperre on the Beschreibung. Must run inside an existing transaction — see
   * the class-level transaction contract.
   */
  @Transactional(TxType.MANDATORY)
  public void releaseApprovalSperre(String beschreibungId) throws DokumentSperreException {
    var sperreOpt = beschreibungsService.findSperreForBeschreibung(beschreibungId)
        .filter(s -> s.getSperreTyp() == SperreTyp.APPROVAL);
    if (sperreOpt.isPresent()) {
      dokumentSperreBoundary.releaseSperreInCurrentTransaction(sperreOpt.get());
    }
  }
}
