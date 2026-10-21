package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GraphQlQuery.Field;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@ToString
@EqualsAndHashCode
public class Labels {

  public static final String TEXT = "text";
  public static final String ISOCODE = "isoCode";

  public static final Set<Field> FIELDS = new LinkedHashSet<>(Arrays.asList(
      Field.of(Labels.ISOCODE),
      Field.of(Labels.TEXT)));

  @JsonProperty(value = "isoCode")
  private String isoCode;

  @JsonProperty(value = "text")
  private String text;

}
