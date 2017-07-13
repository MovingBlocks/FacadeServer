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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

public class ActionResult {

    public static final ActionResult OK = new ActionResult(Status.OK);

    public enum Status {
        OK,
        BAD_REQUEST,
        UNAUTHORIZED,
        ACTION_NOT_ALLOWED,
        NOT_FOUND,
        GENERIC_ERROR
    }

    private Status status;
    private String message;
    private JsonElement data;

    public ActionResult(Status status, String message, JsonElement data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public ActionResult(Status status, String message) {
        this(status, message, null);
    }

    public ActionResult(Status status) {
        this(status, "");
    }

    public ActionResult(JsonElement data) {
        this(Status.OK, null, data);
    }

    public ActionResult(JsonSyntaxException ex) {
        this(Status.BAD_REQUEST, getExceptionMessage(ex), null);
    }

    private static String getExceptionMessage(JsonSyntaxException ex) {
        Throwable cause = ex.getCause();
        if (cause != null) {
            return cause.getMessage();
        }
        return ex.getMessage();
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public JsonElement getData() {
        return data;
    }

    public JsonElement toJsonTree(Gson gson) {
        return gson.toJsonTree(this);
    }
}
