package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DigitalisatViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 07.10.2020.
 * @version 1.0
 */

@SessionScoped
@Named
@Data
@Slf4j
@LoginCheck
public class DigitalisatErfassenModel implements Serializable {

  @Serial
  private static final long serialVersionUID = -4968518784266012126L;

  private KulturObjektDokumentViewModel kulturObjektDokumentViewModel;

  private DigitalisatViewModel digitalisatViewModel;

  private Map<String, NormdatenReferenz> koerperschaftViewModelMap = new HashMap<>();

  private Map<String, NormdatenReferenz> digitalisatSchemeTypen = new TreeMap<>();

  private Map<String, NormdatenReferenz> digitalisatConceptTypen = new TreeMap<>();

  private NormdatenReferenz activeSchemeType;

  private NormdatenReferenz activeConceptType;

  private NormdatenReferenz selectedConcept;

  @PostConstruct
  public void construct() {

    if (digitalisatViewModel == null) {
      log.info("Creating new Digitalisat Erfassen Model");
      digitalisatViewModel = new DigitalisatViewModel();
    }
  }

  public Collection<NormdatenReferenz> getDigitalisatConceptTypenValues() {
    return digitalisatConceptTypen != null ? digitalisatConceptTypen.values() : Collections.emptyList();
  }

  public Collection<NormdatenReferenz> getDigitalisatSchemeTypenValues() {
    return digitalisatSchemeTypen != null ? digitalisatSchemeTypen.values() : Collections.emptyList();
  }
}
