package de.staatsbibliothek.berlin.hsp.nachweis.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KulturObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungListDbView;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class BeschreibungTestUtils {

  static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
      "dd.MM.yyyy HH:mm:ss");

  public static Beschreibung createBeschreibung(String id, String kodId, String titel,
      String signatur,
      String altSignatur,
      String altIdentifier,
      boolean intern) {
    return createBeschreibung(id, kodId, titel, signatur, altSignatur, altIdentifier,
        null, Set.of(new Bearbeiter("1", "Tester")), intern);
  }

  public static Beschreibung createBeschreibung(
      String id,
      String kodId,
      String titel,
      String signatur,
      String altSignatur,
      String altIdentifier,
      LocalDateTime erstellungsDatum,
      Set<Bearbeiter> bearbeiter,
      boolean intern) {
    NormdatenReferenz besitzer =
        new NormdatenReferenz("774909e2", "Staatsbibliothek zu Berlin", "5036103-X",
            "CorporateBody");

    NormdatenReferenz aufbewahrungsOrt
        = new NormdatenReferenz("ee1611b6", "Berlin", "4005728-8", "Place");

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent(signatur)
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .withAufbewahrungsOrt(aufbewahrungsOrt)
        .build();

    Identifikation identifikationAlt = new Identifikation.IdentifikationBuilder()
        .withIdent(altSignatur)
        .withIdentTyp(IdentTyp.ALTSIGNATUR)
        .withBesitzer(besitzer)
        .withAufbewahrungsOrt(aufbewahrungsOrt)
        .build();

    BeschreibungsKomponenteKopf bkKopf = new BeschreibungsKomponenteKopfBuilder()
        .withId(UUID.randomUUID().toString())
        .withKulturObjektTyp(KulturObjektTyp.CODEX)
        .withTitel(titel)
        .withAlternativeIdentifikationen(Set.of(identifikationAlt))
        .build();

    Beschreibung beschreibung = new BeschreibungsBuilder(id, kodId, identifikation)
        .withKatalog("kat-1")
        .addBeschreibungsKomponente(bkKopf)
        .addAltIdentifier(altIdentifier)
        .withVerwaltungsTyp(intern ? VerwaltungsTyp.INTERN : VerwaltungsTyp.EXTERN)
        .withDokumentObjectTyp(DokumentObjektTyp.HSP_DESCRIPTION)
        .withErstellungsDatum(erstellungsDatum)
        .build();
    if (bearbeiter != null) {
      beschreibung.setBearbeiterIDs(
          bearbeiter.stream()
              .map(Bearbeiter::getId)
              .collect(Collectors.toSet()));
    }

    return beschreibung;
  }

  public static void assertBeschreibungListDTOEqualsBeschreibung(Beschreibung beschreibung,
      BeschreibungListDbView viewModel) {
    assertNotNull(beschreibung);
    assertNotNull(viewModel);

    Identifikation identifikation = beschreibung.getGueltigeIdentifikation().orElse(null);
    assertNotNull(identifikation);

    BeschreibungsKomponenteKopf bkKopf = beschreibung.getBeschreibungsStruktur().stream()
        .filter(BeschreibungsKomponenteKopf.class::isInstance)
        .map(BeschreibungsKomponenteKopf.class::cast)
        .findFirst()
        .orElse(null);
    assertNotNull(bkKopf);

    assertEquals(beschreibung.getId(), viewModel.getHspId());
    assertEquals(beschreibung.getKodID(), viewModel.getKodId());
    assertEquals(beschreibung.getKatalogID(), viewModel.getKatalogId());
    assertEquals(beschreibung.getVerwaltungsTyp().name(), viewModel.getVerwaltungsTyp());
    assertEquals(identifikation.getIdent(), viewModel.getGueltigeSignatur());
    assertEquals(bkKopf.getTitel(), viewModel.getTitel());
    assertEquals(identifikation.getAufbewahrungsOrt().getName(), viewModel.getAufbewahrungsort());
    assertEquals(identifikation.getBesitzer().getName(), viewModel.getBesitzer());
    assertEquals(beschreibung.getErstellungsDatum(), viewModel.getErstellungsdatum());
    assertEquals(beschreibung.getErstellungsDatum().format(DATE_TIME_FORMATTER),
        viewModel.getErstellungsdatumString());
    assertEquals(beschreibung.getAenderungsDatum(), viewModel.getAenderungsdatum());
    assertEquals(beschreibung.getAenderungsDatum().format(DATE_TIME_FORMATTER),
        viewModel.getAenderungsdatumString());
  }

  public static Beschreibung createBeschreibung(KulturObjektDokument kod) {
    PURL internal = new PURL(URI.create("https://resolver.url/HSP-123"),
        URI.create("https://target.url/HSP-123"), PURLTyp.INTERNAL);
    PURL external = new PURL(URI.create("https://doi.org/HSP-123"),
        URI.create("https://doi.org/HSP-123"), PURLTyp.EXTERNAL);

    LinkedHashSet<PURL> purls = new LinkedHashSet<>();
    purls.add(internal);
    purls.add(external);

    Publikation publikation = new Publikation.PublikationBuilder()
        .withErstpublikationsDatum(LocalDateTime.of(1982, 1, 2, 0, 0))
        .withPublikationsDatumHSP(LocalDateTime.of(1983, 3, 4, 0, 0))
        .withPURLs(purls)
        .build();
    publikation.onApproved();

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopfBuilder()
        .withId("777")
        .withTitel("Flavius Josephus, de bello Judaico")
        .build();
    String beschreibungId = "40170459";
    Beschreibung beschreibung = new BeschreibungsBuilder(beschreibungId, kod.getId(),
        kod.getGueltigeIdentifikation())
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withPublikation(publikation)
        .addAutor(new NormdatenReferenz("NORM-" + UUID.randomUUID().toString(), "Tester", "GNDID"))
        .withDokumentObjectTyp(DokumentObjektTyp.HSP_DESCRIPTION_RETRO)
        .build();
    beschreibung.getBeschreibungsStruktur().add(kopf);
    return beschreibung;
  }
}
