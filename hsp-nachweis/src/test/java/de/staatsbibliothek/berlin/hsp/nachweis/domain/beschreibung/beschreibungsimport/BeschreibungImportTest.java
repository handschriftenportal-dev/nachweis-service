package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.Mockito.mock;

import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.model.ActivityStreamMessage;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 06.06.2019.
 */
public class BeschreibungImportTest {


  @Test
  public void test_givenValues_Creation() throws ActivityStreamsException {
    ActivityStream message = mock(ActivityStreamMessage.class);
    Mockito.when(message.getId()).thenReturn("1");

    BeschreibungImport beschreibungImportJob = new BeschreibungImport(ImportStatus.OFFEN, message);

    MatcherAssert.assertThat(beschreibungImportJob.getStatus(), is(ImportStatus.OFFEN));

    MatcherAssert.assertThat(beschreibungImportJob.getCreationDate(), is(notNullValue()));

    MatcherAssert.assertThat(beschreibungImportJob.getModificationDate(), is(notNullValue()));

    MatcherAssert.assertThat(beschreibungImportJob.getMessageDTO().getId(), is(message.getId()));

  }
}
