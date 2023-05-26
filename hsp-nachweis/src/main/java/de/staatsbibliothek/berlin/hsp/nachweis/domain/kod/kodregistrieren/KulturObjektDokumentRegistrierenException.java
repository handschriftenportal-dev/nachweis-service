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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodregistrieren;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import java.util.ArrayList;
import java.util.List;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 25.08.2020.
 * @version 1.0
 */
public class KulturObjektDokumentRegistrierenException extends Exception {

  private static final long serialVersionUID = -3525630010074159014L;

  private List<KulturObjektDokument> kodFailureList = new ArrayList<>();
  private ERROR_TYPE errorType;

  public KulturObjektDokumentRegistrierenException() {
  }

  public KulturObjektDokumentRegistrierenException(String message) {
    super(message);
  }

  public KulturObjektDokumentRegistrierenException(String message,
      ERROR_TYPE errorType, Throwable cause) {
    super(message, cause);
    this.errorType = errorType;
  }

  public KulturObjektDokumentRegistrierenException(String message,
      List<KulturObjektDokument> kodFailureList, ERROR_TYPE error_type) {
    super(message);
    this.kodFailureList.addAll(kodFailureList);
    this.errorType = error_type;
  }

  public KulturObjektDokumentRegistrierenException(String message, Throwable cause) {
    super(message, cause);
  }

  public KulturObjektDokumentRegistrierenException(Throwable cause) {
    super(cause);
  }

  public KulturObjektDokumentRegistrierenException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public List<KulturObjektDokument> getKodFailureList() {
    return kodFailureList;
  }

  public ERROR_TYPE getErrorType() {
    return errorType;
  }

  public enum ERROR_TYPE {TECHNICAL, GUELTIGE_SIGNATUR_IN_SYSTEM, GUELTIGE_SIGNATUR_IN_DATEI}
}
