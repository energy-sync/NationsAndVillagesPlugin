package com.github.dawsonvilamaa.nationsandvillagesplugin.classes;

import org.json.simple.JSONObject;

public class NationsChunk {
    private int x;
    private int z;
    private int nationID;

    /**
     * @param x
     * @param z
     * @param nationID
     */
    public NationsChunk(int x, int z, int nationID) {
        this.x = x;
        this.z = z;
        this.nationID = nationID;
    }

    /**
     * @param jsonNationsChunk
     */
    public NationsChunk(JSONObject jsonNationsChunk) {
        this.x = Integer.parseInt(jsonNationsChunk.get("x").toString());
        this.z = Integer.parseInt(jsonNationsChunk.get("z").toString());
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
     * Returns this NationsChunk as a JSONObject
     * @return jsonChunk
     */
    public JSONObject toJSON() {
        JSONObject jsonChunk = new JSONObject();
        jsonChunk.put("x", String.valueOf(this.x));
        jsonChunk.put("z", String.valueOf(this.z));
        jsonChunk.put("nationID", String.valueOf(this.nationID));
        return jsonChunk;
    }
}
