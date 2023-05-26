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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto;

import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamActor;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author piotr.czarnecki@sbb.spk-berlin.de
 * @since 12.11.20
 */
@Entity
@Table(name = "stream_actor")
@Cacheable
public class ActivityStreamActorDTO implements Serializable {

  private static final long serialVersionUID = 3002286024342772090L;

  @Id
  private String id;
  @Column(name = "type")
  private String type;
  @Column(name = "name")
  private String name;
  @Column(name = "url")
  private String url;

  protected ActivityStreamActorDTO() {
  }

  public ActivityStreamActorDTO(ActivityStreamActor actor) {
    if (actor.getId() == null) {
      this.id = UUID.randomUUID().toString();
    } else {
      this.id = actor.getId();
    }
    this.type = actor.getType();
    this.name = actor.getName();
    this.url = actor.getUrl();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ActivityStreamActorDTO that = (ActivityStreamActorDTO) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }


  @Override
  public String toString() {
    return toString(true);
  }

  private String toString(boolean withID) {
    final StringBuilder sb = new StringBuilder("ActivityStreamActorDTO{");
    if (withID) {
      sb.append("id='").append(id).append('\'');
    }
    sb.append(", type='").append(type).append('\'');
    sb.append(", name='").append(name).append('\'');
    sb.append(", url='").append(url).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
