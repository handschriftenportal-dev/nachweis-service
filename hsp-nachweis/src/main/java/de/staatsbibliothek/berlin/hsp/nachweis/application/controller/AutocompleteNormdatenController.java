package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.person.PersonFilter;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.person.PersonGraphQLClient;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 07.10.2020.
 * @version 1.0
 */

@ViewScoped
@Named
@Slf4j
public class AutocompleteNormdatenController implements Serializable {

  @Serial
  private static final long serialVersionUID = -7373703447572273448L;

  private final transient NormdatenReferenzBoundary normdatenReferenzBoundary;
  private final transient PersonGraphQLClient personGraphQLClient;

  @Inject
  public AutocompleteNormdatenController(
      NormdatenReferenzBoundary normdatenReferenzBoundary,
      PersonGraphQLClient personGraphQLClient
  ) {
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
    this.personGraphQLClient = personGraphQLClient;
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

    if (StringUtils.isBlank(keyValue)) {
      return List.of();
    }

    var filter = PersonFilter
        .builder()
        .name(keyValue)
        .build();

    return personGraphQLClient.findPersons(20, 1, filter)
        .getItems()
        .stream()
        .map(
            p -> new NormdatenReferenz(p.getId(), p.getPreferredName(), p.getGndIdentifier(),
                NormdatenReferenz.PERSON_TYPE_NAME))
        .sorted(Comparator.comparing(NormdatenReferenz::getName))
        .toList();
  }

  private List<NormdatenReferenz> autocomplete(String idOrName, String type) {
    if (Objects.isNull(idOrName) || idOrName.isBlank()) {
      return List.of();
    }

    return normdatenReferenzBoundary.findAllByIdOrNameAndType(idOrName, type, false)
        .stream()
        .sorted(Comparator.comparing(NormdatenReferenz::getName))
        .toList();
  }

}
