package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey;

import java.util.ArrayList;
import java.util.List;

public record SortKeyBatchResult(
    int updatedCount,
    List<String> unknownUuids
) {

  public SortKeyBatchResult {
    unknownUuids = List.copyOf(unknownUuids);
  }

  public static SortKeyBatchResult combine(List<SortKeyBatchResult> chunks) {
    int updatedCount = 0;
    List<String> unknownUuids = new ArrayList<>();
    for (SortKeyBatchResult chunk : chunks) {
      updatedCount += chunk.updatedCount();
      unknownUuids.addAll(chunk.unknownUuids());
    }

    return new SortKeyBatchResult(updatedCount, unknownUuids);
  }


}
