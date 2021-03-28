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

import org.codehaus.plexus.util.Base64;
import org.joml.Vector3i;
import org.junit.Before;
import org.junit.Test;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.InjectionHelper;
import org.terasology.engine.world.RelevanceRegionComponent;
import org.terasology.engine.world.WorldProvider;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class WorldMapResourceTest {

    private WorldMapResource worldMapResource;
    private EntityManager entityManagerMock;

    private static BufferedImage getTestImage() {
        BufferedImage imageToTest = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = imageToTest.createGraphics();
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, 5, 5);
        graphics.setColor(Color.GREEN);
        graphics.fillRect(5, 0, 5, 5);
        graphics.setColor(Color.BLUE);
        graphics.fillRect(0, 5, 5, 5);
        graphics.setColor(Color.RED);
        graphics.fillRect(5, 5, 5, 5);
        return imageToTest;
    }

    @Before
    public void setup() {
        worldMapResource = new WorldMapResource();
        WorldProvider worldProviderMock = mock(WorldProvider.class);
        entityManagerMock = mock(EntityManager.class);
        Context context = new ContextImpl();
        context.put(WorldProvider.class, worldProviderMock);
        context.put(EntityManager.class, entityManagerMock);
        InjectionHelper.inject(worldMapResource, context);
    }

    @Test
    public void testColorOfTexture() {
        // each color takes up 1/4th of the image
        final int colorToTest = new Color(60, 60, 60).getRGB() & 0x00FFFFFF;
        assertEquals(colorToTest, worldMapResource.getColorOfTexture(getTestImage(), 75));
    }

    @Test
    public void testImageConvertsToBase64String() {
        BufferedImage testImage = getTestImage();
        assertTrue(Base64.isArrayByteBase64(worldMapResource.convertImageToBase64String(testImage)
                .getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testChunksStartLoading() {
        worldMapResource.loadChunks(new Vector3i(0, 0, 0), 32, 32);
        verify(entityManagerMock).create(any(LocationComponent.class), any(RelevanceRegionComponent.class));
    }

}
