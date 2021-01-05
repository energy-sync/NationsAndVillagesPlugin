package com.github.dawsonvilamaa.nationsandvillagesplugin.classes;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.NationsManager;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import java.util.UUID;

public class NationsPlayer {
    private String username;
    private UUID uuid;
    private int money;
    private int nationID;
    private NationsManager.Rank rank;
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
        this.rank = NationsManager.Rank.NONMEMBER;
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
        this.rank = NationsManager.Rank.valueOf(jsonPlayer.get("rank").toString());
        JSONObject jsonCurrentChunk = (JSONObject) jsonPlayer.get("currentChunk");
        this.currentChunk = new NationsChunk(Integer.parseInt(jsonCurrentChunk.get("x").toString()), Integer.parseInt(jsonCurrentChunk.get("z").toString()), Integer.parseInt(jsonCurrentChunk.get("id").toString()));
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

    /**
     * @return rank
     */
    public NationsManager.Rank getRank() {
        return this.rank;
    }

    /**
     * @param rank
     */
    public void setRank(NationsManager.Rank rank) {
        this.rank = rank;
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

    public JSONObject toJSON() {
        JSONObject jsonNationsPlayer = new JSONObject();
        jsonNationsPlayer.put("username", this.username);
        jsonNationsPlayer.put("uuid", this.uuid.toString());
        jsonNationsPlayer.put("money", String.valueOf(this.money));
        jsonNationsPlayer.put("nationID", String.valueOf(this.nationID));
        jsonNationsPlayer.put("rank", this.rank.toString());
        JSONObject jsonCurrentChunk = new JSONObject();
        if (this.currentChunk == null) {
            jsonCurrentChunk.put("x", 0);
            jsonCurrentChunk.put("z", 0);
            jsonCurrentChunk.put("id", -1);
        }
        else {
            jsonCurrentChunk.put("x", this.currentChunk.getX());
            jsonCurrentChunk.put("z", this.currentChunk.getZ());
            jsonCurrentChunk.put("id", this.currentChunk.getNationID());
        }
        jsonNationsPlayer.put("currentChunk", jsonCurrentChunk);
        return jsonNationsPlayer;
    }
}