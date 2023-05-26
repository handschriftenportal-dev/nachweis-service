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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.model.ActivityStreamMessage;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportStatus;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 06.06.2019.
 */
public class ImportRepositoryTest {

  @Test
  public void test_GivenActivityMessageStream_Save() throws ActivityStreamsException {
    ImportRepositoryAdapter repository = Mockito.mock(ImportRepositoryAdapter.class);
    ActivityStream message = mock(ActivityStreamMessage.class);

    BeschreibungImport beschreibungImport = new BeschreibungImport(ImportStatus.OFFEN, message);

    Mockito.when(repository.save(beschreibungImport)).thenReturn(beschreibungImport);
    BeschreibungImport saved = repository.save(beschreibungImport);

    Assertions.assertNotNull(saved);

    assertEquals(saved.getStatus(), ImportStatus.OFFEN);
  }

  @Test
  public void test_GivenSavedActivityMessage_FindAll() throws ActivityStreamsException {
    ImportRepositoryAdapter repository = Mockito.mock(ImportRepositoryAdapter.class);
    ActivityStream message = mock(ActivityStreamMessage.class);

    BeschreibungImport beschreibungImport = new BeschreibungImport(ImportStatus.OFFEN, message);
    Mockito.when(repository.save(beschreibungImport)).thenReturn(beschreibungImport);
    Mockito.when(repository.listAll()).thenReturn(List.of(beschreibungImport));
    BeschreibungImport saved = repository.save(beschreibungImport);

    assertTrue(repository.listAll().contains(saved));

  }

  @Test
  public void test_GivenSavedImport_FindByStatus() throws ActivityStreamsException {
    ImportRepositoryAdapter repository = Mockito.mock(ImportRepositoryAdapter.class);
    ActivityStream message = mock(ActivityStreamMessage.class);

    BeschreibungImport message1 = new BeschreibungImport(ImportStatus.OFFEN, message);
    BeschreibungImport message2 = new BeschreibungImport(ImportStatus.FEHLGESCHLAGEN, message);

    Mockito.when(repository.save(message1)).thenReturn(message1);
    Mockito.when(repository.save(message2)).thenReturn(message2);

    Mockito.when(repository.findByStatus(ImportStatus.OFFEN)).thenReturn(List.of(message1));
    Mockito.when(repository.findByStatus(ImportStatus.FEHLGESCHLAGEN)).thenReturn(List.of(message2));

    BeschreibungImport offen = repository.save(message1);
    BeschreibungImport fehlgeschlagen = repository.save(message2);

    assertEquals(repository.findByStatus(ImportStatus.OFFEN).size(), 1);
    assertTrue(repository.findByStatus(ImportStatus.OFFEN).contains(offen));
    assertTrue(repository.findByStatus(ImportStatus.FEHLGESCHLAGEN).contains(fehlgeschlagen));
  }
}
