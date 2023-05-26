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

import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.ElementFactoryRegistry.buildElement;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Abmessung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.AttributsReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Beschreibstoff;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Buchschmuck;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungsort;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungszeit;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Format;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Grundsprache;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Handschriftentyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Musiknotation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Status;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Titel;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Umfang;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPMapperException;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.TEI;

public class TEI2AttributsReferenzMapper {

  private TEI2AttributsReferenzMapper() {
    //do nothing
  }

  public static Map<AttributsTyp, AttributsReferenz> map(String teiXML) throws HSPMapperException {
    final TEI tei = unmarshalTEI(teiXML);
    final MsDesc msDesc = findFirstMsDesc(tei);

    return map(tei, msDesc);
  }

  public static Map<AttributsTyp, AttributsReferenz> map(TEI tei, MsDesc msDesc) throws HSPMapperException {
    return Stream.of(
            mapAttributsReferenz(msDesc, tei, Titel.class),
            mapAttributsReferenz(msDesc, tei, Beschreibstoff.class),
            mapAttributsReferenz(msDesc, tei, Umfang.class),
            mapAttributsReferenz(msDesc, tei, Abmessung.class),
            mapAttributsReferenz(msDesc, tei, Format.class),
            mapAttributsReferenz(msDesc, tei, Entstehungsort.class),
            mapAttributsReferenz(msDesc, tei, Entstehungszeit.class),
            mapAttributsReferenz(msDesc, tei, Grundsprache.class),
            mapAttributsReferenz(msDesc, tei, Handschriftentyp.class),
            mapAttributsReferenz(msDesc, tei, Status.class),
            mapAttributsReferenz(msDesc, tei, Buchschmuck.class),
            mapAttributsReferenz(msDesc, tei, Musiknotation.class))
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(AttributsReferenz::getAttributsTyp,
            Function.identity(),
            (ar1, ar2) -> ar2,
            LinkedHashMap::new));
  }

  public static <T extends AttributsReferenz> T mapAttributsReferenz(MsDesc msDesc, TEI tei,
      Class<T> attributsReferenzClass) throws HSPMapperException {
    T element = buildElement(attributsReferenzClass, msDesc, tei);

    return Optional.ofNullable(element)
        .filter(attributsReferenz -> Objects.nonNull(attributsReferenz.getText()))
        .filter(attributsReferenz -> !attributsReferenz.getText().isEmpty())
        .orElse(null);
  }

  public static TEI unmarshalTEI(String teiXML) throws HSPMapperException {
    Objects.requireNonNull(teiXML, "teiXML is required!");

    try (InputStream is = new ByteArrayInputStream(teiXML.getBytes(StandardCharsets.UTF_8))) {
      return TEIObjectFactory.unmarshalOne(is)
          .orElseThrow(() -> new Exception("teiXML contains no TEI!"));
    } catch (Exception e) {
      throw new HSPMapperException("Error unmarshalling teiXML", e);
    }
  }

  public static MsDesc findFirstMsDesc(TEI tei) throws HSPMapperException {
    try {
      return TEICommon.findFirst(MsDesc.class, tei)
          .orElseThrow(() -> new HSPMapperException("TEI contains no MsDesc!"));
    } catch (HSPMapperException hspMapperException) {
      throw hspMapperException;
    } catch (Exception e) {
      throw new HSPMapperException("Error finding MsDesc in TEI!", e);
    }
  }
}
