package com.github.dawsonvilamaa.nationsandvillagesplugin;

import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.*;
import com.github.dawsonvilamaa.nationsandvillagesplugin.exceptions.NationNotFoundException;
import com.github.dawsonvilamaa.nationsandvillagesplugin.exceptions.VillageNotFoundException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class NationsManager {
    private HashMap<Integer, Nation> nations;
    private HashMap<UUID, NationsVillager> villagers;
    private HashMap<UUID, NationsPlayer> players;
    private ArrayList<NationsChunk> chunks;

    public static String navDir = "plugins\\NationsAndVillages";
    private int nextNationID;
    private int nextVillagerID;

    public static int startingMoney = 1000;
    public static int chunkCost = 10;

    public NationsManager() {
        this.nations = new HashMap<Integer, Nation>();
        this.villagers = new HashMap<UUID, NationsVillager>();
        this.players = new HashMap<UUID, NationsPlayer>();
        this.chunks = new ArrayList<NationsChunk>();
        this.nextNationID = -1;
        this.nextVillagerID = -1;
    }

    /**
     * @return nations
     */
    public HashMap<Integer, Nation> getNations() {
        return this.nations;
    }

    /**
     * @return villagers
     */
    public HashMap<UUID, NationsVillager> getVillagers() {
        return this.villagers;
    }

    /**
     * @return players
     */
    public HashMap<UUID, NationsPlayer> getPlayers() {
        return this.players;
    }

    /**
     * Use this when loading data
     * @param players
     */
    public void setPlayers(HashMap<UUID, NationsPlayer> players) {
        this.players = players;
    }

    /**
     * @return chunks
     */
    public ArrayList<NationsChunk> getChunks() {
        return this.chunks;
    }

    /**
     * @param chunks
     */
    public void setChunks(ArrayList<NationsChunk> chunks) {
        this.chunks = chunks;
    }

    /**
     * Adds a nation to the worldwide list
     * @param newNation
     */
    public void addNation(Nation newNation) {
        this.nations.put(newNation.getID(), newNation);
    }

    /**
     * Removes a nation from the worldwide list
     * @param nationID
     * @throws NationNotFoundException
     */
    public void removeNation(int nationID) throws NationNotFoundException {
        Nation removedNation = this.nations.get(nationID);
        if (removedNation == null) throw new NationNotFoundException();
        else this.nations.remove(nationID);
    }

    /**
     * Returns a nation with the given ID
     * @param nationID
     * @return
     */
    public Nation getNationByID(int nationID) {
        return this.nations.get(nationID);
    }

    /**
     * Returns a nation by a searched name, or returns null if it does not exist
     * @param name
     * @return nation
     */
    public Nation getNationByName(String name) {
        for (Nation nation : this.nations.values()) {
            if (nation.getName().toLowerCase().equals(name.toLowerCase())) return nation;
        }
        return null;
    }

    /**
     * @param uuid
     * @param nationsVillager
     */
    public void addVillager(UUID uuid, NationsVillager nationsVillager) {
        this.villagers.put(uuid, nationsVillager);
        Bukkit.broadcastMessage("Villagers: " + this.villagers.values().size());
    }

    /**
     * @param uuid
     */
    public void removeVillager(UUID uuid) {
        this.villagers.remove(uuid);
        Bukkit.broadcastMessage("Villagers: " + this.villagers.values().size());
    }

    /**
     * @param uuid
     * @return
     */
    public NationsVillager getVillagerByUUID(UUID uuid) {
        return this.villagers.get(uuid);
    }

    /**
     * Adds a player to the worldwide list
     * @param newPlayer
     */
    public NationsPlayer addPlayer(Player newPlayer) {
        this.players.put(newPlayer.getUniqueId(), new NationsPlayer(newPlayer));
        return this.players.get(newPlayer.getUniqueId());
    }

    /**
     * Returns a player by a searched UUID, or returns null if it does not exist
     * @param uuid
     * @return
     */
    public NationsPlayer getPlayerByUUID(UUID uuid) {
        for (NationsPlayer player : this.players.values()) {
            if (player.getUUID().equals(uuid)) return player;
        }
        return null;
    }

    /**
     * Adds a claimed chunk to the worldwide list, saving its coordinates and the ID of the owning nation
     * @param x
     * @param y
     * @param nationID
     */
    public void addChunk(int x, int y, int nationID) {
        this.chunks.add(new NationsChunk(x, y, nationID));
    }

    /**
     * Unclaims a chunk and removes it from the worldwide list
     * @param x
     * @param y
     * @param nationID
     */
    public void removeChunk(int x, int y, int nationID) {
        for (int i = 0; i < this.chunks.size(); i++) {
            if (this.chunks.get(i).getX() == x && this.chunks.get(i).getZ() == y && this.chunks.get(i).getNationID() == nationID) {
                this.chunks.remove(i);
                this.chunks.trimToSize();
            }
        }
    }

    /**
     * Returns a NationsChunk at the given coordinates if it is claimed. If it is not claimed, null is returned
     * @param x
     * @param z
     * @return chunk
     */
    public NationsChunk getChunkByCoords(int x, int z) {
        for (NationsChunk chunk : this.chunks) {
            if (chunk.getX() == x && chunk.getZ() == z) return chunk;
        }
        return null;
    }

    //IDs

    /**
     * @return nextNationID
     */
    public int nextNationID() {
        this.nextNationID = this.nextNationID + 1;
        return this.nextNationID;
    }

    /**
     * @return nextVillagerID
     */
    public int nextVillagerID() {
        this.nextVillagerID = this.nextVillagerID + 1;
        return this.nextVillagerID;
    }
}