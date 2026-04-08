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

public class MaceTracker {

    private final MaceControl plugin;

    public MaceTracker(MaceControl plugin) {
        this.plugin = plugin;
    }

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

    public boolean wouldExceedGlobal(int extra) {
        return (countGlobal() + extra) > plugin.getMaxGlobal();
    }

    public int countForPlayer(Player player) {
        return countInInventory(player.getInventory());
    }

    public boolean playerAtLimit(Player player) {
        return countForPlayer(player) >= plugin.getMaxPerPlayer();
    }

    private int countInInventory(Inventory inv) {
        int count = 0;
        for (ItemStack stack : inv.getContents()) {
            if (stack != null && stack.getType() == Material.MACE) {
                count += stack.getAmount();
            }
        }
        if (inv instanceof PlayerInventory pi) {
            ItemStack offhand = pi.getItemInOffHand();
            if (offhand.getType() == Material.MACE) {
                count += offhand.getAmount();
            }
        }
        return count;
    }
}
