package de.staatsbibliothek.berlin.hsp.nachweis.domain.purl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLConfiguration.PURLResolverConfiguration;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class PURLConfigurationTest {

  @Inject
  PURLConfiguration purlConfiguration;

  @Test
  void testResolverDefaults() {
    PURLResolverConfiguration resolver = purlConfiguration.resolver();

    // not set in application.yml -> @WithDefault values
    assertEquals("hsp_purl.txt", resolver.txtFileName());
    assertEquals("hsp_purl.dbm", resolver.dbmFileName());
    assertEquals("/tmp/test/", resolver.directory());
    assertEquals("httxt2dbm -f DB -i {0} -o {1}", resolver.command());
    assertEquals("https://resolver.staatsbibliothek-berlin.de/", resolver.url());
    assertFalse(resolver.enabled());
  }

  @Test
  void testAutogenerateEnabled() {
    assertTrue(purlConfiguration.autogenerate().enabled());
  }
}
