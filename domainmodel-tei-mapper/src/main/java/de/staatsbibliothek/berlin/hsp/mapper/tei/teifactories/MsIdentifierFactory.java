package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.tei_c.ns._1.Idno;
import org.tei_c.ns._1.MsIdentifier;
import org.tei_c.ns._1.Repository;
import org.tei_c.ns._1.Settlement;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 18.05.2020.
 * @version 1.0
 * <p>
 * Class to create TEI Element MsIdentifier based on HSP Domainmodel
 */

public class MsIdentifierFactory {

  public static MsIdentifier build(
      Identifikation gueltigeSignatur,
      Set<Identifikation> alternativeSignaturen) {

    MsIdentifier msIdentifier = new MsIdentifier();

    if (IdentTyp.GUELTIGE_SIGNATUR.equals(gueltigeSignatur.getIdentTyp())) {

      msIdentifier.getIdnos().clear();
      Idno idno = new Idno();
      idno.getContent().add(gueltigeSignatur.getIdent());
      idno.setSortKey(StringUtils.trimToNull(gueltigeSignatur.getSortKey()));

      msIdentifier.getIdnos().add(idno);

      Optional.of(gueltigeSignatur).map(Identifikation::getBesitzer)
          .ifPresent(besitzer -> {

            if (besitzer.getName() != null && !besitzer.getName().isEmpty()) {
              final Repository repository = createRepository(besitzer);

              msIdentifier.setRepository(repository);
            }
          });

      Optional.of(gueltigeSignatur).map(Identifikation::getAufbewahrungsOrt)
          .ifPresent(aufbewahrungsOrt -> {

            if (aufbewahrungsOrt.getName() != null && !aufbewahrungsOrt.getName().isEmpty()) {
              final Settlement settlement = createSettlement(aufbewahrungsOrt);

              msIdentifier.setSettlement(settlement);
            }
          });
    }

    if (alternativeSignaturen != null && !alternativeSignaturen.isEmpty()) {
      alternativeSignaturen.forEach(i -> msIdentifier.getMsNamesAndObjectNamesAndAltIdentifiers()
          .add(AltIdentifierFactory
              .build(i)));
    }

    return msIdentifier;
  }

  public static Repository createRepository(NormdatenReferenz besitzer) {

    if (besitzer != null) {

      Repository repository = new Repository();
      repository.getContent().add(besitzer.getName());

      if (besitzer.getId() != null && !besitzer.getId().isEmpty()) {
        repository.setKey(besitzer.getId());
      }

      if (besitzer.getGndID() != null && !besitzer.getGndID().isEmpty()) {
        repository.getReves().add(besitzer.getGndID());
      }
      return repository;
    }
    return null;
  }

  public static Settlement createSettlement(NormdatenReferenz aufbewahrungsOrt) {

    if (aufbewahrungsOrt != null) {

      Settlement settlement = new Settlement();
      settlement.getContent().add(aufbewahrungsOrt.getName());

      if (aufbewahrungsOrt.getId() != null && !aufbewahrungsOrt.getId().isEmpty()) {
        settlement.setKey(aufbewahrungsOrt.getId());
      }

      if (aufbewahrungsOrt.getGndID() != null && !aufbewahrungsOrt.getGndID().isEmpty()) {
        settlement.getReves().add(aufbewahrungsOrt.getGndID());
      }
      return settlement;
    }
    return null;
  }
}
