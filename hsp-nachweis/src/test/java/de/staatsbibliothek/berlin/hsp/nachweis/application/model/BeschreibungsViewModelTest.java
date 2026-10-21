package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationStatus;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel.BeschreibungsViewBuilder;
import java.lang.reflect.Field;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 13.06.2019.
 */
class BeschreibungsViewModelTest {

  @Test
  void test_GivenValues_Creation() {
    final LocalDateTime erstellungsDatum = LocalDateTime.of(2020, 11, 15, 23, 10, 23);
    final LocalDateTime aenderungsDatum = LocalDateTime.of(2020, 12, 16, 22, 9, 22);
    final NormdatenReferenz deutsch = new NormdatenReferenz("17", "deutsch", "4113292-0",
        "Language");

    List<Bearbeiter> bearbeiters = List.of(
        Bearbeiter.newBuilder().withId("b1").withVorname("Vorname").withNachname("Nachname")
            .build(),
        Bearbeiter.newBuilder().withId("b2").withVorname("Vorname1").withNachname("Nachname1")
            .build()
    );

    BeschreibungsViewModel view = new BeschreibungsViewBuilder()
        .withImportJobID("1")
        .withID("2")
        .withTitel("Titel der Beschreibung")
        .withSignatur("Signatur")
        .withExterneID("123")
        .withKODID("KOD123")
        .withKODSignatur("Test-Signatur")
        .withTEIXML("")
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .withErstellungsDatum(erstellungsDatum)
        .withAenderungsDatum(aenderungsDatum)
        .withBeschreibungsSprache(deutsch)
        .withGedrucktemKatalog(true)
        .withHspPurl("https://resolver.staatsbibliothek-berlin.de/HSP0000002300000000")
        .withBearbeiter(bearbeiters)
        .build();

    assertEquals("1", view.getImportJobID());
    assertEquals("2", view.getId());
    assertEquals("Titel der Beschreibung", view.getTitel());
    assertEquals("Signatur", view.getSignatur());
    assertEquals("123", view.getExterneID());
    assertEquals("KOD123", view.getKodID());
    assertEquals("Test-Signatur", view.getKodSignatur());
    assertEquals("", view.getTeiXML());
    assertEquals(VerwaltungsTyp.INTERN, view.getVerwaltungsTyp());
    assertEquals(erstellungsDatum, view.getErstellungsDatum());
    assertEquals(aenderungsDatum, view.getAenderungsDatum());
    assertEquals(NormdatenReferenzViewModel.from(deutsch), view.getBeschreibungsSprache());
    assertTrue(view.getAusGedrucktemKatalog());
    assertEquals("https://resolver.staatsbibliothek-berlin.de/HSP0000002300000000",
        view.getHspPurl());
    assertEquals(2, view.getBearbeiterNames().size());
  }

  @Test
  void test_GivenBeschreibungen_Equals() {
    List<Bearbeiter> bearbeiters = List.of(
        Bearbeiter.newBuilder().withId("b1").withVorname("Vorname").withNachname("Nachname").build()
    );
    BeschreibungsViewModel view1 = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withImportJobID("1")
        .withID("2")
        .withTitel("Titel der Beschreibung")
        .withSignatur("Signatur")
        .withExterneID("123")
        .withKODView("KOD123")
        .withTEIXML("")
        .withBearbeiter(bearbeiters).build();

    BeschreibungsViewModel view2 = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withImportJobID("1")
        .withID("3")
        .withTitel("Titel der Beschreibung")
        .withSignatur("Signatur")
        .withExterneID("123")
        .withKODView("KOD123")
        .withTEIXML("")
        .withBearbeiter(bearbeiters).build();

    assertNotEquals(view1, view2);

  }

  @Test
  void testCreationWithEmptyLists() {
    BeschreibungsViewModel view1 = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withID("2").build();

    assertDoesNotThrow(view1::toString);
  }

