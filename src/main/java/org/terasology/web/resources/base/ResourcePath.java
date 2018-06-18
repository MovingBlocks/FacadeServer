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

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;

/**
 * This class represents the path to a resource with a series of strings. Example: config/MOTD
 */
// TODO: consider making it immutable and returning a new instance for each operation
public class ResourcePath {

    private Deque<String> items;

    public ResourcePath(Collection<String> items) {
        this.items = new ArrayDeque<>(items);
        this.items.removeIf(String::isEmpty);
    }

    public ResourcePath(String... items) {
        this(Arrays.asList(items));
    }

    public static ResourcePath createEmpty() {
        return new ResourcePath(Collections.emptyList());
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
            throw ResourceAccessException.NOT_FOUND;
        }
        return consumeNextItem();
    }

    public void assertEmpty() throws ResourceAccessException {
        if (!items.isEmpty()) {
            throw ResourceAccessException.NOT_FOUND;
        }
    }

    public ResourcePath pushItem(String item) {
        items.offerFirst(item);
        return this;
    }

    public Collection<String> getItemList() {
        return items;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ResourcePath && toString().equals(other.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public ResourcePath clone() {
        return new ResourcePath(getItemList());
    }

    @Override
    public String toString() {
        return String.join("/", items);
    }
}
