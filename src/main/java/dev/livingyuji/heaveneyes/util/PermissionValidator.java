package dev.livingyuji.heaveneyes.util;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Validates permission nodes for format correctness and security safeguards.
 */
public final class PermissionValidator {

    private static final Pattern VALID_NODE_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-]+(\\.[a-zA-Z0-9_\\-]+)*$");

    private PermissionValidator() {
    }

    /**
     * Checks if a permission node has valid syntax.
     */
    public static boolean isValidNode(String node) {
        if (node == null || node.trim().isEmpty()) {
            return false;
        }
        String trimmed = node.trim();
        if (trimmed.length() > 128) {
            return false;
        }
        return VALID_NODE_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Checks whether assigning this permission is restricted as a root or dangerous wildcard.
     */
    public static boolean isDangerousNode(String node) {
        if (node == null) {
            return true;
        }
        String lower = node.trim().toLowerCase(Locale.ROOT);
        return lower.equals("*")
                || lower.equals("'*'")
                || lower.equals("heaveneyes.*")
                || lower.equals("bukkit.*")
                || lower.equals("minecraft.*")
                || lower.equals("essentials.*")
                || lower.equals("luckperms.*");
    }

    /**
     * Verifies if a node can be safely assigned by a staff member.
     *
     * @param node the permission node to evaluate
     * @return true if valid and not dangerous
     */
    public static boolean isSafeToAssign(String node) {
        return isValidNode(node) && !isDangerousNode(node);
    }
}
