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
package org.terasology.web.resources.onlinePlayers;

import org.terasology.network.Client;
import org.terasology.rendering.nui.Color;

import java.util.Objects;

public class OnlinePlayerMetadata {

    private String id;
    private String name;
    private RgbaColor color;

    public OnlinePlayerMetadata(String id, String name, Color color) {
        this.id = id;
        this.name = name;
        this.color = new RgbaColor(color);
    }

    public OnlinePlayerMetadata(Client client) {
        this(client.getId(), client.getName(), client.getColor());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OnlinePlayerMetadata) {
            OnlinePlayerMetadata other = (OnlinePlayerMetadata) obj;
            return id.equals(other.id) && name.equals(other.name) && color.equals(other.color);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, color);
    }

    private final class RgbaColor {
        private int r;
        private int g;
        private int b;
        private int a;

        private RgbaColor(Color source) {
            r = source.r();
            g = source.g();
            b = source.b();
            a = source.a();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof RgbaColor) {
                RgbaColor other = (RgbaColor) obj;
                return r == other.r && g == other.g && b == other.b && a == other.a;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(r, g, b, a);
        }
    }
}
