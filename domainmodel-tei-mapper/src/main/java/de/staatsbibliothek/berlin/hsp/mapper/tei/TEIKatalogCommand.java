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

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tei_c.ns._1.Idno;
import org.tei_c.ns._1.PublicationStmt;
import org.tei_c.ns._1.TEI;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 28.02.22
 */
public class TEIKatalogCommand {

  private static Logger logger = LoggerFactory.getLogger(TEIKatalogCommand.class);

  private TEIKatalogCommand() {
  }

  public static void updateId(Katalog katalog) throws Exception {
    if (Objects.nonNull(katalog) && Objects.nonNull(katalog.getTeiXML()) && Objects.nonNull(katalog.getId())) {

      logger.info("Update Katalog ID {} ", katalog.getId());

      List<TEI> tei = TEIObjectFactory
          .unmarshal(new ByteArrayInputStream(katalog.getTeiXML().getBytes(StandardCharsets.UTF_8)));

      if (tei.size() == 1) {
        TEI t = tei.get(0);

        PublicationStmt publicationStmt = TEICommon.findFirst(PublicationStmt.class, tei)
            .orElseThrow(() -> new Exception("tei contains no publicationStmt"));

        Optional<Idno> optionalIdno = publicationStmt.getPublishersAndDistributorsAndAuthorities()
            .stream()
            .filter(elem -> Idno.class.isAssignableFrom(elem.getClass()))
            .map(elem -> (Idno) elem)
            .filter(idno -> TEIValues.IDNO_TYPE_HSP.equals(idno.getType()))
            .findFirst();

        if (optionalIdno.isPresent()) {
          optionalIdno.get().getContent().clear();
          optionalIdno.get().getContent().add(katalog.getId());
          logger.info("Updated idno with id {}", katalog.getId());
        } else {
          Idno idno = new Idno();
          idno.setType(TEIValues.IDNO_TYPE_HSP);
          idno.getContent().add(katalog.getId());
          publicationStmt.getPublishersAndDistributorsAndAuthorities().add(idno);
          logger.info("Added new idno with id {}", katalog.getId());
        }

        katalog.setTeiXML(TEIObjectFactory.marshal(t));
      }
    }
  }
}
