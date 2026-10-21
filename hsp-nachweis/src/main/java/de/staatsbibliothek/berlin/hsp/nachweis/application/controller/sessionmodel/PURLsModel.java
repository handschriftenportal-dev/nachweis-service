package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.DataTableStoreController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLViewModel;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 09.08.2022
 */

@EqualsAndHashCode(callSuper = true)
@SessionScoped
@Named
@Slf4j
@LoginCheck
public class PURLsModel extends DataTableStoreController implements Serializable {

  @Serial
  private static final long serialVersionUID = 9087770245166202224L;
  private static final String DATATABLE_WIDGET_VAR = "purlsTableWidget";
  private static final String ROOT_CLIENT_ID = "purlsform:purlsTable";
  private static final String ID_SELECTION = "selection";
  private static final String ID_DOKUMENT_ID = "dokumentId";
  private static final String ID_DOKUMENT_OBJEKT_TYP = "dokumentObjektTyp";
  private static final String ID_PURLS_INTERNAL = "purlsINTERNAL";
  private static final String ID_PURLS_EXTERNAL = "purlsEXTERNAL";
  private static final List<String> COLUMS_INDEX =
      List.of(ID_SELECTION, ID_DOKUMENT_ID, ID_DOKUMENT_OBJEKT_TYP, ID_PURLS_INTERNAL,
          ID_PURLS_EXTERNAL);
  private static final List<Boolean> COLUMNS_VISIBLE = List.of(true, true, true, true, true);
  private static final List<String> COLUMNS_WIDTH = List.of("50", "250", "100", "300", "300");
  private static final List<SortMeta> INITIAL_SORTING
      = Collections.singletonList(
      SortMeta.builder().field(ID_DOKUMENT_ID).order(SortOrder.ASCENDING).build());

  private transient PURLRepository purlRepository;

  @Getter
  @Setter
  private transient List<PURLViewModel> purlViewModels;
  @Setter
  @Getter
  private transient List<PURLViewModel> filteredPURLViewModels;
  @Setter
  @Getter
  private transient List<PURLViewModel> selectedPURLViewModels;

  public PURLsModel() {
    super(DATATABLE_WIDGET_VAR, ROOT_CLIENT_ID, COLUMS_INDEX,
        INITIAL_SORTING, COLUMNS_WIDTH, COLUMNS_VISIBLE, INIT_HIT_PRO_PAGE,
        ALL_POSSIBLE_HIT_PRO_PAGE);
  }

  @Inject
  public PURLsModel(PURLRepository purlRepository) {
    this();
    this.purlRepository = purlRepository;
  }

  @PostConstruct
  protected void setup() {
    log.debug("Start setup");
    if (Objects.isNull(purlViewModels)) {
      loadAllData();
    }
  }

  public void loadAllData() {
    List<PURLViewModel> allViewModels = purlRepository.findAllAsViewModels();
    purlViewModels = new ArrayList<>(allViewModels);

    filteredPURLViewModels = null;
    selectedPURLViewModels = null;
  }

  public String purlsAsString(Set<PURL> purls) {
    return Stream.ofNullable(purls)
        .flatMap(Set::stream)
        .map(purl -> purl.getPurl().toASCIIString())
        .collect(Collectors.joining(" "));
  }

}
