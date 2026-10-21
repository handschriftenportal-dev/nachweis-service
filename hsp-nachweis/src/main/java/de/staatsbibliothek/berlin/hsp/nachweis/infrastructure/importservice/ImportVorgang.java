package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportServiceAPI.IMPORTJOB_RESULT_VALUES;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.05.2020.
 * @version 1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImportVorgang {

  private static final Logger log = LoggerFactory.getLogger(ImportVorgang.class);

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

  public static final int MAX_COLUMN_LENGTH = 16384;
  public static final int MAX_FEHLER_TEILEN = 4;
  @JsonProperty(ImportServiceAPI.IMPORTJOB_ID_FIELD)
  private String id;

  @JsonProperty(ImportServiceAPI.IMPORTJOB_BENUTZER_FIELD)
  private String benutzerName;

  @JsonProperty(ImportServiceAPI.IMPORTJOB_NAME_FIELD)
  private String name;

  @JsonProperty(ImportServiceAPI.IMPORTJOB_CREATIONDATE_FIELD)
  private LocalDateTime datum;

  @JsonProperty(ImportServiceAPI.IMPORTJOB_RESULT_FIELD)
  private ImportServiceAPI.IMPORTJOB_RESULT_VALUES status;

  @JsonProperty(ImportServiceAPI.IMPORTJOB_ERRORMESSAGE_FIELD)
  private String fehler;

  @JsonIgnore
  private final List<String> fehlerTeil;

  @JsonProperty(ImportServiceAPI.IMPORTJOB_DATATYPE_FIELD)
  private String datentyp;

  @JsonProperty(ImportServiceAPI.IMPORTJOB_IMPORT_DIR)
  private String importDir;

  @JsonProperty(ImportServiceAPI.IMPORTJOB_IMPORTFILES_FIELD)
  private List<ImportFile> importFiles;

  public ImportVorgang() {
    this.importFiles = new ArrayList<>();
    this.fehlerTeil = new ArrayList<>();
  }

  public ImportVorgang(String id, String benutzerName, String name, LocalDateTime datum,
      ImportServiceAPI.IMPORTJOB_RESULT_VALUES status, String fehler, String datentyp) {
    this.id = id;
    this.benutzerName = benutzerName;
    this.name = name;
    this.datum = datum;
    this.status = status;
    this.datentyp = datentyp;
    this.importFiles = new ArrayList<>();
    this.fehlerTeil = new ArrayList<>();
    setFehler(fehler);
  }

  public String getId() {
    return id;
  }

  public String getBenutzerName() {
    return benutzerName;
  }

  public String getName() {
    return name;
  }

  public IMPORTJOB_RESULT_VALUES getStatus() {
    return status;
  }

  public void setStatus(IMPORTJOB_RESULT_VALUES status) {
    this.status = status;
  }

  public boolean isError() {
    return IMPORTJOB_RESULT_VALUES.FAILED == status;
  }

  public boolean isOtherStatus() {
    return IMPORTJOB_RESULT_VALUES.FAILED != status && IMPORTJOB_RESULT_VALUES.SUCCESS != status;
  }

  public String getFehler() {
    return fehler;
  }

  public String getFehlerTeil(int index) {
    if (fehlerTeil.size() > index) {
      return fehlerTeil.get(index);
    }
    return "";
  }

  public void setFehler(String fehler) {
    this.fehlerTeil.clear();
    this.fehler = fehler;
    if (fehler != null) {
      int fehlerLength = fehler.length();
      if (fehlerLength > MAX_COLUMN_LENGTH) {
        fillFehlerTeile();
      } else {
        fehlerTeil.add(fehler);
      }
    }
  }

  private void fillFehlerTeile() {
    int startIndex;
    int endIndex;
    int fehlerLength = fehler.length();
    int fehlerTeilen = getMaxFehlerTeilen(fehlerLength);
    for (int index = 0; index < fehlerTeilen; index++) {
      startIndex = index * MAX_COLUMN_LENGTH;
      endIndex = startIndex + MAX_COLUMN_LENGTH;
      if (endIndex > fehlerLength) {
        fehlerTeil.add(fehler.substring(startIndex));
      } else {
        fehlerTeil.add(fehler.substring(startIndex, endIndex));
      }
    }
  }

  private static int getMaxFehlerTeilen(int fehlerLength) {
    int fehlerTeilen = fehlerLength / MAX_COLUMN_LENGTH;
    if (fehlerTeilen > MAX_FEHLER_TEILEN) {
      log.warn("ColumnLength is too long {} (It will be truncated)", fehlerLength);
      fehlerTeilen = MAX_FEHLER_TEILEN;
    }
    return fehlerTeilen;
  }

  public String getDatentyp() {
    return datentyp;
  }

  public String getDateiFormatAsString() {
    if (importFiles == null) {
      return "";
    }
    Set<String> result = new LinkedHashSet<>();
    for (ImportFile importFile : importFiles) {
      if (importFile.getDateiFormat() != null) {
        result.add(importFile.getDateiFormat());
      }
    }
    return String.join(", ", result);
  }

  public List<ImportFile> getImportFiles() {
    return importFiles;
  }

  public List<DataEntity> getDataEntities() {
    List<DataEntity> result = new ArrayList<>();
    for (ImportFile importFile : importFiles) {
      for (DataEntity dataEntity : importFile.getDataEntityList()) {
        dataEntity.setDateiName(importFile.getDateiName());
        result.add(dataEntity);
      }
    }
    return result;
  }

  public LocalDateTime getDatum() {
    return datum;
  }

  public String getDatumFormatted() {
    if (datum == null) {
      return "";
    }
    return DATE_TIME_FORMATTER.format(datum);
  }

  public String getImportDir() {
    return importDir;
  }

  public boolean hasImportEntityData() {
    return importFiles.stream().anyMatch(importFile -> !importFile.getDataEntityList().isEmpty());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ImportVorgang that = (ImportVorgang) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }


  @Override
  public String toString() {
    return new StringJoiner(", ", ImportVorgang.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("benutzerName='" + benutzerName + "'")
        .add("name='" + name + "'")
        .add("datum=" + datum)
        .add("status=" + status)
        .add("fehler='" + fehler + "'")
        .add("datentyp='" + datentyp + "'")
        .add("importDir='" + importDir + "'")
        .add("importFiles=" + importFiles)
        .toString();
  }
}
