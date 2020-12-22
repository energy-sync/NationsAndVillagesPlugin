package com.github.dawsonvilamaa.nationsandvillagesplugin.classes;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.json.simple.JSONObject;

public class NationsVillager extends EntityVillager {
    private String name;
    private int id;
    private int nationID;
    private Village village;

    /**
     * Constructor with no name, defaults to "Villager"
     * @param world
     */
    public NationsVillager(World world) {
        super(EntityTypes.VILLAGER, ((CraftWorld) world).getHandle());
        this.name = "Villager";
        this.id = -1; //change later
        this.nationID = -1;
    }

    /**
     * Constructor with name
     * @param world
     * @param name
     */
    public NationsVillager(World world, String name) {
        super(EntityTypes.VILLAGER, ((CraftWorld) world).getHandle());
        this.name = name;
        this.id = -1; //change later
        this.nationID = -1;
    }

    /**
     * Constructor with no name but with transferred VillagerData
     * @param world
     * @param villagerData
     */
    public NationsVillager(World world, VillagerData villagerData) {
        super(EntityTypes.VILLAGER, ((CraftWorld) world).getHandle());
        this.name = "Villager";
        this.id = -1; //change later
        this.nationID = -1;
        setVillagerData(villagerData);
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
        setCustomName(new ChatComponentText(newName));
        this.name = newName;
    }

    /**
     * @return id
     */
    public int getID() {
        return this.id;
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
     * @return village
     */
    public Village getVillage() {
        return this.village;
    }

    /**
     * @param newVillage
     */
    public void setVillage(Village newVillage) {
        this.village = newVillage;
    }

    /**
     * Spawns the village at a given location
     * @param loc
     */
    public void spawn(Location loc) {
        setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        world.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    /**
     * Send a message from this villager to a specified player
     * @param player
     * @param message
     */
    public void speakToPlayer(Player player, String message) {
        player.sendMessage("<" + name + "> " + message);
    }

    /**
     * Sends a message to all players within 30 blocks
     * @param message
     */
    public void shout(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (Bukkit.getEntity(getUniqueID()).getLocation().distance(player.getLocation()) <= 30) speakToPlayer(player, message);
        }
    }

    public JSONObject toJSON() {
        JSONObject jsonVillager = new JSONObject();
        jsonVillager.put("name", this.name);
        jsonVillager.put("id", String.valueOf(this.id));
        jsonVillager.put("nation", String.valueOf(this.nationID));
        jsonVillager.put("village", this.village.getID());
        return jsonVillager;
    }
}
