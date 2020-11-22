package com.github.dawsonvilamaa.nationsandvillagesplugin;

import com.github.dawsonvilamaa.nationsandvillagesplugin.listeners.NationsVillagerListener;
import com.github.dawsonvilamaa.nationsandvillagesplugin.listeners.PlayerListener;
import com.github.dawsonvilamaa.nationsandvillagesplugin.listeners.WorldListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private PlayerListener playerListener = new PlayerListener(this);
    private NationsVillagerListener nationsVillagerListener = new NationsVillagerListener(this);
    private WorldListener worldListener = new WorldListener(this);

    public static NationsManager nationsManager;

    @Override
    public void onEnable() {
        nationsManager = new NationsManager();

        //register commands
        NAVCommandExecutor commandExecutor = new NAVCommandExecutor(this);
        getCommand("nation").setExecutor(commandExecutor);

        //register events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(playerListener, this);
        pm.registerEvents(nationsVillagerListener, this);
        pm.registerEvents(worldListener, this);

        //files
    }

    @Override
    public void onDisable() {

    }
}
