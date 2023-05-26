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

package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 30.08.2021
 */
@Getter
@ToString
@EqualsAndHashCode(of = {"id"})
public class SperrenViewModel implements Serializable {

  private static final long serialVersionUID = -6092498054855517416L;

  private final String id;
  private final String bearbeitername;
  private final LocalDateTime startDatum;
  private final SperreTyp sperreTyp;
  private final String sperreGrund;
  private final String txId;
  private final List<SperreEintragViewModel> sperreEintraege;
  private String sperreEintraegeSignaturen;
  private String sperreEintraegeIds;

  public SperrenViewModel(SperrenViewModelBuilder builder) {
    this.id = builder.id;
    this.bearbeitername = builder.bearbeitername;
    this.startDatum = builder.startDatum;
    this.sperreTyp = builder.sperreTyp;
    this.sperreGrund = builder.sperreGrund;
    this.txId = builder.txId;
    this.sperreEintraege = builder.sperreEintraege;

    this.sperreEintraegeSignaturen = builder.sperreEintraege
        .stream()
        .map(se -> se.getSignature() + " " + se.getTargetType())
        .filter(Objects::nonNull)
        .collect(Collectors.joining("  "));

    this.sperreEintraegeIds = builder.sperreEintraege
        .stream()
        .map(se -> se.getTargetId() + " " + se.getTargetType())
        .filter(Objects::nonNull)
        .collect(Collectors.joining("  "));
  }

  public static class SperrenViewModelBuilder {

    private String id;
    private String bearbeitername;
    private LocalDateTime startDatum;
    private SperreTyp sperreTyp;
    private String sperreGrund;
    private String txId;
    private List<SperreEintragViewModel> sperreEintraege = new ArrayList<>();

    public SperrenViewModelBuilder withId(String id) {
      this.id = id;
      return this;
    }

    public SperrenViewModelBuilder withBearbeitername(String bearbeitername) {
      this.bearbeitername = bearbeitername;
      return this;
    }

    public SperrenViewModelBuilder withStartDatum(LocalDateTime startDatum) {
      this.startDatum = startDatum;
      return this;
    }

    public SperrenViewModelBuilder withSperreTyp(SperreTyp sperreTyp) {
      this.sperreTyp = sperreTyp;
      return this;
    }

    public SperrenViewModelBuilder withSperreGrund(String sperreGrund) {
      this.sperreGrund = sperreGrund;
      return this;
    }

    public SperrenViewModelBuilder withTxId(String txId) {
      this.txId = txId;
      return this;
    }

    public SperrenViewModelBuilder withSperreEintraege(List<SperreEintragViewModel> sperreEintraege) {
      this.sperreEintraege.clear();
      if (Objects.nonNull(sperreEintraege)) {
        this.sperreEintraege.addAll(sperreEintraege);
      }
      return this;
    }

    public SperrenViewModelBuilder addSperreEintrag(SperreEintragViewModel sperreEintrag) {
      if (Objects.nonNull(sperreEintrag)) {
        this.sperreEintraege.add(sperreEintrag);
      }
      return this;
    }


    public SperrenViewModel build() {
      return new SperrenViewModel(this);
    }
  }

}
