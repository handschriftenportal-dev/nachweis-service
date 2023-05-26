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
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.CRUDRepository;
import java.util.Optional;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 03.12.20
 */
public interface NormdatenReferenzRepository extends CRUDRepository<NormdatenReferenz, String> {

  NormdatenReferenz findByName(String name);

  default Optional<NormdatenReferenz> findByNameOptional(String name) {
    return Optional.ofNullable(findByName(name));
  }

  NormdatenReferenz findByGndID(String gndID);

  default Optional<NormdatenReferenz> findByGndIDOptional(String gndID) {
    return Optional.ofNullable(findByGndID(gndID));
  }

  NormdatenReferenz findByIdOrIdentifikator(String id);

  default Optional<NormdatenReferenz> findByIdOrIdentifikatorOptional(String id) {
    return Optional.ofNullable(findByIdOrIdentifikator(id));
  }

  NormdatenReferenz findByIdOrName(String idOrName, String type) throws Exception;

  default Optional<NormdatenReferenz> findByIdOrNameOptional(String idOrName, String type) throws Exception {
    return Optional.ofNullable(findByIdOrName(idOrName, type));
  }

}
