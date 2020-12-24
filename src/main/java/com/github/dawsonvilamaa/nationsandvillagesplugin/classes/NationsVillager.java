package com.github.dawsonvilamaa.nationsandvillagesplugin.classes;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.json.simple.JSONObject;

public class NationsVillager {
    private EntityVillager entity;
    private String name;
    private int id;
    private int nationID;
    private Village village;

    /**
     * Creates a NationsVillager data class and attaches it to an entity in the world
     * @param entity
     */
    public NationsVillager(EntityVillager entity) {
        this.entity = entity;
        this.name = "Villager";
        this.id = -1; //change later
        this.nationID = -1;
    }

    /**
     * @return entity
     */
    public EntityVillager getEntity() {
        return this.entity;
    }

    /**
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param newName
     */
    public void setName(String newName) {
        entity.setCustomName(new ChatComponentText(newName));
        this.name = newName;
    }

    /**
     * @return id
     */
    public int getID() {
        return this.id;
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
     * @return village
     */
    public Village getVillage() {
        return this.village;
    }

    /**
     * @param newVillage
     */
    public void setVillage(Village newVillage) {
        this.village = newVillage;
    }

    /**
     * Send a message from this villager to a specified player
     * @param player
     * @param message
     */
    public void speakToPlayer(Player player, String message) {
        player.sendMessage("<" + name + "> " + message);
    }

    /**
     * Sends a message to all players within 30 blocks
     * @param message
     */
    public void shout(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (Bukkit.getEntity(entity.getUniqueID()).getLocation().distance(player.getLocation()) <= 30) speakToPlayer(player, message);
        }
    }

    public JSONObject toJSON() {
        JSONObject jsonVillager = new JSONObject();
        jsonVillager.put("name", this.name);
        jsonVillager.put("id", String.valueOf(this.id));
        jsonVillager.put("nation", String.valueOf(this.nationID));
        jsonVillager.put("village", this.village.getID());
        return jsonVillager;
    }
}
