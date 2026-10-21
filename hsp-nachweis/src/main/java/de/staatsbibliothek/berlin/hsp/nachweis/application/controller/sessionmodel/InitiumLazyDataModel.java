package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static org.apache.commons.lang3.StringUtils.trimToNull;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.HspLazyDataModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.initium.Initium;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.initium.InitiumFilter;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.initium.InitiumGraphQLBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.initium.InitiumPageResult;
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
 * @since 05.12.2025
 */

@Slf4j
@Dependent
public class InitiumLazyDataModel extends HspLazyDataModel<Initium> {

  @Serial
  private static final long serialVersionUID = 3623567491139578209L;

  private final InitiumGraphQLBoundary initiumGraphQLBoundary;

  private int lastTotalCount = 0;

  @Inject
  public InitiumLazyDataModel(InitiumGraphQLBoundary initiumGraphQLBoundary) {
    this.initiumGraphQLBoundary = initiumGraphQLBoundary;
  }

  @Override
  public List<Initium> loadMapped(
      int firstEntry,
      int pageSize,
      Map<String, Boolean> sortByAsc,
      Map<String, String> filter
  ) {

    int size = resolveSize(pageSize);
    int page = resolvePage(firstEntry, size);
    InitiumFilter initiumFilter = buildFilter(filter);

    log.debug("LOAD Initium firstEntry={}, pageSize={}, filter={}", firstEntry, pageSize,
        initiumFilter);

    try {
      InitiumPageResult result = initiumGraphQLBoundary.findInitien(page, size, initiumFilter);
      lastTotalCount = result.getTotalCount();
      return result.getItems();

    } catch (Exception exception) {
      log.error("Error loading Initien from GraphQL (page={}, size={}, filter={})",
          page, size, initiumFilter, exception);

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

  private InitiumFilter buildFilter(Map<String, String> filter) {
    return InitiumFilter.builder()
        .id(trimToNull(filter.get("id")))
        .text(trimToNull(filter.get("text")))
        .language(trimToNull(filter.get("language")))
        .build();
  }

  @Override
  public int count(Map<String, FilterMeta> filterBy) {
    return lastTotalCount;
  }
}
