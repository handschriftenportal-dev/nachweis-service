package de.staatsbibliothek.berlin.hsp.nachweis.domain.purl;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 08.08.22
 */

@Getter
@EqualsAndHashCode(of = {"dokumentId"})
@ToString
public class PURLViewModel implements Serializable {

  @Serial
  private static final long serialVersionUID = 2322354704163687258L;

  private final String dokumentId;
  private final DokumentObjektTyp dokumentObjektTyp;
  private final Set<PURL> internalPURLs;
  private final Set<PURL> externalPURLs;

  public PURLViewModel(String dokumentId, DokumentObjektTyp dokumentObjektTyp, Set<PURL> purls) {
    this.dokumentId = dokumentId;
    this.dokumentObjektTyp = dokumentObjektTyp;
    this.internalPURLs = Stream.ofNullable(purls)
        .flatMap(Collection::stream)
        .filter(PURL::isInternal)
        .collect(Collectors.toSet());
    this.externalPURLs = Stream.ofNullable(purls)
        .flatMap(Collection::stream)
        .filter(PURL::isExternal)
        .collect(Collectors.toSet());
  }

}
