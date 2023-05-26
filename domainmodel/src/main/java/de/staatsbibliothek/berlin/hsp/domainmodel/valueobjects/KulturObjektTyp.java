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

package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Beinhaltet Kultur Objekt Dokument Typen (Gültig nur für DokumentTyp=KULTUR_OBJEKT_DOKUMENT)
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 10.05.19
 */
public enum KulturObjektTyp {
  CODEX,
  FRAGMENT_SELBSTAENDIG_AUSGELOEST,
  FRAGMENT_SELBSTAENDIG_IN_DRUCK,
  FRAGMENT_SELBSTAENDIG_ABKLATSCH,
  EINZELBLATT,
  SAMMELBAND,
  SAMMLUNG,
  DRUCK_MIT_HSL_ANTEILEN,
  DRUCK_ALS_TRAEGEBAND,
  SONSTIGES,
  ROLLE,
  ZUSAMMENGESETZTE_HANDSCHRIFT;

  private static final Logger log = LoggerFactory.getLogger(KulturObjektTyp.class);

  public static KulturObjektTyp getKulturObjektTypFromString(String kulturObjektTypAsString) {
    if (Objects.isNull(kulturObjektTypAsString) || kulturObjektTypAsString.isEmpty()) {
      return null;
    }
    //Codex;zusammengesetzteHs;Sammelband;Einzelblatthandschrift;Rolle;Fragment;Sammlung;DruckHslAntl;DruckTrgbd;Sonstiges
    kulturObjektTypAsString = kulturObjektTypAsString.toUpperCase();
    switch (kulturObjektTypAsString) {
      case "CODEX":
        return CODEX;
      case "ZUSAMMENGESETZTEHS":
      case "ZUSAMMENGESETZTE HANDSCHRIFT":
        return ZUSAMMENGESETZTE_HANDSCHRIFT;
      case "SAMMELBAND":
        return SAMMELBAND;
      case "EINZELBLATT":
      case "EINZELBLATTHANDSCHRIFT":
        return EINZELBLATT;
      case "ROLLE":
        return ROLLE;
      case "FRAGMENT SELBSTAENDIG AUSGELOEST":
      case "FRAGMENT":
        return FRAGMENT_SELBSTAENDIG_AUSGELOEST;
      case "FRAGMENT SELBSTAENDIG ABKLATSCH":
        return FRAGMENT_SELBSTAENDIG_ABKLATSCH;
      case "FRAGMENT SELBSTAENDIG IN DRUCK":
        return FRAGMENT_SELBSTAENDIG_IN_DRUCK;
      case "SAMMLUNG":
        return SAMMLUNG;
      case "DRUCKHSLANTL":
      case "DRUCK MIT HSL ANTEILEN":
        return DRUCK_MIT_HSL_ANTEILEN;
      case "DRUCK IN TRAGEBAND":
      case "DRUCKTRGBD":
        return DRUCK_ALS_TRAEGEBAND;
      case "SONSTIGES":
        return SONSTIGES;
      default:
        log.warn("Not handled KulturObjektTyp: {}", kulturObjektTypAsString);
        return null;
    }

  }
}
