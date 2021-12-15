package com.github.dawsonvilamaa.nationsandvillagesplugin.classes;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.NationsManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import java.util.UUID;

public class NationsPlayer {
    public final int STARTING_MONEY = 1000;
    public enum AUTOCLAIM_MODE {
        NONE,
        AUTOCLAIM,
        AUTOUNCLAIM
    }

    private String username;
    private UUID uuid;
    private int money;
    private int nationID;
    private NationsManager.Rank rank;
    private NationsChunk currentChunk;
    private AUTOCLAIM_MODE autoClaimMode;

    /**
     * @param player
     */
    public NationsPlayer(Player player) {
        this.username = player.getName();
        this.uuid = player.getUniqueId();
        this.money = STARTING_MONEY;
        this.nationID = -1;
        this.rank = NationsManager.Rank.NONMEMBER;
        this.currentChunk = new NationsChunk(-1, -1, player.getWorld(), -1);
        this.autoClaimMode = AUTOCLAIM_MODE.NONE;;
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
        this.currentChunk = new NationsChunk(Integer.parseInt(jsonCurrentChunk.get("x").toString()), Integer.parseInt(jsonCurrentChunk.get("z").toString()), Bukkit.getWorld(jsonCurrentChunk.get("world").toString()), Integer.parseInt(jsonCurrentChunk.get("nationID").toString()));
        this.autoClaimMode = AUTOCLAIM_MODE.NONE;
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
    public UUID getUniqueID() {
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
        Player player = Bukkit.getPlayer(this.uuid);
        if (player != null)
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.5f);
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
     * Returns if the player is autoclaiming, autounclaiming, or neither
     * @return autoClaimMode
     */
    public AUTOCLAIM_MODE getAutoClaimMode() {
        return this.autoClaimMode;
    }

    /**
     * @param autoClaimMode
     */
    public void setAutoClaimMode(AUTOCLAIM_MODE autoClaimMode) {
        this.autoClaimMode = autoClaimMode;
    }

    public JSONObject toJSON() {
        JSONObject jsonNationsPlayer = new JSONObject();
        jsonNationsPlayer.put("username", this.username);
        jsonNationsPlayer.put("uuid", this.uuid.toString());
        jsonNationsPlayer.put("money", String.valueOf(this.money));
        jsonNationsPlayer.put("nationID", String.valueOf(this.nationID));
        jsonNationsPlayer.put("rank", this.rank.toString());
        if (this.currentChunk == null)
            jsonNationsPlayer.put("currentChunk", new NationsChunk(0, 0, Bukkit.getWorlds().get(0), -1).toJSON());
        else
            jsonNationsPlayer.put("currentChunk", this.currentChunk.toJSON());
        return jsonNationsPlayer;
    }
}