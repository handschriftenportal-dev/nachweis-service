package de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import java.util.List;
import java.util.Set;

/**
 * Provide access to document lock system
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 17.08.21
 */
public interface DokumentSperreBoundary {

  Sperre acquireSperre(Bearbeiter bearbeiter, SperreTyp sperreTyp, String sperreGrund,
      SperreEintrag... sperreEintraege)
      throws DokumentSperreException, SperreAlreadyExistException;

  default Sperre acquireSperre(Bearbeiter bearbeiter, SperreTyp sperreTyp, String sperreGrund,
      KulturObjektDokument... kulturObjektDokumente)
      throws DokumentSperreException, SperreAlreadyExistException {
    return acquireSperre(bearbeiter, sperreTyp, sperreGrund,
        createSperreEintraege(kulturObjektDokumente).toArray(new SperreEintrag[0]));
  }

  default Sperre acquireSperre(Bearbeiter bearbeiter, SperreTyp sperreTyp, String sperreGrund,
      Beschreibung... beschreibungen)
      throws DokumentSperreException, SperreAlreadyExistException {
    return acquireSperre(bearbeiter, sperreTyp, sperreGrund,
        createSperreEintraege(beschreibungen).toArray(new SperreEintrag[0]));
  }

  boolean releaseSperre(Sperre sperreToRelease) throws DokumentSperreException;

  boolean releaseSperreInCurrentTransaction(Sperre sperreToRelease) throws DokumentSperreException;

  List<Sperre> findConflictSperrenForTransaction(String transactionId,
      SperreEintrag... sperreEintraege)
      throws DokumentSperreException;

  List<Sperre> findConflictSperrenForBearbeiter(String bearbeitername,
      SperreEintrag... sperreEintraege)
      throws DokumentSperreException;

  List<Sperre> findBySperreEintraege(SperreEintrag... sperreEintraege)
      throws DokumentSperreException;

  default boolean isGesperrt(SperreEintrag... sperreEintraege) throws DokumentSperreException {
    return !findBySperreEintraege(sperreEintraege).isEmpty();
  }

  default List<Sperre> findAllSperrenForBeschreibung(String beschreibungId)
      throws DokumentSperreException {
    return findBySperreEintraege(
        new SperreEintrag(beschreibungId, SperreDokumentTyp.BESCHREIBUNG));
  }

  default boolean hasAnySperre(String beschreibungId) throws DokumentSperreException {
    return !findAllSperrenForBeschreibung(beschreibungId).isEmpty();
  }

  List<Sperre> findAll() throws DokumentSperreException;

  Set<SperreEintrag> createSperreEintraege(Beschreibung... beschreibungen);

  Set<SperreEintrag> createSperreEintraege(KulturObjektDokument... kulturObjektDokumente);

  Set<SperreEintrag> createSperreEintraege(Set<KulturObjektDokument> kulturObjektDokumente,
      Set<Beschreibung> beschreibungen);

  List<Sperre> findExpiredSperren();

  Sperre findById(String sperreId);
}
