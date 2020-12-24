package com.github.dawsonvilamaa.nationsandvillagesplugin.listeners;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsVillager;
import net.minecraft.server.v1_16_R3.EntityVillager;
import net.minecraft.server.v1_16_R3.VillagerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class WorldListener implements Listener {
    private Main plugin;

    /**
     * @param plugin
     */
    public WorldListener(Main plugin) {
        this.plugin = plugin;
    }

    //Adds players to data when they join if not already on the list
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (Main.nationsManager.getPlayerByUUID(e.getPlayer().getUniqueId()) == null) {
            Main.nationsManager.addPlayer(e.getPlayer());
            e.getPlayer().sendMessage(ChatColor.GREEN + "You received $1000 for being a new player");
        }
    }

    //Replaces all spawned villagers with NationsVillagers
    @EventHandler
    public void onVillagerSpawn(EntitySpawnEvent e) {
        if (e.getEntity() instanceof CraftVillager) {
            EntityVillager villager = ((CraftVillager) e.getEntity()).getHandle();
            if (Main.nationsManager.getVillagers().get(villager.getUniqueID()) == null) {
                Main.nationsManager.getVillagers().put(villager.getUniqueID(), new NationsVillager(villager));
            }
        }
    }

    //Replaces all already existing villagers with NationsVillagers when new chunks are loaded
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if (e.isNewChunk() == true) {
            for (Entity entity : e.getChunk().getEntities()) {
                if (entity instanceof CraftVillager) {
                    EntityVillager villager = ((CraftVillager) entity).getHandle();
                    if (Main.nationsManager.getVillagers().get(villager.getUniqueID()) == null) {
                        Main.nationsManager.getVillagers().put(villager.getUniqueID(), new NationsVillager(villager));
                        Bukkit.broadcastMessage("Villagers: " + Main.nationsManager.getVillagers().values().size());
                    }
                }
            }
        }
    }
}
