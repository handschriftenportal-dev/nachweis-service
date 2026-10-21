package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportResult.Status.ERFOLGREICH;
import static jakarta.transaction.Status.STATUS_ACTIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionManager;
import java.util.Optional;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ExterneRessourcenImportEntryServiceTest {

  private static final String KOD_ID = "HSP-123";

  @Inject
  ExterneRessourcenImportEntryService entryService;

  @Inject
  TransactionManager transactionManager;

  @InjectMock
  KulturObjektDokumentBoundary kulturObjektDokumentBoundary;

  @Test
  void shouldProcessEntryWithinActiveTransaction() throws Exception {
    KulturObjektDokument kod = new KulturObjektDokumentBuilder().withId(KOD_ID).build();
    when(kulturObjektDokumentBoundary.findById(KOD_ID)).thenAnswer(invocation -> {
      assertEquals(STATUS_ACTIVE, transactionManager.getStatus());
      return Optional.of(kod);
    });
    doAnswer(invocation -> {
      assertEquals(STATUS_ACTIVE, transactionManager.getStatus());
      return null;
    }).when(kulturObjektDokumentBoundary).externeRessourceImportieren(any(), any());

    ExterneRessourcenImportResult result = entryService.importieren(
        ExterneRessourcenImportEntry.builder()
            .kodUuid(KOD_ID)
            .ressource("kdih")
            .link("https://kdih.badw.de/datenbank/handschrift/38/2/1")
            .lineNumber(1)
            .build());

    assertEquals(ERFOLGREICH, result.status());
    assertEquals(KOD_ID, result.kodUuid());
  }
}
