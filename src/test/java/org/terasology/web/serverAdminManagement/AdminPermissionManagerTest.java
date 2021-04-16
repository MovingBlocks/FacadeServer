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
package org.terasology.web.serverAdminManagement;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.InjectionHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AdminPermissionManagerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private AdminPermissionManager createPermissionListWithTwoAdmins() {
        AdminPermissionManager adminPermissionManager = new AdminPermissionManager(null, false);
        adminPermissionManager.addAdmin("first");
        adminPermissionManager.giveAllPermissionsToAdmin("first");
        adminPermissionManager.addAdmin("second");
        return adminPermissionManager;
    }

    @Test
    public void testAdminAddRemove() {
        AdminPermissionManager adminPermissionManager = new AdminPermissionManager(null, false);
        assertTrue(adminPermissionManager.getAdminPermissions().isEmpty());
        adminPermissionManager.addAdmin("test");
        assertEquals(adminPermissionManager.getAdminPermissions().iterator().next().getId(), "test");
        assertFalse(adminPermissionManager.getAdminPermissions().isEmpty());
        adminPermissionManager.removeAdmin("test");
        assertTrue(adminPermissionManager.getAdminPermissions().isEmpty());
    }

    @Test
    public void testFirstAdminHasAllPermissions() {
        AdminPermissionManager adminPermissionManager = createPermissionListWithTwoAdmins();
        Map<PermissionType, Boolean> allPermissionsTrue = PermissionType.generatePermissionMap(true);
        assertEquals(allPermissionsTrue, adminPermissionManager.getPermissionsOfAdmin("first"));
    }

    @Test
    public void testSecondAdminHasNoPermissions() {
        AdminPermissionManager adminPermissionManager = createPermissionListWithTwoAdmins();
        Map<PermissionType, Boolean> allPermissionsFalse = PermissionType.generatePermissionMap(false);
        assertEquals(allPermissionsFalse, adminPermissionManager.getPermissionsOfAdmin("second"));
    }

    @Test
    public void testConsoleAdminPermissions() {
        PermissionManager permissionManagerMock = mock(PermissionManager.class);
        EntityManager entityManagerMock = mock(EntityManager.class);
        EntityRef clientEntityRefMock = mock(EntityRef.class);
        when(entityManagerMock.create("engine:client")).thenReturn(clientEntityRefMock);
        when(clientEntityRefMock.getComponent(ClientComponent.class)).thenReturn(new ClientComponent());

        Context context = new ContextImpl();
        context.put(PermissionManager.class, permissionManagerMock);
        AdminPermissionManager adminPermissionManager = createPermissionListWithTwoAdmins();
        InjectionHelper.inject(adminPermissionManager, context);

        verify(permissionManagerMock, never()).addPermission(any(), eq("cheat"));
        adminPermissionManager.updateAdminConsolePermissions("first", clientEntityRefMock);
        verify(permissionManagerMock, atLeastOnce()).addPermission(any(), eq("cheat"));

        Map<PermissionType, Boolean> newPermissionMap = PermissionType.generatePermissionMap(true);
        newPermissionMap.put(PermissionType.CONSOLE_CHEAT, false);
        adminPermissionManager.setAdminPermissions("first", new IdPermissionPair("first", newPermissionMap));

        verify(permissionManagerMock, never()).removePermission(any(), eq("cheat"));
        adminPermissionManager.updateAdminConsolePermissions("first", clientEntityRefMock);
        verify(permissionManagerMock, atLeastOnce()).removePermission(any(), eq("cheat"));
    }

    @Test
    public void testAdminPermissionsGetsRemoved() {
        AdminPermissionManager adminPermissionManager = createPermissionListWithTwoAdmins();
        assertTrue(adminPermissionManager.getPermissionsOfAdmin("first").get(PermissionType.ADMIN_MANAGEMENT));
        Map<PermissionType, Boolean> permissionMapWithoutAdminManagement = PermissionType.generatePermissionMap(true);
        permissionMapWithoutAdminManagement.put(PermissionType.ADMIN_MANAGEMENT, false);
        adminPermissionManager.setAdminPermissions("first", new IdPermissionPair("first", permissionMapWithoutAdminManagement));
        assertFalse(adminPermissionManager.getPermissionsOfAdmin("first").get(PermissionType.ADMIN_MANAGEMENT));
    }

    @Test
    public void testSaveLoad() {
        Path filePath = temporaryFolder.getRoot().toPath().resolve("adminPermissions.json");
        AdminPermissionManager adminPermissionManager = new AdminPermissionManager(filePath, true);
        adminPermissionManager.addAdmin("first");
        adminPermissionManager.giveAllPermissionsToAdmin("first");
        adminPermissionManager.addAdmin("second");
        try {
            adminPermissionManager.saveAdminPermissionList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        adminPermissionManager = new AdminPermissionManager(filePath, true);
        adminPermissionManager.loadAdminPermissionList();
        Set<IdPermissionPair> expectedPermissions = new HashSet<>();
        expectedPermissions.add(new IdPermissionPair("first", PermissionType.generatePermissionMap(true)));
        expectedPermissions.add(new IdPermissionPair("second", PermissionType.generatePermissionMap(false)));
        assertEquals(adminPermissionManager.getAdminPermissions(), expectedPermissions);
    }

}
