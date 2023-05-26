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

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.DATE_TIME_PATTERN;

import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.tei_c.ns._1.Creation;
import org.tei_c.ns._1.Date;
import org.tei_c.ns._1.ProfileDesc;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 07.04.2022.
 * @version 1.0
 */
public class ProfilDescFactory {

  static void buildWithCreationDate(Optional<ProfileDesc> profileDesc,
      LocalDateTime creationDateTime) {
    if (profileDesc.isPresent() && creationDateTime != null) {
      profileDesc.get().getTextDescsAndParticDescsAndSettingDescs().stream()
          .filter(e -> e instanceof Creation)
          .map(c -> (Creation) c).findFirst().ifPresentOrElse(creation -> {
            creation.getContent().clear();
            Date creationDate = createCreationDate(creationDateTime);
            creation.getContent().add(creationDate);
          }, () -> {
            Creation creation = new Creation();
            Date creationDate = createCreationDate(creationDateTime);
            creation.getContent().add(creationDate);
            profileDesc.get().getTextDescsAndParticDescsAndSettingDescs().add(creation);
          });
    }
  }

  private static Date createCreationDate(LocalDateTime creationDateTime) {
    Date creationDate = new Date();
    creationDate.setWhen(creationDateTime
        .format(DateTimeFormatter.ofPattern(TEIValues.LOCAL_DATE_FORMAT)));
    creationDate.getContent().add(creationDateTime
        .format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
    return creationDate;
  }
}
