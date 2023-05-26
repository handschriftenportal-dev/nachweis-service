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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 07.10.2020.
 * @version 1.0
 */

@ViewScoped
@Named
@Slf4j
public class AutocompleteNormdatenController implements Serializable {

  private static final long serialVersionUID = -7373703447572273448L;

  private transient NormdatenReferenzBoundary normdatenReferenzBoundary;

  @Inject
  public AutocompleteNormdatenController(NormdatenReferenzBoundary normdatenReferenzBoundary) {
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
  }

  public List<NormdatenReferenz> autocompleteOrt(String keyValue) {
    log.info("Autocomplete for Ort: keyValue {} ", keyValue);

    return autocomplete(keyValue, NormdatenReferenz.ORT_TYPE_NAME);
  }

  public List<NormdatenReferenz> autocompleteKoerperschaft(String keyValue) {
    log.info("Autocomplete for Koerperschaft: keyValue {} ", keyValue);

    return autocomplete(keyValue, NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME);
  }

  public List<NormdatenReferenz> autocompletePerson(String keyValue) {
    log.info("Autocomplete for Person: keyValue {} ", keyValue);

    return autocomplete(keyValue, NormdatenReferenz.PERSON_TYPE_NAME);
  }

  private List<NormdatenReferenz> autocomplete(String idOrName, String type) {
    if (Objects.isNull(idOrName) || idOrName.isBlank()) {
      return List.of();
    }

    return normdatenReferenzBoundary.findAllByIdOrNameAndType(idOrName, type, false)
        .stream()
        .sorted(Comparator.comparing(NormdatenReferenz::getName))
        .collect(Collectors.toList());
  }

}
