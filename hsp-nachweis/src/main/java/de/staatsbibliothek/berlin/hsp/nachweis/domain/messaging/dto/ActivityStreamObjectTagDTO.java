package de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto;

import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObjectTag;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author piotr.czarnecki@sbb.spk-berlin.de
 * @since 12.11.20
 */
@Entity
@Table(name = "stream_object_tag")
@Cacheable
public class ActivityStreamObjectTagDTO implements Serializable {

  private static final long serialVersionUID = 1906415895714181358L;

  @Id
  private String id;

  @Column(name = "type")
  private String type;

  @Column(name = "name")
  private String name;

  protected ActivityStreamObjectTagDTO() {
  }

  public ActivityStreamObjectTagDTO(ActivityStreamObjectTag tag) {
    if (tag.getId() == null) {
      this.id = tag.getType() + "|" + tag.getName();
    } else {
      this.id = tag.getId();
    }
    this.type = tag.getType();
    this.name = tag.getName();
  }

  public String getType() {
    return type;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ActivityStreamObjectTagDTO that = (ActivityStreamObjectTagDTO) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }


  @Override
  public String toString() {
    return new StringJoiner(", ", ActivityStreamObjectTagDTO.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("type='" + type + "'")
        .add("name='" + name + "'")
        .toString();
  }
}
