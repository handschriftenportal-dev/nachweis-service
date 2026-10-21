package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GraphQlQuery.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 19.09.23.
 * @version 1.0
 */
@Getter
@ToString
@EqualsAndHashCode(of = {"id"})
public class Concept {

  public static final String ID = "id";
  public static final String URI = "uri";
  public static final String NOTATION = "notation";
  public static final String LABELS = "labels";
  public static final Set<GraphQlQuery.Field> FIELDS = new LinkedHashSet<>(Arrays.asList(
      GraphQlQuery.Field.of(ID),
      GraphQlQuery.Field.of(URI),
      GraphQlQuery.Field.of(NOTATION),
      Field.of(LABELS, Labels.FIELDS)));

  private String id;

  private String uri;

  private String notation;

  private List<Labels> labels;

  protected Concept() {
  }

  public Concept(String id, String uri, String notation) {
    this.id = id;
    this.uri = uri;
    this.notation = notation;
    this.labels = new ArrayList<>();
  }


  public Concept(String id, String uri, String notation, List<Labels> labels) {
    this.id = id;
    this.uri = uri;
    this.notation = notation;
    this.labels = labels;
  }
}


