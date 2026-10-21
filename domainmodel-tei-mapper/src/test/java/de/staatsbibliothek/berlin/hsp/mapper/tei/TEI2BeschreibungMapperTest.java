package de.staatsbibliothek.berlin.hsp.mapper.tei;

import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsKomponentenTyp.KOPF;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KulturObjektTyp.CODEX;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEI2BeschreibungMapper.IDNO_HSK_TYPE;
import static java.nio.file.Files.newInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.GlobaleBeschreibungsKomponente;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.PtrFactory;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.tei_c.ns._1.Creation;
import org.tei_c.ns._1.Date;
import org.tei_c.ns._1.FileDesc;
import org.tei_c.ns._1.Idno;
import org.tei_c.ns._1.Licence;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.ProfileDesc;
import org.tei_c.ns._1.Ptr;
import org.tei_c.ns._1.PublicationStmt;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.TeiHeader;
import org.tei_c.ns._1.Title;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 21.04.20
 */
class TEI2BeschreibungMapperTest {

  static TEI tei;

  @BeforeAll
  static void init() throws Exception {
    Path teiFilePath = Paths.get("src", "test", "resources", "tei", "tei-msDesc_Westphal.xml");
    try (InputStream is = newInputStream(teiFilePath)) {
      List<TEI> teiList = TEIObjectFactory.unmarshal(is);
      tei = teiList.getFirst();
    }
  }

  @Test
  void testfindKatalogHSKID() {

    TEI tei = new TEI();
    Idno idno = new Idno();
    idno.setType(IDNO_HSK_TYPE);
    idno.getContent().add("440");
    PublicationStmt publicationStmt = new PublicationStmt();
    publicationStmt.getPublishersAndDistributorsAndAuthorities().add(idno);
    TeiHeader header = new TeiHeader();
    FileDesc fileDesc = new FileDesc();
    fileDesc.setPublicationStmt(publicationStmt);
    header.setFileDesc(fileDesc);
    tei.setTeiHeader(header);

    assertEquals("440", TEI2BeschreibungMapper.findKatalogHSKID(tei));
  }

  @Test
  void findAenderungsdatum() {
    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();

    assertEquals(LocalDateTime.of(2020, 1, 1, 0, 0),
        beschreibungMapper.findAenderungsdatumInTEIHeaderAndFiledesc(tei));

    assertNull(beschreibungMapper.findAenderungsdatumInTEIHeaderAndFiledesc(null));

    assertNull(beschreibungMapper.findAenderungsdatumInTEIHeaderAndFiledesc(new TEI()));

  }

  @Test
  void findErstellungsDatum() {
    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    LocalDateTime dateTime = LocalDateTime.of(2020, 1, 1, 0, 0);

    assertEquals(dateTime, beschreibungMapper.findErstellungsDatum(tei));

    assertNull(beschreibungMapper.findErstellungsDatum(null));

    TEI tei2 = new TEI();
    assertNull(beschreibungMapper.findErstellungsDatum(tei2));

    tei2.setTeiHeader(new TeiHeader());
    assertNull(beschreibungMapper.findErstellungsDatum(tei2));

    ProfileDesc profileDesc = new ProfileDesc();
    tei2.getTeiHeader().getEncodingDescsAndProfileDescsAndXenoDatas().add(profileDesc);
    assertNull(beschreibungMapper.findErstellungsDatum(tei2));

    Creation creation = new Creation();
    profileDesc.getAbstractsAndCreationsAndLangUsages().add(creation);
    assertNull(beschreibungMapper.findErstellungsDatum(tei2));

    Date date = new Date();
    creation.getContent().add(date);
    assertNull(beschreibungMapper.findErstellungsDatum(tei2));

    date.getContent().add("1.1.2020");
    assertNull(beschreibungMapper.findErstellungsDatum(tei2));

    date.getContent().add("1.1.2020");
    assertNull(beschreibungMapper.findErstellungsDatum(tei2));

    date.setWhen("2020-01-01");
    assertEquals(dateTime, beschreibungMapper.findErstellungsDatum(tei2));

    date.setWhen("2020");
    assertEquals(LocalDateTime.of(2020, 12, 31, 0, 0),
        beschreibungMapper.findErstellungsDatum(tei2));

    date.setWhen("1.1.2020");
    assertNull(beschreibungMapper.findErstellungsDatum(tei2));

  }

