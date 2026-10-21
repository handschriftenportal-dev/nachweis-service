package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.PERSON_TYPE_NAME;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ROLE_AUTHOR;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIQuery;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tei_c.ns._1.FileDesc;
import org.tei_c.ns._1.PersName;
import org.tei_c.ns._1.RespStmt;
import org.tei_c.ns._1.TitleStmt;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 11.11.2021.
 * @version 1.0
 */
public class AutorenFactory {

  private static final Logger logger = LoggerFactory.getLogger(
      AutorenFactory.class);

  public static Set<NormdatenReferenz> buildAutoren(
      Optional<FileDesc> fileDesc) {

    return fileDesc.flatMap(desc -> findElement(TitleStmt.class, desc))
        .flatMap(titleStmt -> findElement(RespStmt.class, titleStmt))
        .map(AutorenFactory::mapAutoren).orElse(Collections.emptySet());
  }

  private static Set<NormdatenReferenz> mapAutoren(RespStmt respStmt) {
    try {
      List<PersName> persNames = new ArrayList<>();
      TEICommon.findAll(PersName.class, respStmt, persNames);
      return persNames.stream()
          .filter(persName -> Objects.nonNull(persName.getRoles())
              && persName.getRoles()
              .contains(ROLE_AUTHOR))
          .map(AutorenFactory::createNormdatenReferenzFromPersname)
          .collect(Collectors.toSet());

    } catch (Exception error) {
      logger.error("Can't find Element {} in TEI Container {} for Autoren",
          PersName.class,
          respStmt.getClass(), error);
      return Collections.emptySet();
    }
  }

  private static NormdatenReferenz createNormdatenReferenzFromPersname(
      PersName persName) {
    logger.debug("Map PersName to Autoren {}",
        TEICommon.getContentAsString(persName.getContent()));
    String id = persName.getKey();
    String gndid = persName.isSetReves() ? TEIQuery.findGNDIDinListOfStrings(
        persName.getReves()) : "";
    String name = TEICommon.getContentAsString(persName.getContent());
    return new NormdatenReferenz(id, name, gndid, PERSON_TYPE_NAME);
  }

  private static <T> Optional<T> findElement(Class<T> clazz, Object container) {

    try {
      return TEICommon.findFirst(clazz, container);
    } catch (Exception e) {
      logger.error("Can't find Element {} in TEI Container {} ", clazz,
          container.getClass(), e);
      return Optional.empty();
    }
  }
}
