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
package org.terasology.web.resources.base;

import org.terasology.web.resources.ResourceAccessException;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

public class ResourcePath {

    private Deque<String> items;

    public ResourcePath(Collection<String> items) {
        this.items = new ArrayDeque<>(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public String consumeNextItem() throws ResourceAccessException {
        if (items.isEmpty()) {
            throw ResourceAccessException.NOT_FOUND;
        }
        return items.pollFirst();
    }

    public String assertAndConsumeLastItem() throws ResourceAccessException {
        if (items.size() != 1) {
            throw new RuntimeException("The path is either empty or the end hasn't ben reached yet"); // TODO change exception type and message
        }
        return consumeNextItem();
    }

    public void assertEmpty() {
        if (!items.isEmpty()) {
            throw new RuntimeException("The path is not empty"); // TODO change exception type and message
        }
    }
}
