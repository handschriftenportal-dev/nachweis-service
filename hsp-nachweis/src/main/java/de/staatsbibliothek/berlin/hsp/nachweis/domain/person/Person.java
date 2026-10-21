package de.staatsbibliothek.berlin.hsp.nachweis.domain.person;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 24.03.2026
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class Person {

  private static final String DNB_BASE_URL = "https://d-nb.info/gnd/";

  private String id;
  private String preferredName;
  private String gndIdentifier;
  private List<PersonVariantName> variantName;

  public String getVariantName() {
    if (variantName == null || variantName.isEmpty()) {
      return "";
    }
    return variantName.stream()
        .map(PersonVariantName::name)
        .filter(s -> s != null && !s.isBlank())
        .collect(Collectors.joining(", "));
  }

  @JsonIgnore
  public String getGndUrl() {
    if (StringUtils.isBlank(gndIdentifier)) {
      return null;
    }
    return DNB_BASE_URL + gndIdentifier;
  }
}
