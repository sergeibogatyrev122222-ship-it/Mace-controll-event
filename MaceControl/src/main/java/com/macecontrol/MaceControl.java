package com.macecontrol;

import org.bukkit.plugin.java.JavaPlugin;

public class MaceControl extends JavaPlugin {

    private static MaceControl instance;
    private MaceTracker tracker;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        tracker = new MaceTracker(this);
        getServer().getPluginManager().registerEvents(new MaceListener(this), this);
        MaceCommand cmd = new MaceCommand(this);
        getCommand("mace").setExecutor(cmd);
        getCommand("mace").setTabCompleter(cmd);
        getLogger().info("MaceControl enabled. Global limit: " + getMaxGlobal()
                + " | Per-player limit: " + getMaxPerPlayer());
    }

    @Override
    public void onDisable() {
        getLogger().info("MaceControl disabled.");
    }

    public static MaceControl getInstance() { return instance; }
    public MaceTracker getTracker() { return tracker; }
    public int getMaxGlobal() { return getConfig().getInt("max-global", 3); }
    public int getMaxPerPlayer() { return getConfig().getInt("max-per-player", 1); }

    public void setMaxGlobal(int amount) {
        getConfig().set("max-global", amount);
        saveConfig();
    }

    public void setMaxPerPlayer(int amount) {
        getConfig().set("max-per-player", amount);
        saveConfig();
    }
}
