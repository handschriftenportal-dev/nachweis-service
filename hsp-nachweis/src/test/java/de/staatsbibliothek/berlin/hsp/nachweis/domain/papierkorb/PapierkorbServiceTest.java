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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.papierkorb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.GeloeschtesDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KulturObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 29.11.22
 */

@Slf4j
@QuarkusTest
@TestTransaction
class PapierkorbServiceTest {

  @Inject
  GeloeschtesDokumentRepository geloeschtesDokumentRepository;

  @Test
  void testErzeugeGeloeschtesDokumentKOD() {
    PapierkorbService papierkorbService = new PapierkorbService(geloeschtesDokumentRepository);

    KulturObjektDokument kod = createKOD();
    Bearbeiter bearbeiter = createBearbeiter();

    String geloeschtesDokumentId = papierkorbService.erzeugeGeloeschtesDokument(kod, bearbeiter);

    assertNotNull(geloeschtesDokumentId);

    GeloeschtesDokument geloeschtesDokument = geloeschtesDokumentRepository.findById(geloeschtesDokumentId);
    assertNotNull(geloeschtesDokument);
    assertNotNull(geloeschtesDokument.getId());
    assertEquals(kod.getId(), geloeschtesDokument.getDokumentId());
    assertNotNull(geloeschtesDokument);
    assertSame(DokumentObjektTyp.HSP_OBJECT, geloeschtesDokument.getDokumentObjektTyp());
    assertEquals("Msc.Can.18", geloeschtesDokument.getGueltigeSignatur());
    assertNotNull(geloeschtesDokument.getAlternativeSignaturen());
    assertTrue(geloeschtesDokument.getAlternativeSignaturen().contains("Msc.Can.18#1"));
    assertTrue(geloeschtesDokument.getAlternativeSignaturen().contains("Msc.Can.18#2"));
    assertEquals("https://resolver.staatsbibliothek-berlin.de/k-1", geloeschtesDokument.getInternePurls());
    assertEquals("774909e2", geloeschtesDokument.getBesitzerId());
    assertEquals("Staatsbibliothek zu Berlin", geloeschtesDokument.getBesitzerName());
    assertEquals("ee1611b6", geloeschtesDokument.getAufbewahrungsortId());
    assertEquals("Berlin", geloeschtesDokument.getAufbewahrungsortName());
    assertEquals("<TEI/>", geloeschtesDokument.getTeiXML());
    assertNotNull(geloeschtesDokument.getLoeschdatum());
    assertEquals("b-jd123", geloeschtesDokument.getBearbeiterId());
    assertEquals("John Doe", geloeschtesDokument.getBearbeiterName());
  }

  @Test
  void testErzeugeGeloeschtesDokumentBeschreibung() {
    PapierkorbService papierkorbService = new PapierkorbService(geloeschtesDokumentRepository);

    Beschreibung beschreibung = createBeschreibung();
    Bearbeiter bearbeiter = createBearbeiter();

    String geloeschtesDokumentId = papierkorbService.erzeugeGeloeschtesDokument(beschreibung, bearbeiter);

    assertNotNull(geloeschtesDokumentId);

    GeloeschtesDokument geloeschtesDokument = geloeschtesDokumentRepository.findById(geloeschtesDokumentId);
    assertNotNull(geloeschtesDokument);
    assertNotNull(geloeschtesDokument.getId());
    assertEquals(beschreibung.getId(), geloeschtesDokument.getDokumentId());
    assertSame(DokumentObjektTyp.HSP_DESCRIPTION, geloeschtesDokument.getDokumentObjektTyp());
    assertEquals("Sig. 1", geloeschtesDokument.getGueltigeSignatur());
    assertNotNull(geloeschtesDokument.getAlternativeSignaturen());
    assertTrue(geloeschtesDokument.getAlternativeSignaturen().contains("Alt. 1"));
    assertTrue(geloeschtesDokument.getAlternativeSignaturen().contains("Alt. 2"));
    assertEquals("https://resolver.staatsbibliothek-berlin.de/b-1", geloeschtesDokument.getInternePurls());
    assertEquals("774909e2", geloeschtesDokument.getBesitzerId());
    assertEquals("Staatsbibliothek zu Berlin", geloeschtesDokument.getBesitzerName());
    assertEquals("ee1611b6", geloeschtesDokument.getAufbewahrungsortId());
    assertEquals("Berlin", geloeschtesDokument.getAufbewahrungsortName());
    assertEquals("<TEI/>", geloeschtesDokument.getTeiXML());
    assertNotNull(geloeschtesDokument.getLoeschdatum());
    assertEquals("b-jd123", geloeschtesDokument.getBearbeiterId());
    assertEquals("John Doe", geloeschtesDokument.getBearbeiterName());
  }

  @Test
  void testFindAllGeloeschteDokumente() {
    PapierkorbService papierkorbService = new PapierkorbService(geloeschtesDokumentRepository);

    Bearbeiter bearbeiter = createBearbeiter();

    papierkorbService.erzeugeGeloeschtesDokument(createKOD(), bearbeiter);
    papierkorbService.erzeugeGeloeschtesDokument(createBeschreibung(), bearbeiter);

    List<GeloeschtesDokument> geloeschteDokumente = papierkorbService.findAllGeloeschteDokumente();
    assertNotNull(geloeschteDokumente);
    assertEquals(2, geloeschteDokumente.size());
  }

