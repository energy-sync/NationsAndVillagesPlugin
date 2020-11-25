package com.github.dawsonvilamaa.nationsandvillagesplugin.classes;

import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EntityVillager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.json.simple.JSONObject;

public class NationsVillager extends EntityVillager {
    private String name;
    private int id;
    private Nation nation;
    private Village village;

    public boolean isNationsVillager;

    /**
     * Constructor with no name, defaults to "Villager"
     * @param world
     */
    public NationsVillager(World world) {
        super(EntityTypes.VILLAGER, ((CraftWorld) world).getHandle());
        this.name = "Villager";
        this.id = -1;
        this.isNationsVillager = true;
    }

    /**
     * Constructor with name
     * @param world
     * @param name
     */
    public NationsVillager(World world, String name) {
        super(EntityTypes.VILLAGER, ((CraftWorld) world).getHandle());
        this.name = name;
        this.id = -1;
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
        jsonVillager.put("nation", String.valueOf(this.nation.getID()));
        jsonVillager.put("village", this.village.getID());
        return jsonVillager;
    }
}
