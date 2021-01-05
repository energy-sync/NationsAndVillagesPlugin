package com.github.dawsonvilamaa.nationsandvillagesplugin.listeners;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.Nation;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsChunk;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsVillager;
import net.minecraft.server.v1_16_R3.EntityVillager;
import net.minecraft.server.v1_16_R3.VillagerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
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

    //Replaces all spawned villagers with NationsVillagers
    @EventHandler
    public void onVillagerSpawn(EntitySpawnEvent e) {
        if (e.getEntity() instanceof CraftVillager) {
            EntityVillager villager = ((CraftVillager) e.getEntity()).getHandle();
            NationsVillager nationsVillager = Main.nationsManager.getVillagerByUUID(villager.getUniqueID());
            if (nationsVillager == null) {
                NationsVillager newNationsVillager = new NationsVillager(villager);
                Main.nationsManager.addVillager(villager.getUniqueID(), newNationsVillager);
                Chunk chunk = e.getLocation().getChunk();
                NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ());
                if (nationsChunk != null) {
                    newNationsVillager.setNationID(nationsChunk.getNationID());
                    Main.nationsManager.getNationByID(nationsChunk.getNationID()).incrementPopulation();
                }
            }
        }
    }

    //Removes villager data once a villager dies
    @EventHandler
    public void onVillagerDeath(EntityDeathEvent e) {
        if (e.getEntity() instanceof CraftVillager) {
            EntityVillager villager = ((CraftVillager) e.getEntity()).getHandle();
            NationsVillager nationsVillager = Main.nationsManager.getVillagerByUUID(villager.getUniqueID());
            if (Main.nationsManager.getNationByID(nationsVillager.getNationID()) != null)
                Main.nationsManager.getNationByID(nationsVillager.getNationID()).decrementPopulation();
            Main.nationsManager.removeVillager(villager.getUniqueID());
        }
    }

    //Replaces all already existing villagers with NationsVillagers when new chunks are loaded
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if (e.isNewChunk() == true) {
            for (Entity entity : e.getChunk().getEntities()) {
                if (entity instanceof CraftVillager) {
                    EntityVillager villager = ((CraftVillager) entity).getHandle();
                    //add new villager if one with that UUID doesn't already exist
                    if (Main.nationsManager.getVillagerByUUID(villager.getUniqueID()) == null)
                        Main.nationsManager.addVillager(villager.getUniqueID(), new NationsVillager(villager));
                    NationsVillager nationsVillager = Main.nationsManager.getVillagerByUUID(villager.getUniqueID());
                    Chunk chunk = e.getChunk();
                    NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ());
                    //add villager to nation if it is in a claimed chunk
                    if (nationsChunk != null) {
                        if (nationsVillager.getNationID() == -1) {
                            nationsVillager.setNationID(nationsChunk.getNationID());
                            Main.nationsManager.getNationByID(nationsVillager.getNationID()).incrementPopulation();
                        }
                    }
                    //remove villager from its nation if it is not in a claimed chunk
                    else {
                        if (nationsVillager.getNationID() != -1) {
                            Main.nationsManager.getNationByID(nationsVillager.getNationID()).decrementPopulation();
                            nationsVillager.setNationID(-1);
                        }
                    }
                }
            }
        }
    }
}
