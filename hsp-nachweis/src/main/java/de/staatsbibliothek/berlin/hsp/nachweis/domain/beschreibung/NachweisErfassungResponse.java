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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 22.03.2021.
 * @version 1.0
 */
public class NachweisErfassungResponse {

  private boolean success;

  private String message;

  private String level;

  private Object content;

  public boolean isSuccess() {
    return success;
  }

  public void withSuccess(String message) {
    this.success = true;
    this.message = message;
    this.level = "info";
    this.content = "";
  }

  public void successWithContent(String message, Object content) {
    withSuccess(message);
    this.content = content;
  }

  public void withError(String message) {
    this.success = false;
    this.message = message;
    this.level = "error";
    this.content = "";
  }

  public void errorWithContent(String message, Object content) {
    withError(message);
    this.content = content;
  }

  public String getMessage() {
    return message;
  }

  public String getLevel() {
    return level;
  }

  public Object getContent() {
    return content;
  }

}
