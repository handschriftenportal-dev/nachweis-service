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

@Entity(name = "KodXML")
@Table(name = "KulturObjektDokument")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class KodXmlDTO implements XMLDokument, Serializable {

  @Serial
  private static final long serialVersionUID = -1656605474240630782L;

  private transient final DokumentTyp dokumentTyp = DokumentTyp.KOD;

  @Id
  @Column(name = "id")
  private String id;
  @Column(name = "teixml", columnDefinition = "TEXT", length = 2097152)
  private String teiXml;

}
