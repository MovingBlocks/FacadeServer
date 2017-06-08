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

public class InvalidClientMessageException extends Exception {

    public enum Reason {
        MESSAGETYPE_EMPTY("messageType is empty or not valid"),
        DATA_REQUIRED("data is required"),
        DATA_NOT_REQUIRED("no data must be sent");

        private final String message;

        Reason(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public InvalidClientMessageException(Reason reason) {
        super("The received message is not valid: " + reason.getMessage());
    }
}
