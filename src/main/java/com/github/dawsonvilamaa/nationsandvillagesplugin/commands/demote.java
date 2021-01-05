package com.github.dawsonvilamaa.nationsandvillagesplugin.commands;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.NationsManager;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.Nation;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsPlayer;
import com.github.dawsonvilamaa.nationsandvillagesplugin.commands.Command;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class demote implements Command {
    /**
     * @param sender
     * @param args
     */
    public static boolean run(Player sender, String[] args) {
        if (args.length < 1) return false;
        NationsPlayer player = Main.nationsManager.getPlayerByUUID(sender.getUniqueId());
        Nation nation = Main.nationsManager.getNationByID(player.getNationID());
        //check if player is in a nation
        if (nation == null) {
            sender.sendMessage(ChatColor.RED + "You are not in a nation");
            return true;
        }
        //check if player has permission to demote members
        if (nation.getPermissionByRank(player.getRank()).canManageMembers() == false) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to manage members of your nation");
            return true;
        }
        //check if player being demoted is online
        Player promotedPlayer = Bukkit.getPlayer(args[0]);
        if (promotedPlayer == null) {
            sender.sendMessage(ChatColor.RED + "That player cannot be found. Make sure the spelling is correct and that the player is online");
            return true;
        }
        //check if player being demoted is a member of the same nation
        NationsPlayer nationsDemotedPlayer = Main.nationsManager.getPlayerByUUID(promotedPlayer.getUniqueId());
        if (player.getNationID() != nationsDemotedPlayer.getNationID()) {
            sender.sendMessage(ChatColor.RED + "That player is not a member of your nation");
            return true;
        }
        //check if player being demoted is a leader
        if (nationsDemotedPlayer.getRank() == NationsManager.Rank.LEADER) {
            sender.sendMessage(ChatColor.RED + "You cannot demote yourself since you are the leader of your nation");
            return true;
        }
        //check if player being demoted is already a member (lowest rank)
        if (nationsDemotedPlayer.getRank() == NationsManager.Rank.MEMBER) {
            sender.sendMessage(ChatColor.RED + "That player is already at the lowest possible rank");
            return true;
        }
        //demote player
        nationsDemotedPlayer.setRank(NationsManager.Rank.MEMBER);
        Bukkit.getPlayer(nationsDemotedPlayer.getUUID()).sendMessage(ChatColor.RED + "You have been demoted from legate to member rank in " + nation.getName());
        //send message to all members
        for (UUID uuid : nation.getMembers()) {
            Player msgPlayer = Bukkit.getPlayer(uuid);
            if (msgPlayer != null && !(msgPlayer.getUniqueId().equals(nationsDemotedPlayer.getUUID())))
                msgPlayer.sendMessage(ChatColor.YELLOW + nationsDemotedPlayer.getName() + " has been demoted to member");
        }
        return true;
    }
}
