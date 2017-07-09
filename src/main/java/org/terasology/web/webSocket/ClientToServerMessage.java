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
import org.terasology.web.io.gsonUtils.InvalidClientMessageException;
import org.terasology.web.io.gsonUtils.Validable;

public class ClientToServerMessage implements Validable {

    public enum MessageType {
        AUTHENTICATION_REQUEST,
        AUTHENTICATION_DATA,
        RESOURCE_REQUEST
    }

    private MessageType messageType;
    private JsonElement data;

    public MessageType getMessageType() {
        return messageType;
    }

    public JsonElement getData() {
        return data;
    }

    @Override
    public void validate() throws InvalidClientMessageException {
        if (messageType == null) {
            throw new InvalidClientMessageException("messageType is empty or not valid");
        } else if (messageType == MessageType.AUTHENTICATION_REQUEST && data != null) {
            throw new InvalidClientMessageException("no data must be sent");
        } else if (messageType != MessageType.AUTHENTICATION_REQUEST && data == null) {
            throw new InvalidClientMessageException("data is required");
        }
    }
}
