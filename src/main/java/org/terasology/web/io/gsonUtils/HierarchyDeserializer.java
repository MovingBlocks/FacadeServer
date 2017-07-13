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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

// inspired by https://stackoverflow.com/a/8683689
public class HierarchyDeserializer<T> implements JsonDeserializer<T> {

    //example: com.mypackage.%s
    private String classNameTemplate;

    public HierarchyDeserializer(String classNameTemplate) {
        this.classNameTemplate = classNameTemplate;
    }

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();
        String typeName = String.format(classNameTemplate, root.get("type").getAsString());
        Class clazz;
        try {
            clazz = Class.forName(typeName);
        } catch (ClassNotFoundException ex) {
            throw new JsonParseException("Invalid type " + typeName, ex);
        }
        if (!((Class<?>) typeOfT).isAssignableFrom(clazz)) {
            throw new JsonParseException("Type " + typeName + " is not supported");
        }
        return context.deserialize(root.get("data"), clazz);
    }
}
