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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Availability;
import org.tei_c.ns._1.FileDesc;
import org.tei_c.ns._1.Licence;
import org.tei_c.ns._1.P;
import org.tei_c.ns._1.PublicationStmt;
import org.tei_c.ns._1.RespStmt;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 07.04.2022.
 * @version 1.0
 */
public class FileDescFactoryTest {

  @Test
  void buildWithLicenseAndAutor() {

    Lizenz lizenz = createTestLizenz();
    Set<NormdatenReferenz> autoren = new HashSet<>();
    autoren.add(new NormdatenReferenz("1", "Konrad Eichstädt", "123123x"));
    Optional<FileDesc> fileDesc = Optional.of(new FileDesc());

    FileDescFactory.buildWithLicenseAndAutor(fileDesc, lizenz, autoren);

    fileDesc.ifPresent(fd -> {
      assertNotNull(fd.getTitleStmt());
      fd.getTitleStmt().getAuthorsAndEditorsAndRespStmts().stream()
          .filter(e -> e instanceof RespStmt).findFirst()
          .ifPresent(rsp -> assertEquals(1, ((RespStmt) rsp).getRespsAndNamesAndOrgNames().size()));
    });

  }

  @Test
  void testUpdateLicence() {
    Lizenz lizenz = createTestLizenz();

    FileDesc fileDesc = new FileDesc();

    FileDescFactory.updateLicence(fileDesc, lizenz);
    assertNull(fileDesc.getPublicationStmt());

    fileDesc.setPublicationStmt(new PublicationStmt());
    FileDescFactory.updateLicence(fileDesc, lizenz);
    assertTrue(
        fileDesc.getPublicationStmt().getPublishersAndDistributorsAndAuthorities().isEmpty());

    Availability availability = new Availability();
    fileDesc.getPublicationStmt().getPublishersAndDistributorsAndAuthorities().add(availability);
    FileDescFactory.updateLicence(fileDesc, lizenz);
    assertTrue(availability.getLicencesAndPSAndAbs().isEmpty());

    Licence licence = new Licence();
    licence.getContent().add(new P());
    availability.getLicencesAndPSAndAbs().add(licence);
    FileDescFactory.updateLicence(fileDesc, lizenz);
    assertTrue(licence.getTargets().stream().allMatch(t -> lizenz.getUris().contains(t)));
    assertTrue(lizenz.getUris().stream().allMatch(u -> licence.getTargets().contains(u)));
    assertTrue(
        TEICommon.getContentAsString(licence.getContent()).contains(lizenz.getBeschreibungsText()));
  }

  private Lizenz createTestLizenz() {
    Lizenz lizenz = new Lizenz.LizenzBuilder().withBeschreibungsText(
            "Die Katalogdaten sind Public Domain.")
        .addUri("https://creativecommons.org/publicdomain/mark/1.0/")
        .addUri("https://creativecommons.org/publicdomain/mark/1.0/deed.de").build();
    return lizenz;
  }

}
