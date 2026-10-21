package de.staatsbibliothek.berlin.hsp.nachweis.domain.suche;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by udo.boysen@sbb.spk-berlin.de on 26.02.2020.
 */
@Getter
@ToString
@EqualsAndHashCode
public class Sort implements Serializable {

  @Serial
  private static final long serialVersionUID = 6948011459691534645L;

  private final SortCriteria criteria;
  private final SortType type;

  public Sort(SortCriteria criteria, SortType type) {
    Objects.requireNonNull(criteria, "criteria is required");
    Objects.requireNonNull(type, "type is required");

    this.criteria = criteria;
    this.type = type;
  }

}
