package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.KOD_NOT_FOUND;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.LINK_INVALID;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.RESOURCE_INVALID;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.SAVE_ERROR;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.UUID_NOT_FOUND;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportResult.Status.ERFOLGREICH;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportResult.Status.FEHLER;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessource;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessourceTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Processes one external-resource import entry within its own transaction.
 *
 * <p>This separate CDI bean is required so that {@link Transactional} is applied through a CDI
 * proxy. Keeping the entry method in {@link ExterneRessourcenImportService} would result in
 * self-invocation, for which CDI transactional interceptors are not executed.
 *
 * <p>The service provides one independent {@code REQUIRES_NEW} transaction per CSV row. It covers
 * KOD lookup, resource validation and persistence. A failure therefore rolls back only the current
 * row and does not stop or roll back the remaining import entries.
 *
 * <p>{@link ExterneRessourcenImportService} remains responsible for parsing the CSV, preserving
 * input order, continuing after row errors and collecting the result report.
 *
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 25.08.2026
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ExterneRessourcenImportEntryService {

  private final KulturObjektDokumentBoundary kulturObjektDokumentBoundary;

  @Transactional(value = TxType.REQUIRES_NEW, rollbackOn = Exception.class)
  public ExterneRessourcenImportResult importieren(ExterneRessourcenImportEntry entry) {
    String resultKodUuid = entry.kodUuid();
    String resultRessource = entry.ressource();
    String resultLink = entry.link();

    try {
      KulturObjektDokument kod = resolveKod(entry);
      resultKodUuid = kod.getId();

      ExterneRessource externeRessource = resolveExterneRessource(entry);
      resultRessource = externeRessource.typ().getTeiType();
      resultLink = externeRessource.url();

      kulturObjektDokumentBoundary.externeRessourceImportieren(
          resultKodUuid, externeRessource);

      return ExterneRessourcenImportResult.builder()
          .kodUuid(resultKodUuid)
          .ressource(resultRessource)
          .link(resultLink)
          .status(ERFOLGREICH)
          .lineNumber(entry.lineNumber())
          .build();

    } catch (ExterneRessourcenImportException exception) {
      return errorResult(
          entry,
          resultKodUuid,
          resultRessource,
          resultLink,
          exception.getMessageKey(),
          exception.getArgs());

    } catch (Exception exception) {
      String details = StringUtils.defaultIfBlank(
          exception.getMessage(), exception.getClass().getSimpleName());
      log.error("Unexpected external resources import error in line {} for KOD {}",
          entry.lineNumber(), resultKodUuid, exception);
      return errorResult(
          entry,
          resultKodUuid,
          resultRessource,
          resultLink,
          SAVE_ERROR,
          new Object[]{details});
    }
  }

  private KulturObjektDokument resolveKod(ExterneRessourcenImportEntry entry)
      throws ExterneRessourcenImportException {

    if (StringUtils.isNotBlank(entry.kodUuid())) {
      return kulturObjektDokumentBoundary.findById(entry.kodUuid())
          .orElseThrow(() -> new ExterneRessourcenImportException(
              UUID_NOT_FOUND, entry.kodUuid()));
    }

    if (StringUtils.isNotBlank(entry.resolverAdresse())) {
      return kulturObjektDokumentBoundary.findByPurl(entry.resolverAdresse())
          .orElseThrow(() -> new ExterneRessourcenImportException(
              KOD_NOT_FOUND));
    }

    return kulturObjektDokumentBoundary
        .findKulturObjektDokumentBySignaturForExterneRessourcenImport(
            entry.fuehrendeSignatur(),
            entry.besitzendeEinrichtung(),
            entry.aufbewahrungsort());
  }

  private ExterneRessource resolveExterneRessource(ExterneRessourcenImportEntry entry)
      throws ExterneRessourcenImportException {

    ExterneRessourceTyp typ = ExterneRessourceTyp.fromTeiType(entry.ressource())
        .orElseThrow(() -> new ExterneRessourcenImportException(
            RESOURCE_INVALID));

    try {
      return new ExterneRessource(typ, entry.link());
    } catch (IllegalArgumentException | NullPointerException exception) {
      throw new ExterneRessourcenImportException(
          LINK_INVALID, exception);
    }
  }

  private ExterneRessourcenImportResult errorResult(
      ExterneRessourcenImportEntry entry,
      String kodUuid,
      String ressource,
      String link,
      ExterneRessourcenImportErrorMessageKey errorMessageKey,
      Object[] errorMessageArgs) {

    return ExterneRessourcenImportResult.builder()
        .kodUuid(kodUuid)
        .ressource(ressource)
        .link(link)
        .status(FEHLER)
        .errorMessageKey(errorMessageKey)
        .errorMessageArgs(errorMessageArgs)
        .lineNumber(entry.lineNumber())
        .build();
  }
}
