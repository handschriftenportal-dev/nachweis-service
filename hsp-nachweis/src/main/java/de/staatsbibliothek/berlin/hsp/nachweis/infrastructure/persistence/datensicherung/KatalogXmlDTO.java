package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.DokumentTyp;
import java.io.Serial;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "Katalog")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class KatalogXmlDTO implements XMLDokument, Serializable {

  @Serial
  private static final long serialVersionUID = -1656605474240620782L;

  private transient final DokumentTyp dokumentTyp = DokumentTyp.KATALOG;

  @Id
  @Column(name = "id")
  private String id;
  @Column(name = "tei_xml", columnDefinition = "TEXT", length = 2097152)
  private String teiXml;


}
