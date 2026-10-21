package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.stork;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import io.smallrye.stork.api.NoServiceInstanceFoundException;
import io.smallrye.stork.api.ServiceInstance;
import java.net.URI;
import org.junit.jupiter.api.Test;

class StorkDiscoveryTest {

  private final StorkDiscovery storkDiscovery = new StorkDiscovery();

  @Test
  void resolveUri_withNonStorkScheme_returnsOriginal() {
    URI uri = URI.create("http://example.com/foo");
    assertEquals(uri, storkDiscovery.resolveUri(uri));
  }

  @Test
  void resolveUri_withNullUri_returnsNull() {
    assertEquals(null, storkDiscovery.resolveUri(null));
  }

  @Test
  void resolveUri_withTypoScheme_returnsOriginal() {
    URI uri = URI.create("storkk://importservice/rest");
    assertEquals(uri, storkDiscovery.resolveUri(uri));
  }

  @Test
  void resolveUri_withUnrelatedSchemeStartingWithStork_returnsOriginal() {
    URI uri = URI.create("storky://importservice/rest");
    assertEquals(uri, storkDiscovery.resolveUri(uri));
  }

  @Test
  void resolveUri_withBlankHost_returnsOriginal() {
    URI uri = URI.create("stork:///rest");
    assertEquals(uri, storkDiscovery.resolveUri(uri));
  }

  @Test
  void resolveUri_happyPath_returnsResolved() {
    StorkDiscovery spy = spy(new StorkDiscovery());
    doReturn(instance("b-dev1047.pk.de", 9296, false)).when(spy).selectInstance("importservice");

    URI resolved = spy.resolveUri(URI.create("stork://importservice/rest/import/job"));

    assertEquals("http://b-dev1047.pk.de:9296/rest/import/job", resolved.toASCIIString());
  }

  @Test
  void resolveUri_storkLookupFails_returnsOriginal() {
    StorkDiscovery spy = spy(new StorkDiscovery());
    doThrow(new NoServiceInstanceFoundException("no instance"))
        .when(spy).selectInstance("importservice");

    URI uri = URI.create("stork://importservice/rest");
    URI resolved = spy.resolveUri(uri);

    assertSame(uri, resolved);
  }

  @Test
  void resolveUri_storkLookupFailsWithRuntimeException_returnsOriginal() {
    StorkDiscovery spy = spy(new StorkDiscovery());
    doThrow(new IllegalStateException("Stork not initialized"))
        .when(spy).selectInstance("importservice");

    URI uri = URI.create("storks://importservice");
    URI resolved = spy.resolveUri(uri);

    assertSame(uri, resolved);
  }

  @Test
  void createResolvedUri_storkScheme_resolvesToHttp() {
    ServiceInstance instance = instance("b-dev1047.pk.de", 9296, false);
    URI resolved = storkDiscovery.createResolvedUri(URI.create("stork://importservice"), instance);
    assertEquals("http://b-dev1047.pk.de:9296", resolved.toASCIIString());
  }

  @Test
  void createResolvedUri_storksScheme_resolvesToHttps() {
    ServiceInstance instance = instance("b-dev1047.pk.de", 9296, false);
    URI resolved = storkDiscovery.createResolvedUri(URI.create("storks://importservice"), instance);
    assertEquals("https://b-dev1047.pk.de:9296", resolved.toASCIIString());
  }

  @Test
  void createResolvedUri_secureInstance_resolvesToHttps() {
    ServiceInstance instance = instance("b-dev1047.pk.de", 9296, true);
    URI resolved = storkDiscovery.createResolvedUri(URI.create("stork://importservice"), instance);
    assertEquals("https://b-dev1047.pk.de:9296", resolved.toASCIIString());
  }

  @Test
  void createResolvedUri_preservesPath() {
    ServiceInstance instance = instance("b-dev1047.pk.de", 9296, false);
    URI resolved = storkDiscovery.createResolvedUri(
        URI.create("stork://importservice/rest/import/job"), instance);
    assertEquals("http://b-dev1047.pk.de:9296/rest/import/job", resolved.toASCIIString());
  }

  @Test
  void createResolvedUri_preservesQueryAndFragment() {
    ServiceInstance instance = instance("b-dev1047.pk.de", 9296, false);
    URI resolved = storkDiscovery.createResolvedUri(
        URI.create("stork://importservice/rest?foo=bar&baz=qux#anchor"), instance);
    assertEquals("http://b-dev1047.pk.de:9296/rest?foo=bar&baz=qux#anchor",
        resolved.toASCIIString());
  }

  @Test
  void createResolvedUri_storksWithPath_resolvesHttpsWithPath() {
    ServiceInstance instance = instance("b-dev1047.pk.de", 9296, false);
    URI resolved = storkDiscovery.createResolvedUri(
        URI.create("storks://importservice/rest/tei-xml/validate"), instance);
    assertEquals("https://b-dev1047.pk.de:9296/rest/tei-xml/validate", resolved.toASCIIString());
  }

  @Test
  void createResolvedUri_secureInstanceWithPath_resolvesHttpsWithPath() {
    ServiceInstance instance = instance("b-dev1047.pk.de", 9296, true);
    URI resolved = storkDiscovery.createResolvedUri(
        URI.create("stork://importservice/rest/import/job"), instance);
    assertEquals("https://b-dev1047.pk.de:9296/rest/import/job", resolved.toASCIIString());
  }

  @Test
  void createResolvedUri_secureInstanceWithQueryAndFragment_resolvesHttps() {
    ServiceInstance instance = instance("b-dev1047.pk.de", 9296, true);
    URI resolved = storkDiscovery.createResolvedUri(
        URI.create("stork://importservice/rest?foo=bar&baz=qux#anchor"), instance);
    assertEquals("https://b-dev1047.pk.de:9296/rest?foo=bar&baz=qux#anchor",
        resolved.toASCIIString());
  }

  @Test
  void createResolvedUri_secureInstanceAndStorksScheme_resolvesHttps() {
    ServiceInstance instance = instance("b-dev1047.pk.de", 9296, true);
    URI resolved = storkDiscovery.createResolvedUri(
        URI.create("storks://importservice/rest"), instance);
    assertEquals("https://b-dev1047.pk.de:9296/rest", resolved.toASCIIString());
  }

  private ServiceInstance instance(String host, int port, boolean secure) {
    ServiceInstance instance = mock(ServiceInstance.class);
    when(instance.getHost()).thenReturn(host);
    when(instance.getPort()).thenReturn(port);
    when(instance.isSecure()).thenReturn(secure);
    return instance;
  }
}
