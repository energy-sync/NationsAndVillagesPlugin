package com.github.dawsonvilamaa.nationsandvillagesplugin;

import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.*;
import com.github.dawsonvilamaa.nationsandvillagesplugin.exceptions.NationNotFoundException;
import com.github.dawsonvilamaa.nationsandvillagesplugin.gui.InventoryGUI;
import com.github.dawsonvilamaa.nationsandvillagesplugin.npcs.NationsIronGolem;
import com.github.dawsonvilamaa.nationsandvillagesplugin.npcs.NationsVillager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class NationsManager {
    private HashMap<Integer, Nation> nations;
    private HashMap<UUID, NationsVillager> villagers;
    private HashMap<UUID, NationsPlayer> players;
    private HashMap<UUID, NationsIronGolem> golems;
    private ArrayList<NationsChunk> chunks;
    private HashMap<UUID, InventoryGUI> menus;
    private HashMap<UUID, Map.Entry<ShopItem, ScheduledThreadPoolExecutor>> playersChoosingMerchants;

    public static String navDir = "plugins\\NationsAndVillages";
    private int nextNationID;

    public static int startingMoney = 1000;
    public static int chunkCost = 10;

    public enum Rank {
        LEADER,
        LEGATE,
        MEMBER,
        NONMEMBER
    }

    public NationsManager() {
        this.nations = new HashMap<>();
        this.villagers = new HashMap<>();
        this.players = new HashMap<>();
        this.golems = new HashMap<>();
        this.chunks = new ArrayList<>();
        this.menus = new HashMap<>();
        this.nextNationID = -1;
        this.playersChoosingMerchants = new HashMap<>();
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
     * @return golems
     */
    public HashMap<UUID, NationsIronGolem> getGolems() {
        return this.golems;
    }

    /**
     * @return chunks
     */
    public ArrayList<NationsChunk> getChunks() {
        return this.chunks;
    }

    //Nations

    /**
     * Adds a nation to the worldwide list
     * @param newNation
     */
    public void addNation(Nation newNation) {
        this.nations.put(newNation.getID(), newNation);
        if (newNation.getID() > this.nextNationID)
            this.nextNationID = newNation.getID();
    }

    /**
     * Removes a nation from the worldwide list
     * @param nationID
     * @throws NationNotFoundException
     */
    public void removeNation(int nationID) {
        //unclaim all chunks
        for (int i = 0; i < this.chunks.size(); i++) {
            if (this.chunks.get(i).getNationID() == nationID)
                this.chunks.remove(this.chunks.get(i));
        }
        this.chunks.trimToSize();
        //remove all members
        for (NationsPlayer nationsPlayer : this.players.values()) {
            if (nationsPlayer.getNationID() == nationID) {
                nationsPlayer.setNationID(-1);
                nationsPlayer.setRank(Rank.NONMEMBER);
            }
        }
        //remove all villagers from nation
        for (NationsVillager nationsVillager : this.villagers.values()) {
            if (nationsVillager.getNationID() == nationID)
                nationsVillager.setNationID(-1);
        }
        this.nations.remove(nationID);
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

    //Villagers

    /**
     * @param nationsVillager
     * @return nationsVillager
     */
    public NationsVillager addVillager(NationsVillager nationsVillager) {
        this.villagers.put(nationsVillager.getUniqueID(), nationsVillager);
        Bukkit.broadcastMessage("Villagers: " + this.villagers.values().size());
        return getVillagerByUUID(nationsVillager.getUniqueID());
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

    //Players

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
     * @return nationsPlayer
     */
    public NationsPlayer getPlayerByUUID(UUID uuid) {
        for (NationsPlayer player : this.players.values()) {
            if (player.getUniqueID().equals(uuid))
                return player;
        }
        return null;
    }

    //Iron Golems

    /**
     * @param nationsIronGolem
     * @return nationsIronGolem
     */
    public NationsIronGolem addGolem(NationsIronGolem nationsIronGolem) {
        this.golems.put(nationsIronGolem.getUniqueID(), nationsIronGolem);
        Bukkit.broadcastMessage("Iron Golems: " + this.golems.values().size());
        return getGolemByUUID(nationsIronGolem.getUniqueID());
    }

    /**
     * @param uuid
     */
    public void removeGolem(UUID uuid) {
        this.golems.remove(uuid);
        Bukkit.broadcastMessage("Iron Golems: " + this.golems.values().size());
    }

    /**
     * Returns an iron golem by a searched UUID, or returns null if it does not exist
     * @param uuid
     * @return nationsIronGolem
     */
    public NationsIronGolem getGolemByUUID(UUID uuid) {
        for (NationsIronGolem golem : this.golems.values()) {
            if (golem.getUniqueID().equals(uuid))
                return golem;
        }
        return null;
    }

    //Chunks

    /**
     * Adds a claimed chunk to the worldwide list, saving its coordinates and the ID of the owning nation
     * @param x
     * @param y
     * @param nationID
     */
    public void addChunk(int x, int y, World world, int nationID) {
        this.chunks.add(new NationsChunk(x, y, world, nationID));
    }

    /**
     * Returns a NationsChunk at the given coordinates if it is claimed. If it is not claimed, null is returned
     * @param x
     * @param z
     * @return chunk
     */
    public NationsChunk getChunkByCoords(int x, int z, World world) {
        for (NationsChunk chunk : this.chunks) {
            if (chunk.getX() == x && chunk.getZ() == z && chunk.getWorld().equals(world)) return chunk;
        }
        return null;
    }

    //Menus

    /**
     * Adds a menu to the manager
     * @param playerUUID
     * @param gui
     */
    public void addMenu(UUID playerUUID, InventoryGUI gui) {
        this.menus.put(playerUUID, gui);
    }

    /**
     * Removes a menu from the manager, call this when it is closed
     * @param playerUUID
     */
    public void removeMenu(UUID playerUUID) {
        this.menus.remove(playerUUID);
    }

    /**
     * @param playerUUID
     * @return
     */
    public InventoryGUI getMenuByPlayerUUID(UUID playerUUID) {
        return this.menus.get(playerUUID);
    }

    /**
     * @return playersChoosingMerchants
     */
    public HashMap<UUID, Map.Entry<ShopItem, ScheduledThreadPoolExecutor>> getPlayersChoosingMerchants() {
        return this.playersChoosingMerchants;
    }

    /**
     * @param uuid
     * @return isPlayerChoosingMerchant
     */
    public boolean isPlayerChoosingMerchant(UUID uuid) {
        return this.playersChoosingMerchants.get(uuid) == null ? false : true;
    }

    //IDs

    /**
     * @return nextNationID
     */
    public int nextNationID() {
        this.nextNationID++;
        return this.nextNationID;
    }

    /**
     * @return nextNationID
     */
    public int getNextNationID() {
        return this.nextNationID;
    }

    /**
     * @param nextNationID
     */
    public void setNextNationID(int nextNationID) {
        this.nextNationID = nextNationID;
    }
}