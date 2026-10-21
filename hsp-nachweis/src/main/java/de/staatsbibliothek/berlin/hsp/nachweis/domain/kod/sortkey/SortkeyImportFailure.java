package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey;

/**
 * Result of a failed SortKey import. updatedCountBeforeFailure > 0 means the import was aborted
 * mid-run and already committed chunks remain saved.
 */
public record SortkeyImportFailure(int updatedCountBeforeFailure) {

  public boolean isPartial() {
    return updatedCountBeforeFailure > 0;
  }
}