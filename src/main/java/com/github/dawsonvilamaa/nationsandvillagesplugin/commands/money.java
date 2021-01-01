package com.github.dawsonvilamaa.nationsandvillagesplugin.commands;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class money implements Command {
    /**
     * @param sender
     * @param args
     */
    public static boolean run(Player sender, String[] args) {
        sender.sendMessage(ChatColor.GREEN + "$" + Main.nationsManager.getPlayerByUUID(sender.getUniqueId()).getMoney());
        return true;
    }
}