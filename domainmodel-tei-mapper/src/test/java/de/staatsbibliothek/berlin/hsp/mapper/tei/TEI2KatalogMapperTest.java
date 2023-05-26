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

package de.staatsbibliothek.berlin.hsp.mapper.tei;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.IDNO_TYPE_HSK;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.IDNO_TYPE_HSP;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.PTR_TYPE_HSP;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.PTR_TYPE_THUMBNAIL;
import static java.nio.file.Files.newInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPMapperException;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.FileDesc;
import org.tei_c.ns._1.Idno;
import org.tei_c.ns._1.Ptr;
import org.tei_c.ns._1.PublicationStmt;
import org.tei_c.ns._1.TEI;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 24.02.2022
 */
public class TEI2KatalogMapperTest {

  static TEI tei;

  @BeforeAll
  static void init() throws Exception {
    Path teiFilePath = Paths.get("src", "test", "resources", "tei", "tei-katalog_Aurich.xml");
    try (InputStream is = newInputStream(teiFilePath)) {
      List<TEI> teiList = TEIObjectFactory.unmarshal(is);
      tei = teiList.get(0);
    }
  }

  @Test
  void testGetFileDesc() throws Exception {
    Assertions.assertThrows(HSPMapperException.class, () -> {
      TEI2KatalogMapper.getFileDesc(null);
    });

    Assertions.assertThrows(HSPMapperException.class, () -> {
      TEI2KatalogMapper.getFileDesc(new TEI());
    });

    assertNotNull(TEI2KatalogMapper.getFileDesc(tei));
  }

  @Test
  void testGetBody() throws Exception {
    Assertions.assertThrows(HSPMapperException.class, () -> {
      TEI2KatalogMapper.getBody(null);
    });

    Assertions.assertThrows(HSPMapperException.class, () -> {
      TEI2KatalogMapper.getBody(new TEI());
    });

    assertNotNull(TEI2KatalogMapper.getBody(tei));
  }

  @Test
  void testGetPublicationStmt() throws Exception {
    Assertions.assertThrows(HSPMapperException.class, () -> {
      TEI2KatalogMapper.getPublicationStmt(null);
    });

    Assertions.assertThrows(HSPMapperException.class, () -> {
      TEI2KatalogMapper.getPublicationStmt(new FileDesc());
    });

    FileDesc fileDesc = TEI2KatalogMapper.getFileDesc(tei);
    assertNotNull(fileDesc);
    assertNotNull(TEI2KatalogMapper.getPublicationStmt(fileDesc));
  }

  @Test
  void testGetIdnoForType() throws Exception {
    List<Idno> idnos = new ArrayList<>();

    Idno hskIdno = new Idno();
    hskIdno.setType(IDNO_TYPE_HSK);
    hskIdno.getContent().add("hsk123");
    idnos.add(hskIdno);

    Optional<String> idnoValue = TEI2KatalogMapper.getIdnoForType(idnos, IDNO_TYPE_HSK);
    assertTrue(idnoValue.isPresent());
    assertEquals("hsk123", idnoValue.get());

    Optional<String> notExists = TEI2KatalogMapper.getIdnoForType(idnos, IDNO_TYPE_HSP);
    assertFalse(notExists.isPresent());

    notExists = TEI2KatalogMapper.getIdnoForType(idnos, IDNO_TYPE_HSP);
    assertFalse(notExists.isPresent());

  }

  @Test
  void testGetTitel() throws Exception {
    assertEquals("", TEI2KatalogMapper.getTitel(null));
    assertEquals("", TEI2KatalogMapper.getTitel(new FileDesc()));

    FileDesc fileDesc = TEI2KatalogMapper.getFileDesc(tei);
    assertNotNull(fileDesc);

    String titel = TEI2KatalogMapper.getTitel(fileDesc);
    assertNotNull(titel);
    assertEquals(
        "STAHL, Irene: Handschriften in Nordwestdeutschland: Aurich - Emden - Oldenburg. "
            + "- Wiesbaden: Harrassowitz, 1993. - (Mittelalterliche Handschriften in Niedersachsen: Kurzkatalog; 3)",
        titel);
  }

  @Test
  void testGetLizenzURI() throws Exception {
    assertEquals("", TEI2KatalogMapper.getLizenzURI(null));
    assertEquals("", TEI2KatalogMapper.getLizenzURI(new PublicationStmt()));

    FileDesc fileDesc = TEI2KatalogMapper.getFileDesc(tei);
    assertNotNull(fileDesc);

    PublicationStmt publicationStmt = TEI2KatalogMapper.getPublicationStmt(fileDesc);
    assertNotNull(publicationStmt);

    String lizenzURI = TEI2KatalogMapper.getLizenzURI(publicationStmt);
    assertNotNull(lizenzURI);
    assertEquals("https://rightsstatements.org/page/InC/1.0/", lizenzURI);
  }

  @Test
  void testGetVerlag() throws Exception {
    assertNull(TEI2KatalogMapper.getVerlag(null));
    assertNull(TEI2KatalogMapper.getVerlag(new PublicationStmt()));

    FileDesc fileDesc = TEI2KatalogMapper.getFileDesc(tei);
    assertNotNull(fileDesc);

    PublicationStmt publicationStmt = TEI2KatalogMapper.getPublicationStmt(fileDesc);
    assertNotNull(publicationStmt);

    NormdatenReferenz verlag = TEI2KatalogMapper.getVerlag(publicationStmt);
    assertNotNull(verlag);
    assertNotNull(verlag.getId());
    assertEquals("CorporateBody", verlag.getTypeName());
    assertEquals("Harrassowitz", verlag.getName());
    assertEquals("", verlag.getGndID());
    assertNotNull(verlag.getIdentifikator());
    assertEquals(0, verlag.getIdentifikator().size());
    assertNotNull(verlag.getVarianterName());
    assertEquals(0, verlag.getVarianterName().size());
  }

