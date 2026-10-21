package de.staatsbibliothek.berlin.hsp.nachweis.domain.suche;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by udo.boysen@sbb.spk-berlin.de on 14.02.2020.
 */
@Data
@NoArgsConstructor
public class ErgebnisEintrag implements Serializable {

  private static final long serialVersionUID = -3612271537746627234L;

  private String id;

  private SuchDokumentTyp typ;

  private String signatur;

  private String title;

  private String sichtbarkeit;

  private String verwaltungsTyp;

  private List<String> bearbeiter;

  private String bestandhaltendeInstitutionName;

  private String bestandhaltendeInstitutionOrt;

  private Integer jahrDerPublikation;

  private LocalDateTime lastUpdate;

  private Boolean containsDigitalisat;

  private Boolean containsBuchschmuck;

  private Boolean containsBeschreibung;

  private Boolean publiziert;

  private List<String> autoren;

}
