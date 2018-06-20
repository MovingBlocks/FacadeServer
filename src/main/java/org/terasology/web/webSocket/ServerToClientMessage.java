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
package org.terasology.web.webSocket;

import com.google.gson.JsonElement;

import java.util.Collection;

/**
 * Represents a message to the client from the server.
 */
public class ServerToClientMessage {

    public enum MessageType {
        ACTION_RESULT,
        RESOURCE_CHANGED,
        RESOURCE_EVENT
    }

    private MessageType messageType;
    private Collection<String> resourcePath;
    private JsonElement data;

    public ServerToClientMessage(MessageType messageType, Collection<String> resourcePath, JsonElement data) {
        this.messageType = messageType;
        this.resourcePath = resourcePath;
        this.data = data;
    }

    public ServerToClientMessage(MessageType messageType, JsonElement data) {
        this.messageType = messageType;
        this.data = data;
    }
}
