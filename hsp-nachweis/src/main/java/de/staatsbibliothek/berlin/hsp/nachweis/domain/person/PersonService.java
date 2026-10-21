package de.staatsbibliothek.berlin.hsp.nachweis.domain.person;

import static org.apache.commons.lang3.StringUtils.trimToNull;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.person.PersonGraphQLClient;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.person.PersonPageResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 24.03.2026
 */

@Slf4j
@ApplicationScoped
public class PersonService implements PersonGraphQLBoundary {

  private final PersonGraphQLClient client;

  @Inject
  public PersonService(PersonGraphQLClient client) {
    this.client = client;
  }

  @Override
  public PersonPageResult findPersons(int page, int pageSize, PersonFilter filter) {

    PersonFilter trimmed = trimFilter(filter);

    try {
      return client.findPersons(pageSize, page, trimmed);
    } catch (Exception exception) {
      log.error("GraphQL error in findPersons(page={}, pageSize={}, filter={})",
          page, pageSize, trimmed, exception);
      return new PersonPageResult(List.of(), 0, 0, 0, false);
    }
  }

  private PersonFilter trimFilter(PersonFilter filter) {
    if (filter == null) {
      return null;
    }
    PersonFilter trimmed = PersonFilter.builder()
        .id(trimToNull(filter.id()))
        .name(trimToNull(filter.name()))
        .preferredName(trimToNull(filter.preferredName()))
        .variantName(trimToNull(filter.variantName()))
        .gndIdentifier(trimToNull(filter.gndIdentifier()))
        .build();

    return hasAnyValue(trimmed) ? trimmed : null;
  }

  private boolean hasAnyValue(PersonFilter filter) {
    return filter.id() != null
        || filter.name() != null
        || filter.preferredName() != null
        || filter.variantName() != null
        || filter.gndIdentifier() != null;
  }

}
