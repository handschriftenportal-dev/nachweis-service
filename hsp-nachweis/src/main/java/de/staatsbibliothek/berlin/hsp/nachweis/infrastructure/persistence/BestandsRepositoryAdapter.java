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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BestandNachweisDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsRepository;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 24.04.23.
 * @version 1.0
 */

@ApplicationScoped
public class BestandsRepositoryAdapter implements BestandsRepository {

  @Inject
  private EntityManager entityManager;
  @Override
  public BestandNachweisDTO findAllBestaende() {

    Query nq = entityManager.createNativeQuery("select * from (select count(*) anzahlKOD from kulturobjektdokument kods) as k, "
            + "(select count(*) anzahlBeschreibungen from beschreibungsdokument) as b, (select count(*) anzahlBeschreibungenIntern from beschreibungsdokument where verwaltungstyp = 'INTERN') as bi, "
        + "(select count(*) anzahlBeschreibungenExtern from beschreibungsdokument where verwaltungstyp = 'EXTERN') as be,(select count(*) anzahlBeschreibungenKatalog from beschreibungsdokument where dokumentobjekttyp = 'HSP_DESCRIPTION_RETRO') as bk,"
        + "(select count(*) anzahlDigitalisat from digitalisat) as d, "
        + "(select count(*) anzahlKatalog from katalog) as c","BestandNachweisDTOMapping");

    return (BestandNachweisDTO) nq.getSingleResult();
  }

  public EntityManager getEntityManager() {
    return entityManager;
  }
}
