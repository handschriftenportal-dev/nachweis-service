package de.staatsbibliothek.berlin.hsp.nachweis;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import java.net.URL;
import org.junit.jupiter.api.Test;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 05.06.2019.
 */

@QuarkusTest
public class ApplicationIT {

  @TestHTTPResource("/kulturObjektDokument/digitalisat-anlegen.xhtml?kodid=test1")
  URL url;

  @Test
  public void testIfContainerIsRunning() {

    assertNotNull(url);
  }

}