  @Test
  void testCreationWith2Publications() {
    PublikationViewModel publikationViewModel = PublikationViewModel.fromEntity(
        new Publikation.PublikationBuilder()
            .withErstpublikationsDatum(LocalDateTime.of(1967, 1, 1, 0, 0))
            .withPublikationsDatumHSP(LocalDateTime.of(2020, 10, 20, 0, 0))
            .build());
    BeschreibungsViewModel view1 = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withID("2")
        .withPublikation(publikationViewModel).build();

    assertEquals(publikationViewModel, view1.getPublikation());
  }

  @Test
  void testCreationWith2Beteiligte() {
    List<NormdatenReferenz> beteiligte = List.of(
        new NormdatenReferenz("12", "Klaus", "wedr", "Person"),
        new NormdatenReferenz("123", "Olaf", "gndid", "Person"));
    BeschreibungsViewModel view1 = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withID("2")
        .withBeteiligte(beteiligte).build();

    assertEquals(
        beteiligte.stream().map(NormdatenReferenzViewModel::from).toList(),
        view1.getBeteiligte());
  }

  @Test
  void testGetFormattedAutoren() {
    List<BeschreibungsAutorView> autoren = new ArrayList<>();
    autoren.add(new BeschreibungsAutorView("Carter, Randolph", ""));
    autoren.add(new BeschreibungsAutorView("Armitage, Henry", ""));
    BeschreibungsViewModel view = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withID("1")
        .withAutoren(autoren)
        .build();

    assertEquals(2, view.getAutoren().size());
    assertEquals("Carter, Randolph; Armitage, Henry", view.getFormattedAutoren());

    view = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withID("2")
        .build();

    assertEquals(0, view.getAutoren().size());
    assertEquals("", view.getFormattedAutoren());
  }

  @Test
  void testAddExternenLink() {
    BeschreibungsViewModel view = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withID("1")
        .withExterneLinks(Arrays.asList("http://doi.org/123", "http://doi.org/456"))
        .addExternenLink("http://doi.org/789")
        .build();

    assertNotNull(view.getExterneLinks());
    assertEquals(3, view.getExterneLinks().size());
    assertTrue(view.getExterneLinks().contains("http://doi.org/123"));
    assertTrue(view.getExterneLinks().contains("http://doi.org/456"));
    assertTrue(view.getExterneLinks().contains("http://doi.org/789"));
  }

  @Test
  void testGetFormattedBearbeiter() {
    List<Bearbeiter> bearbeiteren = List.of(
        Bearbeiter.newBuilder().withId("b1").withVorname("Masaoud").withNachname("Khallouf")
            .build(),
        Bearbeiter.newBuilder().withId("b2").withVorname("Vorname").withNachname("Nachname").build()
    );
    BeschreibungsViewModel view = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withID("1")
        .withBearbeiter(bearbeiteren)
        .build();

    assertEquals(2, view.getBearbeiterNames().size());
    assertEquals(2, view.getBearbeiterIds().size());
    assertTrue(view.getBearbeiterIds().contains("b1"));
    assertTrue(view.getBearbeiterIds().contains("b2"));
    assertEquals("Khallouf, Masaoud; Nachname, Vorname", view.getFormattedBearbeiter());
  }

  @Test
  void testShorten() {
    BeschreibungsViewModel beschreibungsViewModel = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withAutoren(List.of(
            new BeschreibungsAutorView("Looooooooooooooooooooooong, Randolphooooooooooooooooooooo",
                ""),
            new BeschreibungsAutorView("Carter, Randolph", ""),
            new BeschreibungsAutorView("Carter, Randolph", ""),
            new BeschreibungsAutorView("Carter, Randolph", "")
        ))
        .withTitel(
            "Sehr langer Titel, der unbedingt gekuerzt werden muss, da er sonst einfach viiiel zu lang ist")
        .build();
    String shortenedTitel = beschreibungsViewModel.getFormattedTitel();
    String shortenedAuthors = beschreibungsViewModel.getFormattedAutoren();
    assertEquals("Sehr langer Titel, der unbedingt gekuerzt werden muss, da er...", shortenedTitel);
    assertEquals("Looooooooooooooooooooooong, Randolphooooooooooooooooooooo; Ca...",
        shortenedAuthors);
  }

  @Test
  void testIsEditable_noPublikation() {
    BeschreibungsViewModel view = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withID("1")
        .build();
    assertTrue(view.isEditable());
  }

