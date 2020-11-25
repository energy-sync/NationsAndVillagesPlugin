package com.github.dawsonvilamaa.nationsandvillagesplugin;

import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.Nation;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsPlayer;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.Village;
import com.github.dawsonvilamaa.nationsandvillagesplugin.exceptions.NationNotFoundException;
import com.github.dawsonvilamaa.nationsandvillagesplugin.exceptions.VillageNotFoundException;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class NationsManager {
    private ArrayList<Nation> nations;
    private ArrayList<Village> villages;
    private HashMap<UUID, NationsPlayer> players;

    public static String navDir = "plugins\\NationsAndVillages";
    public int nextNationID;
    public int nextVillageID;
    public int nextVillagerID;

    public NationsManager() {
        this.nations = new ArrayList<Nation>();
        this.villages = new ArrayList<Village>();
        this.players = new HashMap<UUID, NationsPlayer>();
        this.nextNationID = 0;
        this.nextVillageID = 0;
        this.nextVillagerID = 0;
    }

    /**
     * @return nations
     */
    public ArrayList<Nation> getNations() {
        return this.nations;
    }

    /**
     * Use this when loading data
     * @param nations
     */
    public void setNations(ArrayList<Nation> nations) {
        this.nations = nations;
    }

    /**
     * @return villages
     */
    public ArrayList<Village> getVillages() {
        return this.villages;
    }

    /**
     * Use this when loading data
     * @param villages
     */
    public void setVillages(ArrayList<Village> villages) {
        this.villages = villages;
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
     * Adds a nation to the worldwide list
     * @param newNation
     */
    public void addNation(Nation newNation) {
        this.nations.add(newNation);
    }

    /**
     * Removes a nation from the worldwide list
     * @param removedNation
     * @throws NationNotFoundException
     */
    public void removeNation(Nation removedNation) throws NationNotFoundException {
        for (Nation nation : this.nations) {
            if (nation.equals(removedNation)) {
                this.nations.remove(nation);
                return;
            }
        }
        throw new NationNotFoundException();
    }

    /**
     * Returns a nation by a searched name, or returns null if it does not exist
     * @param name
     * @return nation
     */
    public Nation getNationByName(String name) {
        for (Nation nation : this.nations) {
            if (nation.getName().toLowerCase().equals(name.toLowerCase())) return nation;
        }
        return null;
    }

    /**
     * Adds a village to the worldwide list
     * @param newVillage
     */
    public void addVillage(Village newVillage) {
        this.villages.add(newVillage);
    }

    /**
     * Removes a village from the worldwide list
     * @param removedVillage
     * @throws VillageNotFoundException
     */
    public void removeVillage(Village removedVillage) throws VillageNotFoundException {
        for (Village village : this.villages) {
            if (village.equals(removedVillage)) {
                this.villages.remove(village);
                return;
            }
        }
        throw new VillageNotFoundException();
    }

    /**
     * Adds a player to the worldwide list
     * @param newPlayer
     */
    public void addPlayer(Player newPlayer) {
        this.players.put(newPlayer.getUniqueId(), new NationsPlayer(newPlayer));
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
}