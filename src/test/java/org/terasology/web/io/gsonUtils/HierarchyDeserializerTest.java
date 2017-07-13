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
package org.terasology.web.io.gsonUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HierarchyDeserializerTest {

    private interface Parent {
    }

    private class Child1 implements Parent {
        private String field;
    }

    private class Child2 implements Parent {
        private int field;
    }

    private static final String CLASS1NAME = "Child1";
    private static final String CLASS2NAME = "Child2";
    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(Parent.class, new HierarchyDeserializer<Parent>("org.terasology.web.io.gsonUtils.HierarchyDeserializerTest$%s"))
            .create();

    @Test
    public void testDeserializeOk() {
        Parent test1 = gson.fromJson("{\"type\":\"" + CLASS1NAME + "\", \"data\":{\"field\":\"test\"}}", Parent.class);
        assertTrue(test1 instanceof Child1);
        assertEquals("test", ((Child1)test1).field);

        Parent test2 = gson.fromJson("{\"type\":\"" + CLASS2NAME + "\", \"data\":{\"field\":1}}", Parent.class);
        assertTrue(test2 instanceof Child2);
        assertEquals(1, ((Child2)test2).field);
    }

    @Test(expected = JsonParseException.class)
    public void testInvalidType() {
        gson.fromJson("{\"type\":\"java.lang.String\", \"data\":\"this is a string\"}", Parent.class);
    }

}
