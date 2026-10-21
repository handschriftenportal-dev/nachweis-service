package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import jakarta.xml.bind.JAXBException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Additional;
import org.tei_c.ns._1.Bibl;
import org.tei_c.ns._1.FileDesc;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.SourceDesc;
import org.tei_c.ns._1.Surrogates;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.TeiHeader;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 06.10.2020.
 * @version 1.0
 */
public class SurrogatesFactoryTest {

  private static final String TEI_ORIGINAL = """
      <?xml version="1.0" encoding="UTF-8" standalone="yes"?><TEI xmlns="http://www.tei-c.org/ns/1.0" xmlns:xi="http://www.w3.org/2001/XInclude" xmlns:dcr="http://www.isocat.org/ns/dcr" xmlns:egXML="http://www.tei-c.org/ns/Examples" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><teiHeader><fileDesc><sourceDesc><msDesc><additional><surrogates><bibl default="true" xml:id="HSP-12"><idno>12</idno><ref target="https://iiif.ub.uni-leipzig.de/0000009089/manifest.json" type="manifest"/><ref target="https://iiif.ub.uni-leipzig.de/0000009089/bild.png" type="other"/><ref target="https://iiif.ub.uni-leipzig.de/fcgi-bin/iipsrv.fcgi?iiif=/j2k/0000/0090/0000009089/00000057.jpx/full/100,/0/default.jpg" type="thumbnail"/><ref target="http://normdaten.staatsbibliothek-berlin.de/vocabulary/digitalizationType/color"/><date when="2020-10-06" type="digitized">06.10.2020</date><date when="2020-10-07" type="issued">07.10.2020</date><date when="2023-07-03" type="published">03.07.2023</date><placeName ref="123" type="digitizing">Berlin</placeName><orgName ref="456" type="digitizing">Staatsbibliothek zu Berlin</orgName></bibl></surrogates></additional></msDesc></sourceDesc></fileDesc></teiHeader></TEI>""";

  @Test
  void testBuildSurrogate() throws JAXBException {

    Digitalisat digitalisat =
        Digitalisat.DigitalisatBuilder()
            .withID("12")
            .withManifest(
                "https://iiif.ub.uni-leipzig.de/0000009089/manifest.json")
            .withAlternativeUrl(
                "https://iiif.ub.uni-leipzig.de/0000009089/bild.png")
            .withThumbnail(
                "https://iiif.ub.uni-leipzig.de/fcgi-bin/iipsrv.fcgi?iiif=/j2k/0000/0090/0000009089/00000057.jpx/full/100,/0/default.jpg")
            .withEntstehungsort(
                new NormdatenReferenz("123", "Berlin", "GDN123"))
            .withDigitalisierendeEinrichtung(
                new NormdatenReferenz("456", "Staatsbibliothek zu Berlin",
                    "GND-X123"))
            .withDigitalisierungsdatum(
                LocalDate.parse("06.10.2020",
                    DateTimeFormatter.ofPattern("dd.MM.yyyy")))
            .withErstellungsdatum(
                LocalDate.parse("07.10.2020",
                    DateTimeFormatter.ofPattern("dd.MM.yyyy")))
            .withPublikationsdatum(
                LocalDate.parse("03.07.2023",
                    DateTimeFormatter.ofPattern("dd.MM.yyyy")))
            .addDigitalisatsKonzept(
                new NormdatenReferenz.NormdatenReferenzBuilder()
                    .withName("color")
                    .withUri(
                        "http://normdaten.staatsbibliothek-berlin.de/vocabulary/digitalizationType/color")
                    .build())
            .build();

    Surrogates surrogates = SurrogatesFactory.build(List.of(digitalisat));

    assertNotNull(surrogates);

    assertEquals(1, surrogates.getContent().size());

    Bibl bibl = (Bibl) surrogates.getContent().getFirst();

    assertEquals(10, bibl.getContent().size());

    TEI tei = new TEI();
    TeiHeader teiHeader = new TeiHeader();
    tei.setTeiHeader(teiHeader);

    FileDesc fileDesc = new FileDesc();
    teiHeader.setFileDesc(fileDesc);

    SourceDesc sourceDesc = new SourceDesc();
    fileDesc.getSourceDescs().add(sourceDesc);

    MsDesc msDesc = new MsDesc();
    sourceDesc.getBiblsAndBiblStructsAndListBibls().add(msDesc);

    Additional additional = new Additional();
    msDesc.getMsContentsAndPhysDescsAndHistories().add(additional);
    additional.setSurrogates(surrogates);

    final String teiXML = TEIObjectFactory.marshal(tei);

    Diff diff =
        DiffBuilder.compare(TEI_ORIGINAL)
            .withTest(Input.fromString(teiXML))
            .ignoreComments()
            .ignoreWhitespace()
            .build();

    assertFalse(diff.hasDifferences(), diff.toString());
  }
}
