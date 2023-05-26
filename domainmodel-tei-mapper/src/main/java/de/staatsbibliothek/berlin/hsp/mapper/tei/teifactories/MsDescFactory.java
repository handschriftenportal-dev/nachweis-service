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

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.GlobaleBeschreibungsKomponente;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsKomponentenTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.Title;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 07.04.2022.
 * @version 1.0
 */
public class MsDescFactory {

  static void buildWithInitialBeschreibung(Optional<MsDesc> msDesc, Beschreibung beschreibung) {

    msDesc.ifPresent(m -> {
      m.setId(beschreibung.getId());
      m.setType(DokumentObjektTyp.HSP_DESCRIPTION.toString());

      if (beschreibung.getBeschreibungsTyp() != null) {
        m.setSubtype(beschreibung.getBeschreibungsTyp().toString());
      }

      if (beschreibung.getVerwaltungsTyp() != null) {
        m.setStatus(beschreibung.getVerwaltungsTyp().name().toLowerCase());
      }

      if (beschreibung.getBeschreibungsSprache() != null) {
        m.setLang(beschreibung.getBeschreibungsSprache()
            .getIdentifikatorByTypeName("ISO_639-1")
            .stream().findFirst()
            .map(i -> i.getText())
            .orElse(null));
      }

      for (GlobaleBeschreibungsKomponente globaleBeschreibungsKomponente : beschreibung
          .getBeschreibungsStruktur()) {
        if (globaleBeschreibungsKomponente.getTyp() == BeschreibungsKomponentenTyp.KOPF) {
          boolean hasBeschreibungKopf = globaleBeschreibungsKomponente.getIdentifikationen()
              .stream()
              .anyMatch(identifikation -> IdentTyp.GUELTIGE_SIGNATUR
                  .equals(identifikation.getIdentTyp()));
          if (hasBeschreibungKopf) {

            BeschreibungsKomponenteKopf kopf = (BeschreibungsKomponenteKopf) globaleBeschreibungsKomponente;

            Optional<Identifikation> gueltigeSignatur = kopf
                .getIdentifikationen().stream()
                .filter(identifikation -> IdentTyp.GUELTIGE_SIGNATUR
                    .equals(identifikation.getIdentTyp()))
                .findFirst();
            Set<Identifikation> alternativeIdentifier = kopf
                .getIdentifikationen().stream()
                .filter(i -> !IdentTyp.GUELTIGE_SIGNATUR.equals(i.getIdentTyp()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

            m.setMsIdentifier(
                MsIdentifierFactory
                    .build(gueltigeSignatur.orElse(null), alternativeIdentifier));

            m.getHeads().stream().findFirst().ifPresent(h -> {
              h.getContent().stream().filter(e -> e instanceof Title).findFirst()
                  .ifPresent(title -> {
                    ((Title) title).getContent().clear();
                    ((Title) title).getContent().add(kopf.getTitel());
                  });
            });

          }
        }
      }
    });
  }

}
