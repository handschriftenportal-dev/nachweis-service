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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.katalogimport;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import java.util.Objects;

public class KatalogImport {

  private final ActivityStreamMessageDTO messageDTO;

  private final Bearbeiter bearbeiter;

  public KatalogImport(ActivityStream message, Bearbeiter bearbeiter)
      throws ActivityStreamsException {
    this.messageDTO = new ActivityStreamMessageDTO(message);
    this.bearbeiter = bearbeiter;
  }

  public KatalogImport(ActivityStreamMessageDTO message, Bearbeiter bearbeiter)
      throws ActivityStreamsException {
    this.messageDTO = message;
    this.bearbeiter = bearbeiter;
  }

  public ActivityStreamMessageDTO getMessageDTO() {
    return messageDTO;
  }

  public Bearbeiter getBearbeiter() {
    return bearbeiter;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KatalogImport that = (KatalogImport) o;
    return Objects.equals(messageDTO, that.messageDTO);
  }

  @Override
  public int hashCode() {
    return Objects.hash(messageDTO);
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("KatalogImport{");
    sb.append(", message=").append(messageDTO);
    sb.append('}');
    return sb.toString();
  }
}
