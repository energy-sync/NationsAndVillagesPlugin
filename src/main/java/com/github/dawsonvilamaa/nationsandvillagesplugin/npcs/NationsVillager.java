package com.github.dawsonvilamaa.nationsandvillagesplugin.npcs;

import net.minecraft.server.v1_16_R3.ChatComponentText;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.UUID;

public class NationsVillager {
    //combat constants
    public static final double ATTACK_RANGE = 2.0;
    public static final double BASE_DAMAGE = 1.0;
    public static final double KNOCKBACK = 0.4;
    public static final double ENEMY_DETECTION_RANGE = 20.0;

    private String name;
    private UUID uuid;
    private int nationID;
    private Job job;
    private Consumer<PlayerInteractEntityEvent> onClick;
    private BukkitTask runnable;

    public enum Job {
      NONE,
        MERCHANT,
        MINER,
        FARMER,
        LUMBERJACK,
        GUARD
    };

    /**
     * Creates a NationsVillager data class and attaches it to an entity in the world
     * @param uuid
     */
    public NationsVillager(UUID uuid) {
        this.name = "Villager";
        this.uuid = uuid;
        this.nationID = -1;
        this.job = Job.NONE;
        this.onClick = null;
        this.runnable = null;
    }

    /**
     * @param jsonVillager
     */
    public NationsVillager(JSONObject jsonVillager) {
        this.uuid = UUID.fromString(jsonVillager.get("uuid").toString());
        this.name = jsonVillager.get("name").toString();
        this.nationID = Integer.parseInt(jsonVillager.get("nationID").toString());
        this.job = Job.valueOf(jsonVillager.get("job").toString());
        this.onClick = null;
        this.runnable = null;
    }

    /**
     * @return uuid
     */
    public UUID getUniqueID() {
        return this.uuid;
    }

    /**
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
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
     * @return job
     */
    public Job getJob() {
        return this.job;
    }

    /**
     * @param job
     */
    public void setJob(Job job) {
        this.job = job;
    }

    /**
     * Stops the villager's bukkit runnable and sets it to null
     */
    public void stopRunnable() {
        if (this.runnable != null) {
            this.runnable.cancel();
            this.runnable = null;
        }
    }

    /**
     * @param e
     */
    public void onClick(PlayerInteractEntityEvent e) {
        if (this.onClick != null)
            this.onClick.accept(e);
    }

    /**
     * @param consumer
     */
    public void setOnClick(Consumer<PlayerInteractEntityEvent> consumer) {
        this.onClick = consumer;
    }

