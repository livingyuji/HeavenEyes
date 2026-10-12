package dev.livingyuji.heaveneyes.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PermissionValidatorTest {

    @Test
    public void testValidNodes() {
        assertTrue(PermissionValidator.isValidNode("heaveneyes.inspect"));
        assertTrue(PermissionValidator.isValidNode("heaveneyes.alerts"));
        assertTrue(PermissionValidator.isValidNode("group.moderator"));
        assertTrue(PermissionValidator.isValidNode("custom.plugin.sub-perm_1"));
    }

    @Test
    public void testInvalidSyntax() {
        assertFalse(PermissionValidator.isValidNode(null));
        assertFalse(PermissionValidator.isValidNode(""));
        assertFalse(PermissionValidator.isValidNode("heaveneyes..inspect"));
        assertFalse(PermissionValidator.isValidNode("heaveneyes."));
        assertFalse(PermissionValidator.isValidNode(".heaveneyes"));
        assertFalse(PermissionValidator.isValidNode("heaveneyes/admin"));
        assertFalse(PermissionValidator.isValidNode("heaveneyes@admin"));
    }

    @Test
    public void testDangerousNodes() {
        assertTrue(PermissionValidator.isDangerousNode("*"));
        assertTrue(PermissionValidator.isDangerousNode("'*'"));
        assertTrue(PermissionValidator.isDangerousNode("heaveneyes.*"));
        assertTrue(PermissionValidator.isDangerousNode("bukkit.*"));
        assertTrue(PermissionValidator.isDangerousNode("minecraft.*"));
        assertTrue(PermissionValidator.isDangerousNode("luckperms.*"));

        assertFalse(PermissionValidator.isDangerousNode("heaveneyes.inspect"));
        assertFalse(PermissionValidator.isDangerousNode("heaveneyes.help"));
    }

    @Test
    public void testSafeToAssign() {
        assertTrue(PermissionValidator.isSafeToAssign("heaveneyes.inspect"));
        assertTrue(PermissionValidator.isSafeToAssign("heaveneyes.alerts"));
        assertFalse(PermissionValidator.isSafeToAssign("*"));
        assertFalse(PermissionValidator.isSafeToAssign("heaveneyes.*"));
        assertFalse(PermissionValidator.isSafeToAssign("invalid..node"));
    }
}
