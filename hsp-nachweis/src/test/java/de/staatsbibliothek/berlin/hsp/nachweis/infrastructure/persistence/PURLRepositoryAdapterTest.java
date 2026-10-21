package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLViewModel;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
@Execution(ExecutionMode.SAME_THREAD)
class PURLRepositoryAdapterTest {

  @Inject
  PURLRepositoryAdapter repository;

  @Inject
  KulturObjektDokumentRepository kodRepository;

  @Inject
  BeschreibungsRepository beschreibungsRepository;

  @Test
  @TestTransaction
  void testFindAllAsViewModels() {
    KulturObjektDokument kod_1 = new KulturObjektDokumentBuilder()
        .withId("k_1")
        .addPURL(
            createPURL("https://resolver.test/k_1", "https://local.test/k_1", PURLTyp.INTERNAL))
        .addPURL(createPURL("https://doi.test/k_1", "https://doi.test/k_1", PURLTyp.EXTERNAL))
        .build();
    kodRepository.save(kod_1);

    KulturObjektDokument kod_2 = new KulturObjektDokumentBuilder()
        .withId("k_2")
        .build();
    kodRepository.save(kod_2);

    Publikation publikation1 = new Publikation.PublikationBuilder()
        .addPURL(createPURL("https://nbn-resolving.de/urn:nbn:de:b_1",
            "https://nbn-resolving.de/urn:nbn:de:b_1", PURLTyp.EXTERNAL))
        .build();
    Beschreibung beschreibung_1 = new BeschreibungsBuilder("b_1", "k_10", null)
        .withDokumentObjectTyp(DokumentObjektTyp.HSP_DESCRIPTION)
        .withPublikation(publikation1)
        .build();
    beschreibungsRepository.save(beschreibung_1);

    Beschreibung beschreibung_2 = new BeschreibungsBuilder("b_2", "k_20", null)
        .withDokumentObjectTyp(DokumentObjektTyp.HSP_DESCRIPTION)
        .build();
    beschreibungsRepository.save(beschreibung_2);

    Publikation publikation3 = new Publikation.PublikationBuilder()
        .addPURL(createPURL("https://nbn-resolving.de/urn:nbn:de:b_3",
            "https://nbn-resolving.de/urn:nbn:de:b_3", PURLTyp.EXTERNAL))
        .addPURL(
            createPURL("https://resolver.test/b_3", "https://local.test/b_3", PURLTyp.INTERNAL))
        .build();
    Beschreibung beschreibung_3 = new BeschreibungsBuilder("b_3", "k_30", null)
        .withDokumentObjectTyp(DokumentObjektTyp.HSP_DESCRIPTION_RETRO)
        .withPublikation(publikation3)
        .build();
    beschreibungsRepository.save(beschreibung_3);

    List<PURLViewModel> result = repository.findAllAsViewModels();
    assertNotNull(result);
    assertEquals(5, result.size());

    checkPURLViewModel(result, "k_1", DokumentObjektTyp.HSP_OBJECT, 1, 1);
    checkPURLViewModel(result, "k_2", DokumentObjektTyp.HSP_OBJECT, 0, 0);
    checkPURLViewModel(result, "b_1", DokumentObjektTyp.HSP_DESCRIPTION, 0, 1);
    checkPURLViewModel(result, "b_2", DokumentObjektTyp.HSP_DESCRIPTION, 0, 0);
    checkPURLViewModel(result, "b_3", DokumentObjektTyp.HSP_DESCRIPTION_RETRO, 1, 1);

    PURLViewModel viewModelKod1 = result.stream()
        .filter(pvm -> "k_1".equals(pvm.getDokumentId()))
        .findFirst()
        .orElse(null);

    assertNotNull(viewModelKod1);

    assertTrue(viewModelKod1.getInternalPURLs().stream()
        .anyMatch(purl -> Objects.nonNull(purl.getPurl())
            && "https://resolver.test/k_1".equals(purl.getPurl().toASCIIString())
            && Objects.nonNull(purl.getTarget())
            && "https://local.test/k_1".equals(purl.getTarget().toASCIIString())
            && PURLTyp.INTERNAL == purl.getTyp()));

    assertTrue(viewModelKod1.getExternalPURLs().stream()
        .anyMatch(purl -> Objects.nonNull(purl.getPurl())
            && "https://doi.test/k_1".equals(purl.getPurl().toASCIIString())
            && Objects.nonNull(purl.getTarget())
            && "https://doi.test/k_1".equals(purl.getTarget().toASCIIString())
            && PURLTyp.EXTERNAL == purl.getTyp()));
  }

