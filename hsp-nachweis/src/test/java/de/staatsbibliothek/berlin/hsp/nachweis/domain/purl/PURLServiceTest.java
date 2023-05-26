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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.purl;

import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp.HSP_DESCRIPTION;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp.HSP_DESCRIPTION_RETRO;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp.HSP_OBJECT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.konfiguration.KonfigurationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.purl.PURLGeneratorException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.purl.SBBPURLGenerator;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import java.net.URI;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 11.08.22
 */

@QuarkusTest
@TestTransaction
class PURLServiceTest {

  final String kodTargetTemplate = "https://handschriftenportal.de/search?hspobjectid={0}";
  final String beschreibungTargetTemplate = "https://handschriftenportal.de/workspace?type=hsp:description&id={0}";
  final String beschreibungRetroTargetTemplate = "https://handschriftenportal.de/workspace?type=hsp:description_retro&id={0}";

  @Inject
  PURLRepository purlRepository;

  @Inject
  KulturObjektDokumentRepository kulturObjektDokumentRepository;

  @Inject
  BeschreibungsRepository beschreibungsRepository;

  @Test
  void testCreateNewPURL() throws Exception {
    PURLService purlService = createPURLService();

    NullPointerException npe = Assertions.assertThrows(NullPointerException.class,
        () -> purlService.createNewPURL(null, null));
    assertEquals("dokumentId is required.", npe.getMessage());

    npe = Assertions.assertThrows(NullPointerException.class,
        () -> purlService.createNewPURL("HSP-9876", null));
    assertEquals("dokumentObjektTyp is required.", npe.getMessage());

    PURL purlKod = purlService.createNewPURL("HSP-123", HSP_OBJECT);
    assertNotNull(purlKod);
    assertNotNull(purlKod.getId());
    assertNotNull(purlKod.getPurl());
    assertNotNull(purlKod.getTarget());
    assertEquals("https://handschriftenportal.de/search?hspobjectid=HSP-123", purlKod.getTarget().toASCIIString());
    assertEquals(PURLTyp.INTERNAL, purlKod.getTyp());

    PURL purlBeschreibung = purlService.createNewPURL("HSP-123", DokumentObjektTyp.HSP_DESCRIPTION);
    assertNotNull(purlBeschreibung);
    assertNotNull(purlBeschreibung.getId());
    assertNotNull(purlBeschreibung.getPurl());
    assertNotNull(purlBeschreibung.getTarget());
    assertEquals("https://handschriftenportal.de/workspace?type=hsp:description&id=HSP-123",
        purlBeschreibung.getTarget().toASCIIString());
    assertEquals(PURLTyp.INTERNAL, purlBeschreibung.getTyp());

    PURL purlBeschreibungRetro = purlService.createNewPURL("HSP-123", DokumentObjektTyp.HSP_DESCRIPTION_RETRO);
    assertNotNull(purlBeschreibungRetro);
    assertNotNull(purlBeschreibungRetro.getId());
    assertNotNull(purlBeschreibungRetro.getPurl());
    assertNotNull(purlBeschreibungRetro.getTarget());
    assertEquals("https://handschriftenportal.de/workspace?type=hsp:description_retro&id=HSP-123",
        purlBeschreibungRetro.getTarget().toASCIIString());
    assertEquals(PURLTyp.INTERNAL, purlBeschreibungRetro.getTyp());

    PURL purlInternal = new PURL(URI.create("https://resolver.de/KOD-ID"), URI.create("https://target.de/KOD-ID"),
        PURLTyp.INTERNAL);
    KulturObjektDokument kod = new KulturObjektDokumentBuilder().withId("KOD-ID")
        .addPURL(purlInternal)
        .build();

    kulturObjektDokumentRepository.save(kod);

    PURLException peExists = Assertions.assertThrows(PURLException.class,
        () -> purlService.createNewPURL(kod.getId(), HSP_OBJECT));
    assertEquals("PURL vom Typ INTERNAL existiert bereits für Dokument KOD-ID.", peExists.getMessage());
  }

