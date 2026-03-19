package com.gaiaac.gaia.checks.player;
import com.gaiaac.gaia.checks.Check;
import com.gaiaac.gaia.core.GaiaPlugin;
import com.gaiaac.gaia.core.PlayerData;
import org.bukkit.entity.Player;
/** Invalid (G) - Detects gliding state without elytra equipped. */
public class InvalidG extends Check {
    public InvalidG(GaiaPlugin plugin) { super(plugin, "Invalid", "G", "invalid", true, 3); }
    @Override public void handle(Player player, PlayerData data) {
        if (data.isGliding()) {
            try {
                org.bukkit.inventory.ItemStack chest = player.getInventory().getChestplate();
                if (chest == null || chest.getType() != org.bukkit.Material.ELYTRA) {
                    flag(player, data, 3.0, "glideNoElytra");
                }
            } catch (Exception ignored) {}
        }
    }
}
