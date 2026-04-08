package com.macecontrol;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class MaceListener implements Listener {

    private final MaceControl plugin;

    public MaceListener(MaceControl plugin) {
        this.plugin = plugin;
    }

    // ---------------------------------------------------------------
    //  Crafting
    // ---------------------------------------------------------------

    /**
     * Fired when a player clicks to take a crafted result.
     * Blocks the craft if either limit would be exceeded.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (event.getRecipe().getResult().getType() != Material.MACE) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        MaceTracker tracker = plugin.getTracker();

        // How many will this craft produce?
        int amount = event.getRecipe().getResult().getAmount();

        // Global check
        if (tracker.wouldExceedGlobal(amount)) {
            event.setCancelled(true);
            player.sendMessage("§cA maximum of §e" + plugin.getMaxGlobal()
                    + " §cmaces can exist on the server at once. "
                    + "Current count: §e" + tracker.countGlobal() + "§c.");
            return;
        }

        // Per-player check
        if (tracker.countForPlayer(player) + amount > plugin.getMaxPerPlayer()) {
            event.setCancelled(true);
            player.sendMessage("§cYou can only carry §e" + plugin.getMaxPerPlayer()
                    + " §cmace(s) at a time.");
        }
    }

    // ---------------------------------------------------------------
    //  Pickup
    // ---------------------------------------------------------------

    /** Handles item pickup for 1.21+ Paper/Spigot servers. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getItem().getItemStack().getType() != Material.MACE) return;

        MaceTracker tracker = plugin.getTracker();

        // Per-player check (global doesn't change on pickup — item already exists)
        if (tracker.playerAtLimit(player)) {
            event.setCancelled(true);
            player.sendMessage("§cYou can only carry §e" + plugin.getMaxPerPlayer()
                    + " §cmace(s). Drop or store yours before picking up another.");
        }
    }

    // ---------------------------------------------------------------
    //  Inventory click (e.g. moving mace from chest into inventory)
    // ---------------------------------------------------------------

    /**
     * Prevents a player from moving a mace into their own inventory
     * (e.g. from a chest) if they are already at the per-player limit.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        // Player dragging a mace from cursor into their inventory slot
        if (cursor != null && cursor.getType() == Material.MACE) {
            if (isMovingIntoPlayerInv(event, player)) {
                if (plugin.getTracker().playerAtLimit(player)) {
                    event.setCancelled(true);
                    player.sendMessage("§cYou can only carry §e" + plugin.getMaxPerPlayer()
                            + " §cmace(s) at a time.");
                }
            }
        }
    }

    // ---------------------------------------------------------------
    //  Helpers
    // ---------------------------------------------------------------

    private boolean isMovingIntoPlayerInv(InventoryClickEvent event, Player player) {
        // Clicked inside the player's own inventory view
        if (event.getClickedInventory() == null) return false;
        return event.getClickedInventory().equals(player.getInventory());
    }
}
