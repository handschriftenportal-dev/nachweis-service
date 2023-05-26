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

import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamTarget;
import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;
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
@Table(name = "stream_target")
@Cacheable
public class ActivityStreamTargetDTO implements Serializable {

  private static final long serialVersionUID = -3904182775884310650L;

  @Id
  private String id;

  @Column(name = "name")
  private String name;

  @Column(name = "type")
  private String type;

  protected ActivityStreamTargetDTO() {
  }

  public ActivityStreamTargetDTO(ActivityStreamTarget target) {
    if (target.getId() == null) {
      this.id = UUID.randomUUID().toString();
    } else {
      this.id = target.getId();
    }
    this.name = target.getName();
    this.type = target.getName();
  }

  public String getId() {
    return id;
  }


  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ActivityStreamTargetDTO that = (ActivityStreamTargetDTO) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }


  @Override
  public String toString() {
    return new StringJoiner(", ", ActivityStreamTargetDTO.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("name='" + name + "'")
        .add("type='" + type + "'")
        .toString();
  }
}
