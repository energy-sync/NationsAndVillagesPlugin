package com.github.dawsonvilamaa.nationsandvillagesplugin.classes;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.json.simple.JSONObject;

public class NationsChunk {
    private int x;
    private int z;
    private String worldName;
    private int nationID;

    /**
     * @param x
     * @param z
     * @param world
     * @param nationID
     */
    public NationsChunk(int x, int z, World world, int nationID) {
        this.x = x;
        this.z = z;
        this.worldName = world.getName();
        this.nationID = nationID;
    }

    /**
     * @param jsonNationsChunk
     */
    public NationsChunk(JSONObject jsonNationsChunk) {
        this.x = Integer.parseInt(jsonNationsChunk.get("x").toString());
        this.z = Integer.parseInt(jsonNationsChunk.get("z").toString());
        this.worldName = jsonNationsChunk.get("world").toString();
        this.nationID = Integer.parseInt(jsonNationsChunk.get("nationID").toString());
    }

    /**
     * @return x
     */
    public int getX() {
        return this.x;
    }

    /**
     * @param x
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return z
     */
    public int getZ() {
        return this.z;
    }

    /**
     * @param z
     */
    public void setZ(int z) {
        this.z = z;
    }

    /**
     * @return worldName
     */
    public String getWorldName() {
        return this.worldName;
    }

    /**
     * @return world
     */
    public World getWorld() {
        return Bukkit.getWorld(this.worldName);
    }

    /**
     * @param worldName
     */
    public void setWorldName(String worldName) {
        this.worldName = worldName;
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
     * Returns a "coordinate" which includes the chunk's x and z coordinates as well as the world name in one object
     * @return ChunkCoord
     */
    public ChunkCoord getChunkCoord() {
        return new ChunkCoord(this.x, this.z, this.worldName);
    }

    /**
     * Returns this NationsChunk as a JSONObject
     * @return jsonChunk
     */
    public JSONObject toJSON() {
        JSONObject jsonChunk = new JSONObject();
        jsonChunk.put("x", String.valueOf(this.x));
        jsonChunk.put("z", String.valueOf(this.z));
        jsonChunk.put("world", this.worldName);
        jsonChunk.put("nationID", String.valueOf(this.nationID));
        return jsonChunk;
    }
}