  @Test
  void testGetPtrTarget() {
    assertEquals("", TEI2KatalogMapper.getPtrTarget(null, null));
    assertEquals("", TEI2KatalogMapper.getPtrTarget(new ArrayList<>(), null));
    assertEquals("", TEI2KatalogMapper.getPtrTarget(null, PTR_TYPE_HSP));
    assertEquals("", TEI2KatalogMapper.getPtrTarget(new ArrayList<>(), PTR_TYPE_HSP));

    List<Ptr> ptrs = new ArrayList();
    Ptr ptr = new Ptr();
    ptr.getTargets().add("http://some.url");
    ptr.setType(PTR_TYPE_HSP);
    ptrs.add(ptr);

    assertEquals("http://some.url", TEI2KatalogMapper.getPtrTarget(ptrs, PTR_TYPE_HSP));
    assertEquals("", TEI2KatalogMapper.getPtrTarget(ptrs, PTR_TYPE_THUMBNAIL));
  }

  @Test
  void testGetDigitalisat() throws Exception {
    assertNotNull(TEI2KatalogMapper.getDigitalisat(null));
    assertNotNull(TEI2KatalogMapper.getDigitalisat(new PublicationStmt()));

    FileDesc fileDesc = TEI2KatalogMapper.getFileDesc(tei);
    assertNotNull(fileDesc);

    PublicationStmt publicationStmt = TEI2KatalogMapper.getPublicationStmt(fileDesc);
    assertNotNull(publicationStmt);

    Digitalisat digitalisat = TEI2KatalogMapper.getDigitalisat(publicationStmt);
    assertNotNull(digitalisat);

    assertNotNull(digitalisat.getId());
    assertNotNull(digitalisat.getManifestURL());
    assertEquals(new URI("https://iiif.hab.de/object/mss_560-helmst/manifest"), digitalisat.getManifestURL());

    assertNotNull(digitalisat.getThumbnailURL());
    assertEquals(
        new URI("https://iiif.ub.uni-leipzig.de/iiif/j2k/0000/0345/0000034537/00000003.jpx/full/200,/0/default.jpg"),
        digitalisat.getThumbnailURL());

    assertNull(digitalisat.getAlternativeURL());

  }

  @Test
  void testGetPublikationsJahr() throws Exception {
    assertEquals("", TEI2KatalogMapper.getPublikationsJahr(null));
    assertEquals("", TEI2KatalogMapper.getPublikationsJahr(new PublicationStmt()));

    FileDesc fileDesc = TEI2KatalogMapper.getFileDesc(tei);
    assertNotNull(fileDesc);

    PublicationStmt publicationStmt = TEI2KatalogMapper.getPublicationStmt(fileDesc);
    assertNotNull(publicationStmt);

    String publikationsJahr = TEI2KatalogMapper.getPublikationsJahr(publicationStmt);
    assertNotNull(publikationsJahr);
    assertEquals("1993", publikationsJahr);
  }

  @Test
  void map() throws Exception {
    TEI2KatalogMapper mapper = new TEI2KatalogMapper();

    Assertions.assertThrows(HSPMapperException.class, () -> {
      mapper.map(null);
    });

    Assertions.assertThrows(HSPMapperException.class, () -> {
      mapper.map(new TEI());
    });

    Katalog katalog = mapper.map(tei);
    assertNotNull(katalog);

    assertNotNull(katalog.getId());
    assertEquals("HSP-a8abb4bb-284b-3b27-aa7c-b790dc20f80b", katalog.getId());

    assertNotNull(katalog.getTitle());
    assertEquals("STAHL, Irene: Handschriften in Nordwestdeutschland: Aurich - Emden - Oldenburg. "
            + "- Wiesbaden: Harrassowitz, 1993. - (Mittelalterliche Handschriften in Niedersachsen: Kurzkatalog; 3)",
        katalog.getTitle());

    assertNotNull(katalog.getAutoren());
    assertEquals(1, katalog.getAutoren().size());
    assertTrue(katalog.getAutoren().stream().anyMatch(a -> "Irene Stahl".equals(a.getName())));

    assertNotNull(katalog.getVerlag());
    assertEquals("Harrassowitz", katalog.getVerlag().getName());

    assertNotNull(katalog.getPublikationsJahr());
    assertEquals("1993", katalog.getPublikationsJahr());

    assertNotNull(katalog.getLizenzUri());
    assertEquals("https://rightsstatements.org/page/InC/1.0/", katalog.getLizenzUri());

    assertNotNull(katalog.getHskID());
    assertEquals("440", katalog.getHskID());

    assertNotNull(katalog.getTeiXML());
    assertTrue(katalog.getTeiXML().contains("Ergebnis einer Digitaliserung an der Staatsbibliothek zu Berlin"));

    assertNotNull(katalog.getErstellDatum());
    assertNotNull(katalog.getAenderungsDatum());

    assertNotNull(katalog.getDigitalisat());
    assertNotNull(katalog.getDigitalisat().getId());
    assertNotNull(katalog.getDigitalisat().getManifestURL());
    assertEquals(new URI("https://iiif.hab.de/object/mss_560-helmst/manifest"),
        katalog.getDigitalisat().getManifestURL());

    assertNull(katalog.getDigitalisat().getAlternativeURL());
    assertNotNull(katalog.getDigitalisat().getThumbnailURL());
    assertEquals(
        new URI("https://iiif.ub.uni-leipzig.de/iiif/j2k/0000/0345/0000034537/00000003.jpx/full/200,/0/default.jpg"),
        katalog.getDigitalisat().getThumbnailURL());

  }

}