  @Test
  void testUpdateInternalPURLs() throws PURLException, PURLGeneratorException {
    PURLService purlService = createPURLService();

    kulturObjektDokumentRepository.save(createKOD());
    beschreibungsRepository.save(createBeschreibung("B-1", DokumentObjektTyp.HSP_DESCRIPTION));
    beschreibungsRepository.save(createBeschreibung("B-2", DokumentObjektTyp.HSP_DESCRIPTION_RETRO));

    Set<PURLViewModel> viewModels = purlRepository.findAllAsViewModels();

    assertNotNull(viewModels);
    assertEquals(3, viewModels.stream().map(PURLViewModel::getInternalPURLs).mapToLong(Set::size).sum());
    assertEquals(3, viewModels.stream().map(PURLViewModel::getExternalPURLs).mapToLong(Set::size).sum());

    assertFalse(viewModels.stream().map(PURLViewModel::getInternalPURLs)
        .flatMap(Set::stream)
        .map(PURL::getTarget)
        .map(URI::toASCIIString)
        .anyMatch(target -> target.startsWith("https://handschriftenportal.de/")));

    purlService.updateInternalPURLs(viewModels);

    Set<PURLViewModel> updatedViewModels = purlRepository.findAllAsViewModels();
    assertEquals(3, updatedViewModels.stream()
        .map(PURLViewModel::getInternalPURLs)
        .mapToLong(Set::size)
        .sum());

    assertEquals(1, updatedViewModels.stream().map(PURLViewModel::getInternalPURLs)
        .flatMap(Set::stream)
        .map(PURL::getTarget)
        .map(URI::toASCIIString)
        .filter(target -> MessageFormat.format(kodTargetTemplate, "K-1").equals(target))
        .count());

    assertEquals(1, updatedViewModels.stream().map(PURLViewModel::getInternalPURLs)
        .flatMap(Set::stream)
        .map(PURL::getTarget)
        .map(URI::toASCIIString)
        .filter(target -> MessageFormat.format(beschreibungTargetTemplate, "B-1").equals(target))
        .count());

    assertEquals(1, updatedViewModels.stream().map(PURLViewModel::getInternalPURLs)
        .flatMap(Set::stream)
        .map(PURL::getTarget)
        .map(URI::toASCIIString)
        .filter(target -> MessageFormat.format(beschreibungRetroTargetTemplate, "B-2").equals(target))
        .count());

    assertEquals(3, updatedViewModels.stream().map(PURLViewModel::getExternalPURLs)
        .flatMap(Set::stream)
        .map(PURL::getTarget)
        .map(URI::toASCIIString)
        .filter(target -> target.startsWith("https://target.de/"))
        .count());
  }

  @Test
  void testCreateTargetURI() throws PURLException, PURLGeneratorException {
    PURLService purlService = createPURLService();

    URI uri = purlService.createTargetURI(HSP_OBJECT, "HSP-1234");
    assertNotNull(uri);
    assertEquals("https://handschriftenportal.de/search?hspobjectid=HSP-1234",
        uri.toASCIIString());

    uri = purlService.createTargetURI(DokumentObjektTyp.HSP_DESCRIPTION, "HSP-1234");
    assertNotNull(uri);
    assertEquals("https://handschriftenportal.de/workspace?type=hsp:description&id=HSP-1234",
        uri.toASCIIString());

    uri = purlService.createTargetURI(DokumentObjektTyp.HSP_DESCRIPTION_RETRO, "HSP-1234");
    assertNotNull(uri);
    assertEquals("https://handschriftenportal.de/workspace?type=hsp:description_retro&id=HSP-1234",
        uri.toASCIIString());
  }

  @Test
  void testCreateDBMFile() throws PURLException, PURLGeneratorException {
    PURLService purlService = createPURLService();

    PURL purl = new PURL(URI.create("https://resolver.staatsbibliothek-berlin.de/HSP-123"),
        URI.create("https://hsp.de/HSP-123"), PURLTyp.INTERNAL);

    Set<PURL> response = purlService.createDBMFile(Set.of(purl));
    assertNotNull(response);
    assertEquals(1, response.size());
  }

  @Test
  void testGetTargetTemplate() throws PURLException, PURLGeneratorException {
    PURLService purlService = createPURLService();

    assertEquals(kodTargetTemplate, purlService.getTargetTemplate(HSP_OBJECT));
    assertEquals(beschreibungTargetTemplate, purlService.getTargetTemplate(HSP_DESCRIPTION));
    assertEquals(beschreibungRetroTargetTemplate, purlService.getTargetTemplate(HSP_DESCRIPTION_RETRO));
  }

  @Test
  void testUpdateTargetTemplate() throws PURLException, PURLGeneratorException {
    PURLService purlService = createPURLService();

    final String kodTargetTemplateNew = "https://neu.de/hspobjectid/{0}";
    purlService.updateTargetTemplate(HSP_OBJECT, kodTargetTemplateNew);
    assertEquals(kodTargetTemplateNew, purlService.getTargetTemplate(HSP_OBJECT));

    final String beschreibungTargetTemplateNew = "https://neu.de/hsp_description/{0}";
    purlService.updateTargetTemplate(HSP_DESCRIPTION, beschreibungTargetTemplateNew);
    assertEquals(beschreibungTargetTemplateNew, purlService.getTargetTemplate(HSP_DESCRIPTION));

    final String beschreibungRetroTargetTemplateNew = "https://neu.de/hsp_description_retro/{0}";
    purlService.updateTargetTemplate(HSP_DESCRIPTION_RETRO, beschreibungRetroTargetTemplateNew);
    assertEquals(beschreibungRetroTargetTemplateNew, purlService.getTargetTemplate(HSP_DESCRIPTION_RETRO));

    final String invalidTemplateNew = "https://invalid.de";
    PURLException purlException = assertThrows(PURLException.class,
        () -> purlService.updateTargetTemplate(HSP_OBJECT, invalidTemplateNew));
    assertEquals("Die URL-Vorlage https://invalid.de ist ungültig.", purlException.getMessage());
  }