  @Test
  void testIsEditable_draftPublikation() {
    PublikationViewModel publikationViewModel = PublikationViewModel.fromEntity(
        new Publikation.PublikationBuilder()
            .withErstpublikationsDatum(LocalDateTime.of(2020, 1, 1, 0, 0))
            .build());
    BeschreibungsViewModel view = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withID("1")
        .withPublikation(publikationViewModel)
        .build();
    assertTrue(view.isEditable());
  }

  @Test
  void testIsEditable_publishedPublikation() {
    Publikation publikation = new Publikation.PublikationBuilder()
        .withErstpublikationsDatum(LocalDateTime.of(2020, 1, 1, 0, 0))
        .build();
    publikation.onApproved();

    BeschreibungsViewModel view = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withID("1")
        .withPublikation(PublikationViewModel.fromEntity(publikation))
        .build();
    assertFalse(view.isEditable());
  }

  @Test
  void testCanRequestApproval() {
    // No publikation - cannot request approval
    BeschreibungsViewModel viewWithoutPub = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withID("1")
        .build();
    assertFalse(viewWithoutPub.canRequestApproval());

    // With draft publikation - can request approval
    PublikationViewModel publikationViewModel = PublikationViewModel.fromEntity(
        new Publikation.PublikationBuilder()
            .withErstpublikationsDatum(LocalDateTime.of(2020, 1, 1, 0, 0))
            .build());
    BeschreibungsViewModel viewWithPub = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withID("2")
        .withPublikation(publikationViewModel)
        .build();
    assertTrue(viewWithPub.canRequestApproval());
  }

  // --- Tests for BeschreibungsViewBuilder(Beschreibung) constructor ---

