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

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import java.util.List;
import javax.persistence.EntityManager;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 29.11.2019.
 * @version 1.0
 */
public interface CRUDRepository<Entity, Id> extends PanacheRepositoryBase<Entity, Id> {

  default void saveAll(List<Entity> entities) {
    EntityManager entityManager = getEntityManager();
    for (int i = 0, k = entities.size(); i < k; i++) {
      Entity entity = entities.get(i);
      entities.set(i, entityManager.merge(entity));
    }
  }

  default Entity save(Entity entity) {
    EntityManager entityManager = getEntityManager();
    entityManager.merge(entity);
    return entity;
  }

  default Entity saveAndFlush(Entity entity) {
    Entity result = save(entity);
    flush();
    return result;
  }

  default boolean deleteByIdAndFlush(Id id) {
    boolean result = deleteById(id);
    flush();
    return result;
  }

  default List<Entity> listAll(boolean cacheEnabled) {
    return makeQueryCacheable(
        findAll(), cacheEnabled
    ).list();
  }

  default PanacheQuery<Entity> makeQueryCacheable(PanacheQuery<Entity> panacheQuery) {
    return makeQueryCacheable(panacheQuery, true);
  }

  default PanacheQuery<Entity> makeQueryCacheable(PanacheQuery<Entity> panacheQuery, boolean value) {
    return panacheQuery.withHint("org.hibernate.cacheable", String.valueOf(value));
  }

  default Entity firstEntityFromList(PanacheQuery<Entity> panacheQuery) {
    return panacheQuery.range(0, 1).firstResult();
  }
}
