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