  @Test
  void testIsTemplateValid() throws PURLException, PURLGeneratorException {
    PURLService purlService = createPURLService();

    assertFalse(purlService.isTemplateValid(null));
    assertFalse(purlService.isTemplateValid("no_url"));
    assertFalse(purlService.isTemplateValid("https://no_placeholder"));
    assertTrue(purlService.isTemplateValid("https://valid.template/{0}"));
  }

  @Test
  void testGetConfigKeyForDokumentObjektTyp() {
    assertEquals(PURLService.CONFIG_PURL_TARGET_KOD,
        PURLService.getConfigKeyForDokumentObjektTyp(HSP_OBJECT));
    assertEquals(PURLService.CONFIG_PURL_TARGET_BESCHREIBUNG,
        PURLService.getConfigKeyForDokumentObjektTyp(HSP_DESCRIPTION));
    assertEquals(PURLService.CONFIG_PURL_TARGET_BESCHREIBUNG_RETRO,
        PURLService.getConfigKeyForDokumentObjektTyp(HSP_DESCRIPTION_RETRO));
  }

  @Test
  void testCreatePURLMapWithBeschreibungsID() throws PURLGeneratorException, PURLException {

    PURLService purlService = createPURLService();
    Beschreibung beschreibung = createBeschreibung("BS3", HSP_DESCRIPTION);
    beschreibungsRepository.save(beschreibung);
    purlRepository.saveAll(beschreibung.getPURLs().stream().collect(Collectors.toList()));

    Map<String,PURL> purlMap = purlService.createPURLMapWithBeschreibungsID(Set.of("BS3"));

    assertNotNull(purlMap);
    assertEquals(1,purlMap.size());
  }

  KulturObjektDokument createKOD() {
    return new KulturObjektDokumentBuilder().withId("K-1")
        .addPURL(new PURL(URI.create("https://resolver.de/K-1"), URI.create("https://target.de/K-1"),
            PURLTyp.INTERNAL))
        .addPURL(new PURL(URI.create("https://external.de/K-1"), URI.create("https://target.de/K-1"),
            PURLTyp.EXTERNAL))
        .build();
  }

  Beschreibung createBeschreibung(String id, DokumentObjektTyp dokumentObjektTyp) {
    return new BeschreibungsBuilder()
        .withId(id)
        .withDokumentObjectTyp(dokumentObjektTyp)
        .addPURL(new PURL(URI.create("https://resolver.de/" + id), URI.create("https://target.de/" + id),
            PURLTyp.INTERNAL))
        .addPURL(new PURL(URI.create("https://external.de/" + id), URI.create("https://target.de/" + id),
            PURLTyp.EXTERNAL))
        .build();
  }

  PURLService createPURLService() throws PURLException, PURLGeneratorException {
    PURLResolverRepository purlResolverRepositoryMock = mock(PURLResolverRepository.class);
    when(purlResolverRepositoryMock.createDBMFile(anySet())).thenAnswer(i -> i.getArguments()[0]);

    SBBPURLGenerator sbbPURLGeneratorMock = mock(SBBPURLGenerator.class);
    when(sbbPURLGeneratorMock.createNewPURL(any(URI.class)))
        .thenAnswer(i ->
            new PURL(URI.create("https://resolver.staatsbibliothek-berlin.de/HSP" + System.currentTimeMillis()),
                (URI) i.getArguments()[0], PURLTyp.INTERNAL));

    Map<String, String> configMap = new HashMap<>();
    KonfigurationBoundary konfigurationBoundaryMock = mock(KonfigurationBoundary.class);

    doAnswer(i -> configMap.put((String) i.getArguments()[0], (String) i.getArguments()[1]))
        .when(konfigurationBoundaryMock).setWert(anyString(), anyString());

    when(konfigurationBoundaryMock.getWert(anyString()))
        .thenAnswer(i -> Optional.ofNullable(configMap.get((String) i.getArguments()[0])));

    return new PURLService(sbbPURLGeneratorMock,
        purlRepository,
        purlResolverRepositoryMock,
        konfigurationBoundaryMock,
        kodTargetTemplate,
        beschreibungTargetTemplate,
        beschreibungRetroTargetTemplate);
  }

}
