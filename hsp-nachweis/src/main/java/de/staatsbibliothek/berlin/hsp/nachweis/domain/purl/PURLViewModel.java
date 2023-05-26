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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.purl;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 08.08.22
 */

@Getter
@EqualsAndHashCode(of = {"dokumentId"})
@ToString
public class PURLViewModel implements Serializable {

  private static final long serialVersionUID = 2322354704163687258L;

  private final String dokumentId;
  private final DokumentObjektTyp dokumentObjektTyp;
  private final Set<PURL> internalPURLs;
  private final Set<PURL> externalPURLs;

  public PURLViewModel(String dokumentId, DokumentObjektTyp dokumentObjektTyp, Set<PURL> purls) {
    this.dokumentId = dokumentId;
    this.dokumentObjektTyp = dokumentObjektTyp;
    this.internalPURLs = Stream.ofNullable(purls)
        .flatMap(Collection::stream)
        .filter(purl -> PURLTyp.INTERNAL == purl.getTyp()).collect(Collectors.toSet());
    this.externalPURLs = Stream.ofNullable(purls)
        .flatMap(Collection::stream)
        .filter(purl -> PURLTyp.EXTERNAL == purl.getTyp()).collect(Collectors.toSet());
  }

}