  @Test
  void testJoinInternePURLs() {
    PapierkorbService papierkorbService = new PapierkorbService(geloeschtesDokumentRepository);
    Set<PURL> purls = new LinkedHashSet<>();

    assertEquals("", papierkorbService.joinInternePURLs(purls));

    purls.add(new PURL(URI.create("https://resolver.staatsbibliothek-berlin.de/b-1"),
        URI.create("https://hsp.de/b-1"), PURLTyp.INTERNAL));
    purls.add(new PURL(URI.create("https://resolver.staatsbibliothek-berlin.de/b-2"),
        URI.create("https://hsp.de/b-2"), PURLTyp.INTERNAL));
    purls.add(new PURL(URI.create("https://any.url/b-2"),
        URI.create("https://hsp.de/b-2"), PURLTyp.EXTERNAL));

    assertEquals("https://resolver.staatsbibliothek-berlin.de/b-1"
            + " https://resolver.staatsbibliothek-berlin.de/b-2",
        papierkorbService.joinInternePURLs(purls));
  }

  KulturObjektDokument createKOD() {
    NormdatenReferenz besitzer =
        new NormdatenReferenz("774909e2", "Staatsbibliothek zu Berlin", "5036103-X", "CorporateBody");

    NormdatenReferenz aufbewahrungsOrt
        = new NormdatenReferenz("ee1611b6", "Berlin", "4005728-8", "Place");

    PURL purl = new PURL(URI.create("https://resolver.staatsbibliothek-berlin.de/k-1"),
        URI.create("https://hsp.de/k-1"), PURLTyp.INTERNAL);

    return new KulturObjektDokumentBuilder()
        .withId(TEIValues.UUID_PREFIX + UUID.randomUUID())
        .withGndIdentifier("GND-ID-123")
        .withRegistrierungsDatum(LocalDateTime.now())
        .withTEIXml("<TEI/>")
        .withGueltigerIdentifikation(new IdentifikationBuilder()
            .withId("18")
            .withIdent("Msc.Can.18")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(besitzer)
            .withAufbewahrungsOrt(aufbewahrungsOrt).build())
        .addAlternativeIdentifikation(new IdentifikationBuilder()
            .withId("181")
            .withIdent("Msc.Can.18#1")
            .withIdentTyp(IdentTyp.ALTSIGNATUR)
            .withBesitzer(besitzer)
            .withAufbewahrungsOrt(aufbewahrungsOrt).build())
        .addAlternativeIdentifikation(new IdentifikationBuilder()
            .withId("182")
            .withIdent("Msc.Can.18#2")
            .withIdentTyp(IdentTyp.ALTSIGNATUR)
            .withBesitzer(besitzer)
            .withAufbewahrungsOrt(aufbewahrungsOrt).build())
        .addPURL(purl)
        .build();
  }

  private Beschreibung createBeschreibung() {
    NormdatenReferenz besitzer =
        new NormdatenReferenz("774909e2", "Staatsbibliothek zu Berlin", "5036103-X", "CorporateBody");

    NormdatenReferenz aufbewahrungsOrt
        = new NormdatenReferenz("ee1611b6", "Berlin", "4005728-8", "Place");

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Sig. 1")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .withAufbewahrungsOrt(aufbewahrungsOrt)
        .build();

    Identifikation identifikationAlt1 = new Identifikation.IdentifikationBuilder()
        .withIdent("Alt. 1")
        .withIdentTyp(IdentTyp.ALTSIGNATUR)
        .withBesitzer(besitzer)
        .withAufbewahrungsOrt(aufbewahrungsOrt)
        .build();

    Identifikation identifikationAlt2 = new Identifikation.IdentifikationBuilder()
        .withIdent("Alt. 2")
        .withIdentTyp(IdentTyp.ALTSIGNATUR)
        .withBesitzer(besitzer)
        .withAufbewahrungsOrt(aufbewahrungsOrt)
        .build();

    BeschreibungsKomponenteKopf bkKopf = new BeschreibungsKomponenteKopfBuilder()
        .withId(UUID.randomUUID().toString())
        .withKulturObjektTyp(KulturObjektTyp.CODEX)
        .withTitel("titel")
        .withIndentifikationen(Set.of(identifikation, identifikationAlt1, identifikationAlt2))
        .build();

    PURL purl = new PURL(URI.create("https://resolver.staatsbibliothek-berlin.de/b-1"),
        URI.create("https://hsp.de/b-1"), PURLTyp.INTERNAL);

    return new BeschreibungsBuilder()
        .withId("b-1")
        .withKodID("kod-1")
        .withKatalog("kat-1")
        .addBeschreibungsKomponente(bkKopf)
        .addAltIdentifier("alt-id-1")
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withDokumentObjectTyp(DokumentObjektTyp.HSP_DESCRIPTION)
        .withBeschreibungsTyp(BeschreibungsTyp.MEDIEVAL)
        .addPURL(purl)
        .withTEIXml("<TEI/>")
        .withErstellungsDatum(LocalDateTime.now())
        .build();
  }

  Bearbeiter createBearbeiter() {
    return Bearbeiter.newBuilder()
        .withBearbeitername("b-jd123")
        .withVorname("John")
        .withNachname("Doe")
        .withEmail("John@Doe.de")
        .withRolle("Redakteur")
        .build();
  }
}
