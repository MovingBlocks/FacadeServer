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

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.RelevanceRegionComponent;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.tiles.BlockTile;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.math.TeraMath;
import org.terasology.engine.network.Client;
import org.terasology.web.resources.base.AbstractSimpleResource;
import org.terasology.web.resources.base.ClientSecurityRequirements;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.ResourceMethod;
import org.terasology.web.resources.base.ResourcePath;
import org.terasology.web.serverAdminManagement.ServerAdminsManager;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.terasology.web.resources.base.ResourceMethodFactory.createParameterlessMethod;

/**
 * {@link org.terasology.web.resources.base.Resource} class used for sending png images of the world map, encoded in base64.
 */
public class WorldMapResource extends AbstractSimpleResource {

    private static final Logger logger = LoggerFactory.getLogger(WorldMapResource.class);
    private static final int BLOCK_Y_DEFAULT = 40;

    @In
    private WorldProvider worldProvider;

    @In
    private EntityManager entityManager;

    // TODO: reimplement this as a GET request with parameters
    @Override
    protected ResourceMethod<WorldMapInput, String> getPutMethod(ResourcePath path) throws ResourceAccessException {
        return createParameterlessMethod(path, ClientSecurityRequirements.PUBLIC, WorldMapInput.class,
                (data, client) -> getWorldMapBase64ImageString(data.getCenter(), data.getMapBlockWidth(), data.getMapBlockLength(), data.isSurface(), client));
    }

    /**
     * Get the color of blocks in the world map and convert them into a base64 encoded image.
     * @param center the location of the block in the center of the map.
     * @param mapBlockWidth the width of the map.
     * @param mapBlockLength the length of the map.
     * @param isSurface whether or not to get blocks on the surface (using the block at the highest elevation that isn't air).
     * @return a base64 encoded png image of the world map with location and size depending on the parameters.
     */
    private String getWorldMapBase64ImageString(Vector3i center, int mapBlockWidth, int mapBlockLength, boolean isSurface, Client client) {
        final int mapColorSizeDecreaseThreshold = 125 * 125;
        final int largerColorSizeMultiplier = 60;
        final int smallerColorSizeMultiplier = 30;
        final int colorSizeMultiplier = mapBlockWidth * mapBlockLength <= mapColorSizeDecreaseThreshold ? largerColorSizeMultiplier : smallerColorSizeMultiplier;
        int blockY = BLOCK_Y_DEFAULT;
        List<List<Color>> colors = new ArrayList<>(mapBlockWidth);
        for (int i = 0; i < mapBlockWidth; ++i) {
            colors.add(i, new ArrayList<>(mapBlockLength));
        }

        EntityRef mapLoadingRef = EntityRef.NULL;
        // TODO: Change this check to be configurable, so it can be disabled. Also provide feedback for when this doesn't trigger
        if (ServerAdminsManager.getInstance().getAdminIds().contains(client.getId())) {
            mapLoadingRef = loadChunks(center, mapBlockWidth, mapBlockLength);
        }

        for (int x = (int) Math.floor((double) center.x() - mapBlockWidth / 2); x < (int) Math.ceil((double) mapBlockWidth / 2 + center.x()); ++x) {
            for (int z = (int) Math.floor((double) center.z() - mapBlockLength / 2); z < (int) Math.ceil((double) mapBlockLength / 2 + center.z()); ++z) {
                while (worldProvider.getBlock(x, blockY, z).getURI().toString().equals("engine:unloaded")) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mapLoadingRef.destroy();
                blockY = isSurface ? getSurfaceY(x, blockY, z) : center.y();
                Block block = worldProvider.getBlock(x, blockY, z);
                ResourceUrn blockUrn = block.getURI().getBlockFamilyDefinitionUrn();
                if (Assets.get(blockUrn, BlockTile.class).isPresent()) {
                    BufferedImage blockImage = Assets.get(blockUrn, BlockTile.class).get().getImage();
                    colors.get(x - (center.x() - mapBlockWidth / 2)).add(new Color(getColorOfTexture(blockImage, blockY)));
                } else {
                    logger.warn("cannot find texture of block " + blockUrn.toString());
                    colors.get(x - (center.x() - mapBlockWidth / 2)).add(Color.BLACK);
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

    /**
     * Converts a BufferedImage into a base64 string.
     * @param img the image to convert.
     * @return a base64 encoded string representation of the image, or null if the image cannot be converted.
     */
    String convertImageToBase64String(final BufferedImage img) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "png", byteArrayOutputStream);
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            logger.error("cannot convert image to base 64", e);
        }
        return null;
    }

    /**
     * Get the average color of an individual block's texture and adjust its brightness, where a lower y-coordinate is darker.
     * @param bufferedImage the texture of a single block.
     * @param blockY the y coordinate of the block for use in determining brightness.
     * @return the average color of the block with brightness changed.
     */
    int getColorOfTexture(BufferedImage bufferedImage, int blockY) {
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

    /**
     * Get the y coordinate of the block at the given x and z coordinates where the block is at the surface.
     * This method is heavily inspired by the renderCell method of the MinimapGrid class in the Minimap module.
     * @param x the x coordinate of the block
     * @param yEstimate an estimate of the y coordinate
     * @param z the z coordinate of the block
     * @return the y coordinate of the block
     */
    private int getSurfaceY(int x, int yEstimate, int z) {
        final int yMinimum = 0;
        final int yMaximum = 200;
        int y = yEstimate;
        Block block = worldProvider.getBlock(x, yEstimate, z);
        if (isIgnoredByMap(block)) {
            while (isIgnoredByMap(block)) {
                --y;
                block = worldProvider.getBlock(x, y, z);
                if (y <= yMinimum) {
                    return BLOCK_Y_DEFAULT;
                }
            }
        } else {
            while (!isIgnoredByMap(block)) {
                ++y;
                block = worldProvider.getBlock(x, y, z);
                if (y >= yMaximum) {
                    return BLOCK_Y_DEFAULT;
                }
            }
            --y;
        }
        return y;
    }

    /**
     * Determine if the given block should be ignored when trying to find the y-coordinate of a block.
     * @param block the block to check.
     * @return whether or not the block should be ignored.
     */
    private static boolean isIgnoredByMap(Block block) {
        return block.isPenetrable() && !block.isWater() && !block.getURI().toString().equals("engine:unloaded");
    }

    /**
     * load all chunks around the designated map area so that the texture of each can be obtained.
     * @param center the location of the block in the center of the map.
     * @param mapBlockWidth the width of the map.
     * @param mapBlockLength the length of the map.
     */
    EntityRef loadChunks(Vector3i center, int mapBlockWidth, int mapBlockLength) {
        final int maximumVerticalChunks = 8;
        LocationComponent locationComponent = new LocationComponent();
        locationComponent.setWorldPosition(new Vector3f(center));
        RelevanceRegionComponent relevanceRegionComponent = new RelevanceRegionComponent();
        relevanceRegionComponent.distance = new Vector3i(((int) Math.ceil((double) mapBlockWidth / Chunks.SIZE_X) * 2) + 2, maximumVerticalChunks,
                ((int) Math.ceil((double) mapBlockLength / Chunks.SIZE_Z) * 2) + 2);
        return entityManager.create(locationComponent, relevanceRegionComponent);
    }

}
