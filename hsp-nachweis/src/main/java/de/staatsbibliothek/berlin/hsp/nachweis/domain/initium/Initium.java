package de.staatsbibliothek.berlin.hsp.nachweis.domain.initium;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

/**
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 03.12.2025
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class Initium {

  private String id;
  private String text;
  private List<InitiumLanguage> languages;

  public String getLanguage() {
    return getLanguageLabelsCapitalized();
  }

  public String getLanguageLabelsCapitalized() {
    if (languages == null || languages.isEmpty()) {
      return "";
    }
    return languages.stream()
        .map(InitiumLanguage::preferredName)
        .filter(s -> s != null && !s.isBlank())
        .map(this::capitalizeFirst)
        .collect(Collectors.joining(", "));
  }

  private String capitalizeFirst(String value) {
    if (StringUtils.isBlank(value)) {
      return value;
    }
    return value.substring(0, 1).toUpperCase() + value.substring(1);
  }
}
