package de.staatsbibliothek.berlin.hsp.nachweis.application.model.normdatenerkennung;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung.NormdatenErkennungEintrag;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.Treffer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.normdatenerkennung.NormdatenErkennungErgebnis;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link NormdatenErkennungAuftragErgebnisViewModel#isNoResults()}.
 *
 * <p>Test strategy:
 * <ul>
 *   <li>The {@code isNoResults()} method is a thin accessor over the {@code treffer}
 *       list built in the constructor from {@link NormdatenErkennungEintrag#getErgebnis()}.</li>
 *   <li>Coverage focuses on the four meaningful states of the underlying ergebnis:
 *       (a) no ergebnis at all, (b) ergebnis with neither empfehlung nor treffer,
 *       (c) ergebnis with only an empfehlung, (d) ergebnis with only treffer.</li>
 *   <li>{@link Treffer} is mocked because it is a record carrying nested types we do not
 *       need to construct; the view model only touches {@code getGndEntityFact()} and
 *       {@code getErklaerung()}, both of which return {@code null} on the mock and are
 *       handled gracefully by the view model.</li>
 * </ul>
 */
class NormdatenErkennungAuftragErgebnisViewModelTest {

  private static NormdatenErkennungEintrag newResponse() {
    return new NormdatenErkennungEintrag(
        "//tei:persName", NormdatenTyp.Person, "Goethe");
  }

  @Nested
  @DisplayName("isNoResults() returns true")
  class NoResults {

    @Test
    @DisplayName("when the response has no ergebnis at all")
    void isNoResults_true_whenNoErgebnis() {
      var response = newResponse();

      var vm = new NormdatenErkennungAuftragErgebnisViewModel(response);

      assertTrue(vm.isNoResults());
      assertTrue(vm.getTreffer().isEmpty());
    }

    @Test
    @DisplayName("when ergebnis has null empfehlung and an empty treffer list")
    void isNoResults_true_whenBothTrefferAndEmpfehlungAreEmpty() {
      var response = newResponse();
      response.setErgebnis(new NormdatenErkennungErgebnis(Collections.emptyList(), null));

      var vm = new NormdatenErkennungAuftragErgebnisViewModel(response);

      assertTrue(vm.isNoResults());
      assertTrue(vm.getTreffer().isEmpty());
    }
  }

  @Nested
  @DisplayName("isNoResults() returns false")
  class HasResults {

    @Test
    @DisplayName("when only an empfehlung is present")
    void isNoResults_false_whenEmpfehlungPresent() {
      var empfehlung = mock(Treffer.class);
      var response = newResponse();
      response.setErgebnis(new NormdatenErkennungErgebnis(Collections.emptyList(), empfehlung));

      var vm = new NormdatenErkennungAuftragErgebnisViewModel(response);

      assertTrue(!vm.isNoResults());
      assertEquals(1, vm.getTreffer().size());
    }

    @Test
    @DisplayName("when treffer list is non-empty (no empfehlung)")
    void isNoResults_false_whenTrefferPresent() {
      var treffer = mock(Treffer.class);
      var response = newResponse();
      response.setErgebnis(new NormdatenErkennungErgebnis(List.of(treffer), null));

      var vm = new NormdatenErkennungAuftragErgebnisViewModel(response);

      assertFalse(vm.isNoResults());
      assertEquals(1, vm.getTreffer().size());
    }
  }
}
