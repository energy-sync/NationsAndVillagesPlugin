package com.github.dawsonvilamaa.nationsandvillagesplugin.classes;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.exceptions.VillageNotFoundException;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

public class Nation {
    private String name;
    private int id;
    private NationsPlayer owner;
    private int population;
    private ArrayList<Village> villages;

    /**
     * @param name
     * @param owner
     */
    public Nation(String name, Player owner) {
        this.name = name;
        this.id = Main.nationsManager.nextNationID++;
        this.owner = Main.nationsManager.getPlayers().get(owner.getUniqueId());
        this.population = 0;
        this.villages = new ArrayList<Village>();
    }

    public Nation(JSONObject jsonNation) {
        this.name = jsonNation.get("name").toString();
        this.id = Integer.parseInt(jsonNation.get("id").toString());
        this.owner = Main.nationsManager.getPlayerByUUID(UUID.fromString(jsonNation.get("owner").toString()));
        this.population = Integer.parseInt(jsonNation.get("population").toString());
        this.villages = new ArrayList<Village>(); //change later
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
     * @return population
     */
    public int getPopulation() {
        return this.population;
    }

    /**
     * Returns searched village by given ID, returns null if it does not exist
     * @param id
     * @return village
     */
    public Village getVillageByID(int id) {
        for (Village village : this.villages) {
            if (village.getID() == id) return village;
        }
        return null;
    }

    /**
     * Returns searched village by given name, returns null if it does not exist
     * @param name
     * @return village
     */
    public Village getVillageByName(String name) {
        for (Village village : this.villages) {
            if (village.getName().equals(name)) return village;
        }
        return null;
    }

    /**
     * @return villages
     */
    public ArrayList<Village> getVillages() {
        return this.villages;
    }

    /**
     * Adds a village to this nation and updates the nation's population
     * @param newVillage
     */
    public void addVillage(Village newVillage) {
        this.population += newVillage.getPopulation();
        newVillage.setNation(this);
        this.villages.add(newVillage);
    }

    public void removeVillage(Village removedVillage) throws VillageNotFoundException {
        for (Village village : Main.nationsManager.getVillages()) {
            if (village.getID() == removedVillage.getID()) {
                this.population -= removedVillage.getPopulation();
                this.villages.remove(id);
                return;
            }
        }
        throw new VillageNotFoundException();
    }

    public JSONObject toJSON() {
        JSONObject jsonNation = new JSONObject();
        jsonNation.put("name", this.name);
        jsonNation.put("id", String.valueOf(this.id));
        jsonNation.put("owner", this.owner.getUUID().toString());
        jsonNation.put("population", String.valueOf(this.population));
        JSONArray jsonVillages = new JSONArray();
        for (Village village : this.villages)
            jsonVillages.add(String.valueOf(village.getID()));
        jsonNation.put("villages", jsonVillages);
        return jsonNation;
    }
}
