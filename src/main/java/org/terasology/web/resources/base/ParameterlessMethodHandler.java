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

import org.terasology.network.Client;

/**
 * Method handler for {@link ResourceMethodFactory} which is used for resources where the client needs to get
 * data from the server after the {@link ResourceMethod} finishes executing. Used in GET methods.
 * @param <INTYPE> the type of data sent to the server from the client through the {@link ResourceMethod}.
 * @param <OUTTYPE> the type of data received from server by the {@link ResourceMethod} after executing.
 */
@FunctionalInterface
public interface ParameterlessMethodHandler<INTYPE, OUTTYPE> {

    OUTTYPE perform(INTYPE data, Client client) throws ResourceAccessException;
}
