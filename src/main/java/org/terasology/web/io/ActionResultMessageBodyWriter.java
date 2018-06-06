/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.web.io;

import com.google.common.collect.ImmutableMap;
import org.terasology.web.servlet.JsonWebApplicationException;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * {@link MessageBodyWriter} implementation for ActionResult, which writes the result of an ActionResult to a stream.
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ActionResultMessageBodyWriter implements MessageBodyWriter<ActionResult> {

    private static final Map<ActionResult.Status, Response.Status> ERRORMAP = ImmutableMap.<ActionResult.Status, Response.Status>builder()
            .put(ActionResult.Status.BAD_REQUEST, Response.Status.BAD_REQUEST)
            .put(ActionResult.Status.FORBIDDEN, Response.Status.FORBIDDEN)
            .put(ActionResult.Status.NOT_FOUND, Response.Status.NOT_FOUND)
            .put(ActionResult.Status.ACTION_NOT_ALLOWED, Response.Status.METHOD_NOT_ALLOWED)
            .put(ActionResult.Status.GENERIC_ERROR, Response.Status.INTERNAL_SERVER_ERROR)
            .put(ActionResult.Status.CONFLICT, Response.Status.CONFLICT)
            .build();

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public long getSize(ActionResult actionResult, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(ActionResult actionResult, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        if (actionResult.getStatus() != ActionResult.Status.OK) {
            throw new JsonWebApplicationException(actionResult.getMessage(), ERRORMAP.get(actionResult.getStatus()));
        }
        if (actionResult.getData() != null && !actionResult.getData().isJsonNull()) {
            try (Writer writer = new OutputStreamWriter(entityStream, StandardCharsets.UTF_8)) {
                writer.write(actionResult.getData().toString());
            }
        }
    }
}
