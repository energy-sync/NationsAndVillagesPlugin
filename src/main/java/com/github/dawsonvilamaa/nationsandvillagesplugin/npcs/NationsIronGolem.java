package com.github.dawsonvilamaa.nationsandvillagesplugin.npcs;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.Nation;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftIronGolem;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.json.simple.JSONObject;

import java.util.*;

public class NationsIronGolem {
    static final int RADIUS = 16;

    private UUID uuid;
    private int nationID;
    private int ticks;

    private BukkitTask runnable;

    /**
     * Creates a NationsIronGolem data class and attaches it to an entity in the world
     * @param uuid
     */
    public NationsIronGolem(UUID uuid) {
        this.uuid = uuid;
        this.nationID = -1;
        this.ticks = 0;
        this.runnable = null;
        startJob();
    }

    /**
     * Creates a NationsIronGolem data class from a json object when loading saved entities
     * @param jsonGolem
     */
    public NationsIronGolem(JSONObject jsonGolem) {
        this.uuid = UUID.fromString(jsonGolem.get("uuid").toString());
        this.nationID = Integer.parseInt(jsonGolem.get("nationID").toString());
        this.ticks = 0;
        this.runnable = null;
        startJob();
    }

    /**
     * @return uuid
     */
    public UUID getUniqueID() {
        return this.uuid;
    }

    /**
     * @return nationID
     */
    public int getNationID() {
        return this.nationID;
    }

    /**
     * @param nationID
     */
    public void setNationID(int nationID) {
        this.nationID = nationID;
    }

    /**
     * Makes the iron golem walk to a location
     * @param location
     * @return
     */
    public boolean walkToLocation(Location location) {
        return ((CraftIronGolem) Bukkit.getEntity(this.uuid)).getHandle().getNavigation().a(location.getX(), location.getY(), location.getZ(), 0.3);
    }

    /**
     * Stars the iron golem's job of attacking enemies
     */
    public void startJob() {
        final CraftIronGolem[] golem = {(CraftIronGolem) Bukkit.getEntity(getUniqueID())};
        this.runnable = new BukkitRunnable() {
            @Override
            public void run() {
                Entity vEntity = Bukkit.getEntity(getUniqueID());
                if (vEntity != null && vEntity.getLocation().getChunk().isLoaded()) {
                    ticks++;
                    if (golem[0] == null)
                        golem[0] = (CraftIronGolem) Bukkit.getEntity(getUniqueID());
                    if (golem[0] != null) {

                        //find nearby hostile enemies
                        List<Entity> nearbyEntities = golem[0].getNearbyEntities(RADIUS, RADIUS, RADIUS);
                        if (nearbyEntities.size() > 0) {
                            ArrayList<Map.Entry<Entity, Double>> closestMobs = new ArrayList<>();
                            for (Entity entity : nearbyEntities) {
                                //enemy players and villagers
                                if (entity.getType() == EntityType.PLAYER || entity.getType() == EntityType.VILLAGER) {
                                    Nation golemNation = Main.nationsManager.getNationByID(getNationID());
                                    if (golemNation != null) {
                                        Nation otherNation = null;
                                        if (entity.getType() == EntityType.PLAYER)
                                            otherNation = Main.nationsManager.getNationByID(Main.nationsManager.getPlayerByUUID(entity.getUniqueId()).getNationID());
                                        else if (entity.getType() == EntityType.VILLAGER) {
                                            NationsVillager enemyVillager = Main.nationsManager.getVillagerByUUID(entity.getUniqueId());
                                            if (enemyVillager != null)
                                                otherNation = Main.nationsManager.getNationByID(enemyVillager.getNationID());
                                        }
                                        if (otherNation != null && golemNation.isEnemy(otherNation.getID())) {
                                            Location golemLoc = golem[0].getLocation();
                                            Location mobLoc = entity.getLocation();
                                            double distance = distance(golemLoc, mobLoc);
                                            closestMobs.add(new AbstractMap.SimpleEntry<>(entity, distance));
                                        }
                                    }
                                }
                            }
                            if (closestMobs.size() > 0) {
                                closestMobs.sort(Comparator.comparing(Map.Entry::getValue));
                                Entity mob = closestMobs.get(0).getKey();
                                //attack mob
                                if (ticks % 4 == 0) {
                                    EntityIronGolem g = golem[0].getHandle();
                                    if (mob instanceof CraftPlayer)
                                        g.targetSelector.a(0, new PathfinderGoalNearestAttackableTarget<>(g, EntityHuman.class, true));
                                    else if (mob instanceof CraftVillager)
                                        g.targetSelector.a(0, new PathfinderGoalNearestAttackableTarget<>(g, EntityVillager.class, true));
                                }
                            }
                        }
                        if (ticks >= 8)
                            ticks = 0;
                    }
                }
            }
        }.runTaskTimer(Main.plugin, 20, 5);
    }

    /**
     * Stops the iron golem's job of attacking and enemies
     */
    public void stopJob() {
        this.runnable.cancel();
    }

    /**
     * Makes the golem look at a location
     * @param location
     * @param golem
     */
    private void lookAtLocation(Location location, Entity golem) {
        Vector lookAt = location.toVector().subtract(golem.getLocation().toVector());
        Location loc = golem.getLocation();
        loc.setDirection(lookAt);
        golem.teleport(loc);
    }

    /**
     * Returns this NationsIronGolem in JSON format
     * @return
     */
    public JSONObject toJSON() {
        JSONObject jsonGolem = new JSONObject();
        jsonGolem.put("uuid", this.uuid.toString());
        jsonGolem.put("nationID", String.valueOf(this.nationID));
        return jsonGolem;
    }

    /**
     * @param loc1
     * @param loc2
     * @return distance
     */
    private double distance(Location loc1, Location loc2) {
        return Math.sqrt(Math.pow(loc2.getX() - loc1.getX(), 2) + Math.pow(loc2.getY() - loc1.getY(), 2) + Math.pow(loc2.getZ() - loc1.getZ(), 2));
    }
}
