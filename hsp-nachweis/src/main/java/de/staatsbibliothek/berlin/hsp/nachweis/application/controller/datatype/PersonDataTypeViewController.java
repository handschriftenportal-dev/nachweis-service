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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.datatype;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.PersonModel;
import java.io.Serializable;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

/**
 * Author: Christoph Marten on 10.03.2021 at 13:02
 */
@ViewScoped
@Slf4j
public class PersonDataTypeViewController implements Serializable {

  private static final long serialVersionUID = 8837144449085033224L;

  @Delegate
  PersonModel personModel;

  @Inject
  PersonDataTypeViewController(PersonModel personModel) {
    this.personModel = personModel;
  }

}
