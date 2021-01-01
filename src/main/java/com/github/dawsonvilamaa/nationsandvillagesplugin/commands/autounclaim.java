package com.github.dawsonvilamaa.nationsandvillagesplugin.commands;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.Nation;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsChunk;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class autounclaim implements Command {
    /**
     * @param sender
     * @param args
     */
    public static boolean run(Player sender, String[] args) {
        NationsPlayer nationsPlayer = Main.nationsManager.getPlayerByUUID(sender.getUniqueId());
        if (nationsPlayer.getNationID() == -1) {
            sender.sendMessage(ChatColor.RED + "You must own a nation to claim and unclaim chunks");
            return true;
        }
        Nation nation = Main.nationsManager.getNationByID(nationsPlayer.getNationID());
        if (!nationsPlayer.isAutoUnclaiming()) {
            nationsPlayer.setAutoClaim(false);
            nationsPlayer.setAutoUnclaim(true);
            sender.sendMessage(ChatColor.YELLOW + "Auto-unclaiming for " + nation.getName() + " has been " + ChatColor.GREEN + "ENABLED");
            NationsChunk currentChunk = nationsPlayer.getCurrentChunk();
            if (Main.nationsManager.getChunkByCoords(currentChunk.getX(), currentChunk.getZ()) != null) {
                if (Main.nationsManager.getChunkByCoords(currentChunk.getX(), currentChunk.getZ()).getNationID() == nationsPlayer.getNationID())
                    unclaim.run(sender, new String[0]);
            }
        }
        else {
            nationsPlayer.setAutoUnclaim(false);
            sender.sendMessage(ChatColor.YELLOW + "Auto-unclaiming for " + nation.getName() + " has been " + ChatColor.RED + "DISABLED");
        }
        return true;
    }
}
