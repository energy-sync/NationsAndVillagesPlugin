package com.github.dawsonvilamaa.nationsandvillagesplugin.npcs;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.Nation;
import net.minecraft.server.v1_16_R3.EntityHuman;
import net.minecraft.server.v1_16_R3.EntityIronGolem;
import net.minecraft.server.v1_16_R3.EntityVillager;
import net.minecraft.server.v1_16_R3.PathfinderGoalNearestAttackableTarget;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftIronGolem;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.json.simple.JSONObject;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.dawsonvilamaa.nationsandvillagesplugin.npcs.Guard.HOSTILE_MOBS;

public class NationsIronGolem {
    static final int RADIUS = 16;
    static final int ATTACK_RANGE = 2;

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
        this.runnable = new BukkitRunnable() {
            @Override
            public void run() {
                CraftIronGolem ironGolem = (CraftIronGolem) Bukkit.getEntity(uuid);
                if (ironGolem != null && ironGolem.getLocation().getChunk().isLoaded()) {
                    ticks++;

                    //find nearby hostile enemies
                    List<Entity> nearbyEntities = ironGolem.getNearbyEntities(RADIUS, 2, RADIUS);
                    if (nearbyEntities.size() > 0) {
                        Map.Entry<Entity, Double> closestEntity = getClosestEnemy(ironGolem, getNationID());
                        if (closestEntity != null) {
                            //attack enemy
                            Entity enemy = closestEntity.getKey();
                            attackEnemy(ironGolem, enemy);
                        }
                    }

                    if (ticks >= 8)
                        ticks = 0;
                }
            }
        }.runTaskTimer(Main.plugin, 20, 5);
    }

    /**
     * Gets all enemies (hostile mobs, enemy players, villagers, and iron golems) within a certain radius of the iron golem and returns the closest one
     * @param ironGolem
     * @param nationID
     * @return
     */
    public Map.Entry<Entity, Double> getClosestEnemy(CraftIronGolem ironGolem, int nationID) {
        List<Entity> nearbyEntities = ironGolem.getNearbyEntities(RADIUS, 2, RADIUS);

        if (nearbyEntities.size() == 0)
            return null;

        Map.Entry<Entity, Double> closestEntity = null;
        Nation ironGolemNation = Main.nationsManager.getNationByID(nationID);
        for (Entity entity : nearbyEntities) {
            if (!entity.isDead()) {
                boolean isEnemy = false;

                //villager
                if (entity.getType() == EntityType.VILLAGER && ironGolemNation.isEnemy(Main.nationsManager.getVillagerByUUID(entity.getUniqueId()).getNationID()))
                    isEnemy = true;

                    //iron golem
                else if (entity.getType() == EntityType.IRON_GOLEM && ironGolemNation.isEnemy(Main.nationsManager.getGolemByUUID(entity.getUniqueId()).getNationID()))
                    isEnemy = true;

                    //player
                else if (entity.getType() == EntityType.PLAYER && ironGolemNation.isEnemy(Main.nationsManager.getPlayerByUUID(entity.getUniqueId()).getNationID()))
                    isEnemy = true;

                    //hostile mobs
                else {
                    for (EntityType entityType : HOSTILE_MOBS) {
                        if (entity.getType() == entityType)
                            isEnemy = true;
                    }
                }

                //set closest entity
                if (isEnemy) {
                    double dist = distance(ironGolem.getLocation(), entity.getLocation());
                    if (closestEntity == null || dist < closestEntity.getValue())
                        closestEntity = new AbstractMap.SimpleEntry<>(entity, dist);
                }
            }
        }
        return closestEntity;
    }

    /**
     * Makes the iron golem attack an entity if it is a player, villager, or iron golem
     * @param ironGolem
     * @param enemy
     */
    public void attackEnemy(CraftIronGolem ironGolem, Entity enemy) {
        EntityIronGolem ironGolemHandle = ironGolem.getHandle();

        if (enemy instanceof CraftPlayer)
            ironGolemHandle.targetSelector.a(0, new PathfinderGoalNearestAttackableTarget<>(ironGolemHandle, EntityHuman.class, true));
        else if (enemy instanceof CraftVillager)
            ironGolemHandle.targetSelector.a(0, new PathfinderGoalNearestAttackableTarget<>(ironGolemHandle, EntityVillager.class, true));
        else if (enemy instanceof CraftIronGolem)
            ironGolemHandle.targetSelector.a(0, new PathfinderGoalNearestAttackableTarget<>(ironGolemHandle, EntityIronGolem.class, true));
    }

    /**
     * Makes entity attacked by the iron golem attack the iron golem if it is a villager or iron golem
     * @param ironGolem
     * @param enemy
     */
    public void makeEnemyRetaliate(CraftIronGolem ironGolem, Entity enemy) {
        if (!enemy.isDead()) {
            //villager
            if (enemy.getType() == EntityType.VILLAGER) {
                NationsVillager nationsVillager = Main.nationsManager.getVillagerByUUID(enemy.getUniqueId());
                if (nationsVillager.getJob() == NationsVillager.Job.GUARD)
                    ((Guard) nationsVillager).attackEnemy(((CraftVillager) Bukkit.getEntity(nationsVillager.getUniqueID())), ironGolem);
            }

            //iron golem
            else if (enemy.getType() == EntityType.IRON_GOLEM) {
                EntityIronGolem ironGolemHandle = ((CraftIronGolem) enemy).getHandle();
                ironGolemHandle.targetSelector.a(0, new PathfinderGoalNearestAttackableTarget<>(ironGolemHandle, EntityHuman.class, true));
            }
        }
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
