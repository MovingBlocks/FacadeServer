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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3i;
import org.terasology.rendering.nui.Color;
import org.terasology.utilities.Assets;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.tiles.BlockTile;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WorldMapMetadata {

    private transient WorldProvider worldProvider;
    private transient Logger logger = LoggerFactory.getLogger(WorldMapMetadata.class);
    private Vector3i center;
    private List<String> blocks;
    private Map<String, Color> blockLut;
    private int mapBlockWidth;
    private int mapBlockLength;

    WorldMapMetadata(WorldProvider worldProvider) {
        center = new Vector3i(0, 35, 0);
        mapBlockWidth = 32;
        mapBlockLength = 32;
        this.worldProvider = worldProvider;
        blocks = new ArrayList<>();
        blockLut = new HashMap<>();
        this.getWorldMapData();
    }

    private void getWorldMapData() {
        for (int x = center.getX() - mapBlockWidth / 2; x < mapBlockWidth / 2; ++x) {
            for (int z = center.getZ() - mapBlockLength / 2; z < mapBlockLength / 2; ++z) {
                Block block = worldProvider.getBlock(x, center.getY(), z);
                ResourceUrn blockUrn = block.getURI().getBlockFamilyDefinitionUrn();
                if (blockLut.get(blockUrn.toString()) == null) {
                    if (Assets.get(blockUrn, BlockTile.class).isPresent()) {
                        BufferedImage blockImage = Assets.get(blockUrn, BlockTile.class).get().getImage();
                        blockLut.put(blockUrn.toString(), new Color(getAverageRGB(blockImage)));
                    } else {
                        logger.error("unable to get the texture for block " + blockUrn.toString());
                    }
                }
                blocks.add(blockUrn.toString());
            }
        }
    }

    private int getAverageRGB(BufferedImage bufferedImage) {
        long average = 0;
        for (int x = 0; x < bufferedImage.getWidth(); ++x) {
            for (int y = 0; y < bufferedImage.getHeight(); ++y) {
                average += bufferedImage.getRGB(x, y);
            }
        }
        average /= bufferedImage.getWidth() * bufferedImage.getHeight();
        return (int) average;
    }
}
