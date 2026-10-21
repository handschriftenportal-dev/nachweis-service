package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 8.11.2021
 */
@Getter
@ToString
@EqualsAndHashCode(of = {"targetId", "targetType"})
@AllArgsConstructor
public class SperreEintragViewModel implements Serializable {

  private static final long serialVersionUID = -2133028771619185801L;

  private String targetId;

  private SperreDokumentTyp targetType;

  private String signature;
}
