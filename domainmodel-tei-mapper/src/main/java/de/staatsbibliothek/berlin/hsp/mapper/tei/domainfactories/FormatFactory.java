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

package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.FORMAT_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.FORMAT_TERM_TYP_TYPE;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Format;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.FormatWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.FormatWert.FormatWertBuilder;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.Term;

public class FormatFactory extends AttributsReferenzFactory<Format> {

  static Map<String, BiConsumer<Term, FormatWertBuilder>> termBuilders = new HashMap<>();

  static {
    termBuilders.put(FORMAT_TERM_TYP_TEXT,
        (term, builder) -> builder.withText(TEICommon.getContentAsString(term.getContent())));
    termBuilders.put(FORMAT_TERM_TYP_TYPE,
        (term, builder) -> builder.withTyp(TEICommon.getContentAsString(term.getContent())));
  }

  @Override
  public Format build(MsDesc msDesc, TEI tei) {
    Objects.requireNonNull(msDesc, "msDesc is required!");
    Objects.requireNonNull(tei, "tei is required!");

    List<Index> indices = findIndices(msDesc, AttributsTyp.FORMAT.getIndexName());

    if (!indices.isEmpty()) {
      Index index = indices.iterator().next();

      return Format.builder()
          .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
          .withReferenzId(getCopyOfValue(index.getCopyOf(),msDesc))
          .withReferenzURL(index.getSources().stream().findFirst().orElse(""))
          .withFormatWerte(mapFormatWerte(indices))
          .build();
    } else {
      return null;
    }
  }

  private List<FormatWert> mapFormatWerte(List<Index> indices) {
    return indices.stream().map(AttributsReferenzFactory::findTerme)
        .map(this::mapFormatWert)
        .collect(Collectors.toList());
  }

  private FormatWert mapFormatWert(List<Term> terme) {
    FormatWertBuilder builder = FormatWert.builder();
    terme.forEach(term ->
        Optional.ofNullable(termBuilders.get(term.getType()))
            .ifPresentOrElse(
                consumer -> consumer.accept(term, builder),
                () -> {
                  throw new IllegalArgumentException("Unmapped Term: " + term);
                }));
    return builder.build();
  }


}
