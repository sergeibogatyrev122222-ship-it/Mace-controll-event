package com.macecontrol;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Counts how many MACE items currently exist across the entire server:
 *  - All online player inventories (including offhand & armour slots)
 *  - All item entities (dropped items) in every world
 *
 * Chest / container scanning is intentionally excluded because Bukkit gives
 * no reliable event for items sitting in chests being "destroyed". The limit
 * still works correctly: when a player tries to craft or pick up a mace the
 * live count is checked at that moment.
 */
public class MaceTracker {

    private final MaceControl plugin;

    public MaceTracker(MaceControl plugin) {
        this.plugin = plugin;
    }

    // ---------------------------------------------------------------
    //  Global count
    // ---------------------------------------------------------------

    /** Total maces alive on the server right now. */
    public int countGlobal() {
        int total = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            total += countInInventory(player.getInventory());
        }
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item item) {
                    if (item.getItemStack().getType() == Material.MACE) {
                        total += item.getItemStack().getAmount();
                    }
                }
            }
        }
        return total;
    }

    /** True if adding <code>extra</code> maces would exceed the global cap. */
    public boolean wouldExceedGlobal(int extra) {
        return (countGlobal() + extra) > plugin.getMaxGlobal();
    }

    // ---------------------------------------------------------------
    //  Per-player count
    // ---------------------------------------------------------------

    /** Number of maces in this player's full inventory (including offhand). */
    public int countForPlayer(Player player) {
        return countInInventory(player.getInventory());
    }

    /** True if the player already has at least the per-player limit of maces. */
    public boolean playerAtLimit(Player player) {
        return countForPlayer(player) >= plugin.getMaxPerPlayer();
    }

    // ---------------------------------------------------------------
    //  Helpers
    // ---------------------------------------------------------------

    private int countInInventory(Inventory inv) {
        int count = 0;
        for (ItemStack stack : inv.getContents()) {
            if (stack != null && stack.getType() == Material.MACE) {
                count += stack.getAmount();
            }
        }
        // Also check offhand for PlayerInventory
        if (inv instanceof PlayerInventory pi) {
            ItemStack offhand = pi.getItemInOffHand();
            if (offhand.getType() == Material.MACE) {
                count += offhand.getAmount();
            }
        }
        return count;
    }
}
