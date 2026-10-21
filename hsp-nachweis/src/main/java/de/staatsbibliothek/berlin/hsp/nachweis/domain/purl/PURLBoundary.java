package de.staatsbibliothek.berlin.hsp.nachweis.domain.purl;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import java.util.Collection;
import java.util.List;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 08.08.22
 */

public interface PURLBoundary {

  List<PURLViewModel> findAllAsViewModels();

  void resolverDateiGenerieren() throws PURLException;

  PURL createNewPURL(String dokumentId, DokumentObjektTyp dokumentObjektTyp) throws PURLException;

  int updateInternalPURLs(Collection<PURLViewModel> allViewModels) throws PURLException;

  String getTargetTemplate(DokumentObjektTyp dokumentObjektTyp);

  void updateTargetTemplate(DokumentObjektTyp dokumentObjektTyp, String targetTemplate)
      throws PURLException;

  boolean isTemplateValid(String template);

}