    /**
     * @return onClick
     */
    public Consumer<PlayerInteractEntityEvent> getOnClick() {
        return this.onClick;
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
            if (Bukkit.getEntity(this.uuid).getLocation().distance(player.getLocation()) <= 30) speakToPlayer(player, message);
        }
    }

    /**
     * Makes the villager walk to a location
     * @param location
     */
    public boolean walkToLocation(Location location) {
        return ((CraftVillager) Bukkit.getEntity(this.uuid)).getHandle().getNavigation().a(location.getX(), location.getY(), location.getZ(), 0.6);
    }

    /**
     * Makes the villager run to a location
     * @param location
     * @return
     */
    public boolean runToLocation(Location location) {
        return ((CraftVillager) Bukkit.getEntity(this.uuid)).getHandle().getNavigation().a(location.getX(), location.getY(), location.getZ(), 0.8);
    }

    /**
     * Makes the villager look at a location
     * @param location
     * @param villager
     */
    void lookAtLocation(Location location, Entity villager) {
        Vector lookAt = location.toVector().subtract(villager.getLocation().toVector());
        Location loc = villager.getLocation();
        loc.setDirection(lookAt);
        villager.teleport(loc);
    }

    /**
     * Displays the villager's health as a custom name tag if the health is below its max health. Displays name if healed
     */
    public void updateNameTag() {
        StringBuilder nameTag = new StringBuilder();
        CraftVillager villager = (CraftVillager) Bukkit.getEntity(this.uuid);

        //if the villager's name is not the default ("Villager"), then add it to the name tag
        if (!this.name.equals("Villager")) nameTag.append(this.name);

        //if the villager's health is below its max health, add the health to the name tag
        if (villager.getHealth() < villager.getMaxHealth() - 0.5)
            nameTag.append(ChatColor.RED).append(" ❤").append(ChatColor.RESET).append((int) villager.getHealth() > 0 ? Math.round(villager.getHealth()) : 0).append("/").append((int) villager.getMaxHealth());

        if (this.job == Job.GUARD) {
            Guard guard = (Guard) this;

            //if the guard has a sword, add a sword icon to the name tag
            if (guard.getWeapon() != null) {
                switch (guard.getWeapon().getType()) {
                    case STONE_SWORD:
                        nameTag.append(ChatColor.DARK_GRAY);
                        break;

                    case IRON_SWORD:
                        nameTag.append(ChatColor.GRAY);
                        break;

                    case GOLDEN_SWORD:
                        nameTag.append(ChatColor.YELLOW);
                        break;

                    case DIAMOND_SWORD:
                        nameTag.append(ChatColor.AQUA);
                        break;

                    case NETHERITE_SWORD:
                        nameTag.append(ChatColor.BLACK);
                        break;
                }
                nameTag.append(" \uD83D\uDDE1");
            }

            //if the guard has armor, add an armor icon to the name tag
            ItemStack[] armor = guard.getArmor();
            Material armorType = null;

            //chestplate
            if (armor[1] != null)
                armorType = armor[1].getType();
            //leggings
            else if (armor[2] != null)
                armorType = armor[2].getType();
            //helmet
            else if (armor[0] != null)
                armorType = armor[0].getType();
            //boots
            else if (armor[3] != null)
                armorType = armor[3].getType();

            if (armorType != null) {
                String armorTypeStr = armorType.toString();
                //chainmail
                if (armorTypeStr.contains("CHAINMAIL"))
                    nameTag.append(ChatColor.DARK_GRAY);
                //iron
                else if (armorTypeStr.contains("IRON"))
                    nameTag.append(ChatColor.GRAY);
                //gold
                else if (armorTypeStr.contains("GOLDEN"))
                    nameTag.append(ChatColor.YELLOW);
                //diamond
                else if (armorTypeStr.contains("DIAMOND"))
                    nameTag.append(ChatColor.AQUA);
                //netherite
                else if (armorTypeStr.contains("NETHERITE"))
                    nameTag.append(ChatColor.BLACK);

                nameTag.append(" \uD83D\uDEE1");
            }
        }

        //if the villager has a default name with no extra tags, remove the custom name tag
        if (nameTag.equals("") && villager.getCustomName() != null)
                villager.getHandle().setCustomName(null);

        //if the villager has a custom name tag, update it
        else villager.getHandle().setCustomName(new ChatComponentText(nameTag.toString()));
    }

    /**
     * @param loc1
     * @param loc2
     * @return distance
     */
    double distance(Location loc1, Location loc2) {
        return Math.sqrt(Math.pow(loc2.getX() - loc1.getX(), 2) + Math.pow(loc2.getY() - loc1.getY(), 2) + Math.pow(loc2.getZ() - loc1.getZ(), 2));
    }

    /**
     * Returns this NationsVillager in JSON format
     * @return jsonVillager
     */
    public JSONObject toJSON() {
        JSONObject jsonVillager = new JSONObject();
        jsonVillager.put("uuid", this.uuid.toString());
        jsonVillager.put("name", this.name);
        jsonVillager.put("nationID", String.valueOf(this.nationID));
        jsonVillager.put("job", this.job.toString());
        if (this.job == Job.MERCHANT)
            jsonVillager.put("shop", ((Merchant) this).getShop().toJSON());
        else if (this.job == Job.LUMBERJACK)
            jsonVillager.put("inventory", ((Lumberjack) this).inventoryToJSON());
        else if (this.job == Job.GUARD) {
            Guard guard = (Guard) this;

            JSONObject jsonGuardLocation = guard.guardLocationToJSON();
            if (jsonGuardLocation == null)
                jsonVillager.put("guardLocation", null);
            else jsonVillager.put("guardLocation", jsonGuardLocation);

            JSONObject jsonWeapon = guard.weaponToJSON();
            if (jsonWeapon == null)
                jsonVillager.put("weapon", null);
            else jsonVillager.put("weapon", jsonWeapon);

            JSONArray jsonArmor = guard.armorToJSON();
            jsonVillager.put("armor", jsonArmor);
        }
        return jsonVillager;
    }
}
