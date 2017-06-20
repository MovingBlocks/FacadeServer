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

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class ByteArrayBase64SerializerTest {

    @Test
    public void testSerializer() {
        ByteArrayBase64Serializer serializer = new ByteArrayBase64Serializer();
        byte[] data = new byte[10];
        new Random(1).nextBytes(data);
        JsonElement serialized = serializer.serialize(data, null, null);
        assertTrue(serialized.isJsonPrimitive());
        assertTrue(((JsonPrimitive) serialized).isString());
        assertArrayEquals(data, serializer.deserialize(serialized, null, null));
    }
}
