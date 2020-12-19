package com.github.dawsonvilamaa.nationsandvillagesplugin.classes;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.exceptions.VillagerNotFoundException;
import org.bukkit.Location;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class Village {
    private String name;
    private int id;
    private Nation nation;
    private int population;
    private ArrayList<NationsVillager> villagers;

    /**
     * @param name
     * @param nation
     * @param location
     */
    public Village(String name, Nation nation, Location location) {
        this.name = name;
        this.id = Main.nationsManager.nextVillageID();
        this.nation = nation;
        this.population = 0;
        this.villagers = new ArrayList<NationsVillager>();
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
     * @return nation
     */
    public Nation getNation() {
        return this.nation;
    }

    /**
     * @param newNation
     */
    public void setNation(Nation newNation) {
        this.nation = newNation;
    }

    /**
     * @return population
     */
    public int getPopulation() {
        return this.population;
    }

    /**
     * Returns villager by searched ID, or returns null if it does not exist
     * @param id
     * @return
     */
    public NationsVillager getVillagerByID(int id) {
        for (NationsVillager villager : this.villagers) {
            if (villager.getID() == id) return villager;
        }
        return null;
    }

    /**
     * @return villagers
     */
    public ArrayList<NationsVillager> getVillagers() {
        return this.villagers;
    }

    /**
     * Adds a villager to this village and updates its population count
     * @param newVillager
     * @return
     */
    public int addVillager(NationsVillager newVillager) {
        this.villagers.add(newVillager);
        this.population++;
        return this.population;
    }

    public int removeVillager(NationsVillager removedVillager) throws VillagerNotFoundException {
        for (NationsVillager villager : this.villagers) {
            if (villager.getID() == removedVillager.getID()) {
                this.villagers.remove(this.villagers.indexOf((Object) removedVillager));
                this.population--;
                return this.population;
            }
        }
        throw new VillagerNotFoundException();
    }

    public JSONObject toJSON() {
        JSONObject jsonVillage = new JSONObject();
        jsonVillage.put("name", this.name);
        jsonVillage.put("id", String.valueOf(this.id));
        jsonVillage.put("nation", String.valueOf(this.nation.getID()));
        jsonVillage.put("population", String.valueOf(this.population));
        JSONArray jsonVillagers = new JSONArray();
        for (NationsVillager villager : this.villagers)
            jsonVillagers.add(villager.toJSON());
        jsonVillage.put("villagers", jsonVillagers);
        return jsonVillage;
    }
}
