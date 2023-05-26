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

import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamBuilder;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObjectBuilder;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObjectTag;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamObjectTagId;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.model.HSPActivityStreamObjectTag;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * @author piotr.czarnecki@sbb.spk-berlin.de
 * @since 12.11.20
 */
@Entity
@Table(name = "stream_message")
@Cacheable
public class ActivityStreamMessageDTO implements Serializable {

  private static final long serialVersionUID = 7699089758238531546L;

  @Id
  private String id;

  @Column(name = "context")
  private String context;

  @Enumerated(EnumType.STRING)
  @Column(name = "action")
  private ActivityStreamAction action;

  @Column(name = "version")
  private String version;

  @Column(name = "published")
  private LocalDateTime published;

  @Column(name = "summary")
  private String summary;

  @OneToMany(cascade = CascadeType.ALL)
  private List<ActivityStreamObjectDTO> objects;

  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST, CascadeType.MERGE})
  private ActivityStreamActorDTO actor;

  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST, CascadeType.MERGE})
  private ActivityStreamTargetDTO target;

  protected ActivityStreamMessageDTO() {
    this.objects = new ArrayList<>();
  }

  public ActivityStreamMessageDTO(ActivityStream message) throws ActivityStreamsException {
    this.objects = new ArrayList<>();
    if (message.getId() == null) {
      this.id = UUID.randomUUID().toString();
    } else {
      this.id = message.getId();
    }
    this.context = message.getContext();
    this.action = message.getAction();
    this.version = message.getVersion();
    this.published = message.getPublished();
    this.summary = message.getSummary();
    if (message.getObjects() != null) {
      for (ActivityStreamObject object : message.getObjects()) {
        this.objects.add(new ActivityStreamObjectDTO(object));
      }
    }
    if (message.getActor() != null) {
      this.actor = new ActivityStreamActorDTO(message.getActor());
    }
    if (message.getTarget() != null) {
      this.target = new ActivityStreamTargetDTO(message.getTarget());
    }
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public ActivityStreamAction getAction() {
    return action;
  }

  public void setAction(ActivityStreamAction action) {
    this.action = action;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public LocalDateTime getPublished() {
    return published;
  }

  public void setPublished(LocalDateTime published) {
    this.published = published;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public List<ActivityStreamObjectDTO> getObjects() {
    return objects;
  }

  public void setObjects(
      List<ActivityStreamObjectDTO> objects) {
    this.objects = objects;
  }

  public ActivityStreamActorDTO getActor() {
    return actor;
  }

  public void setActor(ActivityStreamActorDTO actor) {
    this.actor = actor;
  }

  public ActivityStreamTargetDTO getTarget() {
    return target;
  }

  public void setTarget(ActivityStreamTargetDTO target) {
    this.target = target;
  }

  public ActivityStream getActivityStream() throws ActivityStreamsException {
    ActivityStreamBuilder builder = ActivityStream.builder();
    builder.withId(id)
        .withPublished(published)
        .withType(action)
        .withTargetName(target.getName())
        .withActorName(actor.getName());
    for (ActivityStreamObjectDTO objectDTO : objects) {
      ActivityStreamObjectBuilder objectBuilder = ActivityStreamObject.builder();
      objectBuilder.withId(objectDTO.getId())
          .withName(objectDTO.getName())
          .withCompressed(objectDTO.isCompressed())
          .withContent(objectDTO.getContent())
          .withGroupId(objectDTO.getGroupId())
          .withMediaType(objectDTO.getMediaType())
          .withUrl(objectDTO.getUrl())
          .withType(objectDTO.getType());
      List<ActivityStreamObjectTag> objectTags = new ArrayList<>();
      for (ActivityStreamObjectTagDTO objectTagDTO : objectDTO.getTag()) {
        objectTags.add(new HSPActivityStreamObjectTag(objectTagDTO.getType(),
            ActivityStreamObjectTagId.valueFrom(objectTagDTO.getId()), objectTagDTO.getName()));
      }
      objectBuilder.withTag(objectTags);
      builder.addObject(objectBuilder.build());
    }
    return builder.build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ActivityStreamMessageDTO that = (ActivityStreamMessageDTO) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }


  @Override
  public String
  toString() {
    return new StringJoiner(", ", ActivityStreamMessageDTO.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("context='" + context + "'")
        .add("action='" + action + "'")
        .add("version='" + version + "'")
        .add("published=" + published)
        .add("summary='" + summary + "'")
        .add("objects=" + objects)
        .add("actor=" + actor)
        .add("target=" + target)
        .toString();
  }
}

