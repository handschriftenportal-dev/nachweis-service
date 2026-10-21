package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.ROW_INVALID;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportResult.Status.FEHLER;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Coordinates the sequential, row-based import of external resources.
 *
 * <p>The service delegates CSV decoding and structural row validation to {@link
 * ExterneRessourcenImportCSVParser}. Valid entries are processed by {@link
 * ExterneRessourcenImportEntryService}, while structurally invalid rows are converted directly
 * into failed report entries. Results are collected in the same order as the input rows, and an
 * error in one row does not stop the remaining import.
 *
 * <p>This service deliberately does not open a transaction for the complete file and does not own
 * KOD lock handling or TEI persistence. Each valid row is processed in the independent transaction
 * provided by {@link ExterneRessourcenImportEntryService}.
 *
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 20.08.2026
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ExterneRessourcenImportService {

  private final ExterneRessourcenImportCSVParser csvParser;
  private final ExterneRessourcenImportEntryService entryService;

  public List<ExterneRessourcenImportResult> importieren(byte[] content)
      throws ExterneRessourcenImportException {

    List<ExterneRessourcenImportResult> results = new ArrayList<>();
    csvParser.parse(
        content,
        entry -> results.add(entryService.importieren(entry)),
        lineNumber -> results.add(parseErrorResult(lineNumber)));

    return List.copyOf(results);
  }

  private ExterneRessourcenImportResult parseErrorResult(int lineNumber) {
    return ExterneRessourcenImportResult.builder()
        .kodUuid("")
        .ressource("")
        .link("")
        .status(FEHLER)
        .errorMessageKey(ROW_INVALID)
        .lineNumber(lineNumber)
        .build();
  }

}
