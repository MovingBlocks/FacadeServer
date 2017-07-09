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
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public final class ValidatorTypeAdapterFactory implements TypeAdapterFactory {

    private static final ValidatorTypeAdapterFactory INSTANCE = new ValidatorTypeAdapterFactory();

    private ValidatorTypeAdapterFactory() {
    }

    public static ValidatorTypeAdapterFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                delegate.write(out, value);
            }
            @Override
            public T read(JsonReader in) throws IOException {
                T value = delegate.read(in);
                if (value == null) {
                    throw new InvalidClientMessageException("the message must not be a JSON null value");
                } else if (value instanceof Validable) {
                    ((Validable) value).validate();
                }
                return value;
            }
        };
    }
}
