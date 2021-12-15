package com.github.dawsonvilamaa.nationsandvillagesplugin.classes;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.NationsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Nation {
    private String name;
    private int id;
    private UUID ownerUUID;
    private ArrayList<UUID> members;
    private int population;
    private int numChunks;
    private NationsConfig config;
    private ArrayList<Integer> enemies;

    ArrayList<UUID> invitedPlayers;

    /**
     * @param name
     * @param owner
     */
    public Nation(String name, Player owner, int id) {
        this.name = name;
        this.id = id;
        this.ownerUUID = owner.getUniqueId();
        this.members = new ArrayList<>();
        this.population = 0;
        this.numChunks = 0;
        this.config = new NationsConfig();
        this.enemies = new ArrayList<>();
        this.invitedPlayers = new ArrayList<>();
    }

    public Nation(JSONObject jsonNation) {
        this.name = jsonNation.get("name").toString();
        this.id = Integer.parseInt(jsonNation.get("id").toString());
        this.ownerUUID = UUID.fromString(jsonNation.get("ownerUUID").toString());
        this.members = new ArrayList<>();
        ArrayList<Object> membersArray = ((JSONArray) jsonNation.get("members"));
        for (Object member : membersArray)
            this.members.add(UUID.fromString(member.toString()));
        this.population = Integer.parseInt(jsonNation.get("population").toString());
        this.numChunks = Integer.parseInt(jsonNation.get("numChunks").toString());
        this.config = new NationsConfig(jsonNation);
        this.enemies = new ArrayList<>();
        ArrayList<Object> enemiesArray = ((JSONArray) jsonNation.get("enemies"));
        for (Object nationID : enemiesArray)
            this.enemies.add(((Long) nationID).intValue());
        this.invitedPlayers = new ArrayList<>();
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
        this.name = newName;
    }

    /**
     * @return id
     */
    public int getID() {
        return this.id;
    }

    /**
     * @return ownerUUID
     */
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    /**
     * @param newOwnerUUID
     */
    public void setOwnerUUID(UUID newOwnerUUID) {
        this.ownerUUID = newOwnerUUID;
    }

    /**
     * @return members
     */
    public ArrayList<UUID> getMemberUUIDs() {
        return this.members;
    }

    /**
     * @param uuid
     */
    public void addMember(UUID uuid) {
        this.members.add(uuid);
    }

    /**
     * @param uuid
     */
    public void removeMember(UUID uuid) {
        this.members.remove(uuid);
        this.members.trimToSize();
    }

    /**
     * @return enemies
     */
    public ArrayList<Integer> getEnemies() {
        return this.enemies;
    }

    /**
     * Adds a nation to this nation's enemy list
     * @param enemyNationID
     */
    public void addEnemy(int enemyNationID) {
        this.enemies.add(enemyNationID);
    }

    /**
     * Removes a nation from the nation's enemy list
     * @param enemyNationID
     */
    public void removeEnemy(int enemyNationID) {
        this.enemies.remove(Integer.valueOf(enemyNationID));
    }

    /**
     * Returns whether the given nation is an enemy
     * @param enemyNationID
     * @return
     */
    public boolean isEnemy(int enemyNationID) {
        if (enemyNationID == -1)
            return false;
        return this.enemies.contains(enemyNationID);
    }

    /**
     * @return population
     */
    public int getPopulation() {
        return this.population;
    }

    /**
     * @return population
     */
    public int incrementPopulation() {
        this.population++;
        return this.population;
    }

    /**
     * @return population
     */
    public int decrementPopulation() {
        this.population--;
        return this.population;
    }

    /**
     * @return chunks
     */
    public int getNumChunks() {
        return this.numChunks;
    }

    public void incrementChunks() {
        this.numChunks++;
    }

    public void decrementChunks() {
        this.numChunks--;
    }

    /**
     * @return config
     */
    public NationsConfig getConfig() {
        return this.config;
    }

    /**
     * @param config
     */
    public void setConfig(NationsConfig config) {
        this.config = config;
    }

    /**
     * @return invitedPlayers
     */
    public ArrayList<UUID> getInvitedPlayers() {
        return this.invitedPlayers;
    }

    /**
     * Adds a player to the list of invited players that can join this nation. Invite expires after 1 minute
     * @param uuid
     */
    public void addInvitedPlayer(UUID uuid) {
        this.invitedPlayers.add(uuid);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline() == true) {
            player.sendMessage(ChatColor.GREEN + "You have been invited to join " + this.name + "! To join, type /nation join [name]");
            ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(1);
            executorService.schedule(() -> {
                invitedPlayers.remove(uuid);
                if (Main.nationsManager.getPlayerByUUID(uuid).getNationID() == -1)
                    player.sendMessage(ChatColor.RED + "Your invite has expired!");
            }, 1, TimeUnit.MINUTES);
        }
        else player.sendMessage(ChatColor.RED + "That player is not online");
    }

    /**
     * Returns whether a given player has a pending invite for this nation
     * @param uuid
     * @return
     */
    public boolean isPlayerInvited(UUID uuid) {
        return this.invitedPlayers.contains(uuid);
    }

    /**
     * Returns this Nation as a JSONObject
     * @return jsonNation
     */
    public JSONObject toJSON() {
        JSONObject jsonNation = new JSONObject();
        jsonNation.put("name", this.name);
        jsonNation.put("id", String.valueOf(this.id));
        jsonNation.put("ownerUUID", this.ownerUUID.toString());
        JSONArray membersArray = new JSONArray();
        for (UUID member : this.members)
            membersArray.add(member.toString());
        jsonNation.put("members", membersArray);
        jsonNation.put("population", String.valueOf(this.population));
        jsonNation.put("numChunks", String.valueOf(this.numChunks));
        jsonNation.put("legatePermissions", this.config.getPermissionByRank(NationsManager.Rank.LEGATE).toJSON());
        jsonNation.put("memberPermissions", this.config.getPermissionByRank(NationsManager.Rank.MEMBER).toJSON());
        jsonNation.put("nonMemberPermissions", this.config.getPermissionByRank(NationsManager.Rank.NONMEMBER).toJSON());
        jsonNation.put("golemsAttackEnemies", String.valueOf(this.config.getGolemsAttackEnemies()));
        JSONArray enemiesArray = new JSONArray();
        for (Integer nationID : this.enemies)
            enemiesArray.add(nationID);
        jsonNation.put("enemies", enemiesArray);
        return jsonNation;
    }
}