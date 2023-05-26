/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
