/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.web.resources.worldMap;

import org.terasology.math.geom.Vector3i;

/**
 * This class defines the data format that is sent from the frontend for use in {@link WorldMapResource}.
 */
@SuppressWarnings("unused")
public final class WorldMapInput {

    private Vector3i center;
    private int mapBlockWidth;
    private int mapBlockLength;
    private boolean surface;

    public Vector3i getCenter() {
        return center;
    }

    public int getMapBlockWidth() {
        return mapBlockWidth;
    }

    public int getMapBlockLength() {
        return mapBlockLength;
    }

    public boolean isSurface() {
        return surface;
    }
}
