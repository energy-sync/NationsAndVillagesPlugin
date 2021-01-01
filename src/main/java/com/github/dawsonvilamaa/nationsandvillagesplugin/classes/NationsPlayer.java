package com.github.dawsonvilamaa.nationsandvillagesplugin.classes;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import java.util.UUID;

public class NationsPlayer {
    private String username;
    private UUID uuid;
    private int money;
    private int nationID;
    private NationsChunk currentChunk;
    private boolean autoClaim;
    private boolean autoUnclaim;

    /**
     * @param player
     */
    public NationsPlayer(Player player) {
        this.username = player.getName();
        this.uuid = player.getUniqueId();
        this.money = Main.nationsManager.startingMoney;
        this.nationID = -1;
        this.currentChunk = new NationsChunk(-1, -1, -1);
        this.autoClaim = false;
        this.autoUnclaim = false;
    }

    /**
     * @param jsonPlayer
     */
    public NationsPlayer(JSONObject jsonPlayer) {
        this.username = jsonPlayer.get("username").toString();
        this.uuid = UUID.fromString(jsonPlayer.get("uuid").toString());
        this.money = Integer.parseInt(jsonPlayer.get("money").toString());
        this.nationID = Integer.parseInt(jsonPlayer.get("nationID").toString());
        this.currentChunk = new NationsChunk(Integer.parseInt(jsonPlayer.get("currentChunkX").toString()), Integer.parseInt(jsonPlayer.get("currentChunkZ").toString()), Integer.parseInt(jsonPlayer.get("currentChunkNationID").toString()));
        this.autoClaim = false;
        this.autoUnclaim = false;
    }

    /**
     * @return name
     */
    public String getName() {
        return this.username;
    }

    /**
     * @param newName
     */
    public void setName(String newName) {
        this.username = newName;
    }

    /**
     * @return uuid
     */
    public UUID getUUID() {
        return this.uuid;
    }

    /**
     * @return money
     */
    public int getMoney() {
        return this.money;
    }

    /**
     * Gives a specified amount of money to this player and returns the new value
     * @param amount
     * @return
     */
    public int addMoney(int amount) {
        this.money += amount;
        return this.money;
    }

    /**
     * Takes away a specified amount of money from this player and returns the new value
     * @param amount
     * @return
     */
    public int removeMoney(int amount) {
        this.money -= amount;
        return this.money;
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

    public JSONObject toJSON() {
        JSONObject jsonNationsPlayer = new JSONObject();
        jsonNationsPlayer.put("username", this.username);
        jsonNationsPlayer.put("uuid", this.uuid.toString());
        jsonNationsPlayer.put("money", String.valueOf(this.money));
        jsonNationsPlayer.put("nationID", String.valueOf(this.nationID));
        if (this.currentChunk == null) {
            jsonNationsPlayer.put("currentChunkX", 0);
            jsonNationsPlayer.put("currentChunkZ", 0);
            jsonNationsPlayer.put("currentChunkNationID", -1);
        }
        else {
            jsonNationsPlayer.put("currentChunkX", this.currentChunk.getX());
            jsonNationsPlayer.put("currentChunkZ", this.currentChunk.getZ());
            jsonNationsPlayer.put("currentChunkNationID", this.currentChunk.getNationID());
        }
        return jsonNationsPlayer;
    }

    /**
     * @return currentChunk
     */
    public NationsChunk getCurrentChunk() {
        return this.currentChunk;
    }

    /**
     * @param chunk
     */
    public void setCurrentChunk(NationsChunk chunk) {
        this.currentChunk = chunk;
    }

    /**
     * @return autoclaim
     */
    public boolean isAutoClaiming() {
        return this.autoClaim;
    }

    /**
     * @param autoClaim
     */
    public void setAutoClaim(boolean autoClaim) {
        this.autoClaim = autoClaim;
    }

    /**
     * @return autounclaim
     */
    public boolean isAutoUnclaiming() {
        return this.autoUnclaim;
    }

    /**
     * @param autoUnclaim
     */
    public void setAutoUnclaim(boolean autoUnclaim) {
        this.autoUnclaim = autoUnclaim;
    }
}