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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege.SchemaPflegeDatei;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.eureka.RestClientRequestFilter;
import java.util.Collection;
import java.util.Collections;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 28.03.22
 */

@Path("/schemaresourcefile")
@RegisterRestClient
@RegisterProvider(RestClientRequestFilter.class)
public interface SchemaPflegeDateiServicePort {

  @GET
  @Retry(maxRetries = 2)
  @Timeout(1000)
  @Path("/{id}")
  @Produces("application/json")
  SchemaPflegeDatei findById(@PathParam(value = "id") String id);

  @GET
  @Retry(maxRetries = 2)
  @Timeout(1000)
  @Path("/")
  @Fallback(SchemaResourceDateiFallbackHandler.class)
  @Produces("application/json")
  Collection<SchemaPflegeDatei> findAll();

  @PUT
  @Retry(maxRetries = 2)
  @Timeout(5000)
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Path("/{id}")
  Response upload(@PathParam(value = "id") String id, @MultipartForm SchemaPflegeDateiMultipartBody multipartBody);

  class SchemaResourceDateiFallbackHandler implements FallbackHandler<Collection<SchemaPflegeDatei>> {

    @Override
    public Collection<SchemaPflegeDatei> handle(ExecutionContext context) {
      return Collections.emptyList();
    }
  }

}