  @Test
  @TestTransaction
  void testFindByPurlOptional() {
    PURL purl = createPURL("https://resolver.test/b_1", "https://nbn-resolving.de/urn:nbn:de:b_1",
        PURLTyp.INTERNAL);

    repository.save(purl);

    Optional<PURL> purlFound = repository.findByPurlOptional(purl.getPurl());
    assertTrue(purlFound.isPresent());
    assertEquals(purl, purlFound.get());

    Optional<PURL> purlNotFound = repository.findByPurlOptional(purl.getTarget());
    assertFalse(purlNotFound.isPresent());
  }

  @Test
  @TestTransaction
  void testFindInternalByDokumentId() {
    KulturObjektDokument kod_1 = new KulturObjektDokumentBuilder()
        .withId("k_1")
        .addPURL(
            createPURL("https://resolver.test/k_1", "https://local.test/k_1", PURLTyp.INTERNAL))
        .addPURL(createPURL("https://doi.test/k_1", "https://local.test/k_1", PURLTyp.EXTERNAL))
        .build();
    kodRepository.save(kod_1);

    KulturObjektDokument kod_2 = new KulturObjektDokumentBuilder()
        .withId("k_2")
        .build();
    kodRepository.save(kod_2);

    Publikation publikationB1 = new Publikation.PublikationBuilder()
        .addPURL(createPURL("https://nbn-resolving.de/urn:nbn:de:b_1", "https://local.test/b_1",
            PURLTyp.EXTERNAL))
        .build();
    Beschreibung beschreibung_1 = new BeschreibungsBuilder("b_1", "k_10", null)
        .withPublikation(publikationB1)
        .build();
    beschreibungsRepository.save(beschreibung_1);

    Beschreibung beschreibung_2 = new BeschreibungsBuilder("b_2", "k_20", null)
        .build();
    beschreibungsRepository.save(beschreibung_2);

    Publikation publikationB3 = new Publikation.PublikationBuilder()
        .addPURL(
            createPURL("https://resolver.test/b_3", "https://local.test/b_3", PURLTyp.INTERNAL))
        .addPURL(createPURL("https://doi.test/b_3", "https://local.test/b_3", PURLTyp.EXTERNAL))
        .build();
    Beschreibung beschreibung_3 = new BeschreibungsBuilder("b_3", "k_30", null)
        .withPublikation(publikationB3)
        .build();
    beschreibungsRepository.save(beschreibung_3);

    assertTrue(repository.findByDokumentIdAndPURLTyp("k_2", PURLTyp.INTERNAL).isEmpty());
    assertTrue(repository.findByDokumentIdAndPURLTyp("k_2", PURLTyp.EXTERNAL).isEmpty());
    assertTrue(repository.findByDokumentIdAndPURLTyp("b_1", PURLTyp.INTERNAL).isEmpty());
    assertTrue(repository.findByDokumentIdAndPURLTyp("b_2", PURLTyp.INTERNAL).isEmpty());
    assertTrue(repository.findByDokumentIdAndPURLTyp("b_2", PURLTyp.EXTERNAL).isEmpty());

    assertPURLFound("https://resolver.test/k_1",
        repository.findByDokumentIdAndPURLTyp("k_1", PURLTyp.INTERNAL));
    assertPURLFound("https://doi.test/k_1",
        repository.findByDokumentIdAndPURLTyp("k_1", PURLTyp.EXTERNAL));
    assertPURLFound("https://nbn-resolving.de/urn:nbn:de:b_1",
        repository.findByDokumentIdAndPURLTyp("b_1", PURLTyp.EXTERNAL));
    assertPURLFound("https://resolver.test/b_3",
        repository.findByDokumentIdAndPURLTyp("b_3", PURLTyp.INTERNAL));
    assertPURLFound("https://doi.test/b_3",
        repository.findByDokumentIdAndPURLTyp("b_3", PURLTyp.EXTERNAL));
  }

  private PURL createPURL(String purl, String target, PURLTyp typ) {
    return new PURL(URI.create(purl), URI.create(target), typ);
  }

  private void checkPURLViewModel(List<PURLViewModel> models, String documentId,
      DokumentObjektTyp dokumentObjektTyp,
      int purlsInternal, int purlsExternal) {
    assertTrue(models.stream().anyMatch(pvm -> documentId.equals(pvm.getDokumentId())
        && dokumentObjektTyp == pvm.getDokumentObjektTyp()
        && Objects.nonNull(pvm.getInternalPURLs())
        && purlsInternal == pvm.getInternalPURLs().size()
        && Objects.nonNull(pvm.getExternalPURLs())
        && purlsExternal == pvm.getExternalPURLs().size()));
  }

  private void assertPURLFound(String purlUrl, Set<PURL> purls) {
    assertNotNull(purls);
    assertEquals(1, purls.size());
    assertTrue(purls.stream()
        .anyMatch(purl -> Objects.nonNull(purl.getPurl()) && Objects.equals(purlUrl,
            purl.getPurl().toASCIIString())));
  }

}
