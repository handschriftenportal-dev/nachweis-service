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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsView.BestandElementTyps.BESCHREIBUNGEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsView.BestandElementTyps.BESCHREIBUNGEN_EXTERN;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsView.BestandElementTyps.BESCHREIBUNGEN_INTERN;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsView.BestandElementTyps.BESCHREIBUNGEN_KATALOG;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsView.BestandElementTyps.DIGITALISATE;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsView.BestandElementTyps.KATALOGE;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsView.BestandElementTyps.KULTUROBJEKTE;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BestandNachweisDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsView.BestandsElement;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.hsp_frontend.DiscoveryServiceInfoDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.hsp_frontend.DiscoveryServicePort;
import java.util.Comparator;
import java.util.ResourceBundle;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.04.23.
 * @version 1.0
 */

@Dependent
@Slf4j
public class BestandsBoundaryService implements BestandsBoundary{

  @Inject
  @RestClient
  private DiscoveryServicePort discoveryServicePort;

  @Inject
  private BestandsRepository bestandsRepository;

  private transient ResourceBundle resourceBundle = ResourceBundle
      .getBundle(I18NController.APPLICATION_MESSAGES, I18NController.getLocale());

  @Override
  public BestandsView createBestandsView() {

    BestandsView bestandsView = new BestandsView();

    try {

      BestandNachweisDTO bestandNachweisDTO = bestandsRepository.findAllBestaende();
      DiscoveryServiceInfoDTO discoveryServiceInfoDTO = discoveryServicePort.findInfo();

      bestandsView.getBestandsElemente().add(new BestandsElement(KULTUROBJEKTE,
          resourceBundle.getString("document_titel_kulturobjektdokumente"),
          Math.toIntExact(bestandNachweisDTO.getAnzahlKOD()),
          discoveryServiceInfoDTO.getItems().getHspObjects()));
      bestandsView.getBestandsElemente().add(new BestandsElement(BESCHREIBUNGEN,
          resourceBundle.getString("document_titel_beschreibungen"),
          Math.toIntExact(bestandNachweisDTO.getAnzahlBeschreibungen()),
          discoveryServiceInfoDTO.getItems().getHspDescriptions()
              + discoveryServiceInfoDTO.getItems().getHspDescriptionsRetro()));
      bestandsView.getBestandsElemente().add(new BestandsElement(BESCHREIBUNGEN_INTERN,
          resourceBundle.getString("document_titel_beschreibungen")+ " (Intern)",
          Math.toIntExact(bestandNachweisDTO.getAnzahlBeschreibungenIntern()),
          -1));
      bestandsView.getBestandsElemente().add(new BestandsElement(BESCHREIBUNGEN_EXTERN,
          resourceBundle.getString("document_titel_beschreibungen")+ " (Extern)",
          Math.toIntExact(bestandNachweisDTO.getAnzahlBeschreibungenExtern()),
          discoveryServiceInfoDTO.getItems().getHspDescriptions()+discoveryServiceInfoDTO.getItems().getHspDescriptionsRetro()));
      bestandsView.getBestandsElemente().add(new BestandsElement(BESCHREIBUNGEN_KATALOG,
          resourceBundle.getString("document_titel_beschreibungen")+ " (Katalog)",
          Math.toIntExact(bestandNachweisDTO.getAnzahlBeschreibungenKatalog()),
          discoveryServiceInfoDTO.getItems().getHspDescriptionsRetro()));
      bestandsView.getBestandsElemente().add(
          new BestandsElement(DIGITALISATE, resourceBundle.getString("kod_detail_digitalisate"),
              Math.toIntExact(bestandNachweisDTO.getAnzahlDigitalisate()),
              discoveryServiceInfoDTO.getItems().getHspDigitized()));
      bestandsView.getBestandsElemente()
          .add(new BestandsElement(KATALOGE, resourceBundle.getString("document_titel_kataloge"),
              Math.toIntExact(bestandNachweisDTO.getAnzahlKataloge()), -1));

      bestandsView.getBestandsElemente().stream().sorted(
          Comparator.comparingInt(BestandsElement::getBestandNachweis));

    }catch (Exception error) {
      log.error("Error during find Bestaende {}",error.getMessage(),error);
      bestandsView = new BestandsView(error.getMessage());
    }

    return bestandsView;
  }

  public DiscoveryServicePort getDiscoveryServicePort() {
    return discoveryServicePort;
  }

  public BestandsRepository getBestandsRepository() {
    return bestandsRepository;
  }
}
