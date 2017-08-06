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

import org.terasology.web.io.ActionResult;

public final class InputCheckUtils {

    private InputCheckUtils() {
    }

    public static void checkNotNull(Object obj, String errorMessage) throws ResourceAccessException {
        if (obj == null) {
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.BAD_REQUEST, errorMessage));
        }
    }

    public static void checkNotNullOrEmpty(String str, String errorMessage) throws ResourceAccessException {
        checkNotNull(str, errorMessage);
        if (str.isEmpty()) {
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.BAD_REQUEST, errorMessage));
        }
    }
}
