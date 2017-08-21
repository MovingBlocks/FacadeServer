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
package org.terasology.web.serverAdminManagement;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.terasology.web.resources.base.ResourceAccessException;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ServerAdminsManagerTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private static <T> void assertSetEquals(Set<T> actualSet, T... expectedItems) {
        Set<T> expectedSet = new HashSet<>(Arrays.asList(expectedItems));
        assertEquals(expectedSet, actualSet);
    }

    private static <T> void assertSetEmpty(Set<T> actualSet) {
        assertTrue(actualSet.isEmpty());
    }

    @Test
    public void testAdminListManipulation() {
        ServerAdminsManager manager = new ServerAdminsManager(null);
        assertTrue(manager.isAnonymousAdminAccessEnabled());
        manager.addAdmin("test");
        assertSetEquals(manager.getAdminIds(), "test");
        assertFalse(manager.isAnonymousAdminAccessEnabled());
        manager.removeAdmin("test");
        assertSetEmpty(manager.getAdminIds());
        assertTrue(manager.isAnonymousAdminAccessEnabled());
    }

    @Test
    public void testAnonymousOk() throws ResourceAccessException {
        ServerAdminsManager manager = new ServerAdminsManager(null);
        manager.checkClientHasAdminPermissions("someone"); // should not throw
    }

    private ServerAdminsManager setUpWithSingleAdmin(String adminId) {
        ServerAdminsManager result = new ServerAdminsManager(null);
        result.addAdmin(adminId);
        return result;
    }

    @Test(expected = ResourceAccessException.class)
    public void testAnonymousFail() throws ResourceAccessException {
        ServerAdminsManager manager = setUpWithSingleAdmin("testAdmin");
        manager.checkClientHasAdminPermissions("someUser"); // should throw
    }

    @Test
    public void testRegisteredOk() throws ResourceAccessException {
        ServerAdminsManager manager = setUpWithSingleAdmin("testAdmin");
        manager.checkClientHasAdminPermissions("testAdmin"); // should not throw
    }

    @Test
    public void testAddFirstAdmin() {
        ServerAdminsManager manager = new ServerAdminsManager(null);
        assertTrue(manager.isAnonymousAdminAccessEnabled());

        // this is the first one and must be added
        manager.addFirstAdminIfNecessary("firstAdmin");
        assertSetEquals(manager.getAdminIds(), "firstAdmin");

        // public access now must be disabled
        assertFalse(manager.isAnonymousAdminAccessEnabled());

        // this is another user and mustn't be added as an admin, and the anonymous access must remain disabled
        manager.addFirstAdminIfNecessary("someoneElse");
        assertSetEquals(manager.getAdminIds(), "firstAdmin");
        assertFalse(manager.isAnonymousAdminAccessEnabled());
    }

    @Test
    public void testSaveLoad() {
        Path filePath = tempFolder.getRoot().toPath().resolve("admins.json");
        ServerAdminsManager manager = new ServerAdminsManager(filePath);
        manager.addAdmin("admin1");
        manager.addAdmin("admin2");
        manager.saveAdminList();
        manager = new ServerAdminsManager(filePath); // reinitialize...
        manager.loadAdminList(); // ...and reload from same file
        assertSetEquals(manager.getAdminIds(), "admin1", "admin2");
    }

}
