package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.DokumentTyp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "beschreibungsdokument")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class BeschreibungXmlDTO implements XMLDokument, Serializable {

  @Serial
  private static final long serialVersionUID = -1656605474240620782L;

  private transient final DokumentTyp dokumentTyp = DokumentTyp.BESCHREIBUNG;

  @Id
  @Column(name = "id")
  private String id;
  @Column(name = "teixml", columnDefinition = "TEXT", length = 2097152)
  private String teiXml;

}
