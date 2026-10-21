package de.staatsbibliothek.berlin.hsp.mapper.tei;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.PtrFactory;
import java.net.URI;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.FileDesc;
import org.tei_c.ns._1.Ptr;
import org.tei_c.ns._1.PubPlace;
import org.tei_c.ns._1.PublicationStmt;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.TeiHeader;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 21.09.22
 */
public class TEIUpdatePURLsCommandTest {

  @Test
  void testupdatePURLs() throws Exception {

    TEI tei = new TEI();
    TeiHeader teiHeader = new TeiHeader();
    tei.setTeiHeader(teiHeader);

    FileDesc fileDesc = new FileDesc();
    teiHeader.setFileDesc(fileDesc);

    PublicationStmt publicationStmt = new PublicationStmt();
    fileDesc.setPublicationStmt(publicationStmt);

    PubPlace pubPlace = new PubPlace();
    publicationStmt.getPublishersAndDistributorsAndAuthorities().add(pubPlace);

    Ptr otherPtr = new Ptr();
    otherPtr.setType("thumbnailForPresentations");
    otherPtr.getTargets().add("https://thumbnails.de/123");
    pubPlace.getContent().add(otherPtr);

    PURL externalPURL = new PURL(URI.create("https://resolver.de/123"), URI.create("https://target.de/123"),
        PURLTyp.EXTERNAL);
    Ptr externalPtr = PtrFactory.build(externalPURL);
    pubPlace.getContent().add(externalPtr);

    PURL internalPURL = new PURL(URI.create("https://resolver.de/456"), URI.create("https://target.de/456"),
        PURLTyp.INTERNAL);

    TEIUpdatePURLsCommand.updatePURLs(tei, Set.of(internalPURL));

    assertEquals(2, pubPlace.getContent().size());
    assertTrue(pubPlace.getContent().contains(otherPtr));
    assertFalse(pubPlace.getContent().contains(externalPtr));

    assertTrue(pubPlace.getContent().stream()
        .filter(Ptr.class::isInstance)
        .map(Ptr.class::cast)
        .anyMatch(ptr -> ptr.getTargets().contains("https://resolver.de/456")
            && PtrFactory.TYPE.equals(ptr.getType())
            && PtrFactory.SUBTYPE.equals(ptr.getSubtype())));
  }

}
