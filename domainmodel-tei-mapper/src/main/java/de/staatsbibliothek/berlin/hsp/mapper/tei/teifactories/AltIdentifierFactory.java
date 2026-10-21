package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang3.StringUtils;
import org.tei_c.ns._1.AltIdentifier;
import org.tei_c.ns._1.Collection;
import org.tei_c.ns._1.Idno;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 12.05.2020.
 * @version 1.0
 */
public class AltIdentifierFactory {

  public static AltIdentifier build(Identifikation altIdentifikation) {
    return createAltidentifier(altIdentifikation,
        IdentTyp.indentTypToTEIEncoding(altIdentifikation.getIdentTyp()));
  }

  private static AltIdentifier createAltidentifier(Identifikation altIdentifikation, String type) {
    AltIdentifier altIdentifier = new AltIdentifier();
    Idno idno = new Idno();
    if (altIdentifikation.getGueltigBis() != null) {
      altIdentifier.setNotAfter(
          altIdentifikation.getGueltigBis().format(DateTimeFormatter.ISO_DATE));
    }

    if (altIdentifikation.getSammlungIDs() != null && !altIdentifikation.getSammlungIDs()
        .isEmpty()) {
      Collection collectionTEI = new Collection();

      collectionTEI.getContent()
          .add(String.join(",", altIdentifikation.getSammlungIDs()));
      altIdentifier.setCollection(collectionTEI);
    }

    altIdentifier.setType(type);

    idno.getContent().add(altIdentifikation.getIdent());
    idno.setSortKey(StringUtils.trimToNull(altIdentifikation.getSortKey()));
    altIdentifier.setIdno(idno);
    altIdentifier.setSettlement(
        MsIdentifierFactory.createSettlement(altIdentifikation.getAufbewahrungsOrt()));
    altIdentifier.setRepository(
        MsIdentifierFactory.createRepository(altIdentifikation.getBesitzer()));
    return altIdentifier;
  }
}
