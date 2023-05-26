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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.jta;

import javax.transaction.xa.XAException;

/**
 * Custom XAException to allow us to provide additional information It is thrown by the Resource Manager to inform the
 * Transaction Manager of an error encountered by the involved transaction.
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 04.03.21
 */
public class HSPXAException extends XAException {

  private static final long serialVersionUID = 690701490953030354L;

  public HSPXAException(String message, int errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  public HSPXAException(String message, Throwable cause, int errorCode) {
    super(message);
    this.initCause(cause);
    this.errorCode = errorCode;
  }

  public HSPXAException(Throwable cause, int errorCode) {
    super(errorCode);
    this.initCause(cause);
  }
}
