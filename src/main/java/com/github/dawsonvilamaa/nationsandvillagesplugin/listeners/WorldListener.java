package com.github.dawsonvilamaa.nationsandvillagesplugin.listeners;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsVillager;
import net.minecraft.server.v1_16_R3.EntityVillager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class WorldListener implements Listener {
    private Main plugin;

    /**
     * @param plugin
     */
    public WorldListener(Main plugin) {
        this.plugin = plugin;
    }

    //Replaces all spawned villagers with NationsVillagers
    @EventHandler
    public void onVillagerSpawn(EntitySpawnEvent e) {
        if (e.getEntity() instanceof CraftVillager) {
            EntityVillager villager = ((CraftVillager) e.getEntity()).getHandle();
            if (!(villager instanceof NationsVillager)) {
                e.setCancelled(true);
                Location loc = e.getLocation();
                NationsVillager nationsVillager = new NationsVillager(loc.getWorld());
                nationsVillager.spawn(e.getLocation());
            }
        }
    }
}
