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
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObjectTag;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author piotr.czarnecki@sbb.spk-berlin.de
 * @since 12.11.20
 */
@Entity
@Table(name = "stream_object")
@Cacheable
public class ActivityStreamObjectDTO implements Serializable {

  private static final Logger log = LoggerFactory.getLogger(ActivityStreamObjectDTO.class);
  private static final long serialVersionUID = 909916715187154098L;

  @Id
  private String id;

  @Column(name = "groupId")
  private String groupId;

  @Column(name = "name")
  private String name;

  @Column(name = "version")
  private String version;

  @Column(name = "compressed")
  private boolean compressed;

  @Enumerated(EnumType.STRING)
  @Column(name = "type")
  private ActivityStreamsDokumentTyp type;

  @Column(name = "url")
  private String url;

  @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private Set<ActivityStreamObjectTagDTO> tag;

  @Column(name = "mediaType")
  private String mediaType;

  @Lob
  @Type(type = "org.hibernate.type.BinaryType")
  @Column(name = "content", length = 5242880)
  @Basic(fetch = FetchType.LAZY)
  protected byte[] content;

  protected ActivityStreamObjectDTO() {
    compressed = true;
    tag = new LinkedHashSet<>();
  }

  public ActivityStreamObjectDTO(ActivityStreamObject object) throws ActivityStreamsException {
    if (object.getId() == null) {
      this.id = UUID.randomUUID().toString();
    } else {
      this.id = object.getId();
    }
    tag = new LinkedHashSet<>();
    this.groupId = object.getGroupId();
    this.name = object.getName();
    this.version = object.getVersion();
    this.compressed = object.isCompressed();
    this.type = object.getType();
    this.url = object.getUrl();
    for (ActivityStreamObjectTag activityStreamObjectTag : object.getTag()) {
      this.tag.add(new ActivityStreamObjectTagDTO((activityStreamObjectTag)));
    }
    this.mediaType = object.getMediaType();
    setContent(object.getContent());
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public boolean isCompressed() {
    return compressed;
  }

  public void setCompressed(boolean compressed) {
    this.compressed = compressed;
  }

  public ActivityStreamsDokumentTyp getType() {
    return type;
  }

  public void setType(ActivityStreamsDokumentTyp type) {
    this.type = type;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Set<ActivityStreamObjectTagDTO> getTag() {
    return tag;
  }

  public void setTag(
      Set<ActivityStreamObjectTagDTO> tag) {
    this.tag = tag;
  }

  public String getMediaType() {
    return mediaType;
  }

  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  public byte[] getContent() throws ActivityStreamsException {
    if (Objects.isNull(content)) {
      return null;
    }

    try (InputStream byteArrayInputStream = new ByteArrayInputStream(content);
        GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream)) {
      return gzipInputStream.readAllBytes();
    } catch (IOException e) {
      throw new ActivityStreamsException(e);
    }

  }

  public void setContent(byte[] content) throws ActivityStreamsException {
    if (content != null) {
      try (
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          GZIPOutputStream gzipOutputStream = new GZIPOutputStream(out)
      ) {
        gzipOutputStream.write(content, 0, content.length);
        gzipOutputStream.finish();
        gzipOutputStream.flush();
        this.content = out.toByteArray();
      } catch (IOException e) {
        log.error("Unable to convert content", e);
        throw new ActivityStreamsException("Unable to convert content", e);
      }
    }

  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ActivityStreamObjectDTO that = (ActivityStreamObjectDTO) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }


  @Override
  public String toString() {
    return new StringJoiner(", ", ActivityStreamObjectDTO.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("groupId='" + groupId + "'")
        .add("name='" + name + "'")
        .add("version='" + version + "'")
        .add("compressed=" + compressed)
        .add("type='" + type + "'")
        .add("url='" + url + "'")
        .add("tag=" + tag)
        .add("mediaType='" + mediaType + "'")
        .toString();
  }
}
