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
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ValidatorTypeAdapterFactoryTest {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(ValidatorTypeAdapterFactory.getInstance())
            .create();

    private static final class ValidableMock implements Validable {
        private String data;
        private boolean mustThrow;

        private ValidableMock(String data, boolean mustThrow) {
            this.data = data;
            this.mustThrow = mustThrow;
        }

        @Override
        public void validate() throws InvalidClientMessageException {
            if (mustThrow) {
                throw new InvalidClientMessageException("");
            }
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof ValidableMock) {
                return ((ValidableMock) other).data.equals(data) && ((ValidableMock) other).mustThrow == mustThrow;
            }
            return false;
        }
    }

    private ValidableMock alwaysValid;
    private ValidableMock alwaysInvalid;
    private JsonObject alwaysValidSerialized;
    private JsonObject alwaysInvalidSerialized;

    @Before
    public void setUp() {
        alwaysValid = new ValidableMock("testData", false);
        alwaysInvalid = new ValidableMock("testData", true);
        alwaysValidSerialized = new JsonObject();
        alwaysValidSerialized.addProperty("data", "testData");
        alwaysValidSerialized.addProperty("mustThrow", false);
        alwaysInvalidSerialized = new JsonObject();
        alwaysInvalidSerialized.addProperty("data", "testData");
        alwaysInvalidSerialized.addProperty("mustThrow", true);
    }

    @Test
    public void testWrite() {
        //writing should work exactly as the default serializers regardless of validation
        assertEquals(new JsonPrimitive("testString"), gson.toJsonTree("testString"));
        //so, these must not throw
        assertEquals(alwaysValidSerialized, gson.toJsonTree(alwaysValid));
        assertEquals(alwaysInvalidSerialized, gson.toJsonTree(alwaysInvalid));
    }

    @Test
    public void testReadValid() {
        assertEquals(alwaysValid, gson.fromJson(alwaysValidSerialized, ValidableMock.class));
    }

    @Test(expected = JsonSyntaxException.class)
    public void testReadInvalid() {
        gson.fromJson(alwaysInvalidSerialized, ValidableMock.class);
    }

}
