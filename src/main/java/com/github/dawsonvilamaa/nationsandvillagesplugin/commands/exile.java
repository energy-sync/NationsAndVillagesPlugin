package com.github.dawsonvilamaa.nationsandvillagesplugin.commands;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.NationsManager;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.Nation;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class exile implements Command {
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
        //check if player has permission to exile members
        if (nation.getPermissionByRank(player.getRank()).canManageMembers() == false) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to manage members of your nation");
            return true;
        }
        //check if player being exiled is online
        Player promotedPlayer = Bukkit.getPlayer(args[0]);
        if (promotedPlayer == null) {
            sender.sendMessage(ChatColor.RED + "That player cannot be found. Make sure the spelling is correct and that the player is online");
            return true;
        }
        //check if player is trying to exile themselves
        NationsPlayer nationsExiledPlayer = Main.nationsManager.getPlayerByUUID(promotedPlayer.getUniqueId());
        if (player.getUniqueID().equals(nationsExiledPlayer.getUniqueID())) {
            sender.sendMessage(ChatColor.RED + "You cannot exile yourself from this nation. Use \"/nation leave\" instead.");
            return true;
        }
        //check if player being exiled is a member of the same nation
        if (player.getNationID() != nationsExiledPlayer.getNationID()) {
            sender.sendMessage(ChatColor.RED + "That player is not a member of your nation");
            return true;
        }
        //check if player being exiled is the leader of the nation
        if (nationsExiledPlayer.getRank() == NationsManager.Rank.LEADER) {
            sender.sendMessage(ChatColor.RED + "You cannot exile the lead of your nation");
            return true;
        }
        //exile player
        nationsExiledPlayer.setNationID(-1);
        nationsExiledPlayer.setRank(NationsManager.Rank.NONMEMBER);
        nation.decrementPopulation();
        Bukkit.getPlayer(nationsExiledPlayer.getUniqueID()).sendMessage(ChatColor.RED + "You have been exiled from " + nation.getName() + "!");
        //send message to all members
        for (UUID uuid : nation.getMembers()) {
            Player msgPlayer = Bukkit.getPlayer(uuid);
            if (msgPlayer != null && !(msgPlayer.getUniqueId().equals(nationsExiledPlayer.getUniqueID())))
                msgPlayer.sendMessage(ChatColor.RED + nationsExiledPlayer.getName() + " has been exiled from " + nation.getName());
        }
        return true;
    }
}
