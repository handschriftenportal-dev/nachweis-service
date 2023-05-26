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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 01.06.21
 */
public class Permissions {
  public static final String B_HSP_ADMIN_APP = "B-HSPADM-APP";
  public static final String B_HSP_LOKALE_REDAKTEUR_APP = "B-HSPLRED-APP";
  public static final String B_HSP_LOKALE_REDAKTEUR_COMMON_APP = "B-HSPLREDCMN-APP";
  public static final String B_HSP_CMS_REDAKTEUR_APP = "B-HSPCMS-APP";
  public static final String B_HSP_NORMDATEN_REDAKTEUR_APP = "B-HSPNORM-APP";
  public static final String B_HSP_REDAKTEUR_APP = "B-HSPRED-APP";
  public static final String B_HSP_ERSCHLIESSER_APP = "B-HSPERSCH-APP";
  public static final String B_HSP_ERSCHLIESSER_COMMON_APP = "B-HSPERSCHCMN-APP";
  public static final String B_HSP_ENDNUTZER_APP = "B-HSP-APP";
}
