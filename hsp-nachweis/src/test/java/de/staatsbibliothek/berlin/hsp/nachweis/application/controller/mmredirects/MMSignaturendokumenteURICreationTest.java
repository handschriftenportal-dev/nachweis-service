package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 19.10.23.
 * @version 1.0
 */
public class MMSignaturendokumenteURICreationTest {

  @Test
  void testextractMinimalDatenFromPath() {

    MMSignaturendokumenteURICreation mmSignaturendokumenteURICreation = new MMSignaturendokumenteURICreation(
        null);

    assertEquals(3, mmSignaturendokumenteURICreation.extractMinimalDatenFromPath(
        "dokumente/html/sigrefsAdmont|||Stiftsarchiv|||Ms. 106").length);
  }

  @Test
  void testCreate() {
    KulturObjektDokumentRepository kulturObjektDokumentRepository = Mockito.mock(
        KulturObjektDokumentRepository.class);

    Identifikation gueltige = new Identifikation.IdentifikationBuilder().withBesitzer(
            new NormdatenReferenz.NormdatenReferenzBuilder().withName("Stiftsarchiv").withGndID("")
                .build()).withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR).withAufbewahrungsOrt(
            new NormdatenReferenz.NormdatenReferenzBuilder().withName("Admont").withGndID("").build())
        .build();

    when(kulturObjektDokumentRepository.findByIdentifikationIdent("Ms. 106")).thenReturn(
        new ArrayList<>(List.of(
            (new KulturObjektDokumentBuilder().withGueltigerIdentifikation(gueltige)
                .withId("HSP123").build()))));

    when(kulturObjektDokumentRepository.findByAlternativeIdentifikationIdent("Ms. 106")).thenReturn(
        List.of());

    MMSignaturendokumenteURICreation mmSignaturendokumenteURICreation = new MMSignaturendokumenteURICreation(
        kulturObjektDokumentRepository);

    assertEquals("https://handschriftenportal.de/?id=HSP123",
        mmSignaturendokumenteURICreation.create(
            "dokumente/html/sigrefsAdmont|||Stiftsarchiv|||Ms. 106").toString());
  }
}
