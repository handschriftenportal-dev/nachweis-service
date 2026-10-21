package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mockito;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 12.03.21
 */
@QuarkusTest
@Execution(ExecutionMode.SAME_THREAD)
class BeschreibungsServiceIT implements Serializable {

  private static final Identifikation IDENTIFIKATION =
      new Identifikation.IdentifikationBuilder()
          .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
          .withIdent("Cod. Donaueschingen 116")
          .build();

  @Serial
  private static final long serialVersionUID = -3437793236866254614L;

  private static final LocalDateTime ERSTELL_DATUM = LocalDateTime.now();

  @Inject
  @SessionScoped
  BeschreibungsService boundary;

  @Inject
  BeschreibungsRepository beschreibungsRepository;

  @Inject
  BearbeiterRepository bearbeiterRepository;

  @Inject
  KulturObjektDokumentRepository kulturObjektDokumentRepository;

  DokumentSperreBoundary sperreBoundary = Mockito.mock(DokumentSperreBoundary.class);

  private Bearbeiter bearbeiter = new Bearbeiter("1", "Unbekannter Tester");

  @Test
  @TestTransaction
  void testupdateBeschreibungMitXML() throws DokumentSperreException {

    boundary.setDokumentSperreBoundary(sperreBoundary);

    Path teiFilePath = Path.of("src", "test", "resources", "tei-code116.xml");
    final String id = "HSP-87332024-06c1-3e6a-b565-cc502dab0944";
    final String kodID = String.valueOf(Math.abs(new Random().nextInt()));

    storeInNewTransaction(id, kodID);

    updateBeschreibungMitXMLInNewThread(teiFilePath, id);

    beschreibungsRepository
        .findByIdOptional(id)
        .ifPresent(
            beschreibung -> {
              assertEquals(id, beschreibung.getId());
              assertEquals(VerwaltungsTyp.INTERN, beschreibung.getVerwaltungsTyp());
              assertEquals(1, beschreibung.getAltIdentifier().size());
              assertEquals(
                  "someBib_obj-31575572_tei-msDesc",
                  beschreibung.getAltIdentifier().iterator().next());
              assertEquals(
                  "Cod. Donaueschingen 116",
                  beschreibung.getGueltigeIdentifikation().get().getIdent());
              beschreibung.getBeschreibungsStruktur().stream()
                  .filter(
                      globaleBeschreibungsKomponente ->
                          globaleBeschreibungsKomponente instanceof BeschreibungsKomponenteKopf)
                  .findFirst()
                  .ifPresent(
                      kopf -> {
                        assertEquals(0, kopf.getIdentifikationen().size());
                      });
            });
  }

  @Transactional(TxType.REQUIRES_NEW)
  void updateBeschreibungMitXMLInNewThread(Path teiFilePath, String id)
      throws RuntimeException, DokumentSperreException {
    try {
      boundary.updateBeschreibungMitXML(id, Files.readString(teiFilePath),
          bearbeiter.getBearbeitername(), true,
          Locale.GERMAN);
    } catch (BeschreibungsException | IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Transactional(TxType.REQUIRES_NEW)
  void storeInNewTransaction(String id, String kodID) {
    bearbeiterRepository.saveAndFlush(bearbeiter);
    Beschreibung code116 =
        new BeschreibungsBuilder(id, kodID, IDENTIFIKATION)
            .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
            .withBearbeiterIds(bearbeiter)
            .build();

    String code116Id = code116.getId();
    Beschreibung inRepository = beschreibungsRepository.findById(code116Id);
    if (inRepository != null) {
      beschreibungsRepository.deleteByIdAndFlush(code116Id);
    }
    beschreibungsRepository.persistAndFlush(code116);
  }

  @RepeatedTest(4)
  @TestTransaction
  void testDelete() throws BeschreibungsException, DokumentSperreException {
    boundary.setDokumentSperreBoundary(sperreBoundary);
    String kodId = UUID.randomUUID().toString();
    String beschreibungsId = UUID.randomUUID().toString();

    Optional<Beschreibung> optionalBeschreibung = boundary.findById(beschreibungsId);
    assertFalse(optionalBeschreibung.isPresent());

    createtestBeschreibungAndKod(beschreibungsId, kodId);
    optionalBeschreibung = boundary.findById(beschreibungsId);
    assertTrue(optionalBeschreibung.isPresent());

    boundary.delete(new Bearbeiter("1", "Unbekannter Tester"), optionalBeschreibung.get());
    optionalBeschreibung = boundary.findById(beschreibungsId);
    assertFalse(optionalBeschreibung.isPresent());
  }

  @Transactional(TxType.REQUIRES_NEW)
  void createtestBeschreibungAndKod(String beschreibungsId, String kodId) {
    NormdatenReferenz koerperschaft =
        new NormdatenReferenz(UUID.randomUUID().toString(), "Czarnecki LTD", "DE-007");
    NormdatenReferenz ort = new NormdatenReferenz(UUID.randomUUID().toString(), "Berlin", "DE-008");
    Identifikation identifikationBeschreibung =
        new IdentifikationBuilder()
            .withId(UUID.randomUUID().toString())
            .withAufbewahrungsOrt(ort)
            .withBesitzer(koerperschaft)
            .withIdent("Chaka")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();
    BeschreibungsKomponenteKopf kopf =
        new BeschreibungsKomponenteKopfBuilder()
            .withId(UUID.randomUUID().toString())
            .withTitel("Beschreibung Titel")
            .build();
    Beschreibung beschreibung =
        new BeschreibungsBuilder(beschreibungsId, kodId, identifikationBeschreibung)
            .withErstellungsDatum(ERSTELL_DATUM)
            .withDokumentObjectTyp(DokumentObjektTyp.HSP_DESCRIPTION)
            .build();
    beschreibung.getBeschreibungsStruktur().add(kopf);

    Identifikation identifikationKOD =
        new IdentifikationBuilder()
            .withId(UUID.randomUUID().toString())
            .withAufbewahrungsOrt(ort)
            .withBesitzer(koerperschaft)
            .withIdent("Chaka")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();
    KulturObjektDokument kulturObjektDokument =
        new KulturObjektDokumentBuilder()
            .withId(kodId)
            .withGueltigerIdentifikation(identifikationKOD)
            .withGndIdentifier("eeee")
            .withRegistrierungsDatum(ERSTELL_DATUM)
            .build();
    beschreibungsRepository.save(beschreibung);
    kulturObjektDokumentRepository.save(kulturObjektDokument);
  }
}
