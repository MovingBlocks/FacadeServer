/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.web.servlet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.mvc.Viewable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

/**
 * Show the about html page.
 */
@Path("/")
public class AboutServlet {

    private static final Logger logger = LoggerFactory.getLogger(AboutServlet.class);

    @GET
    @Path("home")
    @Produces(MediaType.TEXT_HTML)
    public Viewable about() {
        logger.info("Requested about as HTML");
        ImmutableMap<Object, Object> dataModel = ImmutableMap.builder()
                .put("version", VersionInfo.getVersion())
                .build();
        return new Viewable("/about.ftl", dataModel);
    }
}
