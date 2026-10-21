package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static org.apache.commons.lang3.StringUtils.trimToNull;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.HspLazyDataModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.person.Person;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.person.PersonFilter;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.person.PersonGraphQLBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.person.PersonPageResult;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.io.Serial;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.model.FilterMeta;

/**
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 25.03.2026
 */

@Slf4j
@Dependent
public class PersonLazyDataModel extends HspLazyDataModel<Person> {

  @Serial
  private static final long serialVersionUID = 8827867291039095204L;

  private final PersonGraphQLBoundary personGraphQLBoundary;

  private int lastTotalCount = 0;

  @Inject
  public PersonLazyDataModel(PersonGraphQLBoundary personGraphQLBoundary) {
    this.personGraphQLBoundary = personGraphQLBoundary;
  }

  @Override
  public List<Person> loadMapped(
      int firstEntry,
      int pageSize,
      Map<String, Boolean> sortByAsc,
      Map<String, String> filter
  ) {

    int size = resolveSize(pageSize);
    int page = resolvePage(firstEntry, size);
    PersonFilter personFilter = buildFilter(filter);

    log.debug("LOAD Persons firstEntry={}, pageSize={}, filter={}", firstEntry, pageSize, filter);

    try {
      log.info("PersonGraphQLBoundary.findPersons(page={}, size={}, personFilter={})",
          page, size, personFilter);
      PersonPageResult result = personGraphQLBoundary.findPersons(page, size, personFilter);
      lastTotalCount = result.getTotalCount();
      return result.getItems();

    } catch (Exception exception) {
      log.error("Error loading Persons (page={}, size={}, filter={})",
          page, size, personFilter, exception);

      lastTotalCount = 0;
      return Collections.emptyList();
    }
  }

  private int resolvePage(int firstEntry, int size) {
    return (firstEntry / size) + 1;
  }

  private int resolveSize(int pageSize) {
    return pageSize > 0 ? pageSize : 10;
  }

  private PersonFilter buildFilter(Map<String, String> filter) {
    return PersonFilter.builder()
        .id(trimToNull(filter.get("id")))
        .preferredName(trimToNull(filter.get("preferredName")))
        .variantName(trimToNull(filter.get("variantName")))
        .gndIdentifier(trimToNull(filter.get("gndIdentifier")))
        .build();
  }

  @Override
  public int count(Map<String, FilterMeta> filterBy) {
    return lastTotalCount;
  }
}
