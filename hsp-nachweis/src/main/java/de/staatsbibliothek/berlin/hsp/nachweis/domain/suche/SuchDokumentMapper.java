package de.staatsbibliothek.berlin.hsp.nachweis.domain.suche;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 21.12.22
 */

@Slf4j
class SuchDokumentMapper {

  private SuchDokumentMapper() {
  }

  static SuchDokument map(final KulturObjektDokument kod, String kodAsJSON) {
    Objects.requireNonNull(kod, "KulturObjektDokument is required");
    Objects.requireNonNull(kodAsJSON, "kodAsJSON is required");

    final SuchDokument suchDokument = SuchDokument.builder()
        .withId(kod.getId())
        .withSuchDokumentTyp(SuchDokumentTyp.KOD)
        .withSignatur(kod.getGueltigeSignatur())
        .withLastUpdate(kod.getRegistrierungsDatum())
        .withContainsDigitalisat(!kod.getDigitalisate().isEmpty())
        .withSearchableValues(kodAsJSON)
        .withBestandhaltendeInstitutionName(mapBesitzer(kod.getGueltigeIdentifikation()))
        .withBestandhaltendeInstitutionOrt(mapAufbewahrungsOrt(kod.getGueltigeIdentifikation()))
        .withContainsBeschreibung(mapContainsBeschreibung(kod))
        .build();

    log.debug("Mapping KOD to Solr Document {} ", suchDokument);

    return suchDokument;
  }

  static SuchDokument map(final Beschreibung beschreibung, final String beschreibungAsJson,
      BearbeiterBoundary bearbeiterBoundary) {
    Objects.requireNonNull(beschreibung, "beschreibung is required");
    Objects.requireNonNull(beschreibungAsJson, "beschreibungAsJson is required");

    Identifikation gueltigeIdentifikation = beschreibung.getGueltigeIdentifikation().orElse(null);

    return SuchDokument.builder()
        .withId(beschreibung.getId())
        .withSignatur(
            beschreibung.getGueltigeIdentifikation().map(Identifikation::getIdent).orElse(null))
        .withSuchDokumentTyp(SuchDokumentTyp.BS)
        .withTitel(mapBeschreibungTitel(beschreibung))
        .withPubliziert(mapPubliziert(beschreibung))
        .withVerwaltungsTyp(mapVerwaltungsTyp(beschreibung))
        .withBearbeiter(mapBeschreibungsBearbeiter(beschreibung, bearbeiterBoundary))
        .withBestandhaltendeInstitutionName(mapBesitzer(gueltigeIdentifikation))
        .withBestandhaltendeInstitutionOrt(mapAufbewahrungsOrt(gueltigeIdentifikation))
        .withJahrDerPublikation(mapBeschreibungJahrDerPublikation(beschreibung))
        .withLastUpdate(mapBeschreibungLastUpdate(beschreibung))
        .withSearchableValues(beschreibungAsJson)
        .withAutoren(mapAutoren(beschreibung))
        .withSichtbarkeit("NICHT_GESPERRT")
        .build();
  }

  static Boolean mapPubliziert(Beschreibung beschreibung) {
    return Objects.nonNull(beschreibung.getPublikation());
  }

  static LocalDateTime mapBeschreibungLastUpdate(Beschreibung beschreibung) {
    return Optional.ofNullable(beschreibung.getAenderungsDatum())
        .orElse(beschreibung.getErstellungsDatum());
  }

  static List<String> mapBeschreibungsBearbeiter(Beschreibung beschreibung,
      BearbeiterBoundary bearbeiterBoundary) {
    return beschreibung.getBearbeiterIDs().stream()
        .map(bearbeiterID -> bearbeiterBoundary.findBearbeiterWithName(bearbeiterID).getFullName())
        .toList();
  }

  static String mapBeschreibungTitel(Beschreibung beschreibung) {
    return Stream.ofNullable(beschreibung.getBeschreibungsStruktur())
        .flatMap(Collection::stream)
        .filter(BeschreibungsKomponenteKopf.class::isInstance)
        .findFirst()
        .map(BeschreibungsKomponenteKopf.class::cast)
        .map(BeschreibungsKomponenteKopf::getTitel)
        .orElse(null);
  }

  static String mapBesitzer(Identifikation gueltigeIdentifikation) {
    return Optional.ofNullable(gueltigeIdentifikation)
        .map(Identifikation::getBesitzer)
        .map(NormdatenReferenz::getName)
        .orElse(null);
  }

  static String mapAufbewahrungsOrt(Identifikation gueltigeIdentifikation) {
    return Optional.ofNullable(gueltigeIdentifikation)
        .map(Identifikation::getAufbewahrungsOrt)
        .map(NormdatenReferenz::getName)
        .orElse(null);
  }

  static Integer mapBeschreibungJahrDerPublikation(Beschreibung beschreibung) {
    return Optional.ofNullable(beschreibung.getPublikation())
        .map(Publikation::getErstpublikationsDatum)
        .map(LocalDateTime::getYear)
        .orElse(null);
  }

  static List<String> mapAutoren(Beschreibung beschreibung) {
    return Stream.ofNullable(beschreibung.getAutoren())
        .flatMap(Collection::stream)
        .map(NormdatenReferenz::getName)
        .toList();
  }

  static Boolean mapContainsBeschreibung(KulturObjektDokument kod) {
    return Objects.nonNull(kod.getBeschreibungenIDs()) && !kod.getBeschreibungenIDs().isEmpty();
  }

  static String mapVerwaltungsTyp(Beschreibung beschreibung) {
    return Optional.ofNullable(beschreibung.getVerwaltungsTyp()).map(VerwaltungsTyp::toString)
        .orElse("");
  }
}
