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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Creation;
import org.tei_c.ns._1.Date;
import org.tei_c.ns._1.ProfileDesc;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 07.04.2022.
 * @version 1.0
 */
public class ProfilDescFactoryTest {

  @Test
  void buildWithCreationDate() {
    LocalDateTime creatioNDate = LocalDateTime.of(2022, 4, 7, 9, 27, 0);
    Optional<ProfileDesc> profileDesc = Optional.of(new ProfileDesc());
    ProfilDescFactory.buildWithCreationDate(profileDesc, creatioNDate);

    assertEquals(1,
        profileDesc.get().getTextDescsAndParticDescsAndSettingDescs().size());

    profileDesc.get().getTextDescsAndParticDescsAndSettingDescs().stream().findFirst()
        .ifPresent(e -> ((Creation) e).getContent().stream().findFirst()
            .ifPresent(d -> Assertions.assertEquals("2022-04-07", ((Date) d).getWhen())));
  }
}
