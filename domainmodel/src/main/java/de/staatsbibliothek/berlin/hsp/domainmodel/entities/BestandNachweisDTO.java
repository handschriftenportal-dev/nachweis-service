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

package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import java.util.UUID;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 24.04.23.
 * @version 1.0
 */

public class BestandNachweisDTO {

  public BestandNachweisDTO(long anzahlKOD, long anzahlBeschreibungen,long anzahlBeschreibungenIntern,long anzahlBeschreibungenExtern,long anzahlBeschreibungenKatalog, long anzahlDigitalisate, long anzahlKataloge) {
    this.id = UUID.randomUUID().toString();
    this.anzahlKOD = anzahlKOD;
    this.anzahlBeschreibungen = anzahlBeschreibungen;
    this.anzahlBeschreibungenIntern = anzahlBeschreibungenIntern;
    this.anzahlBeschreibungenExtern = anzahlBeschreibungenExtern;
    this.anzahlBeschreibungenKatalog = anzahlBeschreibungenKatalog;
    this.anzahlDigitalisate = anzahlDigitalisate;
    this.anzahlKataloge = anzahlKataloge;
  }

  private String id;
  private long anzahlKOD;
  private long anzahlBeschreibungen;
  private long anzahlBeschreibungenIntern;
  private long anzahlBeschreibungenExtern;
  private long anzahlBeschreibungenKatalog;
  private long anzahlDigitalisate;
  private long anzahlKataloge;
  public BestandNachweisDTO() {
    this.id = UUID.randomUUID().toString();
  }

  public long getAnzahlKOD() {
    return anzahlKOD;
  }

  public long getAnzahlBeschreibungen() {
    return anzahlBeschreibungen;
  }

  public long getAnzahlDigitalisate() {
    return anzahlDigitalisate;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public long getAnzahlKataloge() {
    return anzahlKataloge;
  }

  public BestandNachweisDTO(long anzahlBeschreibungenIntern) {
    this.anzahlBeschreibungenIntern = anzahlBeschreibungenIntern;
  }

  public long getAnzahlBeschreibungenIntern() {
    return anzahlBeschreibungenIntern;
  }

  public long getAnzahlBeschreibungenExtern() {
    return anzahlBeschreibungenExtern;
  }

  public long getAnzahlBeschreibungenKatalog() {
    return anzahlBeschreibungenKatalog;
  }
}
