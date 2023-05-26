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

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 09.09.20
 */
@Named
@RequestScoped
public class UserRequestController implements Serializable {

  private static final long serialVersionUID = 2883393570827924458L;

  @Inject
  HttpServletRequest request;

  public UserRequestController() {
  }

  public String getUriWithRedirect() {
    String uri = request.getRequestURI();
    if (uri.contains("faces-redirect=true")) {
      return uri;
    }
    StringBuilder result = new StringBuilder();
    result.append(uri);
    if (uri.contains("?")) {
      result.append('&');
    } else {
      result.append('?');
    }
    result.append("faces-redirect=true");
    return result.toString();
  }


  public int getSessionInactivityTimeout() {
    ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
    int result = ec.getSessionMaxInactiveInterval();
    if (result < 20) {
      result = 20;
    } else {
      result -= 10;
    }
    return result;
  }
}
