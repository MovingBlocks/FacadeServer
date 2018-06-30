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
    private transient boolean isSurface;
    private Vector3i center;
    private List<List<String>> blocks;
    private Map<String, String> blockLut;
    private int mapBlockWidth;
    private int mapBlockLength;

    WorldMapMetadata(WorldProvider worldProvider, Vector3i center, int mapBlockWidth, int mapBlockLength, boolean isSurface) {
        this.isSurface = isSurface;
        this.center = center;
        this.mapBlockWidth = mapBlockWidth;
        this.mapBlockLength = mapBlockLength;
        this.worldProvider = worldProvider;
        this.blocks = new ArrayList<>(mapBlockWidth);
        for (int i = 0; i < mapBlockWidth; i++) {
            blocks.add(i, new ArrayList<>(mapBlockLength));
        }
        this.blockLut = new HashMap<>();
        this.getWorldMapData();
    }

    private void getWorldMapData() {
        int y = 40;
        for (int x = (int) Math.floor((double) center.getX() - mapBlockWidth / 2); x < (int) Math.ceil((double) mapBlockWidth / 2 + center.getX()); ++x) {
            for (int z = (int) Math.floor((double) center.getZ() - mapBlockLength / 2); z < (int) Math.ceil((double) mapBlockLength / 2 + center.getZ()); ++z) {
                y = isSurface ? getSurfaceY(x, y, z) : center.getY();
                Block block = worldProvider.getBlock(x, y, z);
                ResourceUrn blockUrn = block.getURI().getBlockFamilyDefinitionUrn();
                if (blockLut.get(blockUrn.toString()) == null) {
                    if (Assets.get(blockUrn, BlockTile.class).isPresent()) {
                        BufferedImage blockImage = Assets.get(blockUrn, BlockTile.class).get().getImage();
                        blockLut.put(blockUrn.toString(), Integer.toHexString(getAverageRGB(blockImage)));
                    } else {
                        logger.info("unable to get the texture for block " + blockUrn.toString());
                    }
                }
                blocks.get(x - center.getX() + mapBlockWidth / 2).add(z - center.getZ() + mapBlockLength / 2, blockUrn.toString());
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

    //This method is heavily inspired by the renderCell method of the MinimapGrid class in the Minimap module.
    private int getSurfaceY(int x, int yEstimate, int z) {
        int y = yEstimate;
        Block block = worldProvider.getBlock(x, yEstimate, z);
        if (isIgnoredByMinimap(block)) {
            while (isIgnoredByMinimap(block)) {
                --y;
                block = worldProvider.getBlock(x, y, z);
            }
        } else {
            while (!isIgnoredByMinimap(block)) {
                ++y;
                block = worldProvider.getBlock(x, y, z);
            }
            --y;
        }
        return y;
    }

    private static boolean isIgnoredByMinimap(Block block) {
        return block.isPenetrable() && !block.isWater();
    }
}
