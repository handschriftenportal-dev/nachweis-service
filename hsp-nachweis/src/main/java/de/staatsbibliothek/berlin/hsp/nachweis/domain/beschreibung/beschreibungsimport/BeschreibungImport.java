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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport;

import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 06.06.2019.
 */
@Entity
@Table(name = "beschreibungimport")
@Cacheable
public class BeschreibungImport implements Comparable<BeschreibungImport> {

  @Id
  private String id;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 15)
  private ImportStatus status;

  @OneToOne(cascade = CascadeType.ALL)
  private ActivityStreamMessageDTO messageDTO;

  @Column
  private LocalDateTime creationDate;

  @Column
  private LocalDateTime modificationDate;

  @Column
  private String fehlerBeschreibung;

  @Column
  private boolean isSelectedForImport;

  protected BeschreibungImport() {
  }

  public BeschreibungImport(ImportStatus status, ActivityStream message) throws ActivityStreamsException {
    this.id = UUID.randomUUID().toString();
    this.status = status;
    if (message != null) {
      this.messageDTO = new ActivityStreamMessageDTO(message);
    }
    this.creationDate = LocalDateTime.now();
    this.modificationDate = LocalDateTime.now();
  }

  public String getId() {
    return id;
  }

  public ImportStatus getStatus() {
    return status;
  }

  private ActivityStream getMessage() throws ActivityStreamsException {
    if (messageDTO == null) {
      return null;
    }
    return messageDTO.getActivityStream();
  }

  public ActivityStreamMessageDTO getMessageDTO() {
    return messageDTO;
  }

  public LocalDateTime getCreationDate() {
    return creationDate;
  }

  public LocalDateTime getModificationDate() {
    return modificationDate;
  }

  public boolean isSelectedForImport() {
    return isSelectedForImport;
  }

  public void setStatus(
      ImportStatus status) {
    this.status = status;
  }

  public void setSelectedForImport(boolean selectedForImport) {
    isSelectedForImport = selectedForImport;
  }

  public String getFehlerBeschreibung() {
    return fehlerBeschreibung;
  }

  public void setFehlerBeschreibung(String fehlerBeschreibung) {
    this.fehlerBeschreibung = fehlerBeschreibung;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BeschreibungImport)) {
      return false;
    }
    BeschreibungImport anBeschreibungImport = (BeschreibungImport) o;
    return Objects.equals(id, anBeschreibungImport.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Import{" +
        "id='" + id + '\'' +
        ", status=" + status +
        ", message=" + messageDTO +
        ", creationDate=" + creationDate +
        ", modificationDate=" + modificationDate +
        ", fehlerBeschreibung='" + fehlerBeschreibung + '\'' +
        '}';
  }

  @Override
  public int compareTo(BeschreibungImport o) {
    return o.getCreationDate().compareTo(this.creationDate);
  }
}
