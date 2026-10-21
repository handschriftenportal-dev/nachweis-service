package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.initium;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.common.PaginatedSearchResult;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.initium.Initium;
import java.util.List;


public class InitiumPageResult extends PaginatedSearchResult<Initium> {


  private List<Initium> items;

  public InitiumPageResult() {
  }

  public InitiumPageResult(List<Initium> items, int totalCount, int currentPage, int totalPages,
      boolean hasNextPage) {
    super(totalCount, currentPage, totalPages, hasNextPage);
    this.items = items;
  }

  @Override
  public List<Initium> getItems() {
    return this.items;
  }

}
