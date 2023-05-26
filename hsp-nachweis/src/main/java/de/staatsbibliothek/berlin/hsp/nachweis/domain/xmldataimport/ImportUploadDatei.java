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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 28.05.2020.
 * @version 1.0
 */
public class ImportUploadDatei {

  private final String name;

  private final byte[] content;

  private final String contentType;

  public ImportUploadDatei(String name, byte[] content, String contentType) {
    this.name = name;
    this.content = content;
    this.contentType = contentType;
  }

  public String getName() {
    return name;
  }

  public byte[] getContent() {
    return content;
  }

  public String getContentType() {
    return contentType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ImportUploadDatei)) {
      return false;
    }
    ImportUploadDatei that = (ImportUploadDatei) o;
    return name.equals(that.name) &&
        Arrays.equals(content, that.content) &&
        contentType.equals(that.contentType);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(name, contentType);
    result = 31 * result + Arrays.hashCode(content);
    return result;
  }

  @Override
  public String toString() {
    return "ImportDatei{" +
        "name='" + name + '\'' +
        ", content=" + Arrays.toString(content) +
        ", contentType='" + contentType + '\'' +
        '}';
  }
}
