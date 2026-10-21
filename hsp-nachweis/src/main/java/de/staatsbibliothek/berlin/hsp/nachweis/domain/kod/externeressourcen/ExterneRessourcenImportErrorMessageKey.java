package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen;

/**
 * Defines the message-bundle keys that can represent an external-resources import error.
 *
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 26.08.2026
 */
public enum ExterneRessourcenImportErrorMessageKey {

  FILE_EMPTY("kod_externe_ressourcen_import_file_empty"),
  ENCODING_INVALID("kod_externe_ressourcen_import_encoding_invalid"),
  ROW_INVALID("kod_externe_ressourcen_import_row_invalid"),
  UUID_NOT_FOUND("kod_externe_ressourcen_import_uuid_not_found"),
  KOD_NOT_FOUND("kod_externe_ressourcen_import_kod_not_found"),
  IDENTIFICATION_INCOMPLETE("kod_externe_ressourcen_import_identification_incomplete"),
  KOD_CONFLICTING("kod_externe_ressourcen_import_kod_conflicting"),
  KOD_AMBIGUOUS("kod_externe_ressourcen_import_kod_ambiguous"),
  KOD_LOCKED("kod_externe_ressourcen_import_kod_locked"),
  RESOURCE_INVALID("kod_externe_ressourcen_import_resource_invalid"),
  LINK_INVALID("kod_externe_ressourcen_import_link_invalid"),
  DUPLICATE("kod_externe_ressourcen_import_duplicate"),
  SAVE_ERROR("kod_externe_ressourcen_import_save_error"),
  RESULT_WRITE_ERROR("kod_externe_ressourcen_import_result_write_error"),
  UNEXPECTED_ERROR("kod_externe_ressourcen_import_unexpected_error");

  private final String key;

  ExterneRessourcenImportErrorMessageKey(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }
}
