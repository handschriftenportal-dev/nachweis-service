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

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.util.Optional;
import org.tei_c.ns._1.Title;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 19.05.2020.
 * @version 1.0
 * <p>
 * Class to create TEI Element Title based on HSP Domainmodel Identifikation
 */
public class TitleFactory {

  public static final String KOD_TITLE_PREFIX = "Kulturobjektdokument von ";

  static Title build(Identifikation identifikation) {

    Title title = new Title();

    Optional.ofNullable(identifikation).map(Identifikation::getBesitzer).map(
        NormdatenReferenz::getName).ifPresent(
        nameKoerperschaft -> Optional.of(identifikation).map(Identifikation::getAufbewahrungsOrt)
            .ifPresent(ort -> {
              if (identifikation.getIdent() != null) {
                title.getContent().clear();
                title.getContent().add(
                    KOD_TITLE_PREFIX + ort.getName()
                        + ", " + nameKoerperschaft + ", " + identifikation.getIdent());
              }
            }));

    return title;

  }

}
