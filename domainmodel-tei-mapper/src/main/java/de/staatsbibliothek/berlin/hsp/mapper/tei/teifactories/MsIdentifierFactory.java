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

package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.Optional;
import java.util.Set;
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

  static MsIdentifier build(Identifikation gueltigeSignatur,
      Set<Identifikation> alternativeSignaturen) {

    MsIdentifier msIdentifier = new MsIdentifier();

    if (IdentTyp.GUELTIGE_SIGNATUR.equals(gueltigeSignatur.getIdentTyp())) {

      msIdentifier.getIdnos().clear();
      Idno idno = new Idno();
      idno.getContent().add(gueltigeSignatur.getIdent());
      msIdentifier.getIdnos().add(idno);

      Optional.of(gueltigeSignatur).map(Identifikation::getBesitzer)
          .ifPresent(besitzer -> {

            if (besitzer.getName() != null && !besitzer.getName().isEmpty()) {
              Repository repository = new Repository();
              repository.getContent().add(besitzer.getName());

              if (besitzer.getId() != null && !besitzer.getId().isEmpty()) {
                repository.setKey(besitzer.getId());
              }

              if (besitzer.getGndID() != null && !besitzer.getGndID().isEmpty()) {
                repository.getReves().add(besitzer.getGndID());
              }

              msIdentifier.setRepository(repository);
            }
          });

      Optional.of(gueltigeSignatur).map(Identifikation::getAufbewahrungsOrt)
          .ifPresent(aufbewahrungsOrt -> {

            if (aufbewahrungsOrt.getName() != null && !aufbewahrungsOrt.getName().isEmpty()) {
              Settlement settlement = new Settlement();
              settlement.getContent().add(aufbewahrungsOrt.getName());

              if (aufbewahrungsOrt.getId() != null && !aufbewahrungsOrt.getId().isEmpty()) {
                settlement.setKey(aufbewahrungsOrt.getId());
              }

              if (aufbewahrungsOrt.getGndID() != null && !aufbewahrungsOrt.getGndID().isEmpty()) {
                settlement.getReves().add(aufbewahrungsOrt.getGndID());
              }

              msIdentifier.setSettlement(settlement);
            }
          });
    }

    if (alternativeSignaturen != null && !alternativeSignaturen.isEmpty()) {
      alternativeSignaturen.stream().forEach(i -> {
        if (!i.getSammlungIDs().isEmpty()) {
          i.getSammlungIDs().stream()
              .forEach(sammlung -> msIdentifier.getMsNamesAndObjectNamesAndAltIdentifiers()
                  .add(AltIdentifierFactory
                      .build(i.getIdent(), TEIValues.HSP_ALTIDENTIFIER_HSPID, sammlung)));
        } else {
          msIdentifier.getMsNamesAndObjectNamesAndAltIdentifiers()
              .add(AltIdentifierFactory
                  .build(i.getIdent(), TEIValues.ALTERNATIVE_IDNO_TYPE));
        }

      });

    }

    return msIdentifier;
  }

}
