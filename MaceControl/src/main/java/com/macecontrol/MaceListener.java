package com.macecontrol;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class MaceListener implements Listener {

    private final MaceControl plugin;

    public MaceListener(MaceControl plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (event.getRecipe().getResult().getType() != Material.MACE) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        MaceTracker tracker = plugin.getTracker();
        int amount = event.getRecipe().getResult().getAmount();

        if (tracker.wouldExceedGlobal(amount)) {
            event.setCancelled(true);
            player.sendMessage("§cA maximum of §e" + plugin.getMaxGlobal()
                    + " §cmaces can exist on the server at once. Current count: §e"
                    + tracker.countGlobal() + "§c.");
            return;
        }

        if (tracker.countForPlayer(player) + amount > plugin.getMaxPerPlayer()) {
            event.setCancelled(true);
            player.sendMessage("§cYou can only carry §e" + plugin.getMaxPerPlayer()
                    + " §cmace(s) at a time.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getItem().getItemStack().getType() != Material.MACE) return;

        if (plugin.getTracker().playerAtLimit(player)) {
            event.setCancelled(true);
            player.sendMessage("§cYou can only carry §e" + plugin.getMaxPerPlayer()
                    + " §cmace(s). Drop yours before picking up another.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack cursor = event.getCursor();

        if (cursor != null && cursor.getType() == Material.MACE) {
            if (event.getClickedInventory() != null
                    && event.getClickedInventory().equals(player.getInventory())) {
                if (plugin.getTracker().playerAtLimit(player)) {
                    event.setCancelled(true);
                    player.sendMessage("§cYou can only carry §e" + plugin.getMaxPerPlayer()
                            + " §cmace(s) at a time.");
                }
            }
        }
    }
}
