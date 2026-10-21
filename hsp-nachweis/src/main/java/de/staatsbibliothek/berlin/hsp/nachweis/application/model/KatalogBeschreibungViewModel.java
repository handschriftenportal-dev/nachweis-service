package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 13.10.22
 */
@Getter
@Builder(setterPrefix = "with")
@EqualsAndHashCode(of = {"id"})
public class KatalogBeschreibungViewModel implements Serializable {

  @Serial
  private static final long serialVersionUID = -4013682818747728693L;
  private String id;
  private String katalogId;
  private String signatur;
  private String titel;
  private String bestandhaltendeInstitutionName;
  private String bestandhaltendeInstitutionOrt;
  private String autoren;
  private Integer jahrDerErstpublikation;

  public static KatalogBeschreibungViewModel map(Beschreibung katalogBeschreibung) {
    Objects.requireNonNull(katalogBeschreibung, "katalogBeschreibung is required");

    return KatalogBeschreibungViewModel.builder()
        .withId(katalogBeschreibung.getId())
        .withKatalogId(katalogBeschreibung.getKatalogID())
        .withSignatur(katalogBeschreibung.getGueltigeIdentifikation()
            .map(Identifikation::getIdent)
            .orElse(""))
        .withTitel(mapTitel(katalogBeschreibung))
        .withJahrDerErstpublikation(mapJahrDerErstpublikation(katalogBeschreibung))
        .withAutoren(mapAutoren(katalogBeschreibung))
        .withBestandhaltendeInstitutionName(mapBestandhaltendeInstitutionName(katalogBeschreibung))
        .withBestandhaltendeInstitutionOrt(mapBestandhaltendeInstitutionOrt(katalogBeschreibung))
        .build();
  }

  static String mapTitel(Beschreibung katalogBeschreibung) {
    return katalogBeschreibung.getBeschreibungsStruktur().stream()
        .filter(BeschreibungsKomponenteKopf.class::isInstance)
        .findFirst()
        .map(BeschreibungsKomponenteKopf.class::cast)
        .map(BeschreibungsKomponenteKopf::getTitel)
        .orElse("");
  }

  static Integer mapJahrDerErstpublikation(Beschreibung katalogBeschreibung) {
    Publikation publikation = katalogBeschreibung.getPublikation();
    if (publikation != null && publikation.getErstpublikationsDatum() != null) {
      return publikation.getErstpublikationsDatum().getYear();
    }
    return null;
  }

  static String mapAutoren(Beschreibung katalogBeschreibung) {
    return katalogBeschreibung.getAutoren().stream()
        .map(NormdatenReferenz::getName)
        .collect(Collectors.joining("; "));
  }

  static String mapBestandhaltendeInstitutionName(Beschreibung katalogBeschreibung) {
    return katalogBeschreibung.getGueltigeIdentifikation()
        .map(Identifikation::getBesitzer)
        .map(NormdatenReferenz::getName)
        .orElse("");
  }

  static String mapBestandhaltendeInstitutionOrt(Beschreibung katalogBeschreibung) {
    return katalogBeschreibung.getGueltigeIdentifikation()
        .map(Identifikation::getAufbewahrungsOrt)
        .map(NormdatenReferenz::getName)
        .orElse("");
  }
}
