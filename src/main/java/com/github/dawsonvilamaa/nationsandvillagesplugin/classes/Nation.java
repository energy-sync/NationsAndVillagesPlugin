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
    private int chunks;

    /**
     * @param name
     * @param owner
     */
    public Nation(String name, Player owner, int id) {
        this.name = name;
        this.id = id;
        this.owner = Main.nationsManager.getPlayers().get(owner.getUniqueId());
        this.population = 0;
        this.chunks = 0;
    }

    public Nation(JSONObject jsonNation) {
        this.name = jsonNation.get("name").toString();
        this.id = Integer.parseInt(jsonNation.get("id").toString());
        this.owner = Main.nationsManager.getPlayerByUUID(UUID.fromString(jsonNation.get("owner").toString()));
        this.population = Integer.parseInt(jsonNation.get("population").toString());
        this.chunks = Integer.parseInt(jsonNation.get("chunks").toString());
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
     * Returns this Nation as a JSONObject
     * @return jsonNation
     */
    public JSONObject toJSON() {
        JSONObject jsonNation = new JSONObject();
        jsonNation.put("name", this.name);
        jsonNation.put("id", String.valueOf(this.id));
        jsonNation.put("owner", this.owner.getUUID().toString());
        jsonNation.put("population", String.valueOf(this.population));
        jsonNation.put("chunks", String.valueOf(this.chunks));
        return jsonNation;
    }
}
