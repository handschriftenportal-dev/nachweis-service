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

package de.staatsbibliothek.berlin.hsp.mapper.tei;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.PtrFactory;
import java.util.Objects;
import java.util.Set;
import org.tei_c.ns._1.Ptr;
import org.tei_c.ns._1.TEI;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 21.09.22
 */
public interface TEIUpdatePURLsCommand {

  static void updatePURLs(TEI tei, Set<PURL> purls) throws Exception {
    Objects.requireNonNull(tei);
    Objects.requireNonNull(purls);

    TEIQuery.queryForFileDescPupPlaceElement(tei)
        .ifPresent(pubPlace -> {
          pubPlace.getContent()
              .removeIf(content -> content instanceof Ptr && PtrFactory.TYPE.equals(((Ptr) content).getType()));

          purls.stream()
              .filter(Objects::nonNull)
              .forEach(purl -> pubPlace.getContent().add(PtrFactory.build(purl)));
        });
  }
}
