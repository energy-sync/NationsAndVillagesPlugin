package com.github.dawsonvilamaa.nationsandvillagesplugin.commands;

import org.bukkit.entity.Player;

public interface Command {
    /**
     * @param sender
     * @param args
     */
    public static boolean run(Player sender, String[] args) {
        return false;
    }
}
