package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.DataTableStoreController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.person.Person;
import jakarta.annotation.PostConstruct;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

/**
 * Author: Christoph Marten on 10.03.2021 at 13:03
 */
@EqualsAndHashCode(callSuper = true)
@SessionScoped
@Named
@Slf4j
public class PersonModel extends DataTableStoreController implements Serializable {

  @Serial
  private static final long serialVersionUID = 9087770245166202224L;

  private static final String DATATABLE_WIDGET_VAR = "personTableWidget";
  private static final String ROOT_CLIENT_ID = "personenform:personenTable";

  private static final String ID_NAME = "preferredName";
  private static final String ID_VARIANTE_NAMEN = "variantName";
  private static final String ID_PERSON_ID = "personId";
  private static final String ID_GNDID = "gndIdentifier";

  private static final List<String> COLUMNS_INDEX = new ArrayList<>(
      Arrays.asList(ID_NAME, ID_VARIANTE_NAMEN, ID_PERSON_ID, ID_GNDID));

  private static final List<Boolean> COLUMNS_VISIBLE = new ArrayList<>(
      Arrays.asList(true, true, true, true));

  private static final List<String> COLUMNS_WIDTH = new ArrayList<>(
      Arrays.asList("300", "655", "250", "100"));

  private static final List<SortMeta> INITIAL_SORTING =
      Collections.singletonList(
          SortMeta.builder().field(ID_NAME).order(SortOrder.ASCENDING).build()
      );

  private transient PersonLazyDataModel lazyDataModel;

  PersonModel() {
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
  public PersonModel(PersonLazyDataModel lazyDataModel) {
    this();
    this.lazyDataModel = lazyDataModel;
  }

  @PostConstruct
  public void setup() {
    log.info("PersonModel initialized (Lazy Loading aktiv)");
  }

  @SuppressWarnings("unused")
  public LazyDataModel<Person> getLazyModel() {
    return lazyDataModel;
  }
}
