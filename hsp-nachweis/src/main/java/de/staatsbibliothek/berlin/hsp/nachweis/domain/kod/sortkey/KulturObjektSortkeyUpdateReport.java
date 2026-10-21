package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey;

import java.util.List;

/**
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 01.06.2026
 */

public record KulturObjektSortkeyUpdateReport(
    int updatedCount,
    List<String> unknownUuids,
    List<String> missingOrEmptySortkeyLines) {

  public KulturObjektSortkeyUpdateReport {
    unknownUuids = List.copyOf(unknownUuids);
    missingOrEmptySortkeyLines = List.copyOf(missingOrEmptySortkeyLines);
  }
}
