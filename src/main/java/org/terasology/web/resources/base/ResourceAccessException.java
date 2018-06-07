/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.web.resources.base;

import org.terasology.web.io.ActionResult;

/**
 * This exception is thrown in several different situations, such as when attempting to access a
 * resource which does not exist or when the requested resource doesn't support the specified method.
 */
// TODO: consider decoupling from ActionResult (which is JSON specific)
public class ResourceAccessException extends Exception {

    public static final ResourceAccessException NOT_FOUND = new ResourceAccessException(
            new ActionResult(ActionResult.Status.NOT_FOUND, "Resource not found"));
    public static final ResourceAccessException METHOD_NOT_ALLOWED = new ResourceAccessException(
            new ActionResult(ActionResult.Status.ACTION_NOT_ALLOWED, "Method not supported by this resource"));

    private final ActionResult resultToSend;

    public ResourceAccessException(ActionResult resultToSend) {
        super("Failed to access requested resource: " + resultToSend.getStatus().toString() + resultToSend.getMessage());
        this.resultToSend = resultToSend;
    }

    public ActionResult getResultToSend() {
        return resultToSend;
    }
}
