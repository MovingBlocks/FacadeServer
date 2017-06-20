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

public class ClientMessage {

    public enum MessageType {
        AUTHENTICATION_REQUEST,
        AUTHENTICATION_DATA,
        RESOURCE
    }

    private MessageType messageType;
    private JsonElement data;

    public MessageType getMessageType() {
        return messageType;
    }

    public JsonElement getData() {
        return data;
    }

    public void checkValid() throws InvalidClientMessageException {
        if (messageType == null) {
            throw new InvalidClientMessageException(InvalidClientMessageException.Reason.MESSAGETYPE_EMPTY);
        } else if (messageType == MessageType.AUTHENTICATION_REQUEST && data != null) {
            throw new InvalidClientMessageException(InvalidClientMessageException.Reason.DATA_NOT_REQUIRED);
        } else if (messageType != MessageType.AUTHENTICATION_REQUEST && data == null) {
            throw new InvalidClientMessageException(InvalidClientMessageException.Reason.DATA_REQUIRED);
        }
    }
}
