package de.staatsbibliothek.berlin.hsp.mapper.tei;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.LOCAL_DATE_OPT_MONTH_OPT_DAY_FORMAT;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tei_c.ns._1.Bibl;
import org.tei_c.ns._1.Date;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.MsIdentifier;
import org.tei_c.ns._1.OrgName;
import org.tei_c.ns._1.PersName;
import org.tei_c.ns._1.PlaceName;
import org.tei_c.ns._1.PubPlace;
import org.tei_c.ns._1.PublicationStmt;
import org.tei_c.ns._1.Ref;
import org.tei_c.ns._1.RevisionDesc;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.Term;
import org.tei_c.ns._1.TitleStmt;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 12.05.2020.
 * @version 1.0
 */
public class TEIQuery {

  public static final String ORIG_PLACE_NORM = "origPlace_norm";
  public static final String TEXT_LANG_ID = "textLang-ID";
  private static final Logger log = LoggerFactory.getLogger(TEIQuery.class);
  private static final DateTimeFormatter OPTIONAL_DATE_PARSER =
      DateTimeFormatter.ofPattern(LOCAL_DATE_OPT_MONTH_OPT_DAY_FORMAT);

  private TEIQuery() {
  }

  public static List<MsDesc> queryForMsDescOfType(TEI tei, String type) {

    List<MsDesc> result = new ArrayList<>();

    try {
      TEICommon.findAll(MsDesc.class, tei, result);
      return result.stream()
          .filter(msDesc -> msDesc.getType() != null && type.equals(
              msDesc.getType()))
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("Error during query for MsDesc ", e);
    }

    return result;
  }

  public static List<MsDesc> queryForMsDescAsBeschreibung(TEI tei) {

    List<MsDesc> msDescList = new ArrayList<>();
    List<MsDesc> result = new ArrayList<>();

    try {

      TEICommon.findAll(MsDesc.class, tei, msDescList);

      log.info("Found MsDesc size {} ", msDescList.size());

      result = msDescList.stream()
          .filter(msDesc -> msDesc.getId() != null && !msDesc.getId().isEmpty())
          .collect(Collectors.toList());

    } catch (Exception e) {
      log.error("Error during query for MsDesc ", e);
    }

    return result;
  }

  public static List<String> queryForTermOriginGNDID(
      Beschreibung beschreibung) {
    List<Term> termList = new ArrayList<>();
    List<MsDesc> msDescList = new ArrayList<>();
    List<String> gndIDResult = new ArrayList<>();

    try {

      List<TEI> teis = TEIObjectFactory
          .unmarshal(
              new ByteArrayInputStream(
                  beschreibung.getTeiXML().getBytes(StandardCharsets.UTF_8)));
      TEICommon.findAll(MsDesc.class, teis.getFirst(), msDescList);
      TEICommon.findAll(Term.class, msDescList.getFirst().getHeads().getFirst(),
          termList);
      termList.stream().filter(t -> ORIG_PLACE_NORM.equals(t.getType()))
          .map(Term::getReves)
          .forEach(ref -> {
            if (!ref.isEmpty()) {
              String gndid = ref.getFirst()
                  .substring(ref.getFirst().lastIndexOf("/") + 1);
              if (!gndid.isEmpty()) {
                gndIDResult.add(gndid);
              }
            }
          });

    } catch (Exception e) {
      log.error("Error during find GNDID of Entstehungsort", e);
    }
    return gndIDResult;
  }


  public static Optional<PubPlace> queryForFileDescPupPlaceElement(TEI tei)
      throws Exception {

    Optional<PublicationStmt> publicationStmt = TEICommon.findFirst(
        PublicationStmt.class, tei);

    if (publicationStmt.isPresent()) {

      Optional<PubPlace> pubPlace = publicationStmt.get()
          .getPublishersAndDistributorsAndAuthorities().stream()
          .filter(PubPlace.class::isInstance)
          .map(PubPlace.class::cast)
          .findFirst();

      if (pubPlace.isPresent()) {
        return pubPlace;
      } else {
        PubPlace newPubplace = new PubPlace();
        publicationStmt.get().getPublishersAndDistributorsAndAuthorities()
            .add(newPubplace);
        return Optional.of(newPubplace);
      }

    }
    return Optional.empty();
  }

  public static String extractSignature(MsIdentifier msIdentifier) {

    if (msIdentifier != null && msIdentifier.getIdnos().size() == 1) {
      return msIdentifier.getIdnos().getFirst().getContent().stream()
          .map(String::valueOf)
          .collect(Collectors.joining(""))
          .trim();
    }

    return "";
  }

  public static List<Bibl> queryForBiblInMsDesc(MsDesc msDesc) {
    List<Bibl> biblList = new ArrayList<>();
    try {
      TEICommon.findAll(Bibl.class, msDesc, biblList);
    } catch (Exception e) {
      log.error("Error during find Bibl in MsDesc", e);
    }
    return biblList;
  }

  public static Optional<OrgName> queryForOrgNameInBibl(Bibl bibl, String type)
      throws Exception {

    List<OrgName> orgNames = new ArrayList<>();
    TEICommon.findAll(OrgName.class, bibl, orgNames);

    return orgNames.stream()
        .filter(
            orgName -> Objects.nonNull(orgName.getType()) && orgName.getType()
                .equals(type))
        .findFirst();
  }

  public static Optional<TitleStmt> queryForTitleStmt(TEI tei) {
    try {
      return TEICommon.findFirst(TitleStmt.class, tei);
    } catch (Exception e) {
      log.error("Error during find RespStmt in TEI", e);
      return Optional.empty();
    }
  }

