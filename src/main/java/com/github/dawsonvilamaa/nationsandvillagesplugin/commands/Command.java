package com.github.dawsonvilamaa.nationsandvillagesplugin.commands;

import org.bukkit.entity.Player;

public interface Command {
    /**
     * @param sender
     * @param cmd
     * @param args
     */
    public static boolean run(Player sender, String cmd, String[] args) {
        return false;
    }
}
