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

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 10.06.2020.
 * @version 1.0
 */
public class ImportServiceAPI {

  public static final String IMPORTJOB_RESULT_FIELD = "result";
  public static final String IMPORTJOB_ERRORMESSAGE_FIELD = "errorMessage";
  public static final String IMPORTJOB_ID_FIELD = "id";
  public static final String IMPORTJOB_BENUTZER_FIELD = "benutzerName";
  public static final String IMPORTJOB_NAME_FIELD = "name";
  public static final String IMPORTJOB_CREATIONDATE_FIELD = "creationDate";
  public static final String IMPORTJOB_DATATYPE_FIELD = "datatype";
  public static final String IMPORTJOB_DATEIFORMAT_FIELD = "dateiFormat";
  public static final String IMPORTJOB_LABEL_FIELD = "label";
  public static final String IMPORTJOB_URL_FIELD = "url";
  public static final String IMPORTJOB_ENTITYDATA_FIELD = "importEntityData";
  public static final String IMPORTJOB_IMPORTFILES_FIELD = "importFiles";

  public static final String IMPORTFILE_ID_FIELD = "id";
  public static final String IMPORTFILE_PATH_FIELD = "path";
  public static final String IMPORTFILE_DATEITYP_FIELD = "dateiTyp";
  public static final String IMPORTFILE_DATEINAME_FIELD = "dateiName";
  public static final String IMPORTFILE_DATEIFORMAT_FIELD = "dateiFormat";
  public static final String IMPORTFILE_ERROR_FIELD = "error";
  public static final String IMPORTFILE_MESSAGE_FIELD = "message";
  public static final String IMPORTJOB_IMPORT_DIR = "importDir";

  public enum IMPORTJOB_RESULT_VALUES {
    SUCCESS, IN_PROGRESS, FAILED, NO_RESULT;

    @Override
    public String toString() {
      switch (this) {
        case SUCCESS:
          return "Erfolgreich";
        case IN_PROGRESS:
          return "In Bearbeitung";
        case FAILED:
          return "Gescheitert";
        case NO_RESULT:
          return "Offen";
        default:
          return "";
      }
    }
  }

}