  @Test
  void testBeschreibungsViewBuilder_fromBeschreibung_fullMapping() {
    NormdatenReferenz besitzer = new NormdatenReferenz("besitzer-1",
        "Staatsbibliothek zu Berlin", "gnd-besitzer");
    NormdatenReferenz aufbewahrungsOrt = new NormdatenReferenz("ort-1", "Berlin",
        "gnd-berlin");
    NormdatenReferenz sprache = new NormdatenReferenz("sprache-1", "deutsch",
        "4113292-0", "Language");
    NormdatenReferenz beteiligter = new NormdatenReferenz("bet-1", "Konrad Eichstaedt",
        "gnd-bet-1", "Person");
    NormdatenReferenz autor1 = new NormdatenReferenz("autor-1", "Carter, Randolph",
        "gnd-autor-1", "Person");
    NormdatenReferenz autor2 = new NormdatenReferenz("autor-2", "Armitage, Henry",
        "gnd-autor-2", "Person");

    Identifikation identifikation = new IdentifikationBuilder()
        .withId("ident-1")
        .withIdent("Ms. lat. fol. 123")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .withAufbewahrungsOrt(aufbewahrungsOrt)
        .build();

    LocalDateTime erstellungsDatum = LocalDateTime.of(2020, 6, 15, 10, 30);
    LocalDateTime aenderungsDatum = LocalDateTime.of(2021, 3, 20, 14, 45);
    LocalDateTime erstpublikationsDatum = LocalDateTime.of(2019, 1, 1, 0, 0);

    PURL internalPurl = new PURL(
        URI.create("https://resolver.staatsbibliothek-berlin.de/HSP0000001"),
        URI.create("https://handschriftenportal.de/beschreibungen/HSP-1"),
        PURLTyp.INTERNAL);
    PURL externalPurl1 = new PURL(
        URI.create("https://doi.org/10.1234/hsp-1"),
        URI.create("https://handschriftenportal.de/beschreibungen/HSP-1"),
        PURLTyp.EXTERNAL);
    PURL externalPurl2 = new PURL(
        URI.create("https://doi.org/10.5678/hsp-2"),
        URI.create("https://handschriftenportal.de/beschreibungen/HSP-1"),
        PURLTyp.EXTERNAL);

    Lizenz lizenz = new Lizenz.LizenzBuilder()
        .addUri("https://creativecommons.org/publicdomain/mark/1.0/")
        .withBeschreibungsText("Public Domain Mark 1.0")
        .build();

    Publikation publikation = new Publikation.PublikationBuilder()
        .withErstpublikationsDatum(erstpublikationsDatum)
        .withPublikationsDatumHSP(LocalDateTime.of(2020, 10, 20, 0, 0))
        .withLizenz(lizenz)
        .addPURL(internalPurl)
        .addPURL(externalPurl1)
        .addPURL(externalPurl2)
        .build();

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopfBuilder()
        .withId("kopf-1")
        .withTitel("Flavius Josephus, De bello Judaico")
        .build();

    Beschreibung beschreibung = new BeschreibungsBuilder("HSP-1", "KOD-1", identifikation)
        .withTEIXml("<TEI>test</TEI>")
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .withDokumentObjectTyp(DokumentObjektTyp.HSP_DESCRIPTION_RETRO)
        .withErstellungsDatum(erstellungsDatum)
        .withAenderungsDatum(aenderungsDatum)
        .withBeschreibungssprache(sprache)
        .addBeschreibungsBeteiligter(beteiligter)
        .addAutor(autor1)
        .addAutor(autor2)
        .withPublikation(publikation)
        .addBeschreibungsKomponente(kopf)
        .build();

    BeschreibungsViewModel view = new BeschreibungsViewBuilder(beschreibung).build();

    assertEquals("HSP-1", view.getId());
    assertEquals("Flavius Josephus, De bello Judaico", view.getTitel());
    assertEquals("<TEI>test</TEI>", view.getTeiXML());
    assertEquals("KOD-1", view.getKodID());
    assertEquals(erstellungsDatum, view.getErstellungsDatum());
    assertEquals(aenderungsDatum, view.getAenderungsDatum());
    assertEquals(VerwaltungsTyp.INTERN, view.getVerwaltungsTyp());
    assertEquals(NormdatenReferenzViewModel.from(sprache), view.getBeschreibungsSprache());
    assertTrue(view.getAusGedrucktemKatalog());

    // Identifikation mapping
    assertEquals("Ms. lat. fol. 123", view.getSignatur());
    assertEquals("Staatsbibliothek zu Berlin", view.getBestandhaltendeInstitutionName());
    assertEquals("Berlin", view.getBestandhaltendeInstitutionOrt());

    // Publikation mapping
    assertNotNull(view.getPublikation());
    assertEquals(PublikationStatus.DRAFT, view.getPublikation().status());
    assertEquals(erstpublikationsDatum, view.getDatumErstpublikation());

    // Beteiligte
    assertEquals(1, view.getBeteiligte().size());
    assertTrue(view.getBeteiligte().contains(NormdatenReferenzViewModel.from(beteiligter)));

    // Lizenz mapping
    assertNotNull(view.getLizenz());
    assertEquals("Public Domain Mark 1.0", view.getLizenz().getBeschreibungsText());
    assertTrue(view.getLizenz().getUris()
        .contains("https://creativecommons.org/publicdomain/mark/1.0/"));

    // Autoren mapping
    assertEquals(2, view.getAutoren().size());
    assertTrue(view.getAutoren().stream()
        .anyMatch(a -> "Carter, Randolph".equals(a.preferredName())
            && "gnd-autor-1".equals(a.gndid())));
    assertTrue(view.getAutoren().stream()
        .anyMatch(a -> "Armitage, Henry".equals(a.preferredName())
            && "gnd-autor-2".equals(a.gndid())));

    // PURL splitting
    assertEquals("https://resolver.staatsbibliothek-berlin.de/HSP0000001",
        view.getHspPurl());
    assertEquals(2, view.getExterneLinks().size());
    assertTrue(view.getExterneLinks().contains("https://doi.org/10.1234/hsp-1"));
    assertTrue(view.getExterneLinks().contains("https://doi.org/10.5678/hsp-2"));
    assertTrue(view.getBearbeiterIds().isEmpty());
    assertEquals("", view.getKodSignatur());
  }

