package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkeyupdate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.KulturObjektSortkeyUpdateReport;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class KulturObjektSortkeyUpdateReportTest {

  @Test
  void shouldCreateImmutableCopyOfLists() {

    List<String> unknownUuids = new ArrayList<>();
    unknownUuids.add("uuid-1");

    List<String> missingLines = new ArrayList<>();
    missingLines.add("line-1");

    KulturObjektSortkeyUpdateReport report =
        new KulturObjektSortkeyUpdateReport(1, unknownUuids, missingLines);

    unknownUuids.add("uuid-2");
    missingLines.add("line-2");

    assertEquals(1, report.unknownUuids().size());
    assertEquals(1, report.missingOrEmptySortkeyLines().size());

    assertTrue(report.unknownUuids().contains("uuid-1"));
    assertTrue(report.missingOrEmptySortkeyLines().contains("line-1"));
  }

  @Test
  void shouldReturnUnmodifiableLists() {

    KulturObjektSortkeyUpdateReport report =
        new KulturObjektSortkeyUpdateReport(
            0,
            List.of("uuid-1"),
            List.of("line-1")
        );

    assertThrows(
        UnsupportedOperationException.class,
        () -> report.unknownUuids().add("uuid-2")
    );

    assertThrows(
        UnsupportedOperationException.class,
        () -> report.missingOrEmptySortkeyLines().add("line-2")
    );
  }

  @Test
  void shouldStoreUpdatedCountCorrectly() {

    KulturObjektSortkeyUpdateReport report =
        new KulturObjektSortkeyUpdateReport(
            5,
            List.of(),
            List.of()
        );

    assertEquals(5, report.updatedCount());
  }
}
