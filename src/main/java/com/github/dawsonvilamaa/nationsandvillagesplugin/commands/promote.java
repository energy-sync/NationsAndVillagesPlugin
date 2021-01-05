package com.github.dawsonvilamaa.nationsandvillagesplugin.commands;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.NationsManager;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.Nation;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class promote implements Command {
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
        //check if player has permission to promote members
        if (nation.getPermissionByRank(player.getRank()).canManageMembers() == false) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to manage members of your nation");
            return true;
        }
        //check if player being promoted is online
        Player promotedPlayer = Bukkit.getPlayer(args[0]);
        if (promotedPlayer == null) {
            sender.sendMessage(ChatColor.RED + "That player cannot be found. Make sure the spelling is correct and that the player is online");
            return true;
        }
        //check if player being promoted is a member of the same nation
        NationsPlayer nationsPromotedPlayer = Main.nationsManager.getPlayerByUUID(promotedPlayer.getUniqueId());
        if (player.getNationID() != nationsPromotedPlayer.getNationID()) {
            sender.sendMessage(ChatColor.RED + "That player is not a member of your nation");
            return true;
        }
        //check if player being promoted is already a leader or legate
        if (nationsPromotedPlayer.getRank() == NationsManager.Rank.LEADER || nationsPromotedPlayer.getRank() == NationsManager.Rank.LEGATE) {
            sender.sendMessage(ChatColor.RED + "That player is already at the highest possible rank");
            return true;
        }
        //promote player
        nationsPromotedPlayer.setRank(NationsManager.Rank.LEGATE);
        Bukkit.getPlayer(nationsPromotedPlayer.getUUID()).sendMessage(ChatColor.GREEN + "You have been promoted from member to legate in " + nation.getName());
        //send message to all members
        for (UUID uuid : nation.getMembers()) {
            Player msgPlayer = Bukkit.getPlayer(uuid);
            if (msgPlayer != null && !(msgPlayer.getUniqueId().equals(nationsPromotedPlayer.getUUID())))
                msgPlayer.sendMessage(ChatColor.YELLOW + nationsPromotedPlayer.getName() + " has been promoted to legate");
        }
        return true;
    }
}
