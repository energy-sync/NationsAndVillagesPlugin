package com.github.dawsonvilamaa.nationsandvillagesplugin.commands;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.Nation;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsChunk;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class autoclaim implements Command {
    /**
     * @param sender
     * @param args
     */
    public static boolean run(Player sender, String[] args) {
        NationsPlayer nationsPlayer = Main.nationsManager.getPlayerByUUID(sender.getUniqueId());
        if (nationsPlayer.getNationID() == -1) {
            sender.sendMessage(ChatColor.RED + "You must own a nation to claim chunks");
            return true;
        }
        Nation nation = Main.nationsManager.getNationByID(nationsPlayer.getNationID());
        if (!nationsPlayer.isAutoClaiming()) {
            nationsPlayer.setAutoUnclaim(false);
            nationsPlayer.setAutoClaim(true);
            sender.sendMessage(ChatColor.YELLOW + "Auto-claiming for " + nation.getName() + " has been " + ChatColor.GREEN + "ENABLED");
            NationsChunk currentChunk = nationsPlayer.getCurrentChunk();
            if (Main.nationsManager.getChunkByCoords(currentChunk.getX(), currentChunk.getZ()) == null)
                claim.run(sender, new String[0]);
        }
        else {
            nationsPlayer.setAutoClaim(false);
            sender.sendMessage(ChatColor.YELLOW + "Auto-claiming for " + nation.getName() + " has been " + ChatColor.RED + "DISABLED");
        }
        return true;
    }
}