  @Test
  void testFindPublikation() throws Exception {
    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    LocalDateTime dateDruck = LocalDateTime.of(2011, 12, 31, 0, 0);
    LocalDateTime dateHsp = LocalDateTime.of(2020, 2, 3, 0, 0);

    Publikation publikation = beschreibungMapper.findPublikation(tei);
    assertNotNull(publikation);

    assertEquals(dateDruck, publikation.getErstpublikationsDatum());

    assertEquals(dateHsp, publikation.getPublikationsDatumHSP());

    TEI tei2 = new TEI();
    assertNull(beschreibungMapper.findPublikation(tei2));

    tei2.setTeiHeader(new TeiHeader());
    assertNull(beschreibungMapper.findPublikation(tei2));

    FileDesc fileDesc = new FileDesc();
    tei2.getTeiHeader().setFileDesc(fileDesc);
    assertNull(beschreibungMapper.findPublikation(tei2));

    PublicationStmt publicationStmt = new PublicationStmt();
    fileDesc.setPublicationStmt(publicationStmt);
    assertNull(beschreibungMapper.findPublikation(tei2).getPublikationsDatumHSP());
    assertNull(beschreibungMapper.findPublikation(tei2).getErstpublikationsDatum());
    assertNull(beschreibungMapper.findPublikation(tei2).getLizenz());
    assertEquals(0, beschreibungMapper.findPublikation(tei2).getPURLs().size());

    Date pubPrimaryDate = new Date();
    publicationStmt.getPublishersAndDistributorsAndAuthorities().add(pubPrimaryDate);
    assertNull(beschreibungMapper.findPublikation(tei2).getPublikationsDatumHSP());
    assertNull(beschreibungMapper.findPublikation(tei2).getErstpublikationsDatum());
    assertNull(beschreibungMapper.findPublikation(tei2).getLizenz());
    assertEquals(0, beschreibungMapper.findPublikation(tei2).getPURLs().size());

    pubPrimaryDate.setType(TEIValues.PUBLICATION_DATE_PRIMARY);
    assertNull(beschreibungMapper.findPublikation(tei2).getPublikationsDatumHSP());
    assertNull(beschreibungMapper.findPublikation(tei2).getErstpublikationsDatum());
    assertNull(beschreibungMapper.findPublikation(tei2).getLizenz());
    assertEquals(0, beschreibungMapper.findPublikation(tei2).getPURLs().size());

    pubPrimaryDate.setWhen("invalidDate");
    assertEquals(LocalDateTime.of(1900, 1, 1, 0, 0),
        beschreibungMapper.findPublikation(tei2).getErstpublikationsDatum());

    pubPrimaryDate.setWhen("2020");
    assertEquals(LocalDateTime.of(2020, 12, 31, 0, 0),
        beschreibungMapper.findPublikation(tei2).getErstpublikationsDatum());

    pubPrimaryDate.setWhen("2020-11");
    assertEquals(LocalDateTime.of(1900, 1, 1, 0, 0),
        beschreibungMapper.findPublikation(tei2).getErstpublikationsDatum());

    pubPrimaryDate.setWhen("2020-11-03");
    assertEquals(LocalDateTime.of(2020, 11, 3, 0, 0),
        beschreibungMapper.findPublikation(tei2).getErstpublikationsDatum());
  }

  @Test
  void testgetSchreibsprache() {

    assertNotNull(TEI2BeschreibungMapper.getSchreibsprache("de", "en"));

    assertEquals("de", TEI2BeschreibungMapper.getSchreibsprache("de", "en").getId());
  }

