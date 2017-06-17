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

public class ResourceRequestClientMessage {

    public enum Action {
        READ,
        WRITE
    }

    private Action action;
    private String resourceName;
    private JsonElement data;

    public Action getAction() {
        return action;
    }

    public String getResourceName() {
        return resourceName;
    }

    public JsonElement getData() {
        return data;
    }

    public void checkValid() throws InvalidClientMessageException {
        if (action == null) {
            throw new InvalidClientMessageException(InvalidClientMessageException.Reason.ACTION_REQUIRED);
        } else if (resourceName == null) {
            throw new InvalidClientMessageException(InvalidClientMessageException.Reason.RESOURCENAME_REQUIRED);
        } else if (action == Action.WRITE && data == null) {
            throw new InvalidClientMessageException(InvalidClientMessageException.Reason.DATA_REQUIRED);
        } else if (action == Action.READ && data != null) {
            throw new InvalidClientMessageException(InvalidClientMessageException.Reason.DATA_NOT_REQUIRED);
        }
    }
}
