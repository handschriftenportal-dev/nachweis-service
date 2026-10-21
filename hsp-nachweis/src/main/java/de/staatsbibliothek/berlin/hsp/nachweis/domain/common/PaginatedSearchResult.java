package de.staatsbibliothek.berlin.hsp.nachweis.domain.common;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 24.03.2026
 */
@Getter
@Setter
public abstract class PaginatedSearchResult<T> {

  protected int totalCount;
  protected int currentPage;
  protected int totalPages;
  @Accessors(fluent = true)
  protected boolean hasNextPage;

  protected PaginatedSearchResult() {
  }

  protected PaginatedSearchResult(int totalCount, int currentPage, int totalPages,
      boolean hasNextPage) {
    this.totalCount = totalCount;
    this.currentPage = currentPage;
    this.totalPages = totalPages;
    this.hasNextPage = hasNextPage;
  }

  protected abstract List<T> getItems();
}