  @Test
  void testFindDokumentTyp() {

    MsDesc msDesc = new MsDesc();
    msDesc.setType(DokumentObjektTyp.HSP_DESCRIPTION.toString());

    assertEquals(DokumentObjektTyp.HSP_DESCRIPTION,
        TEI2BeschreibungMapper.findDokumentObjektTyp(msDesc));
  }

  @ParameterizedTest
  @ValueSource(strings = {"intern", "extern"})
  void testFindVerwaltungsTyp(String typ) {

    MsDesc msDesc = new MsDesc();
    msDesc.setStatus(typ);

    assertEquals(VerwaltungsTyp.valueOf(typ.toUpperCase()),
        TEI2BeschreibungMapper.findVerwaltungsTyp(msDesc));

  }

  @ParameterizedTest
  @ValueSource(strings = {"medieval", "illuminated"})
  void testFindBeschreibungsTyp(String typ) {

    MsDesc msDesc = new MsDesc();
    msDesc.setSubtype(typ);

    assertEquals(BeschreibungsTyp.valueOf(typ.toUpperCase()),
        TEI2BeschreibungMapper.findBeschreibungsTyp(msDesc));

  }

  @Test
  void testFindLizenz() {
    assertNull(TEI2BeschreibungMapper.findLizenz(List.of()));
    assertNull(TEI2BeschreibungMapper.findLizenz(List.of(new Licence())));

    String url = "http://some.url";
    Licence licence_1 = new Licence();
    licence_1.getTargets().add(url);
    Lizenz lizenz_1 = TEI2BeschreibungMapper.findLizenz(List.of(licence_1));
    assertNotNull(lizenz_1);
    assertNotNull(lizenz_1.getUris());
    assertTrue(lizenz_1.getUris().stream().anyMatch(u -> Objects.equals(u, url)));

    String beschreibungsText = "Die Beschreibung einer Lizenz.";
    Licence licence_2 = new Licence();
    licence_2.getContent().add(beschreibungsText);
    Lizenz lizenz_2 = TEI2BeschreibungMapper.findLizenz(List.of(licence_2));
    assertNotNull(lizenz_2);
    assertTrue(TEICommon.getContentAsString(licence_2.getContent()).contains(beschreibungsText));
  }

  @Test
  void map() throws Exception {
    Path teiFilePath = Paths.get("src", "test", "resources", "tei", "tei-msDesc_Westphal.xml");
    List<TEI> teiList = TEIObjectFactory.unmarshal(newInputStream(teiFilePath));
    assertEquals(1, teiList.size());
    TEI tei = teiList.getFirst();
    assertNotNull(tei);
    List<Title> titles = tei.getTeiHeader().getFileDesc().getTitleStmt().getTitles();
    assertEquals(1, titles.size());
    String title = titles.getFirst().getContent().stream()
        .map(String::valueOf)
        .collect(Collectors.joining(" "));

    assertTrue(title.contains(
        "Beschreibung von Cod. Guelf. 36.23 Aug. 2° (Die illuminierten Handschriften der"));
    assertTrue(title.contains(
        "Herzog August Bibliothek. Teil 1: 6. bis 12. Jahrhundert, beschrieben von Stefanie"));
    assertTrue(title.contains("Westphal (in Bearbeitung))"));

    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();

    List<Beschreibung> beschreibungen = beschreibungMapper.map(tei);

    assertFalse(beschreibungen.isEmpty());

    Beschreibung beschreibung = beschreibungen.getFirst();

    assertNotNull(beschreibung.getId());

    Set<GlobaleBeschreibungsKomponente> beschreibungsStruktur = beschreibung
        .getBeschreibungsStruktur();

    assertFalse(beschreibungsStruktur.isEmpty());

    assertTrue(beschreibungsStruktur.stream().anyMatch(komponente -> komponente.getTyp() == KOPF));

    assertEquals(VerwaltungsTyp.INTERN, beschreibung.getVerwaltungsTyp());
    assertEquals(2, beschreibung.getAltIdentifier().size());

    assertEquals("HSP-a8b3c8c3-0085-3af5-9886-73fc0609d390", beschreibung.getKodID());

    assertNotNull(beschreibung.getLizenz());
    assertNotNull(beschreibung.getLizenz().getUris());
    assertTrue(beschreibung.getLizenz().getUris().stream()
        .anyMatch(s -> s.equals("https://creativecommons.org/licenses/by-sa/3.0/de/")));

    assertEquals("Dieses Dokument steht unter einer Creative Commons Namensnennung-Weitergabe unter"
            + " gleichen Bedingungen 3.0 Deutschland Lizenz (CC BY-SA). Herzog August Bibliothek Wolfenbüttel"
            + " ( Copyright Information )",
        beschreibung.getLizenz().getBeschreibungsText());

    assertNotNull(beschreibung.getAutoren());
    assertEquals(DokumentObjektTyp.HSP_DESCRIPTION, beschreibung.getDokumentObjektTyp());
    assertEquals(3, beschreibung.getAutoren().size());

    Set<String> autoren =
        beschreibung.getAutoren().stream()
            .map(NormdatenReferenz::getName)
            .collect(Collectors.toSet());

    assertTrue(autoren.contains("Werner Hoffmann"));
    assertTrue(autoren.contains("Konrad Eichstädt"));
    assertTrue(autoren.contains("Anton Müller"));

    for (GlobaleBeschreibungsKomponente komponente : beschreibungsStruktur) {
      assertSame(KOPF, komponente.getTyp());
      BeschreibungsKomponenteKopf komponenteKopf = (BeschreibungsKomponenteKopf) komponente;
      assertEquals(CODEX, komponenteKopf.getKulturObjektTyp());
      assertEquals("Agrimensoren-Codex (Codex Arcerianus)",
          komponenteKopf.getTitel());
      Set<Identifikation> identifikationen = komponenteKopf.getIdentifikationen();
      assertTrue(identifikationen.isEmpty());
    }
  }

