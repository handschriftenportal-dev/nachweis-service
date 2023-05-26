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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.transformation;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.TeiXmlTransformationServicePort.KEY_ENABLED;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.TeiXmlTransformationServicePort.MODE_TEI2HSP;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.TeiXmlTransformationServicePort;
import java.util.Map;
import java.util.Objects;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 29.03.2022
 */

@ApplicationScoped
@Slf4j
public class TeiXmlTransformationService implements TeiXmlTransformationBoundary {

  private final TeiXmlTransformationServicePort teiXmlTransformationServicePort;

  @Inject
  public TeiXmlTransformationService(@RestClient TeiXmlTransformationServicePort teiXmlTransformationServicePort) {
    this.teiXmlTransformationServicePort = teiXmlTransformationServicePort;
  }

  boolean isTeiToHspEnabled() throws TeiXmlTransformationException {
    Map<String, Boolean> result;
    try {
      result = teiXmlTransformationServicePort.isTeiToHspEnabled();
    } catch (Exception e) {
      throw new TeiXmlTransformationException("Error calling isTeiToHspEnabled.", e);
    }

    if (Objects.nonNull(result) && result.containsKey(TeiXmlTransformationServicePort.KEY_ENABLED)) {
      return result.get(KEY_ENABLED);
    } else {
      throw new TeiXmlTransformationException(
          "TeiXmlTransformationServicePort return no result for isTeiToHspEnabled.");
    }
  }

  @Override
  public String transformTei2Hsp(String teiXml) throws TeiXmlTransformationException {
    if (Objects.isNull(teiXml) || teiXml.isEmpty()) {
      throw new TeiXmlTransformationException("Can't transform empty teiXml.");
    }

    String transformed;
    try {
      transformed = teiXmlTransformationServicePort.transformTeiXml(MODE_TEI2HSP, teiXml);
    } catch (Exception e) {
      throw new TeiXmlTransformationException("Error calling transformTeiXml", e);
    }

    if (Objects.isNull(transformed)) {
      throw new TeiXmlTransformationException("TeiXmlTransformationServicePort.transformTeiXml returned no result!");
    }
    log.debug("transformTei2Hsp: transformed teiXml={}", transformed);
    return transformed;
  }

  @Override
  public void transformTei2Hsp(Beschreibung beschreibung) throws TeiXmlTransformationException {
    if (Objects.nonNull(beschreibung) && isTeiToHspEnabled()) {
      log.info("transformTei2Hsp for beschreibung {} ", beschreibung.getId());
      String transformedTEI = transformTei2Hsp(beschreibung.getTeiXML());
      beschreibung.setTeiXML(transformedTEI);
    }
  }

  @Override
  public void transformTei2Hsp(KulturObjektDokument kod) throws TeiXmlTransformationException {
    if (Objects.nonNull(kod) && isTeiToHspEnabled()) {
      log.info("transformTei2Hsp for kod {} ", kod.getId());
      String transformedTEI = transformTei2Hsp(kod.getTeiXML());
      kod.setTeiXML(transformedTEI);
    }
  }
}
