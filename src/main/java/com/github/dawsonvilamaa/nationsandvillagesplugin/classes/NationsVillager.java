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

import java.util.UUID;

public class NationsVillager {
    private EntityVillager entity;
    private String name;
    private int nationID;

    /**
     * Creates a NationsVillager data class and attaches it to an entity in the world
     * @param entity
     */
    public NationsVillager(EntityVillager entity) {
        this.entity = entity;
        this.name = "Villager";
        this.nationID = -1;
    }

    /**
     * @param jsonVillager
     */
    public NationsVillager(JSONObject jsonVillager) {
        this.entity = ((CraftVillager) Bukkit.getEntity(UUID.fromString(jsonVillager.get("uuid").toString()))).getHandle();
        this.name = jsonVillager.get("name").toString();
        this.nationID = Integer.parseInt(jsonVillager.get("nationID").toString());
    }

    /**
     * @return entity
     */
    public EntityVillager getEntity() {
        return this.entity;
    }

    /**
     * @return uuid
     */
    public UUID getUniqueID() {
        return this.entity.getUniqueID();
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
        jsonVillager.put("uuid", this.entity.getUniqueID().toString());
        jsonVillager.put("name", this.name);
        jsonVillager.put("nationID", String.valueOf(this.nationID));
        return jsonVillager;
    }
}
