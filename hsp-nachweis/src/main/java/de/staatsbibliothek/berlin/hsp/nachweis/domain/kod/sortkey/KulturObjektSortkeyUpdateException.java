package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey;

import lombok.Getter;

/**
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 01.06.2026
 */

@Getter
public class KulturObjektSortkeyUpdateException extends Exception {

  private final String messageKey;
  private final Object[] args;

  public KulturObjektSortkeyUpdateException(String messageKey, Object... args) {
    this.messageKey = messageKey;
    this.args = args;
  }

  public KulturObjektSortkeyUpdateException(String messageKey, Throwable cause, Object... args) {
    super(cause);
    this.messageKey = messageKey;
    this.args = args;
  }
}
