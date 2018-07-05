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
import org.terasology.registry.In;
import org.terasology.utilities.Assets;
import org.terasology.web.resources.base.AbstractSimpleResource;
import org.terasology.web.resources.base.ClientSecurityRequirements;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.ResourceMethod;
import org.terasology.web.resources.base.ResourcePath;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.tiles.BlockTile;

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
import static org.terasology.web.resources.base.ResourceMethodFactory.createVoidParameterlessMethod;

public class WorldMapResource extends AbstractSimpleResource {

    private static final Logger logger = LoggerFactory.getLogger(WorldMapResource.class);

    @In
    private WorldProvider worldProvider;

    // This temporary GET method is needed to keep the frontend from displaying errors
    /*@Override
    protected ResourceMethod<Void, String> getGetMethod(ResourcePath path) throws ResourceAccessException {
        return createVoidParameterlessMethod(path, ClientSecurityRequirements.PUBLIC, Void.class,
                (data, client) -> getWorldMapBase64ImageString(new Vector3i(0, 0, 0), 0, 0, true));
    }*/

    // TODO: reimplement this as a GET request with parameters
    @Override
    protected ResourceMethod<WorldMapInput, String> getPutMethod(ResourcePath path) throws ResourceAccessException {
        return createParameterlessMethod(path, ClientSecurityRequirements.PUBLIC, WorldMapInput.class,
                (data, client) -> getWorldMapBase64ImageString(data.getCenter(), data.getMapBlockWidth(), data.getMapBlockLength(), data.isSurface()));
    }

    private String getWorldMapBase64ImageString(Vector3i center, int mapBlockWidth, int mapBlockLength, boolean isSurface) {
        final int colorSizeMultiplier = 30;
        int y = 40;
        List<Color> colors = new ArrayList<>();
        for (int x = (int) Math.floor((double) center.getX() - mapBlockWidth / 2); x < (int) Math.ceil((double) mapBlockWidth / 2 + center.getX()); ++x) {
            for (int z = (int) Math.floor((double) center.getZ() - mapBlockLength / 2); z < (int) Math.ceil((double) mapBlockLength / 2 + center.getZ()); ++z) {
                y = isSurface ? getSurfaceY(x, y, z) : center.getY();
                Block block = worldProvider.getBlock(x, y, z);
                ResourceUrn blockUrn = block.getURI().getBlockFamilyDefinitionUrn();
                if (Assets.get(blockUrn, BlockTile.class).isPresent()) {
                    BufferedImage blockImage = Assets.get(blockUrn, BlockTile.class).get().getImage();
                    colors.add(new Color(getColorOfTexture(blockImage)));
                } else {
                    logger.error("cannot find texture of block " + blockUrn.toString());
                    colors.add(Color.BLACK);
                }
            }
        }
        int colorSize = (int) (1 / Math.floor(Math.log(colors.size())) * colorSizeMultiplier);
        BufferedImage mapImage = new BufferedImage(mapBlockWidth * colorSize, mapBlockLength * colorSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = mapImage.createGraphics();
        for (int i = 0; i < colors.size(); ++i) {
            graphics.setColor(colors.get(i));
            graphics.fillRect(colorSize * (int) Math.ceil(i / mapBlockLength), colorSize * (i % mapBlockWidth), colorSize, colorSize);
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

    // TODO use y coordinate to increase/decrease brightness of texture (lower is darker)
    private int getColorOfTexture(BufferedImage bufferedImage) {
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
