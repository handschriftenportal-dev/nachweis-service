package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.DataTableStoreController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.initium.Initium;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

/**
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 28.11.2025
 */

@EqualsAndHashCode(callSuper = true)
@SessionScoped
@Named
@Slf4j
@LoginCheck
public class InitiumModel extends DataTableStoreController implements Serializable {

  @Serial
  private static final long serialVersionUID = 7843925471298832145L;

  private transient InitiumLazyDataModel lazyDataModel;

  private static final String DATATABLE_WIDGET_VAR = "initiumTableWidget";
  private static final String ROOT_CLIENT_ID = "initiumform:initiumTable";

  private static final String ID_ID = "id";
  private static final String ID_TEXT = "text";
  private static final String ID_LANGUAGE = "language";

  private static final List<String> COLUMNS_INDEX = new ArrayList<>(
      Arrays.asList(
          ID_ID,
          ID_TEXT,
          ID_LANGUAGE
      )
  );

  private static final List<Boolean> COLUMNS_VISIBLE = new ArrayList<>(
      Arrays.asList(true, true, true)
  );

  private static final List<String> COLUMNS_WIDTH = new ArrayList<>(
      Arrays.asList("320", "700", "250")
  );

  private static final List<SortMeta> INITIAL_SORTING =
      Collections.singletonList(
          SortMeta.builder()
              .field(ID_TEXT)
              .order(SortOrder.ASCENDING)
              .build()
      );

  InitiumModel() {
    super(
        DATATABLE_WIDGET_VAR,
        ROOT_CLIENT_ID,
        COLUMNS_INDEX,
        INITIAL_SORTING,
        COLUMNS_WIDTH,
        COLUMNS_VISIBLE,
        INIT_HIT_PRO_PAGE,
        ALL_POSSIBLE_HIT_PRO_PAGE
    );
  }

  @Inject
  public InitiumModel(InitiumLazyDataModel lazyDataModel) {
    this();
    this.lazyDataModel = lazyDataModel;
  }

  @PostConstruct
  public void setup() {
    log.info("InitiumModel initialized");
  }

  public LazyDataModel<Initium> getLazyModel() {
    return lazyDataModel;
  }
}
