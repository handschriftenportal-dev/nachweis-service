package de.staatsbibliothek.berlin.hsp.mapper.tei;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
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
import org.tei_c.ns._1.Publisher;
import org.tei_c.ns._1.TEI;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 28.02.22
 */
public class TEIKatalogCommand {

  private static final Logger logger = LoggerFactory.getLogger(TEIKatalogCommand.class);

  private TEIKatalogCommand() {
  }

  public static void updateVerlag(Katalog katalog) throws Exception {
    if (Objects.nonNull(katalog) && Objects.nonNull(katalog.getTeiXML()) && Objects.nonNull(
        katalog.getVerlag())) {

      logger.info("Update Katalog {} with Verlag {} ", katalog.getId(), katalog.getVerlag());

      List<TEI> teiList = TEIObjectFactory
          .unmarshal(
              new ByteArrayInputStream(katalog.getTeiXML().getBytes(StandardCharsets.UTF_8)));

      if (teiList.size() != 1) {
        return;
      }

      TEI tei = teiList.get(0);

      PublicationStmt publicationStmt = TEICommon.findFirst(PublicationStmt.class, tei)
          .orElseThrow(() -> new Exception("tei contains no publicationStmt"));

      publicationStmt.getPublishersAndDistributorsAndAuthorities().stream()
          .filter(o -> o instanceof Publisher)
          .map(o -> (Publisher) o)
          .findFirst().ifPresent(publisher -> {
            if (katalog.getVerlag().getGndID() != null && !katalog.getVerlag().getGndID().isEmpty()) {
              publisher.getReves().clear();
              publisher.getContent().clear();
              publisher.getReves()
                  .add(NormdatenReferenz.DNB_BASE_URL + katalog.getVerlag().getGndID());
              publisher.getContent().add(katalog.getVerlag().getName());
            } else {
              Publisher publisherNew = new Publisher();
              publisherNew.getContent().add(TEICommon.getContentAsString(publisher.getContent()));
              publicationStmt.getPublishersAndDistributorsAndAuthorities().remove(publisher);
              publicationStmt.getPublishersAndDistributorsAndAuthorities().add(publisherNew);
            }
          });

      katalog.setTeiXML(TEIObjectFactory.marshal(tei));

    }
  }

  public static void updateId(Katalog katalog) throws Exception {
    if (Objects.nonNull(katalog) && Objects.nonNull(katalog.getTeiXML()) && Objects.nonNull(
        katalog.getId())) {

      logger.info("Update Katalog ID {} ", katalog.getId());

      List<TEI> teiList = TEIObjectFactory
          .unmarshal(
              new ByteArrayInputStream(katalog.getTeiXML().getBytes(StandardCharsets.UTF_8)));

      if (teiList.size() != 1) {
        return;
      }

      TEI t = teiList.get(0);

      PublicationStmt publicationStmt = TEICommon.findFirst(PublicationStmt.class, teiList)
          .orElseThrow(() -> new Exception("teiList contains no publicationStmt"));

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
