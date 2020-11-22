package com.github.dawsonvilamaa.nationsandvillagesplugin.classes;

import org.bukkit.entity.Player;

import java.util.UUID;

public class NationsPlayer {
    private String username;
    private UUID uuid;
    private int money;

    /**
     * @param player
     */
    public NationsPlayer(Player player) {
        this.username = player.getName();
        this.uuid = player.getUniqueId();
        this.money = 1000;
    }

    /**
     * @return name
     */
    public String getName() {
        return this.username;
    }

    /**
     * @param newName
     */
    public void setName(String newName) {
        this.username = newName;
    }

    /**
     * @return uuid
     */
    public UUID getUUID() {
        return this.uuid;
    }

    /**
     * @return money
     */
    public int getMoney() {
        return this.money;
    }

    /**
     * Gives a specified amount of money to this player and returns the new value
     * @param amount
     * @return
     */
    public int addMoney(int amount) {
        this.money += amount;
        return this.money;
    }

    /**
     * Takes away a specified amount of money from this player and returns the new value
     * @param amount
     * @return
     */
    public int removeMoney(int amount) {
        this.money += amount;
        return this.money;
    }
}