  @Test
  void testFindPURLs() throws Exception {
    Path teiFilePath = Paths.get("src", "test", "resources", "tei", "tei-msDesc_Westphal.xml");
    List<TEI> teiList = TEIObjectFactory.unmarshal(newInputStream(teiFilePath));
    assertEquals(1, teiList.size());
    TEI tei = teiList.getFirst();
    assertNotNull(tei);

    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    Set<PURL> purls = beschreibungMapper.findPurls(
        tei.getTeiHeader().getFileDesc().getPublicationStmt());

    assertNotNull(purls);
    assertEquals(2, purls.size());

    assertTrue(purls.stream().anyMatch(purl ->
        "HSP-ddd74884-974d-3007-9895-ce8d42201390".equals(purl.getId())
            && PURLTyp.INTERNAL == purl.getTyp()
            && Objects.nonNull(purl.getPurl())
            && "https://resolver.staatsbibliothek-berlin.de/HSP1660133922141000"
            .equals(purl.getPurl().toASCIIString())
            && Objects.nonNull(purl.getTarget())
            && "https://resolver.staatsbibliothek-berlin.de/HSP1660133922141000"
            .equals(purl.getTarget().toASCIIString())));

    assertTrue(purls.stream().anyMatch(purl ->
        "HSP-dc0c68d5-850c-3457-93f1-5f6213115ddd".equals(purl.getId())
            && PURLTyp.EXTERNAL == purl.getTyp()
            && Objects.nonNull(purl.getPurl())
            && "https://diglib.hab.de/mss/36-23-aug-2f/start.htm".equals(
            purl.getPurl().toASCIIString())
            && Objects.nonNull(purl.getTarget())
            && "https://diglib.hab.de/mss/36-23-aug-2f/start.htm".equals(
            purl.getTarget().toASCIIString())));
  }

