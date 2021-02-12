package com.github.dawsonvilamaa.nationsandvillagesplugin.classes;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.NationsManager;
import com.github.dawsonvilamaa.nationsandvillagesplugin.exceptions.VillageNotFoundException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.Time;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Nation {
    private String name;
    private int id;
    private NationsPlayer owner;
    private ArrayList<UUID> members;
    private int population;
    private int chunks;

    //permissions
    NationsPermission legatePermissions;
    NationsPermission memberPermissions;
    NationsPermission nonMemberPermissions;

    ArrayList<UUID> invitedPlayers;

    /**
     * @param name
     * @param owner
     */
    public Nation(String name, Player owner, int id) {
        this.name = name;
        this.id = id;
        this.owner = Main.nationsManager.getPlayers().get(owner.getUniqueId());
        this.members = new ArrayList<>();
        this.population = 0;
        this.chunks = 0;
        this.legatePermissions = new NationsPermission(NationsManager.Rank.LEGATE);
        this.memberPermissions = new NationsPermission(NationsManager.Rank.MEMBER);
        this.nonMemberPermissions = new NationsPermission(NationsManager.Rank.NONMEMBER);
        this.invitedPlayers = new ArrayList<>();
    }

    public Nation(JSONObject jsonNation) {
        this.name = jsonNation.get("name").toString();
        this.id = Integer.parseInt(jsonNation.get("id").toString());
        this.owner = Main.nationsManager.getPlayerByUUID(UUID.fromString(jsonNation.get("owner").toString()));
        this.members = new ArrayList<>();
        ArrayList<Object> membersArray = ((JSONArray) jsonNation.get("members"));
        for (Object member : membersArray)
            this.members.add(UUID.fromString(member.toString()));
        this.population = Integer.parseInt(jsonNation.get("population").toString());
        this.chunks = Integer.parseInt(jsonNation.get("chunks").toString());
        this.legatePermissions = new NationsPermission((JSONObject) jsonNation.get("legatePermissions"));
        this.memberPermissions = new NationsPermission((JSONObject) jsonNation.get("memberPermissions"));
        this.nonMemberPermissions = new NationsPermission((JSONObject) jsonNation.get("nonMemberPermissions"));
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
     * @return owner
     */
    public NationsPlayer getOwner() {
        return this.owner;
    }

    /**
     * @param newOwner
     */
    public void setOwner(NationsPlayer newOwner) {
        this.owner = newOwner;
    }

    /**
     * @return members
     */
    public ArrayList<UUID> getMembers() {
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
     * @return population
     */
    public int getPopulation() {
        return this.population;
    }

    /**
     * @return population
     */
    public int incrementPopulation() {
        this.population = this.population + 1;
        return this.population;
    }

    /**
     * @return population
     */
    public int decrementPopulation() {
        this.population = this.population - 1;
        return this.population;
    }

    /**
     * @return chunks
     */
    public int getNumChunks() {
        return this.chunks;
    }

    public void incrementChunks() {
        this.chunks = this.chunks + 1;
    }

    public void decrementChunks() {
        this.chunks = this.chunks - 1;
    }

    /**
     * Returns the NationsPermission of a given rank in this nation
     * @param rank
     * @return NationsPermission
     */
    public NationsPermission getPermissionByRank(NationsManager.Rank rank) {
        switch (rank) {
            case LEADER:
                return new NationsPermission(NationsManager.Rank.LEADER);
            case LEGATE:
                return this.legatePermissions;
            case MEMBER:
                return this.memberPermissions;
            case NONMEMBER:
                return this.nonMemberPermissions;
            default:
                return null;
        }
    }

    public void setPermissionByRank(NationsManager.Rank rank, NationsPermission perms) {
        switch (rank) {
            case LEGATE:
                this.legatePermissions = perms;
            break;

            case MEMBER:
                this.memberPermissions = perms;
            break;

            case NONMEMBER:
                this.nonMemberPermissions = perms;
            break;
        }
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
    public void addInvitedPlayer(UUID uuid) throws InterruptedException {
        this.invitedPlayers.add(uuid);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline() == true)
            player.sendMessage(ChatColor.GREEN + "You have been invited to join " + this.name + "! To join, type /nation join [name]");
        ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(1);
        executorService.schedule(() -> {
            invitedPlayers.remove(uuid);
            if (player != null && Main.nationsManager.getPlayerByUUID(uuid).getNationID() == -1 && player.isOnline() == true)
                player.sendMessage(ChatColor.RED + "Your invite has expired!");
        }, 1, TimeUnit.MINUTES);
    }

    /**
     * Returns whether or not a given player has a pending invite for this nation
     * @param uuid
     * @return
     */
    public boolean isPlayerInvited(UUID uuid) {
        for (UUID player : this.invitedPlayers) {
            if (player.equals(uuid))
                return true;
        }
        return false;
    }

    /**
     * Returns this Nation as a JSONObject
     * @return jsonNation
     */
    public JSONObject toJSON() {
        JSONObject jsonNation = new JSONObject();
        jsonNation.put("name", this.name);
        jsonNation.put("id", String.valueOf(this.id));
        jsonNation.put("owner", this.owner.getUniqueID().toString());
        JSONArray membersArray = new JSONArray();
        for (UUID member : this.members)
            membersArray.add(member.toString());
        jsonNation.put("members", membersArray);
        jsonNation.put("population", String.valueOf(this.population));
        jsonNation.put("chunks", String.valueOf(this.chunks));
        jsonNation.put("legatePermissions", this.legatePermissions.toJSON());
        jsonNation.put("memberPermissions", this.memberPermissions.toJSON());
        jsonNation.put("nonMemberPermissions", this.nonMemberPermissions.toJSON());
        return jsonNation;
    }
}