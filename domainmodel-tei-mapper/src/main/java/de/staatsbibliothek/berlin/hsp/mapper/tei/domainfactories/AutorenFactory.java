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

package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ROLE_AUTHOR;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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

  private static final Logger logger = LoggerFactory.getLogger(AutorenFactory.class);

  private static final String AUTOREN_TYPE = "Person";

  public static Set<NormdatenReferenz> buildAutoren(Optional<FileDesc> fileDesc) {

    Set<NormdatenReferenz> autoren = fileDesc.flatMap(desc -> findElement(TitleStmt.class, desc))
        .flatMap(titleStmt -> findElement(RespStmt.class, titleStmt))
        .map(respStmt -> mapAutoren(respStmt)).orElse(Collections.emptySet());

    return autoren;
  }

  private static Set<NormdatenReferenz> mapAutoren(RespStmt respStmt) {
    try {
      List<PersName> persNames = new ArrayList<>();
      TEICommon.findAll(PersName.class, respStmt, persNames);
      return persNames.stream()
          .filter(persName -> Objects.nonNull(persName.getRoles()) && persName.getRoles().contains(ROLE_AUTHOR))
          .map(persName -> {
            String id = Optional.ofNullable(persName.getKey())
                .orElseGet(() -> UUID.randomUUID().toString());
            String name = TEICommon.getContentAsString(persName.getContent());
            return new NormdatenReferenz(id, name, "", AUTOREN_TYPE);
          }).collect(Collectors.toSet());

    } catch (Exception e) {
      logger.error("Can't find Element {} in TEI Container {} for Autoren", PersName.class,
          respStmt.getClass(), e);
      return Collections.emptySet();
    }
  }

  private static <T> Optional<T> findElement(Class<T> clazz, Object container) {

    try {
      return TEICommon.findFirst(clazz, container);
    } catch (Exception e) {
      logger.error("Can't find Element {} in TEI Container {} ", clazz, container.getClass(), e);
      return Optional.empty();
    }
  }

}
