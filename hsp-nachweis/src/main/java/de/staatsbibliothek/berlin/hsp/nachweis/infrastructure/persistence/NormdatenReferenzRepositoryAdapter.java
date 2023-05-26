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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import java.util.Objects;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.NoResultException;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 03.12.20
 */
@ApplicationScoped
public class NormdatenReferenzRepositoryAdapter implements NormdatenReferenzRepository {

  @Override
  public NormdatenReferenz findByName(String name) {
    return find("name", name).range(0, 1).firstResult();
  }

  @Override
  public NormdatenReferenz findByGndID(String gndID) {
    return find("gndID", gndID).range(0, 1).firstResult();
  }

  @Override
  public NormdatenReferenz findByIdOrIdentifikator(String id) {
    return find("from NormdatenReferenz nr"
            + " join nr.identifikator i"
            + " where nr.id = :id"
            + " or i.text = :id",
        Parameters.with("id", id).map())
        .firstResult();
  }

  @Override
  public NormdatenReferenz findByIdOrName(String idOrName, String type) throws Exception {
    if (Objects.isNull(idOrName) || idOrName.isEmpty()) {
      throw new Exception("Missing or empty parameter idOrName!");
    }
    if (Objects.isNull(type) || type.isEmpty()) {
      throw new Exception("Missing or empty parameter type!");
    }

    PanacheQuery<NormdatenReferenz> query = find("select distinct nr from NormdatenReferenz nr"
            + " left join nr.identifikator i "
            + " left join nr.varianterName vn "
            + " where nr.typeName = :type"
            + " and (nr.id = :idOrName"
            + " or nr.gndID = :idOrName"
            + " or nr.name = :idOrName"
            + " or i.text = :idOrName"
            + " or vn.name = :idOrName)",
        Parameters
            .with("idOrName", idOrName)
            .and("type", type)
            .map());
    try {
      return query.singleResult();
    } catch (NoResultException nre) {
      return null;
    }
  }

}
