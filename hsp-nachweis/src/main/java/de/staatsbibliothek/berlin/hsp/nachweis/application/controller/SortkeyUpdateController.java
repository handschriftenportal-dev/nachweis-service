package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.SortkeyUpdateModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.KulturObjektSortkeyUpdateReport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortkeyImportFailure;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortkeyImportStateService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;

/**
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 01.06.2026
 */

@Getter
@Named
@ViewScoped
public class SortkeyUpdateController implements Serializable {

  @Serial
  private static final long serialVersionUID = 8901795879928550037L;

  private final SortkeyUpdateModel dataModel;
  private final SortkeyImportStateService sortkeyImportStateService;
  private final BearbeiterBoundary bearbeiterBoundary;

  @Inject
  public SortkeyUpdateController(
      SortkeyUpdateModel dataModel,
      SortkeyImportStateService sortkeyImportStateService,
      BearbeiterBoundary bearbeiterBoundary) {

    this.dataModel = dataModel;
    this.sortkeyImportStateService = sortkeyImportStateService;
    this.bearbeiterBoundary = bearbeiterBoundary;
  }

  /**
   * On initial rendering only the progressbar state is synchronized. Report and failure message are
   * deliberately NOT consumed here: a FacesMessage queued in @PostConstruct of a lazily
   * instantiated ViewScoped bean would be dropped on a GET, because "globalmessages" (template) has
   * already been rendered at that point. Delivery is handled by the first poll tick via AJAX
   * ({@link #checkImportStatus()}), where p:autoUpdate applies.
   */
  @PostConstruct
  public void init() {
    dataModel.setRenderProgressbar(sortkeyImportStateService.isRunning());
  }

  public void showProgressbar() {
    dataModel.setRenderProgressbar(true);
  }

  public boolean isRenderProgressbar() {
    return dataModel.isRenderProgressbar();
  }

  public void handleSortkeysUpload(FileUploadEvent event) {
    dataModel.handleSortkeysUpload(event);
  }

  public boolean isSuccess() {
    return dataModel.getReport() != null;
  }

  public void checkImportStatus() {

    if (syncImportState() == ImportStatusUpdate.REPORT) {
      PrimeFaces.current().ajax().update(
          "sortkeyReportForm:sortkeyReportDialog");
      PrimeFaces.current().executeScript(
          "PF('sortkeyReportDialog').show();");
    }
  }

  /**
   * Synchronizes the global import state into the view model. Report and failure are bound to the
   * Bearbeiter who started the import; for other users the consume* methods return null and the
   * state is left untouched.
   */
  private ImportStatusUpdate syncImportState() {

    if (sortkeyImportStateService.isRunning()) {
      dataModel.setRenderProgressbar(true);
      return ImportStatusUpdate.RUNNING;
    }

    dataModel.setRenderProgressbar(false);

    String bearbeiterId = bearbeiterBoundary.getLoggedBearbeiter().getId();

    KulturObjektSortkeyUpdateReport report =
        sortkeyImportStateService.consumeReport(bearbeiterId);

    if (report != null) {
      dataModel.setReport(report);
      return ImportStatusUpdate.REPORT;
    }

    SortkeyImportFailure failure =
        sortkeyImportStateService.consumeFailure(bearbeiterId);

    if (failure != null) {

      String message = failure.isPartial()
          ? I18NController.getMessage(
          "kod_sortkeys_error_partial", failure.updatedCountBeforeFailure())
          : I18NController.getMessage("kod_sortkeys_error");

      FacesContext.getCurrentInstance().addMessage(
          "globalmessages",
          new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));

      return ImportStatusUpdate.FAILURE;
    }

    return ImportStatusUpdate.IDLE;
  }

  private enum ImportStatusUpdate {
    RUNNING,
    REPORT,
    FAILURE,
    IDLE
  }
}
