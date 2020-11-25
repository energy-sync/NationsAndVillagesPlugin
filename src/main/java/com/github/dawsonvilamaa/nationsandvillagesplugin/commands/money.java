package com.github.dawsonvilamaa.nationsandvillagesplugin.commands;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class money implements Command {
    /**
     * @param sender
     * @param cmd
     * @param args
     * @return
     */
    public static boolean run(Player sender, String cmd, String[] args) {
        Player player = (Player) sender;
        player.sendMessage(ChatColor.GREEN + "$" + Main.nationsManager.getPlayerByUUID(player.getUniqueId()).getMoney());
        return true;
    }
}