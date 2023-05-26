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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;


import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.BeschreibungListDTO;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 11.04.2022
 */
@ViewScoped
@Named
@LoginCheck
@Slf4j
public class DashboardController implements Serializable {

  private static final long serialVersionUID = 3856646111600383437L;

  private BeschreibungsBoundary beschreibungsBoundary;

  @Getter
  private List<BeschreibungListDTO> beschreibungen;

  @Inject
  public DashboardController(BeschreibungsBoundary beschreibungsBoundary) {
    this.beschreibungsBoundary = beschreibungsBoundary;
  }

  @PostConstruct
  public void setup() {
    log.info("setup started");
    beschreibungen = beschreibungsBoundary.findLatestModified();
    log.info("setup finished");
  }

}
