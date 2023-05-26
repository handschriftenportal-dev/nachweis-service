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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 03.06.2022.
 * @version 1.0
 */

@ApplicationScoped
@Slf4j
public class NormdatenUpdateService implements Serializable {

  private static final long serialVersionUID = -1084781654385202627L;

  private NormdatenGraphQLBoundary normdatenGraphQLBoundary;
  private LobidOrgPort lobidOrgPort;

  @Inject
  public NormdatenUpdateService(
      NormdatenGraphQLBoundary normdatenGraphQLBoundary,
      @RestClient LobidOrgPort lobidOrgPort) {
    this.normdatenGraphQLBoundary = normdatenGraphQLBoundary;
    this.lobidOrgPort = lobidOrgPort;
  }

  public Optional<GNDEntityFact> createPlaceIfNotExist(String gndid) {

    AtomicReference<Optional<GNDEntityFact>> result = new AtomicReference<>(Optional.empty());

    normdatenGraphQLBoundary.findOneByIdOrNameAndNodeLabel(gndid, GNDEntityFact.PLACE_TYPE_NAME)
        .ifPresentOrElse(place -> result.set(Optional.of(place)), () -> {

          try {
            Optional.ofNullable(lobidOrgPort.findByGNDID(gndid + ".json"))
                .ifPresent(gndEntityFact -> {
                  gndEntityFact.setTypeName(GNDEntityFact.PLACE_TYPE_NAME);
                  Optional<GNDEntityFact> place = normdatenGraphQLBoundary.createGNDEntityFactAsNode(gndEntityFact);
                  result.set(place);
                });

          } catch (Exception error) {
            log.error("Error during create place if not exist for gndid " + gndid, error);
          }
        });

    return result.get();
  }

}
