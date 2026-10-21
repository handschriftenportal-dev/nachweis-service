package de.staatsbibliothek.berlin.hsp.nachweis.domain.purl;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.events.KulturObjektDokumentRegistriertEvent;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.events.PublikationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PURLResolverFileSchedulerTest {

  private PURLBoundary purlBoundary;
  private PURLResolverFileScheduler scheduler;

  @BeforeEach
  void setUp() {
    purlBoundary = mock(PURLBoundary.class);
    scheduler = new PURLResolverFileScheduler(purlBoundary);
  }

  @Test
  @DisplayName("generates on first run because updateNeeded defaults to true")
  void testGeneratesOnFirstRun() throws Exception {
    scheduler.createPURLResolverFile();

    verify(purlBoundary, times(1)).resolverDateiGenerieren();
  }

  @Test
  @DisplayName("does not regenerate on a subsequent run when nothing changed")
  void testSkipsWhenUpToDate() throws Exception {
    scheduler.createPURLResolverFile();
    scheduler.createPURLResolverFile();

    verify(purlBoundary, times(1)).resolverDateiGenerieren();
  }

  @Test
  @DisplayName("PublikationEvent marks the file stale so the next run regenerates")
  void testEventTriggersRegeneration() throws Exception {
    scheduler.createPURLResolverFile();
    scheduler.onEvent(new PublikationEvent());
    scheduler.createPURLResolverFile();
    // next line should not call purlBoundary.resolverDateiGenerieren()
    scheduler.createPURLResolverFile();

    verify(purlBoundary, times(2)).resolverDateiGenerieren();
  }

  @Test
  @DisplayName("KulturObjektDokumentRegistriertEvent marks the file stale so the next run regenerates")
  void testKODRegistriertEventTriggersRegeneration() throws Exception {
    scheduler.createPURLResolverFile();
    scheduler.onEvent(new KulturObjektDokumentRegistriertEvent());
    scheduler.createPURLResolverFile();
    // next line should not call purlBoundary.resolverDateiGenerieren()
    scheduler.createPURLResolverFile();

    verify(purlBoundary, times(2)).resolverDateiGenerieren();
  }

  @Test
  @DisplayName("keeps updateNeeded set after a failure so the next run retries")
  void testRetriesAfterFailure() throws Exception {
    doThrow(new PURLException("boom")).when(purlBoundary).resolverDateiGenerieren();

    scheduler.createPURLResolverFile();
    scheduler.createPURLResolverFile();

    verify(purlBoundary, times(2)).resolverDateiGenerieren();
  }

  @Test
  @DisplayName("onEvent does not generate by itself, only flags for the next scheduled run")
  void testOnEventDoesNotGenerateImmediately() {
    scheduler.onEvent(new PublikationEvent());

    verifyNoInteractions(purlBoundary);
  }
}
