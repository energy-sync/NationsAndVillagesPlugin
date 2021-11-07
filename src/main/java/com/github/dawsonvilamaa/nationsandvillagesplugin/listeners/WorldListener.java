package com.github.dawsonvilamaa.nationsandvillagesplugin.listeners;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsChunk;
import com.github.dawsonvilamaa.nationsandvillagesplugin.npcs.NationsIronGolem;
import com.github.dawsonvilamaa.nationsandvillagesplugin.npcs.NationsVillager;
import net.minecraft.server.v1_16_R3.EntityIronGolem;
import net.minecraft.server.v1_16_R3.EntityVillager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftIronGolem;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class WorldListener implements Listener {
    private Main plugin;

    /**
     * @param plugin
     */
    public WorldListener(Main plugin) {
        this.plugin = plugin;
    }

    //Replaces all spawned villagers with NationsVillagers and iron golems with NationsIronGolems
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        if (e.getEntity() instanceof CraftVillager) {
            EntityVillager villager = ((CraftVillager) e.getEntity()).getHandle();
            NationsVillager nationsVillager = Main.nationsManager.getVillagerByUUID(villager.getUniqueID());
            if (nationsVillager == null) {
                NationsVillager newNationsVillager = new NationsVillager(villager.getUniqueID());
                Main.nationsManager.addVillager(newNationsVillager);
                Chunk chunk = e.getLocation().getChunk();
                NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ(), chunk.getWorld());
                if (nationsChunk != null) {
                    newNationsVillager.setNationID(nationsChunk.getNationID());
                    Main.nationsManager.getNationByID(nationsChunk.getNationID()).incrementPopulation();
                }
            }
        }
        else if (e.getEntity() instanceof CraftIronGolem) {
            EntityIronGolem golem = ((CraftIronGolem) e.getEntity()).getHandle();
            NationsIronGolem nationsIronGolem = Main.nationsManager.getGolemByUUID(golem.getUniqueID());
            if (nationsIronGolem == null) {
                NationsIronGolem newNationsIronGolem = new NationsIronGolem(golem.getUniqueID());
                Main.nationsManager.addGolem(newNationsIronGolem);
                Chunk chunk = e.getLocation().getChunk();
                NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ(), chunk.getWorld());
                if (nationsChunk != null)
                    newNationsIronGolem.setNationID(nationsChunk.getNationID());
            }
        }
    }

    //Removes villager data once a villager or iron golem dies
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity() instanceof CraftVillager) {
            EntityVillager villager = ((CraftVillager) e.getEntity()).getHandle();
            NationsVillager nationsVillager = Main.nationsManager.getVillagerByUUID(villager.getUniqueID());
            nationsVillager.updateNameTag();
            if (nationsVillager.getNationID() != -1)
                Main.nationsManager.getNationByID(nationsVillager.getNationID()).decrementPopulation();
            Main.nationsManager.removeVillager(villager.getUniqueID());
        }
        else if (e.getEntity() instanceof CraftIronGolem) {
            EntityIronGolem golem = ((CraftIronGolem) e.getEntity()).getHandle();
            Main.nationsManager.getGolemByUUID(golem.getUniqueID()).stopJob();
            Main.nationsManager.removeGolem(golem.getUniqueID());
        }
    }

    //Attaches all already existing villagers and golems to NationsVillagers and NationsIronGolems when new chunks are loaded
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        for (Entity entity : e.getChunk().getEntities()) {
            if (entity instanceof CraftVillager) {
                EntityVillager villager = ((CraftVillager) entity).getHandle();
                //add new villager if one with that UUID doesn't already exist
                if (Main.nationsManager.getVillagerByUUID(villager.getUniqueID()) == null)
                    Main.nationsManager.addVillager(new NationsVillager(villager.getUniqueID()));
                NationsVillager nationsVillager = Main.nationsManager.getVillagerByUUID(villager.getUniqueID());
                Chunk chunk = e.getChunk();
                NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ(), chunk.getWorld());
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
            else if (entity instanceof CraftIronGolem) {
                EntityIronGolem golem = ((CraftIronGolem) entity).getHandle();
                //add new iron golem if one with that UUID doesn't already exist
                if (Main.nationsManager.getGolemByUUID(golem.getUniqueID()) == null)
                    Main.nationsManager.addGolem(new NationsIronGolem(golem.getUniqueID()));
                NationsIronGolem nationsIronGolem = Main.nationsManager.getGolemByUUID(golem.getUniqueID());
                Chunk chunk = e.getChunk();
                NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ(), chunk.getWorld());
                //add iron golem to nation if it is in a claimed chunk
                if (nationsChunk != null) {
                    if (nationsIronGolem.getNationID() == -1)
                        nationsIronGolem.setNationID(nationsChunk.getNationID());
                }
                //remove iron golem form its nation if it is not in a claimed chunk
                else {
                    if (nationsIronGolem.getNationID() != -1)
                        nationsIronGolem.setNationID(-1);
                }
            }
        }
    }
}
