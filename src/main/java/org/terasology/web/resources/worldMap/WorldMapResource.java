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
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.utilities.Assets;
import org.terasology.web.resources.base.AbstractSimpleResource;
import org.terasology.web.resources.base.ClientSecurityRequirements;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.ResourceMethod;
import org.terasology.web.resources.base.ResourcePath;
import org.terasology.world.RelevanceRegionComponent;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.tiles.BlockTile;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.ChunkProvider;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.terasology.web.resources.base.ResourceMethodFactory.createParameterlessMethod;

public class WorldMapResource extends AbstractSimpleResource {

    private static final Logger logger = LoggerFactory.getLogger(WorldMapResource.class);

    @In
    private WorldProvider worldProvider;

    @In
    private EntityManager entityManager;

    // TODO: reimplement this as a GET request with parameters
    @Override
    protected ResourceMethod<WorldMapInput, String> getPutMethod(ResourcePath path) throws ResourceAccessException {
        return createParameterlessMethod(path, ClientSecurityRequirements.PUBLIC, WorldMapInput.class,
                (data, client) -> getWorldMapBase64ImageString(data.getCenter(), data.getMapBlockWidth(), data.getMapBlockLength(), data.isSurface(), client.getEntity()));
    }

    private String getWorldMapBase64ImageString(Vector3i center, int mapBlockWidth, int mapBlockLength, boolean isSurface, EntityRef clientEntity) {
        final int colorSizeMultiplier = mapBlockWidth * mapBlockLength <= 125 * 125 ? 60 : 30;
        int blockY = 40;
        List<List<Color>> colors = new ArrayList<>(mapBlockWidth);
        for (int i = 0; i < mapBlockWidth; ++i) {
            colors.add(i, new ArrayList<>(mapBlockLength));
        }


        loadChunks(center, mapBlockWidth, mapBlockLength);

        for (int x = (int) Math.floor((double) center.getX() - mapBlockWidth / 2); x < (int) Math.ceil((double) mapBlockWidth / 2 + center.getX()); ++x) {
            for (int z = (int) Math.floor((double) center.getZ() - mapBlockLength / 2); z < (int) Math.ceil((double) mapBlockLength / 2 + center.getZ()); ++z) {
                while (worldProvider.getBlock(x, blockY, z).getURI().toString().equals("engine:unloaded")) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                blockY = isSurface ? getSurfaceY(x, blockY, z) : center.getY();
                Block block = worldProvider.getBlock(x, blockY, z);
                ResourceUrn blockUrn = block.getURI().getBlockFamilyDefinitionUrn();
                if (Assets.get(blockUrn, BlockTile.class).isPresent()) {
                    BufferedImage blockImage = Assets.get(blockUrn, BlockTile.class).get().getImage();
                    colors.get(x - (center.getX() - mapBlockWidth / 2)).add(new Color(getColorOfTexture(blockImage, blockY)));
                } else {
                    logger.warn("cannot find texture of block " + blockUrn.toString());
                    colors.get(x - (center.getX() - mapBlockWidth / 2)).add(Color.BLACK);
                }
            }
        }

        int colorSize = (int) (1 / Math.floor(Math.log(mapBlockWidth * mapBlockLength)) * colorSizeMultiplier);
        BufferedImage mapImage = new BufferedImage(mapBlockWidth * colorSize, mapBlockLength * colorSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = mapImage.createGraphics();
        for (int x = 0; x < mapBlockWidth; ++x) {
            for (int y = 0; y < mapBlockLength; ++y) {
                graphics.setColor(colors.get(x).get(y));
                graphics.fillRect(colorSize * x, colorSize * y, colorSize, colorSize);
            }
        }

        graphics.dispose();
        return convertImageToBase64String(mapImage);
    }

    private String convertImageToBase64String(final BufferedImage img) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "png", byteArrayOutputStream);
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            logger.error("cannot convert image to base 64", e);
        }
        return null;
    }

    private int getColorOfTexture(BufferedImage bufferedImage, int blockY) {
        double brightnessMin = 0.85;
        double blockYCoordinateBrightnessChange = 0.0015;
        double brightnessMultiplier = TeraMath.clamp(brightnessMin + blockY * blockYCoordinateBrightnessChange, brightnessMin, 1);
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
        r *= brightnessMultiplier;
        g /= imageSize;
        g *= brightnessMultiplier;
        b /= imageSize;
        b *= brightnessMultiplier;
        // get rid of the alpha because it is always 255
        return new Color(r, g, b).getRGB() & 0x00FFFFFF;
    }

    //This method is heavily inspired by the renderCell method of the MinimapGrid class in the Minimap module.
    private int getSurfaceY(int x, int yEstimate, int z) {
        final int yMinimum = 0;
        final int yMaximum = 200;
        int y = yEstimate;
        Block block = worldProvider.getBlock(x, yEstimate, z);
        if (isIgnoredByMinimap(block)) {
            while (isIgnoredByMinimap(block)) {
                --y;
                block = worldProvider.getBlock(x, y, z);
                if (y <= yMinimum) {
                    return yMinimum;
                }
            }
        } else {
            while (!isIgnoredByMinimap(block)) {
                ++y;
                block = worldProvider.getBlock(x, y, z);
                if (y >= yMaximum) {
                    return yMaximum;
                }
            }
            --y;
        }
        return y;
    }

    private static boolean isIgnoredByMinimap(Block block) {
        return block.isPenetrable() && !block.isWater() && !block.getURI().toString().equals("engine:unloaded");
    }

    private void loadChunks(Vector3i center, int mapBlockWidth, int mapBlockLength) {
        final int maximumVerticalChunks = 5;
        LocationComponent locationComponent = new LocationComponent();
        locationComponent.setWorldPosition(center.toVector3f());
        RelevanceRegionComponent relevanceRegionComponent = new RelevanceRegionComponent();
        relevanceRegionComponent.distance = new Vector3i(((int) Math.ceil((double) mapBlockWidth / ChunkConstants.SIZE_X) * 2) + 2, maximumVerticalChunks,
                ((int) Math.ceil((double) mapBlockLength / ChunkConstants.SIZE_Z) * 2) + 2);
        entityManager.create(locationComponent, relevanceRegionComponent);
    }

}