  @Test
  void testBeschreibungsViewBuilder_fromBeschreibung_withoutGueltigeIdentifikation() {
    Beschreibung beschreibung = new BeschreibungsBuilder("HSP-2", "KOD-2", null)
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .build();

    BeschreibungsViewModel view = new BeschreibungsViewBuilder(beschreibung).build();

    assertEquals("HSP-2", view.getId());
    assertNull(view.getSignatur());
    assertNull(view.getBestandhaltendeInstitutionName());
    assertNull(view.getBestandhaltendeInstitutionOrt());
  }

  @Test
  void testBeschreibungsViewBuilder_fromBeschreibung_identifikationWithoutBesitzerAndOrt() {
    Identifikation identifikation = new IdentifikationBuilder()
        .withId("ident-2")
        .withIdent("Ms. germ. oct. 456")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .build();

    Beschreibung beschreibung = new BeschreibungsBuilder("HSP-3", "KOD-3", identifikation)
        .build();

    BeschreibungsViewModel view = new BeschreibungsViewBuilder(beschreibung).build();

    assertEquals("Ms. germ. oct. 456", view.getSignatur());
    assertNull(view.getBestandhaltendeInstitutionName());
    assertNull(view.getBestandhaltendeInstitutionOrt());
  }

  @Test
  void testBeschreibungsViewBuilder_fromBeschreibung_withoutLizenz() {
    Publikation publikation = new Publikation.PublikationBuilder()
        .withErstpublikationsDatum(LocalDateTime.of(2020, 1, 1, 0, 0))
        .build();

    Beschreibung beschreibung = new BeschreibungsBuilder("HSP-4", "KOD-4", null)
        .withPublikation(publikation)
        .build();

    BeschreibungsViewModel view = new BeschreibungsViewBuilder(beschreibung).build();

    assertNull(view.getLizenz());
  }

  @Test
  void testBeschreibungsViewBuilder_fromBeschreibung_defaultPublikation() {
    // Beschreibung always creates a default Publikation with null dates and DRAFT status
    Beschreibung beschreibung = new BeschreibungsBuilder("HSP-5", "KOD-5", null)
        .build();

    BeschreibungsViewModel view = new BeschreibungsViewBuilder(beschreibung).build();

    assertNotNull(view.getPublikation());
    assertEquals(PublikationStatus.DRAFT, view.getPublikation().status());
    assertNull(view.getDatumErstpublikation());
  }

  @Test
  void testBeschreibungsViewBuilder_fromBeschreibung_nullPublikationViaReflection()
      throws Exception {
    // Use reflection to set publikation to null, testing the Optional.ofNullable paths
    Beschreibung beschreibung = new BeschreibungsBuilder("HSP-6", "KOD-6", null).build();

    Field publikationField = Beschreibung.class.getDeclaredField("publikation");
    publikationField.setAccessible(true);
    publikationField.set(beschreibung, null);

    BeschreibungsViewModel view = new BeschreibungsViewBuilder(beschreibung).build();

    assertNull(view.getPublikation());
    assertNull(view.getDatumErstpublikation());
    assertNull(view.getLizenz());
    assertTrue(view.getExterneLinks().isEmpty());
    assertNull(view.getHspPurl());
  }

  @Test
  void testBeschreibungsViewBuilder_fromBeschreibung_purlSplitting() {
    PURL internal = new PURL(
        URI.create("https://resolver.staatsbibliothek-berlin.de/HSP-INTERNAL"),
        URI.create("https://target.de/1"),
        PURLTyp.INTERNAL);
    PURL external1 = new PURL(
        URI.create("https://doi.org/external-1"),
        URI.create("https://target.de/2"),
        PURLTyp.EXTERNAL);
    PURL external2 = new PURL(
        URI.create("https://doi.org/external-2"),
        URI.create("https://target.de/3"),
        PURLTyp.EXTERNAL);

    Publikation publikation = new Publikation.PublikationBuilder()
        .addPURL(internal)
        .addPURL(external1)
        .addPURL(external2)
        .build();

    Beschreibung beschreibung = new BeschreibungsBuilder("HSP-7", "KOD-7", null)
        .withPublikation(publikation)
        .build();

    BeschreibungsViewModel view = new BeschreibungsViewBuilder(beschreibung).build();

    assertEquals("https://resolver.staatsbibliothek-berlin.de/HSP-INTERNAL",
        view.getHspPurl());
    assertEquals(2, view.getExterneLinks().size());
    assertTrue(view.getExterneLinks().contains("https://doi.org/external-1"));
    assertTrue(view.getExterneLinks().contains("https://doi.org/external-2"));
  }

