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

import static de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon.findAll;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPMapperException;
import de.staatsbibliothek.berlin.hsp.mapper.api.factory.ElementFactory;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tei_c.ns._1.EditionStmt;
import org.tei_c.ns._1.FileDesc;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.Name;
import org.tei_c.ns._1.Resp;
import org.tei_c.ns._1.RespStmt;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.TitleStmt;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 22.04.20
 */
public class BeteiligteSetFactory implements ElementFactory<Set<NormdatenReferenz>, MsDesc, TEI> {

  private static final Logger log = LoggerFactory.getLogger(BeteiligteSetFactory.class);

  private static final Map<String, NormdatenReferenz> NORMDATEN_REFERENZ_MAP = new ConcurrentHashMap<>();

  private static NormdatenReferenz createBeteiligte(String respTypeStr, Name name) {
    NormdatenReferenz beteiligte = null;

    if (name != null) {

      String nameStr = name.getContent().stream().filter(Objects::nonNull)
          .map(String::valueOf).collect(Collectors.joining(" ", "", ""));

      if (!nameStr.isEmpty()) {
        if (NORMDATEN_REFERENZ_MAP.containsKey(nameStr)) {
          beteiligte = NORMDATEN_REFERENZ_MAP.get(nameStr);
        } else {
          beteiligte = new NormdatenReferenz(null, nameStr, "");
          NORMDATEN_REFERENZ_MAP.put(nameStr, beteiligte);
        }
      }
    }

    return beteiligte;
  }

  @Override
  public Set<NormdatenReferenz> build(MsDesc local, TEI global) throws HSPMapperException {
    Set<NormdatenReferenz> result = new LinkedHashSet<>();
    //(/TEI/teiHeader/fileDesc/titleStmt | /TEI/teiHeader/fileDesc/editionStmt)/respStmt/name
    FileDesc fileDesc = global.getTeiHeader().getFileDesc();
    TitleStmt titleStmt = fileDesc.getTitleStmt();
    EditionStmt editionStmt = fileDesc.getEditionStmt();
    List<RespStmt> respStmts = new ArrayList<>();
    try {
      if (Objects.nonNull(titleStmt)) {
        findAll(RespStmt.class, titleStmt.getAuthorsAndEditorsAndRespStmts(), respStmts);
      }
      if (Objects.nonNull(editionStmt)) {
        findAll(RespStmt.class, editionStmt.getAuthorsAndEditorsAndRespStmts(), respStmts);
      }

      log.info("found {} respStmt", respStmts.size());
      NormdatenReferenz beteiligte;
      List<Resp> respTypes = new ArrayList<>();
      List<Name> names = new ArrayList<>();
      String respTypeStr;
      for (RespStmt respStmt : respStmts) {
        respTypes.clear();
        names.clear();
        List<Object> respsAndNamesAndOrgNames = respStmt.getRespsAndNamesAndOrgNames();
        findAll(Resp.class, respsAndNamesAndOrgNames, respTypes);
        findAll(Name.class, respsAndNamesAndOrgNames, names);
        for (Resp resp : respTypes) {
          respTypeStr = resp.getContent().stream()
              .map(String::valueOf).collect(Collectors.joining(" ", "", ""));
          for (Name name : names) {
            beteiligte = createBeteiligte(respTypeStr, name);
            if (Objects.nonNull(beteiligte)) {
              result.add(beteiligte);
            }
          }
        }
      }
    } catch (Exception e) {
      log.warn("Unable to extract Beteiligte due to {}", e.getMessage(), e);
    }
    return result;
  }
}
