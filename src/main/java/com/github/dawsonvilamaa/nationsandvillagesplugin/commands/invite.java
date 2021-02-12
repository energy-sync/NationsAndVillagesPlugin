package com.github.dawsonvilamaa.nationsandvillagesplugin.commands;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.NationsManager;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.Nation;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class invite implements Command {
    /**
     * @param sender
     * @param args
     */
    public static boolean run(Player sender, String[] args) throws InterruptedException {
        if (args.length < 1) return false;
        NationsPlayer player = Main.nationsManager.getPlayerByUUID(sender.getUniqueId());
        Nation nation = Main.nationsManager.getNationByID(player.getNationID());
        //check if player is in a nation
        if (nation == null) {
            sender.sendMessage(ChatColor.RED + "You are not in a nation");
            return true;
        }
        //check if player has permission to invite members
        if (nation.getPermissionByRank(player.getRank()).canManageMembers() == false) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to invite members to your nation");
            return true;
        }
        //check if player being invited is online
        Player invitedPlayer = Bukkit.getPlayer(args[0]);
        if (invitedPlayer == null) {
            sender.sendMessage(ChatColor.RED + "That player cannot be found. Make sure the spelling is correct and that the player is online");
            return true;
        }
        //check if player being invited is a member of the same nation
        NationsPlayer nationsInvitedPlayer = Main.nationsManager.getPlayerByUUID(invitedPlayer.getUniqueId());
        if (player.getNationID() == nationsInvitedPlayer.getNationID()) {
            sender.sendMessage(ChatColor.RED + "That player is already a member of your nation");
            return true;
        }
        //check if player being invited is already in a nation
        if (nationsInvitedPlayer.getNationID() != -1) {
            sender.sendMessage(ChatColor.YELLOW + "That player is already in a nation");
            return true;
        }
        //check if player already has a pending invite
        for (UUID uuid : nation.getInvitedPlayers()) {
            if (uuid.equals(nationsInvitedPlayer.getUniqueID())) {
                sender.sendMessage(ChatColor.RED + nationsInvitedPlayer.getName() + " already has a pending invite to " + nation.getName());
                return true;
            }
        }
        //invite player
        nation.addInvitedPlayer(invitedPlayer.getUniqueId());
        sender.sendMessage(ChatColor.GREEN + args[0] + " has been invited to " + nation.getName());
        return true;
    }
}
