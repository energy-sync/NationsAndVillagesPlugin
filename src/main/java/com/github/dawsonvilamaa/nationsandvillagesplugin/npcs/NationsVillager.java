package com.github.dawsonvilamaa.nationsandvillagesplugin.npcs;

import net.minecraft.server.v1_16_R3.ChatComponentText;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;

import java.util.UUID;

public class NationsVillager {
    private String name;
    private UUID uuid;
    private int nationID;
    private Job job;
    private Consumer<PlayerInteractEntityEvent> onClick;

    public enum Job {
      NONE,
        MERCHANT,
        MINER,
        FARMER,
        LUMBERJACK,
        GUARD
    };

    /**
     * Creates a NationsVillager data class and attaches it to an entity in the world
     * @param uuid
     */
    public NationsVillager(UUID uuid) {
        this.name = "Villager";
        this.uuid = uuid;
        this.nationID = -1;
        this.job = Job.NONE;
        this.onClick = null;
    }

    /**
     * @param jsonVillager
     */
    public NationsVillager(JSONObject jsonVillager) {
        this.uuid = UUID.fromString(jsonVillager.get("uuid").toString());
        this.name = jsonVillager.get("name").toString();
        this.nationID = Integer.parseInt(jsonVillager.get("nationID").toString());
        this.job = Job.valueOf(jsonVillager.get("job").toString());
        setOnClick(e -> {
            Bukkit.broadcastMessage(this.getJob().toString());
        });
    }

    /**
     * @return uuid
     */
    public UUID getUniqueID() {
        return this.uuid;
    }

    /**
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        ((CraftVillager) Bukkit.getEntity(this.uuid)).getHandle().setCustomName(new ChatComponentText(name));
        this.name = name;
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
     * @return job
     */
    public Job getJob() {
        return this.job;
    }

    /**
     * @param job
     */
    public void setJob(Job job) {
        this.job = job;
    }

    /**
     * @param e
     */
    public void onClick(PlayerInteractEntityEvent e) {
        if (this.onClick != null)
            this.onClick.accept(e);
    }

    /**
     * @param consumer
     */
    public void setOnClick(Consumer<PlayerInteractEntityEvent> consumer) {
        this.onClick = consumer;
    }

    /**
     * @return onClick
     */
    public Consumer<PlayerInteractEntityEvent> getOnClick() {
        return this.onClick;
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
            if (Bukkit.getEntity(this.uuid).getLocation().distance(player.getLocation()) <= 30) speakToPlayer(player, message);
        }
    }

    /**
     * Makes the villager walk to a location
     * @param location
     */
    public boolean walkToLocation(Location location) {
        return ((CraftVillager) Bukkit.getEntity(this.uuid)).getHandle().getNavigation().a(location.getX(), location.getY(), location.getZ(), 0.6);
    }

    /**
     * Returns this NationsVillager in JSON format
     * @return jsonVillager
     */
    public JSONObject toJSON() {
        JSONObject jsonVillager = new JSONObject();
        jsonVillager.put("uuid", this.uuid.toString());
        jsonVillager.put("name", this.name);
        jsonVillager.put("nationID", String.valueOf(this.nationID));
        jsonVillager.put("job", this.job.toString());
        if (this.job == Job.MERCHANT)
            jsonVillager.put("shop", ((Merchant) this).getShop().toJSON());
        else if (this.job == Job.LUMBERJACK)
            jsonVillager.put("inventory", ((Lumberjack) this).inventoryToJSON());
        return jsonVillager;
    }
}
