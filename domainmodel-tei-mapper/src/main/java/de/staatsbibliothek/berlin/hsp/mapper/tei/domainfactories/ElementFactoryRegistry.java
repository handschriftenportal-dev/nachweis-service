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

package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Abmessung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Beschreibstoff;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Buchschmuck;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungsort;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungszeit;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Format;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Grundsprache;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Handschriftentyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Musiknotation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Status;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Titel;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Umfang;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPMapperException;
import de.staatsbibliothek.berlin.hsp.mapper.api.factory.ElementFactory;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.TEI;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 12.09.2019.
 */
public class ElementFactoryRegistry {

  private static final Map<Class<?>, ElementFactory<?, MsDesc, TEI>> elementBuilder = new HashMap<>();

  static {
    elementBuilder.put(BeschreibungsKomponenteKopf.class, new BeschreibungsKomponenteKopfFactory());
    elementBuilder.put(Identifikation.class, new IdentifikationSetFactory());
    elementBuilder.put(NormdatenReferenz.class, new BeteiligteSetFactory());
    elementBuilder.put(Titel.class, new TitelFactory());
    elementBuilder.put(Beschreibstoff.class, new BeschreibstoffFactory());
    elementBuilder.put(Umfang.class, new UmfangFactory());
    elementBuilder.put(Abmessung.class, new AbmessungFactory());
    elementBuilder.put(Format.class, new FormatFactory());
    elementBuilder.put(Entstehungsort.class, new EntstehungsortFactory());
    elementBuilder.put(Entstehungszeit.class, new EntstehungszeitFactory());
    elementBuilder.put(Grundsprache.class, new GrundspracheFactory());
    elementBuilder.put(Buchschmuck.class, new BuchschmuckFactory());
    elementBuilder.put(Handschriftentyp.class, new HandschriftentypFactory());
    elementBuilder.put(Musiknotation.class, new MusiknotationFactory());
    elementBuilder.put(Status.class, new StatusFactory());
  }

  public static <T> T buildElement(Type targetType, MsDesc local, TEI global) throws HSPMapperException {
    if (!elementBuilder.containsKey(targetType)) {
      throw new IllegalArgumentException("Missing Element Factory for Type " + targetType);
    }

    return (T) elementBuilder.get(targetType).build(local, global);
  }

}