  public static Optional<RevisionDesc> queryForRevisionDesc(TEI tei) {
    try {
      return TEICommon.findFirst(RevisionDesc.class, tei);
    } catch (Exception e) {
      log.error("Error during find RespStmt in TEI", e);
      return Optional.empty();
    }
  }

  public static Optional<PlaceName> queryForPlaceNameInBibl(Bibl bibl,
      String type)
      throws Exception {

    List<PlaceName> placeNames = new ArrayList<>();
    TEICommon.findAll(PlaceName.class, bibl, placeNames);

    return placeNames.stream().filter(
            placeName -> Objects.nonNull(placeName.getType()) && placeName.getType()
                .equals(type))
        .findFirst();
  }

  public static Optional<Ref> findManifestRefs(List<Ref> refs) {
    return refs.stream()
        .filter(r -> TEIValues.REF_TYPE_MANIFEST.equals(r.getType()))
        .findFirst();
  }

  public static Optional<Ref> findOtherURLRefs(List<Ref> refs) {
    return refs.stream()
        .filter(r -> TEIValues.REF_TYPE_OTHER.equals(r.getType())).findFirst();
  }

  public static Optional<Ref> findThumbnailURLRefs(List<Ref> refs) {
    return refs.stream()
        .filter(r -> TEIValues.REF_TYPE_THUMBNAIL.equals(r.getType()))
        .findFirst();
  }

  public static boolean isManifestOROtherURLInTEI(List<Ref> refs) {
    return refs.stream()
        .anyMatch(
            r -> TEIValues.REF_TYPE_MANIFEST.equals(r.getType())
                || TEIValues.REF_TYPE_OTHER
                .equals(r.getType()));
  }

  public static Optional<LocalDate> findDateByType(List<Date> dates,
      String type) {
    return dates.stream()
        .filter(d -> type.equals(d.getType()))
        .findFirst().map(date -> parseDate(date.getWhen()));

  }

  public static LocalDate parseDate(String value) {
    if (Objects.isNull(value) || value.trim().isEmpty()) {
      return null;
    }

    TemporalAccessor temporalAccessor;
    try {
      temporalAccessor = OPTIONAL_DATE_PARSER.parseBest(value, LocalDate::from,
          YearMonth::from,
          Year::from);
    } catch (Exception e) {
      return null;
    }

    return switch (temporalAccessor) {
      case LocalDate localDate -> localDate;
      case YearMonth yearMonth -> yearMonth.atDay(1);
      case Year year -> year.atMonth(Month.JANUARY).atDay(1);
      default -> null;
    };
  }

  public static String findGNDIDinListOfStrings(List<String> ids) {
    AtomicReference<String> gndid = new AtomicReference<>("");

    if (ids == null || ids.isEmpty()) {
      return gndid.get();
    }

    ids.stream().filter(r -> r.contains("gnd")).findFirst()
        .ifPresentOrElse(gndid::set, () ->
            gndid.set(ids.getFirst()));

    if (gndid.get().contains("http")) {
      gndid.set(gndid.get().substring(gndid.get().lastIndexOf("/") + 1));
    }

    return gndid.get();
  }

  public static Map<String, PersName> findAllPersNamesWithPath(TEI tei) {
    Map<String, PersName> persNames = new HashMap<>();

    try {
      // TEI zu XML String marshallen
      String xmlString = TEIObjectFactory.marshal(tei);

      // DOM Document erstellen
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(
          new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8))
      );

      // Alle persName Elemente finden (ohne XPath)
      List<Element> persNameElements = new ArrayList<>();
      findElementsByName(document.getDocumentElement(), "persName",
          persNameElements);

      // JAXB PersName Objekte aus TEI extrahieren
      List<PersName> persNameList = new ArrayList<>();
      TEICommon.findAll(PersName.class, tei, persNameList);

      // XPath für jedes PersName Element generieren
      for (int i = 0; i < persNameElements.size() && i < persNameList.size();
          i++) {
        Element element = persNameElements.get(i);
        String xpathExpression = generateAbsoluteXPath(element);
        persNames.put(xpathExpression, persNameList.get(i));
      }

    } catch (Exception e) {
      log.error("Error finding PersNames with DOM", e);
    }

    return persNames;
  }

  private static void findElementsByName(Element parent, String tagName,
      List<Element> result) {
    if (tagName.equals(parent.getLocalName()) || tagName.equals(
        parent.getTagName())) {
      result.add(parent);
    }

    NodeList children = parent.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child instanceof Element) {
        findElementsByName((Element) child, tagName, result);
      }
    }
  }

  private static String generateAbsoluteXPath(Element element) {
    StringBuilder xpath = new StringBuilder();
    Node current = element;

    while (current instanceof Element currentElement) {
      String nodeName = currentElement.getLocalName();
      if (nodeName == null) {
        nodeName = currentElement.getTagName();
      }

      // Position des Elements bestimmen
      int position = getElementPosition(currentElement);

      if (!xpath.isEmpty()) {
        xpath.insert(0, "/");
      }
      xpath.insert(0, nodeName + "[" + position + "]");

      current = current.getParentNode();
    }

    return "/" + xpath + "/" + (element != null && element.getTextContent() != null ?
        element.getTextContent().replace("'", "&apos;") : "");
  }

  private static int getElementPosition(Element element) {
    int position = 1;
    Node sibling = element.getPreviousSibling();
    String elementName = element.getLocalName();
    if (elementName == null) {
      elementName = element.getTagName();
    }

    while (sibling != null) {
      if (sibling instanceof Element siblingElement) {
        String siblingName = siblingElement.getLocalName();
        if (siblingName == null) {
          siblingName = siblingElement.getTagName();
        }

        if (elementName.equals(siblingName)) {
          position++;
        }
      }
      sibling = sibling.getPreviousSibling();
    }

    return position;
  }
}
