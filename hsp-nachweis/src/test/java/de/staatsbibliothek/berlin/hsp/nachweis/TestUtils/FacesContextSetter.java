package de.staatsbibliothek.berlin.hsp.nachweis.TestUtils;

import jakarta.faces.context.FacesContext;

public abstract class FacesContextSetter extends FacesContext {
  public static void set(FacesContext context) {
    FacesContext.setCurrentInstance(context);
  }
}