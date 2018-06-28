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
import org.terasology.math.geom.Vector3i;
import org.terasology.utilities.Assets;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.tiles.BlockTile;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WorldMapMetadata {

    private transient WorldProvider worldProvider;
    private transient Logger logger = LoggerFactory.getLogger(WorldMapMetadata.class);
    private Vector3i topLeft;
    private List<List<String>> blocks;
    private Map<String, String> blockLut;
    private int mapBlockWidth;
    private int mapBlockLength;

    // TODO remove test constructor
    WorldMapMetadata(WorldProvider worldProvider) {
        topLeft = new Vector3i(-16, 35, -16);
        mapBlockWidth = 32;
        mapBlockLength = 32;
        this.worldProvider = worldProvider;
        blocks = new ArrayList<>(mapBlockWidth);
        for (int i = 0; i < mapBlockWidth; i++) {
            blocks.add(i, new ArrayList<>(mapBlockLength));
        }
        blockLut = new HashMap<>();
        this.getWorldMapData();
    }

    WorldMapMetadata(WorldProvider worldProvider, Vector3i topLeft, int mapBlockWidth, int mapBlockLength) {
        this.topLeft = topLeft;
        this.mapBlockWidth = mapBlockWidth;
        this.mapBlockLength = mapBlockLength;
        this.worldProvider = worldProvider;
        blocks = new ArrayList<>();
        blockLut = new HashMap<>();
        this.getWorldMapData();
    }

    private void getWorldMapData() {
        for (int x = topLeft.getX(); x < mapBlockWidth + topLeft.getX(); ++x) {
            for (int z = topLeft.getZ(); z < mapBlockLength + topLeft.getZ(); ++z) {
                Block block = worldProvider.getBlock(x, topLeft.getY(), z);
                ResourceUrn blockUrn = block.getURI().getBlockFamilyDefinitionUrn();
                if (blockLut.get(blockUrn.toString()) == null) {
                    if (Assets.get(blockUrn, BlockTile.class).isPresent()) {
                        BufferedImage blockImage = Assets.get(blockUrn, BlockTile.class).get().getImage();
                        blockLut.put(blockUrn.toString(), Integer.toHexString(getAverageRGB(blockImage)));
                    } else {
                        logger.error("unable to get the texture for block " + blockUrn.toString());
                    }
                }
                blocks.get(x - topLeft.getX()).add(z - topLeft.getZ(), blockUrn.toString());
            }
        }
    }

    private int getAverageRGB(BufferedImage bufferedImage) {
        int r = 0;
        int g = 0;
        int b = 0;
        for (int x = 0; x < bufferedImage.getWidth(); ++x) {
            for (int y = 0; y < bufferedImage.getHeight(); ++y) {
                Color color = new Color(bufferedImage.getRGB(x, y));
                r += color.getRed();
                g += color.getGreen();
                b += color.getBlue();
            }
        }
        int imageSize = bufferedImage.getWidth() * bufferedImage.getHeight();
        r /= imageSize;
        g /= imageSize;
        b /= imageSize;
        // get rid of the alpha because it is always 255
        return new Color(r, g, b).getRGB() & 0x00FFFFFF;
    }
}
