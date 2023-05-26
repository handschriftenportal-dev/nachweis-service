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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex;

import java.util.Objects;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "indexjobstatus")
@Cacheable
public class IndexJobStatus {

  @Id
  private long id;

  @Column
  private String statusInPercentage;

  public IndexJobStatus() {
  }

  public IndexJobStatus(Long id, String statusInPercentage) {
    this.id = id;
    this.statusInPercentage = statusInPercentage;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getStatusInPercentage() {
    return statusInPercentage;
  }

  public void setStatusInPercentage(String statusInPercentage) {
    this.statusInPercentage = statusInPercentage;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IndexJobStatus that = (IndexJobStatus) o;
    return id == that.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "IndexJobStatus{" +
        "id='" + id + '\'' +
        ", statusInPercentage='" + statusInPercentage + '\'' +
        '}';
  }

  public static class IndexJobStatusBuilder {

    private long id;
    private String statusInPercentage;

    private IndexJobStatusBuilder() {
    }

    public static IndexJobStatusBuilder anIndexJobStatus() {
      return new IndexJobStatusBuilder();
    }

    public IndexJobStatusBuilder withId(long id) {
      this.id = id;
      return this;
    }

    public IndexJobStatusBuilder withStatusInPercentage(String statusInPercentage) {
      this.statusInPercentage = statusInPercentage;
      return this;
    }

    public IndexJobStatus build() {
      return new IndexJobStatus(id, statusInPercentage);
    }
  }
}
