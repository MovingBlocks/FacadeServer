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
package org.terasology.web.resources;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.terasology.network.Client;
import org.terasology.web.io.ActionResult;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

// at the moment, this is simply a proxy to the meta server API which is not
// accessible directly from a browser since CORS is not enabled
public class AvailableModulesResource implements ReadableResource<JsonArray> {

    private static final URL URL;
    private static final Gson GSON = new Gson();

    static {
        URL url = null;
        try {
            url = new URL("http://meta.terasology.org/modules/list/latest");
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        URL = url;
    }

    @Override
    public String getName() {
        return "availableModules";
    }

    @Override
    public JsonArray read(Client requestingClient) throws ResourceAccessException {
        try {
            URLConnection conn = URL.openConnection();
            return GSON.fromJson(new InputStreamReader(conn.getInputStream()), JsonArray.class);
        } catch (IOException ex) {
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.GENERIC_ERROR, ex.getMessage()));
        }
    }
}
