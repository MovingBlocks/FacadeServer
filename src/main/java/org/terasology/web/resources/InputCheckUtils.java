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
import org.terasology.web.resources.base.ResourceAccessException;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Utility class used to check inputs.
 */
public final class InputCheckUtils {

    private InputCheckUtils() {
    }

    public static <T> void checkPredicate(T obj, Predicate<T> predicate, String errorMessage) throws ResourceAccessException {
        if (!predicate.test(obj)) {
            throw new ResourceAccessException(new ActionResult(ActionResult.Status.BAD_REQUEST, errorMessage));
        }
    }

    public static void checkNull(Object obj, String errorMessage) throws ResourceAccessException {
        checkPredicate(obj, Objects::isNull, errorMessage);
    }

    public static void checkNotNull(Object obj, String errorMessage) throws ResourceAccessException {
        checkPredicate(obj, Objects::nonNull, errorMessage);
    }

    public static void checkNotNullOrEmpty(String str, String errorMessage) throws ResourceAccessException {
        checkNotNull(str, errorMessage);
        checkPredicate(str, s -> !s.isEmpty(), errorMessage);
    }
}
