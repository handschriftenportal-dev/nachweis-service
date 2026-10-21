package de.staatsbibliothek.berlin.hsp.nachweis.domain.initium;

import static org.apache.commons.lang3.StringUtils.trimToNull;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.initium.InitiumGraphQLClient;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.initium.InitiumPageResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 28.11.2025
 */

@Slf4j
@ApplicationScoped
public class InitiumService implements InitiumGraphQLBoundary {

  private final InitiumGraphQLClient client;

  @Inject
  public InitiumService(InitiumGraphQLClient client) {
    this.client = client;
  }

  @Override
  public InitiumPageResult findInitien(int page, int pageSize, InitiumFilter filter) {

    InitiumFilter trimmed = trimFilter(filter);

    try {
      return client.findInitia(pageSize, page, trimmed);
    } catch (Exception exception) {
      log.error("GraphQL error in findInitia(page={}, pageSize={}, filter={})", page, pageSize,
          trimmed, exception);
      return new InitiumPageResult(List.of(), 0, page, 0, false);
    }
  }

  private InitiumFilter trimFilter(InitiumFilter filter) {
    if (filter == null) {
      return null;
    }
    InitiumFilter trimmed = InitiumFilter.builder()
        .id(trimToNull(filter.id()))
        .text(trimToNull(filter.text()))
        .language(trimToNull(filter.language()))
        .build();

    return hasAnyValue(trimmed) ? trimmed : null;
  }

  private boolean hasAnyValue(InitiumFilter filter) {
    return filter.id() != null
        || filter.text() != null
        || filter.language() != null;
  }

}