  @Test
  void testMapPtrToPURL() {
    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();

    String purlInternal = "https://resolver.staatsbibliothek-berlin.de/HSP1660133922141000";

    Ptr internalPtr = new Ptr();
    internalPtr.setId("id");
    internalPtr.setType(PtrFactory.TYPE);
    internalPtr.setSubtype(PtrFactory.SUBTYPE);
    internalPtr.getTargets().add(purlInternal);

    PURL internalPURL = beschreibungMapper.mapPtrToPURL(internalPtr);
    assertNotNull(internalPURL);
    assertEquals("id", internalPURL.getId());
    assertSame(PURLTyp.INTERNAL, internalPURL.getTyp());
    assertNotNull(internalPURL.getPurl());
    assertEquals(purlInternal, internalPURL.getPurl().toASCIIString());
    assertNotNull(internalPURL.getTarget());
    assertEquals(purlInternal, internalPURL.getTarget().toASCIIString());

    String purlExternal = "https://diglib.hab.de/mss/36-23-aug-2f/start.htm";

    Ptr externalPtr = new Ptr();
    externalPtr.setType(PtrFactory.TYPE);
    externalPtr.getTargets().add(purlExternal);

    PURL externalPURL = beschreibungMapper.mapPtrToPURL(externalPtr);
    assertNotNull(externalPURL);
    assertEquals("HSP-dc0c68d5-850c-3457-93f1-5f6213115ddd", externalPURL.getId());
    assertSame(PURLTyp.EXTERNAL, externalPURL.getTyp());
    assertNotNull(externalPURL.getPurl());
    assertEquals(purlExternal, externalPURL.getPurl().toASCIIString());
    assertNotNull(externalPURL.getTarget());
    assertEquals(purlExternal, externalPURL.getTarget().toASCIIString());
  }

  @Test
  void testJaxB() throws Exception {
    Path teiFilePath = Paths.get("src", "test", "resources", "tei", "karlsruhe_tei_leerzeilen.xml");
    try (InputStream is = newInputStream(teiFilePath)) {
      List<TEI> teiList = TEIObjectFactory.unmarshal(is);
      TEI tei = teiList.getFirst();

      assertTrue(TEIObjectFactory.marshal(tei).contains(
              "<note type=\"text\"><ref type=\"locus\">Fragment 1</ref>: Als Ansetzfalz auf dem vorderen und hinteren Innendeckel und jeweils um die erste bzw. letzte Lage herumgelegt Makulatur aus einer <ref type=\"5260[0]\">Pergament</ref>handschrift, weitestgehend von den Papierspiegeln überklebt und nicht mehr sichtbar: zwei Streifen evtl. desselben Doppelblatts (die Schlitze für die Bindung des Ursprungsband befinden sich in derselben Höhe); wohl ursprünglich Quart-Format. Nur wenig Textmaterial sichtbar; Grundtext: <ref type=\"script\">Textualis</ref> mit kursiven Elementen, wohl <ref type=\"origDate\">2. Hälfte des 14. Jh.</ref>, Zeilenhöhe: 1 cm, kein Buchschmuck erhalten, Sprache: <ref type=\"textLang\">lat</ref>., Inhalt nicht ermittelt, sichtbare Textteile: […]<hi rend=\"italic\">minancia</hi>, […]<hi rend=\"italic\">ctis, po</hi>[…]; <hi rend=\"italic\">inabunt</hi>. Zwei zeitliche Schichten von Marginalien: a. (Flügelfalz zwischen fol. 13/14): Kursive der 1. Hälfte des 15. Jh., stark gekürzt, kein Buchschmuck erhalten, Sprache: lat., Inhalt: grammatische Ausführungen, wohl zur Morphologie von Verben, vgl. die Textstücke … <hi rend=\"italic\">geminat …</hi> <hi rend=\"italic\">Exemplum de e </hi>[…] <hi rend=\"italic\">pendeo, pependi</hi>. <hi rend=\"italic\">Exemplum de m </hi>[…] <hi rend=\"italic\">momordi, mordeo</hi>. b. (Flügelfalz zwischen fol. 121/122): Kursive des 15. Jh., stark abgerieben und daher die Schrift kaum zu entziffern. Kein Buchschmuck erhalten. Sprache: lat., Inhalt nicht ermittelt.</note>"),
          TEIObjectFactory.marshal(tei));
    }
  }
}
