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
package org.terasology.web.resources.games;

import org.junit.Test;
import org.terasology.web.resources.base.ResourceAccessException;
import org.terasology.web.resources.base.ResourceMethod;

import static org.junit.Assert.assertTrue;

public class GamesResourceTest {

    // TODO: it's currently not possible to etst the GET methods because GameProvider.getSavedGames() is a ststic method
    // TODO: thus can't easily be mocked. Possibly refactor GameProvider in the engine repository to a singleton

    @Test
    public void testPost() throws ResourceAccessException {
        ResourceMethod<NewGameMetadata, Void> postMethod = new GamesResource().getPostCollectionMethod();
        assertTrue(postMethod instanceof NewGameMethod);
    }

    @Test
    public void testDelete() throws ResourceAccessException {
        ResourceMethod<Void, Void> deleteMethod = new GamesResource().getDeleteItemMethod("gameToDelete");
        assertTrue(deleteMethod instanceof DeleteGameMethod);
    }

    @Test
    public void testPatch() throws ResourceAccessException {
        ResourceMethod<NewGameMetadata, Void> patchMethod = new GamesResource().getPatchItemMethod("gameToPatch");
        assertTrue(patchMethod instanceof PatchGameMethod);
    }
}
