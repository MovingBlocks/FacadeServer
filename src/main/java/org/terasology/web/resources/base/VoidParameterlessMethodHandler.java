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


import org.terasology.engine.network.Client;

/**
 * Method handler for {@link ResourceMethodFactory} that is used for resources where the server does not need to
 * give any data back to the client after the method executes. Used in PUT, POST, PATCH, and DELETE methods.
 * @param <INTYPE> the type of data sent to the server from the client through the {@link ResourceMethod}.
 */
public interface VoidParameterlessMethodHandler<INTYPE> {

    void perform(INTYPE data, Client client) throws ResourceAccessException;
}
