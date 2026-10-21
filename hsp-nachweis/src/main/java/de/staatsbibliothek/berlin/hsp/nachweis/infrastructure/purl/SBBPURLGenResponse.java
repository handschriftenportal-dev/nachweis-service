package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.purl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 02.08.22
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(setterPrefix = "with")
@EqualsAndHashCode(of = {"pid", "mvid", "vid", "page"})
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SBBPURLGenResponse implements Serializable {

  private static final long serialVersionUID = 2932983567051807663L;

  @JsonProperty
  private String status;

  @JsonProperty
  private String message;

  @JsonProperty
  private String pid;

  @JsonProperty
  private String mvid;

  @JsonProperty
  private String vid;

  @JsonProperty
  private String page;

  public String getFullPid() {
    StringBuilder sb = new StringBuilder();
    Optional.ofNullable(pid).ifPresent(sb::append);
    Optional.ofNullable(mvid).ifPresent(sb::append);
    Optional.ofNullable(vid).ifPresent(sb::append);
    Optional.ofNullable(page).ifPresent(sb::append);

    return sb.toString();
  }


}
