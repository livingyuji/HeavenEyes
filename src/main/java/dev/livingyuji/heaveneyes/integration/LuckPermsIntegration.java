package dev.livingyuji.heaveneyes.integration;

import dev.livingyuji.heaveneyes.HeavenEyesPlugin;
import dev.livingyuji.heaveneyes.util.PermissionValidator;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Handles LuckPerms API operations directly on player user data without creating groups.
 */
public class LuckPermsIntegration {

    private final HeavenEyesPlugin plugin;
    private Boolean available = null;

    public LuckPermsIntegration(HeavenEyesPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isAvailable() {
        if (available == null) {
            available = Bukkit.getPluginManager().isPluginEnabled("LuckPerms");
        }
        return available;
    }

    public CompletableFuture<Boolean> addPermission(UUID uuid, String permission) {
        if (!isAvailable()) {
            return CompletableFuture.completedFuture(false);
        }

        if (!PermissionValidator.isValidNode(permission)) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid permission node format"));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                LuckPerms api = LuckPermsProvider.get();
                User user = api.getUserManager().loadUser(uuid).join();
                if (user == null) {
                    return false;
                }

                Node node = PermissionNode.builder(permission).value(true).build();
                var result = user.data().add(node);
                if (result.wasSuccessful()) {
                    api.getUserManager().saveUser(user).join();
                    return true;
                }
                return false;
            } catch (Exception e) {
                plugin.getLogger().warning("Error adding permission in LuckPerms: " + e.getMessage());
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> removePermission(UUID uuid, String permission) {
        if (!isAvailable()) {
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                LuckPerms api = LuckPermsProvider.get();
                User user = api.getUserManager().loadUser(uuid).join();
                if (user == null) {
                    return false;
                }

                Node node = PermissionNode.builder(permission).build();
                var result = user.data().remove(node);
                if (result.wasSuccessful()) {
                    api.getUserManager().saveUser(user).join();
                    return true;
                }
                return false;
            } catch (Exception e) {
                plugin.getLogger().warning("Error removing permission in LuckPerms: " + e.getMessage());
                return false;
            }
        });
    }

    public CompletableFuture<List<String>> listPermissions(UUID uuid) {
        if (!isAvailable()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                LuckPerms api = LuckPermsProvider.get();
                User user = api.getUserManager().loadUser(uuid).join();
                if (user == null) {
                    return Collections.emptyList();
                }

                List<String> list = new ArrayList<>();
                for (Node node : user.getNodes()) {
                    if (node instanceof PermissionNode permNode && permNode.getValue()) {
                        list.add(permNode.getPermission());
                    }
                }
                return list;
            } catch (Exception e) {
                plugin.getLogger().warning("Error listing permissions in LuckPerms: " + e.getMessage());
                return Collections.emptyList();
            }
        });
    }

    public CompletableFuture<Boolean> hasPermission(UUID uuid, String permission) {
        if (!isAvailable()) {
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                LuckPerms api = LuckPermsProvider.get();
                User user = api.getUserManager().loadUser(uuid).join();
                if (user == null) {
                    return false;
                }

                for (Node node : user.getNodes()) {
                    if (node instanceof PermissionNode permNode) {
                        if (permNode.getPermission().equalsIgnoreCase(permission) && permNode.getValue()) {
                            return true;
                        }
                    }
                }
                return false;
            } catch (Exception e) {
                plugin.getLogger().warning("Error checking permission in LuckPerms: " + e.getMessage());
                return false;
            }
        });
    }
}
