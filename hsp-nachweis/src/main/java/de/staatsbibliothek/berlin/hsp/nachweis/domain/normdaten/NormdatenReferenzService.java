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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenGraphQLBoundary;
import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 04.12.20
 */
@Dependent
@Slf4j
public class NormdatenReferenzService implements NormdatenReferenzBoundary, Serializable {

  private static final long serialVersionUID = 7992690266107947322L;

  private final NormdatenGraphQLBoundary normdatenGraphQLBoundary;

  @Inject
  public NormdatenReferenzService(NormdatenGraphQLBoundary normdatenGraphQLBoundary) {
    this.normdatenGraphQLBoundary = normdatenGraphQLBoundary;
  }

  @Override
  @Transactional
  public Optional<NormdatenReferenz> findOneByIdOrNameAndType(String idOrName, String type) {
    log.debug("findOneByIdOrNameAndNodeLabel idOrName={}, type={}", idOrName, type);

    if (Objects.isNull(idOrName) || idOrName.isEmpty()) {
      return Optional.empty();
    }

    return normdatenGraphQLBoundary.findOneByIdOrNameAndNodeLabel(idOrName, type)
        .map(NormdatenReferenzMapper::map);
  }

  @Override
  @Transactional
  public Set<NormdatenReferenz> findAllByIdOrNameAndType(String idOrName, String type, boolean allFields) {
    log.debug("findAllByIdOrNameAndType idOrName={}, type={}, allFields={}", idOrName, type, allFields);

    if ((Objects.isNull(type) || type.isEmpty()) && (Objects.isNull(idOrName) || idOrName.isEmpty())) {
      return Collections.emptySet();
    }

    return normdatenGraphQLBoundary.findAllByIdOrNameAndNodeLabel(idOrName, type, allFields)
        .stream()
        .map(NormdatenReferenzMapper::map)
        .collect(Collectors.toSet());
  }

  @Override
  @Transactional
  public Optional<NormdatenReferenz> findSpracheById(String id) {
    log.debug("findSpracheById id={}", id);

    if (Objects.isNull(id) || id.isEmpty()) {
      return Optional.empty();
    }

    return normdatenGraphQLBoundary.findLanguageById(id)
        .map(NormdatenReferenzMapper::map);
  }

  @Override
  @Transactional
  public Set<NormdatenReferenz> findKoerperschaftenByOrtId(String ortId, boolean allFields) {
    log.debug("findKoerperschaftenByOrtId ortId={}, allFields={}", ortId, allFields);
    if (Objects.isNull(ortId)) {
      return Collections.emptySet();
    }

    return normdatenGraphQLBoundary.findCorporateBodiesByPlaceId(ortId, allFields)
        .stream()
        .map(NormdatenReferenzMapper::map)
        .collect(Collectors.toSet());
  }

}