  @Test
  void testBeschreibungsViewBuilder_fromBeschreibung_noPurls() {
    Beschreibung beschreibung = new BeschreibungsBuilder("HSP-8", "KOD-8", null).build();

    BeschreibungsViewModel view = new BeschreibungsViewBuilder(beschreibung).build();

    assertNull(view.getHspPurl());
    assertNotNull(view.getExterneLinks());
    assertTrue(view.getExterneLinks().isEmpty());
  }

  @Test
  void testBeschreibungsViewBuilder_fromBeschreibung_onlyExternalPurls() {
    PURL external = new PURL(
        URI.create("https://doi.org/only-external"),
        URI.create("https://target.de/x"),
        PURLTyp.EXTERNAL);

    Publikation publikation = new Publikation.PublikationBuilder()
        .addPURL(external)
        .build();

    Beschreibung beschreibung = new BeschreibungsBuilder("HSP-9", "KOD-9", null)
        .withPublikation(publikation)
        .build();

    BeschreibungsViewModel view = new BeschreibungsViewBuilder(beschreibung).build();

    assertNull(view.getHspPurl());
    assertEquals(1, view.getExterneLinks().size());
    assertTrue(view.getExterneLinks().contains("https://doi.org/only-external"));
  }

  @Test
  void testBeschreibungsViewBuilder_fromBeschreibung_autorenMapping() {
    NormdatenReferenz autor = new NormdatenReferenz("a-1", "Mueller, Hans",
        "gnd-mueller", "Person");

    Beschreibung beschreibung = new BeschreibungsBuilder("HSP-10", "KOD-10", null)
        .addAutor(autor)
        .build();

    BeschreibungsViewModel view = new BeschreibungsViewBuilder(beschreibung).build();

    assertEquals(1, view.getAutoren().size());
    BeschreibungsAutorView autorView = view.getAutoren().getFirst();
    assertEquals("Mueller, Hans", autorView.preferredName());
    assertEquals("gnd-mueller", autorView.gndid());
  }

  @Test
  void testBeschreibungsViewBuilder_fromBeschreibung_noAutoren() {
    Beschreibung beschreibung = new BeschreibungsBuilder("HSP-11", "KOD-11", null)
        .build();

    BeschreibungsViewModel view = new BeschreibungsViewBuilder(beschreibung).build();

    assertNotNull(view.getAutoren());
    assertTrue(view.getAutoren().isEmpty());
  }

  @Test
  void testBeschreibungsViewBuilder_fromBeschreibung_ausGedrucktemKatalogRetro() {
    Beschreibung beschreibung = new BeschreibungsBuilder("HSP-12", "KOD-12", null)
        .withDokumentObjectTyp(DokumentObjektTyp.HSP_DESCRIPTION_RETRO)
        .build();

    BeschreibungsViewModel view = new BeschreibungsViewBuilder(beschreibung).build();

    assertTrue(view.getAusGedrucktemKatalog());
  }

  @Test
  void testBeschreibungsViewBuilder_fromBeschreibung_ausGedrucktemKatalogDescription() {
    Beschreibung beschreibung = new BeschreibungsBuilder("HSP-13", "KOD-13", null)
        .withDokumentObjectTyp(DokumentObjektTyp.HSP_DESCRIPTION)
        .build();

    BeschreibungsViewModel view = new BeschreibungsViewBuilder(beschreibung).build();

    assertFalse(view.getAusGedrucktemKatalog());
  }

  @Test
  void testBeschreibungsViewBuilder_fromBeschreibung_ausGedrucktemKatalogNull() {
    Beschreibung beschreibung = new BeschreibungsBuilder("HSP-14", "KOD-14", null)
        .build();

    BeschreibungsViewModel view = new BeschreibungsViewBuilder(beschreibung).build();

    assertFalse(view.getAusGedrucktemKatalog());
  }

