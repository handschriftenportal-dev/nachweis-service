package de.staatsbibliothek.berlin.hsp.nachweis.application.util;

import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerFactory;

/**
 * Registers the {@link RefererViewExpiredExceptionHandler}, see faces-config.xml.
 */
public class RefererViewExpiredExceptionHandlerFactory extends ExceptionHandlerFactory {

  public RefererViewExpiredExceptionHandlerFactory(ExceptionHandlerFactory wrapped) {
    super(wrapped);
  }

  @Override
  public ExceptionHandler getExceptionHandler() {
    return new RefererViewExpiredExceptionHandler(getWrapped().getExceptionHandler());
  }
}
