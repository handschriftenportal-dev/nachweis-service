package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.person;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.common.PaginatedSearchResult;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.person.Person;
import java.util.List;

public class PersonPageResult extends PaginatedSearchResult<Person> {

  List<Person> items;

  public PersonPageResult() {
  }

  public PersonPageResult(List<Person> items, int totalCount, int currentPage, int totalPages,
      boolean hasNextPage) {
    super(totalCount, currentPage, totalPages, hasNextPage);
    this.items = items;
  }

  @Override
  public List<Person> getItems() {
    return this.items;
  }
}
