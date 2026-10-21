package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.persistence.EntityManager;
import java.util.List;

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