  @Test
  void testBeschreibungsViewBuilder_fromBeschreibung_emptyTitel() {
    // Beschreibung without BeschreibungsKomponenteKopf has no titel
    // -> getTitel() returns Optional.empty() -> builder defaults to ""
    Beschreibung beschreibung = new BeschreibungsBuilder("HSP-15", "KOD-15", null)
        .build();

    BeschreibungsViewModel view = new BeschreibungsViewBuilder(beschreibung).build();

    assertEquals("", view.getTitel());
  }

  @Test
  void testBeschreibungsViewBuilder_fromBeschreibung_titelFromKopf() {
    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopfBuilder()
        .withId("kopf-2")
        .withTitel("Ein wichtiger Titel")
        .build();

    Beschreibung beschreibung = new BeschreibungsBuilder("HSP-16", "KOD-16", null)
        .addBeschreibungsKomponente(kopf)
        .build();

    BeschreibungsViewModel view = new BeschreibungsViewBuilder(beschreibung).build();

    assertEquals("Ein wichtiger Titel", view.getTitel());
  }

  @Test
  void testBeschreibungsViewBuilder_fromBeschreibung_lizenzWithMultipleUris() {
    Lizenz lizenz = new Lizenz.LizenzBuilder()
        .addUri("https://creativecommons.org/licenses/by-sa/4.0/")
        .addUri("https://creativecommons.org/licenses/by/4.0/")
        .withBeschreibungsText("CC BY-SA 4.0")
        .build();

    Publikation publikation = new Publikation.PublikationBuilder()
        .withLizenz(lizenz)
        .build();

    Beschreibung beschreibung = new BeschreibungsBuilder("HSP-17", "KOD-17", null)
        .withPublikation(publikation)
        .build();

    BeschreibungsViewModel view = new BeschreibungsViewBuilder(beschreibung).build();

    assertNotNull(view.getLizenz());
    assertEquals("CC BY-SA 4.0", view.getLizenz().getBeschreibungsText());
    assertEquals(2, view.getLizenz().getUris().size());
    assertTrue(view.getLizenz().getUris()
        .contains("https://creativecommons.org/licenses/by-sa/4.0/"));
    assertTrue(view.getLizenz().getUris()
        .contains("https://creativecommons.org/licenses/by/4.0/"));
  }

  @Test
  void testBeschreibungsViewBuilder_fromBeschreibung_beteiligteAreCopied() {
    NormdatenReferenz bet1 = new NormdatenReferenz("b-1", "Person A", "gnd-a", "Person");
    NormdatenReferenz bet2 = new NormdatenReferenz("b-2", "Person B", "gnd-b", "Person");

    Beschreibung beschreibung = new BeschreibungsBuilder("HSP-18", "KOD-18", null)
        .addBeschreibungsBeteiligter(bet1)
        .addBeschreibungsBeteiligter(bet2)
        .build();

    BeschreibungsViewModel view = new BeschreibungsViewBuilder(beschreibung).build();

    assertEquals(2, view.getBeteiligte().size());
    assertTrue(view.getBeteiligte().contains(NormdatenReferenzViewModel.from(bet1)));
    assertTrue(view.getBeteiligte().contains(NormdatenReferenzViewModel.from(bet2)));
  }

  @Test
  void testBeschreibungsViewBuilder_fromBeschreibung_datumErstpublikationFromPublikation() {
    LocalDateTime erstpubDatum = LocalDateTime.of(2018, 5, 10, 12, 0);
    Publikation publikation = new Publikation.PublikationBuilder()
        .withErstpublikationsDatum(erstpubDatum)
        .build();

    Beschreibung beschreibung = new BeschreibungsBuilder("HSP-19", "KOD-19", null)
        .withPublikation(publikation)
        .build();

    BeschreibungsViewModel view = new BeschreibungsViewBuilder(beschreibung).build();

    assertEquals(erstpubDatum, view.getDatumErstpublikation());
  }

  @Test
  void testBeschreibungsViewBuilder_fromBeschreibung_nullBeschreibungThrowsNPE() {
    assertThrows(NullPointerException.class,
        () -> new BeschreibungsViewBuilder(null));
  }

}
