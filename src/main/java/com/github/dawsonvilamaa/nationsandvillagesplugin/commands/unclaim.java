package com.github.dawsonvilamaa.nationsandvillagesplugin.commands;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.Nation;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsChunk;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsPlayer;
import net.minecraft.server.v1_16_R3.EntityVillager;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class unclaim {
    /**
     * @param sender
     * @param args
     */
    public static boolean run(Player sender, String[] args) {
        NationsPlayer player = Main.nationsManager.getPlayerByUUID(sender.getUniqueId());
        Nation nation = Main.nationsManager.getNationByID(player.getNationID());

        //check if player owns a nation
        if (player.getNationID() == -1) {
            sender.sendMessage(ChatColor.RED + "You must be in a nation to claim and unclaim chunks");
            return true;
        }

        //check if player has permission to claim land
        if (nation.getPermissionByRank(player.getRank()).canClaimLand() == false) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to unclaim land from your nation");
            return true;
        }

        Chunk currentChunk = sender.getLocation().getChunk();
        //check if chunk is claimed
        for (NationsChunk chunk : Main.nationsManager.getChunks()) {
            if (chunk.getX() == currentChunk.getX() && chunk.getZ() == currentChunk.getZ()) {
                //check if player's nation owns this chunk
                if (chunk.getNationID() == player.getNationID()) {
                    //unclaim chunk
                    Main.nationsManager.getChunks().remove(chunk);
                    Main.nationsManager.getNationByID(player.getNationID()).decrementChunks();
                    int chunkCost = (Main.nationsManager.chunkCost * Main.nationsManager.getNationByID(player.getNationID()).getNumChunks()) + Main.nationsManager.chunkCost;
                    player.addMoney(chunkCost);
                    sender.sendMessage(ChatColor.GREEN + "Unclaimed this chunk from " + Main.nationsManager.getNationByID(player.getNationID()).getName() + ". You received $" + chunkCost);
                    player.setCurrentChunk(new NationsChunk(currentChunk.getX(), currentChunk.getZ(), -1)); //update currentChunk
                    //update all villagers in the chunk
                    for (Entity entity : currentChunk.getEntities()) {
                        if (entity instanceof CraftVillager) {
                            EntityVillager villager = ((CraftVillager) entity).getHandle();
                            if (Main.nationsManager.getVillagerByUUID(villager.getUniqueID()).getNationID() == player.getNationID()) {
                                Main.nationsManager.getVillagerByUUID(villager.getUniqueID()).setNationID(-1);
                                Main.nationsManager.getNationByID(player.getNationID()).decrementPopulation();
                            }
                        }
                    }
                }
                else sender.sendMessage(ChatColor.RED + "Your nation does not own this chunk");
                return true;
            }
        }

        //when this chunk is not claimed
        sender.sendMessage(ChatColor.RED + "This chunk is not claimed");
        return true;
    }
}
