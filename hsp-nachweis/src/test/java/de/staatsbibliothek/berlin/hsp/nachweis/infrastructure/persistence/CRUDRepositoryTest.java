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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.Test;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 07.12.20
 */
class CRUDRepositoryTest {


  @Test
  void testSaveAll() {
    CRUDRepository<Object, String> testling = mock(CRUDRepository.class, CALLS_REAL_METHODS);
    EntityManager entityManager = mock(EntityManager.class);
    Object obj = new Object();
    List<Object> list = new ArrayList<>(List.of(obj, obj));

    doReturn(obj).when(entityManager).merge(obj);
    doReturn(entityManager).when(testling).getEntityManager();

    testling.saveAll(list);

    verify(entityManager, times(2)).merge(any());

  }

  @Test
  void testSave() {
    CRUDRepository<Object, String> testling = mock(CRUDRepository.class, CALLS_REAL_METHODS);
    EntityManager entityManager = mock(EntityManager.class);
    Object obj = new Object();

    doReturn(obj).when(entityManager).merge(obj);
    doReturn(entityManager).when(testling).getEntityManager();

    testling.save(obj);

    verify(entityManager, times(1)).merge(any());
  }

  @Test
  void testListAll() {
    CRUDRepository<Object, String> testling = mock(CRUDRepository.class, CALLS_REAL_METHODS);
    PanacheQuery<Object> panacheQuery = mock(PanacheQuery.class);
    Object obj = new Object();
    doReturn(obj).when(panacheQuery).firstResult();
    doReturn(panacheQuery).when(panacheQuery).withHint("org.hibernate.cacheable", "true");
    doReturn(panacheQuery).when(panacheQuery).withHint("org.hibernate.cacheable", "false");

    doReturn(panacheQuery).when(testling).findAll();

    assertNotNull(testling.listAll(true));

    verify(panacheQuery, times(1)).withHint("org.hibernate.cacheable", "true");

    assertNotNull(testling.listAll(false));

    verify(panacheQuery, times(1)).withHint("org.hibernate.cacheable", "false");
  }

  @Test
  void testFirstEntityFromList() {
    CRUDRepository<Object, String> testling = mock(CRUDRepository.class, CALLS_REAL_METHODS);
    PanacheQuery<Object> panacheQuery = mock(PanacheQuery.class);
    Object obj = new Object();
    List<Object> list = List.of(obj, obj);
    doReturn(panacheQuery).when(panacheQuery).range(anyInt(), anyInt());
    doReturn(list.get(0)).when(panacheQuery).firstResult();

    assertNotNull(testling.firstEntityFromList(panacheQuery));
  }
}
