package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey;

import lombok.Getter;

/**
 * Signals an aborted SortKey import in which previously processed chunks have already been
 * committed. Carries the number of Kulturobjekte updated before the failure, so the UI does not
 * have to falsely report "nothing was changed".
 */
@Getter
public class SortkeyPartialImportException extends KulturObjektSortkeyUpdateException {

  private final int updatedCountBeforeFailure;

  public SortkeyPartialImportException(int updatedCountBeforeFailure, Throwable cause) {
    super("kod_sortkeys_error_partial", cause, updatedCountBeforeFailure);
    this.updatedCountBeforeFailure = updatedCountBeforeFailure;
  }
}
