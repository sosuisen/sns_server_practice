package com.example.user;

import static org.junit.jupiter.api.Assertions.*;

import com.example.constraint.UserConstraints;
import com.example.error.EmailInUseException;
import com.example.error.ResourceNotFoundException;
import com.example.user.dto.PatchUserRequest;
import com.example.user.dto.UserRequest;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class UserServiceTest {

    @Inject
    UserService userService;

    @Test
    @TestTransaction
    void create_hashesPasswordAndPersistsUser() {
        var email = uniqueEmail("create");
        var response = userService.create(new UserRequest("svcuser", email, "pass1234"));

        assertNotNull(response.id());
        assertEquals("svcuser", response.username());
        assertEquals(email, response.email());
        assertEquals(Role.user, response.role());
        User user = User.findById(response.id());
        assertTrue(BcryptUtil.matches("pass1234", user.passwordHash));
        assertFalse(user.passwordHash.equals("pass1234"));
    }

    @Test
    @TestTransaction
    void patch_updatesProvidedFieldsOnly() {
        var user = userService.create(new UserRequest("patchsrc", uniqueEmail("patchsrc"), "pass1234"));

        var patched = userService.patch(user.id(), new PatchUserRequest("patchdst", null, "newpass1234"));

        assertEquals("patchdst", patched.username());
        assertEquals(user.email(), patched.email());
        User updated = User.findById(patched.id());
        assertTrue(BcryptUtil.matches("newpass1234", updated.passwordHash));
    }

    @Test
    @TestTransaction
    void patch_duplicateEmailThrowsEmailInUseException() {
        var owner = userService.create(new UserRequest("dupeowner", uniqueEmail("dupeowner"), "pass1234"));
        var other = userService.create(new UserRequest("dupeother", uniqueEmail("dupeother"), "pass1234"));

        assertThrows(
                EmailInUseException.class,
                () -> userService.patch(owner.id(), new PatchUserRequest(null, other.email(), null)));
    }

    @Test
    @TestTransaction
    void changeRole_updatesExistingUser() {
        var user = userService.create(new UserRequest("roleuser", uniqueEmail("roleuser"), "pass1234"));

        var changed = userService.changeRole(user.id(), Role.admin);

        assertEquals(Role.admin, changed.role());
    }

    @Test
    @TestTransaction
    void patch_notFound_throwsResourceNotFoundException() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.patch(Long.MAX_VALUE, new PatchUserRequest("ghost", null, null)));
    }

    @Test
    @TestTransaction
    void changeRole_notFound_throwsResourceNotFoundException() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.changeRole(Long.MAX_VALUE, Role.admin));
    }

    @Test
    @TestTransaction
    void delete_removesExistingUser() {
        var user = userService.create(new UserRequest("deleteuser", uniqueEmail("deleteuser"), "pass1234"));

        userService.delete(user.id());
        assertNull(User.findById(user.id()));
    }

    @Test
    @TestTransaction
    void delete_notFound_throwsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> userService.delete(Long.MAX_VALUE));
    }

    @Test
    @TestTransaction
    void list_usesDefaultLimitWhenLimitIsNull() {
        for (int i = 0; i < UserConstraints.DEFAULT_LIST_LIMIT + 1; i++) {
            userService.create(new UserRequest("list" + i, uniqueEmail("list" + i), "pass1234"));
        }

        var users = userService.list(null);

        assertEquals(UserConstraints.DEFAULT_LIST_LIMIT, users.size());
    }

    @Test
    void list_rejectsInvalidLimit() {
        assertThrows(IllegalArgumentException.class, () -> userService.list(0));
        assertThrows(IllegalArgumentException.class, () -> userService.list(UserConstraints.MAX_LIST_LIMIT + 1));
    }

    private String uniqueEmail(String prefix) {
        return prefix + "-" + System.nanoTime() + "@example.com";
    }
}
