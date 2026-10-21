package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.purl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLConfiguration;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLConfiguration.PURLResolverConfiguration;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 04.08.22
 */

class SBBPURLGeneratorTest {

  SBBPURLGenPort sbbPURLGenPortMock;
  String configuredUrlPrefix = "https://resolver.staatsbibliothek-berlin.de/";
  URI target = URI.create("http://target.url/documentId");

  @BeforeEach
  void init() {
    sbbPURLGenPortMock = Mockito.mock(SBBPURLGenPort.class);
  }

  @Test
  void testCreateNewPURL() throws PURLGeneratorException {

    when(sbbPURLGenPortMock.createPURL(SBBPURLGenPort.PARAM_PID, SBBPURLGenPort.PARAM_MVID))
        .thenReturn(createResponse());

    SBBPURLGenerator generator = new SBBPURLGenerator(purlConfiguration(),
        sbbPURLGenPortMock);

    PURL purl = generator.createNewPURL(target);
    assertNotNull(purl);
    assertNotNull(purl.getPurl());
    assertEquals(target, purl.getTarget());
    assertEquals(PURLTyp.INTERNAL, purl.getTyp());
  }

  @Test
  void testCreateNewPURL_GenPortError() {

    when(sbbPURLGenPortMock.createPURL(SBBPURLGenPort.PARAM_PID, SBBPURLGenPort.PARAM_MVID))
        .thenThrow(new RuntimeException("TestException"));

    SBBPURLGenerator generator = new SBBPURLGenerator(purlConfiguration(),
        sbbPURLGenPortMock);

    PURLGeneratorException exception = assertThrows(PURLGeneratorException.class, () ->
        generator.createNewPURL(target));
    assertEquals("Error calling sbbPURLGenPort!", exception.getMessage());
  }

  @Test
  void testCreatePID() throws PURLGeneratorException {

    SBBPURLGenerator generator = new SBBPURLGenerator(purlConfiguration(),
        sbbPURLGenPortMock);

    NullPointerException npe = assertThrows(NullPointerException.class, generator::createPID);
    assertEquals("Calling sbbPURLGenPort returned no response!", npe.getMessage());

    when(sbbPURLGenPortMock.createPURL(SBBPURLGenPort.PARAM_PID, SBBPURLGenPort.PARAM_MVID))
        .thenReturn(
            SBBPURLGenResponse.builder().withStatus("ERROR").withMessage("MESSAGE").build());

    PURLGeneratorException exception = assertThrows(PURLGeneratorException.class,
        generator::createPID);
    assertEquals("Calling sbbPURLGenPort returned status error with message: MESSAGE",
        exception.getMessage());

    when(sbbPURLGenPortMock.createPURL(SBBPURLGenPort.PARAM_PID, SBBPURLGenPort.PARAM_MVID))
        .thenReturn(SBBPURLGenResponse.builder().withStatus("OK").withMvid("INVALID").build());

    exception = assertThrows(PURLGeneratorException.class, generator::createPID);
    assertEquals("Calling sbbPURLGenPort returned invalid pid: INVALID", exception.getMessage());

    when(sbbPURLGenPortMock.createPURL(SBBPURLGenPort.PARAM_PID, SBBPURLGenPort.PARAM_MVID))
        .thenReturn(createResponse());

    String pid = generator.createPID();
    assertEquals("HSP0000002300000001", pid);
  }

  private SBBPURLGenResponse createResponse() {
    return SBBPURLGenResponse.builder()
        .withStatus("OK")
        .withMessage("")
        .withPid("HSP")
        .withMvid("00000023")
        .withVid("0000")
        .withPage("0001")
        .build();
  }

  private PURLConfiguration purlConfiguration() {
    PURLResolverConfiguration resolver =
        new TestResolverConfiguration(null, null, null, null, configuredUrlPrefix, false, "off");
    return new TestPURLConfiguration(resolver);
  }

  private record TestPURLConfiguration(PURLResolverConfiguration resolver)
      implements PURLConfiguration {

    @Override
    public PURLTargetConfiguration target() {
      return null;
    }

    @Override
    public PURLAutogenerateConfiguration autogenerate() {
      return null;
    }
  }

  private record TestResolverConfiguration(String directory, String txtFileName, String dbmFileName,
                                           String command, String url, boolean enabled,
                                           String schedulerEvery)
      implements PURLResolverConfiguration {

  }
}
