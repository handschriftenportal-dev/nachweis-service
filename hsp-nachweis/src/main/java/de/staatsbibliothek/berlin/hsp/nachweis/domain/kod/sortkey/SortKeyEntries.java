package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey;

import java.util.List;

public record SortKeyEntries(
    List<SortKeyEntry> entries,
    List<String> missingOrEmptySortkeyLines) {

  public SortKeyEntries {
    entries = List.copyOf(entries);
    missingOrEmptySortkeyLines = List.copyOf(missingOrEmptySortkeyLines);
  }
}
