package com.github.dawsonvilamaa.nationsandvillagesplugin.commands;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.Nation;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsChunk;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsPlayer;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsVillager;
import net.minecraft.server.v1_16_R3.EntityVillager;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class claim implements Command {
    /**
     * @param sender
     * @param args
     */
    public static boolean run(Player sender, String[] args) {
        NationsPlayer player = Main.nationsManager.getPlayerByUUID(sender.getUniqueId());

        //check if player owns a nation
        if (player.getNationID() == -1) {
            sender.sendMessage(ChatColor.RED + "You must be in a nation to claim land");
            return true;
        }
        else {
            Nation nation = Main.nationsManager.getNationByID(player.getNationID());
            //check if player has permission to claim land
            if (nation.getPermissionByRank(player.getRank()).canClaimLand() == false) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to claim land for your nation");
                return true;
            }

            Chunk currentChunk = sender.getLocation().getChunk();
            //check if chunk is already claimed
            for (NationsChunk chunk : Main.nationsManager.getChunks()) {
                if (chunk.getX() == currentChunk.getX() && chunk.getZ() == currentChunk.getZ()) {
                    sender.sendMessage(ChatColor.RED + "This chunk is already claimed");
                    return true;
                }
            }

            //check if player has enough money to buy a new chunk
            int chunkCost = (nation.getNumChunks() * Main.nationsManager.chunkCost) + Main.nationsManager.chunkCost;
            if (player.getMoney() - chunkCost < 0) {
                sender.sendMessage(ChatColor.RED + "You do not have enough money to purchase a new chunk. It costs $" + chunkCost);
                return true;
            }
            else {
                //claim chunk
                Main.nationsManager.addChunk(currentChunk.getX(), currentChunk.getZ(), player.getNationID());
                player.removeMoney(chunkCost);
                nation.incrementChunks();
                sender.sendMessage(ChatColor.GREEN + "Claimed this chunk for " + nation.getName() + " for $" + chunkCost);
                player.setCurrentChunk(new NationsChunk(currentChunk.getX(), currentChunk.getZ(), player.getNationID())); //update currentChunk

                //update all villagers in the chunk
                for (Entity entity : currentChunk.getEntities()) {
                    if (entity instanceof CraftVillager) {
                        EntityVillager villager = ((CraftVillager) entity).getHandle();
                        if (Main.nationsManager.getVillagerByUUID(villager.getUniqueID()).getNationID() != player.getNationID()) {
                            Main.nationsManager.getVillagerByUUID(villager.getUniqueID()).setNationID(player.getNationID());
                            nation.incrementPopulation();
                        }
                    }
                }
            }

            return true;
        }
    }
}
