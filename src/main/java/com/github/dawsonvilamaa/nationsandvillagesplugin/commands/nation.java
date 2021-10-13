package com.github.dawsonvilamaa.nationsandvillagesplugin.commands;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.NationsManager;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.*;
import com.github.dawsonvilamaa.nationsandvillagesplugin.gui.InventoryGUI;
import com.github.dawsonvilamaa.nationsandvillagesplugin.gui.InventoryGUIButton;
import net.minecraft.server.v1_16_R3.EntityVillager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

import static com.github.dawsonvilamaa.nationsandvillagesplugin.Main.nationsManager;

public class nation implements Command {

    /**
     * @param player Player invoking command
     * @param args Arguments player gives for command
     */
    public static boolean run(Player player, String[] args) throws InterruptedException {
        if (args.length == 0) return false;

        NationsPlayer nationsPlayer = nationsManager.getPlayerByUUID(player.getUniqueId());
        StringBuilder fullName = new StringBuilder();
        for (int i = 1; i < args.length; i++)
            fullName.append(args[i]).append(" ");
        if (fullName.length() > 0)
            fullName = new StringBuilder(fullName.substring(0, fullName.length() - 1));
        Nation playerNation = nationsManager.getNationByID(nationsPlayer.getNationID());
        Nation argNation = nationsManager.getNationByName(args.length >= 2 ? args[1] : args[0]);

        switch (args[0]) {
            case "autoclaim":
            case "ac":
                if (nationsPlayer.getNationID() == -1) {
                    player.sendMessage(ChatColor.RED + "You must own a nation to claim chunks");
                    return true;
                }
                if (!nationsPlayer.isAutoClaiming()) {
                    nationsPlayer.setAutoUnclaim(false);
                    nationsPlayer.setAutoClaim(true);
                    player.sendMessage(ChatColor.YELLOW + "Auto-claiming for " + playerNation.getName() + " has been " + ChatColor.GREEN + "ENABLED");
                    NationsChunk currentChunk = nationsPlayer.getCurrentChunk();
                    if (Main.nationsManager.getChunkByCoords(currentChunk.getX(), currentChunk.getZ()) == null)
                        claim.run(player, new String[0]);
                }
                else {
                    nationsPlayer.setAutoClaim(false);
                    player.sendMessage(ChatColor.YELLOW + "Auto-claiming for " + playerNation.getName() + " has been " + ChatColor.RED + "DISABLED");
                }
                return true;
                
            case "autounclaim":
            case "auc":
                if (nationsPlayer.getNationID() == -1) {
                    player.sendMessage(ChatColor.RED + "You must own a nation to claim and unclaim chunks");
                    return true;
                }
                if (!nationsPlayer.isAutoUnclaiming()) {
                    nationsPlayer.setAutoClaim(false);
                    nationsPlayer.setAutoUnclaim(true);
                    player.sendMessage(ChatColor.YELLOW + "Auto-unclaiming for " + playerNation.getName() + " has been " + ChatColor.GREEN + "ENABLED");
                    NationsChunk currentChunk = nationsPlayer.getCurrentChunk();
                    if (Main.nationsManager.getChunkByCoords(currentChunk.getX(), currentChunk.getZ()) != null) {
                        if (Main.nationsManager.getChunkByCoords(currentChunk.getX(), currentChunk.getZ()).getNationID() == nationsPlayer.getNationID())
                            unclaim.run(player, new String[0]);
                    }
                }
                else {
                    nationsPlayer.setAutoUnclaim(false);
                    player.sendMessage(ChatColor.YELLOW + "Auto-unclaiming for " + playerNation.getName() + " has been " + ChatColor.RED + "DISABLED");
                }
                return true;
                
            case "claim":
            case "c":
                //check if nationsPlayer owns a nation
                if (nationsPlayer.getNationID() == -1)
                    player.sendMessage(ChatColor.RED + "You must be in a nation to claim land");
                else {
                    //check if nationsPlayer has permission to claim land
                    if (!playerNation.getPermissionByRank(nationsPlayer.getRank()).canClaimLand()) {
                        player.sendMessage(ChatColor.RED + "You do not have permission to claim land for your nation");
                        return true;
                    }

                    Chunk currentChunk = player.getLocation().getChunk();
                    //check if chunk is already claimed
                    for (NationsChunk chunk : Main.nationsManager.getChunks()) {
                        if (chunk.getX() == currentChunk.getX() && chunk.getZ() == currentChunk.getZ()) {
                            player.sendMessage(ChatColor.RED + "This chunk is already claimed");
                            return true;
                        }
                    }

                    //check if nationsPlayer has enough money to buy a new chunk
                    int chunkCost = (playerNation.getNumChunks() * NationsManager.chunkCost) + NationsManager.chunkCost;
                    if (nationsPlayer.getMoney() - chunkCost < 0) {
                        player.sendMessage(ChatColor.RED + "You do not have enough money to purchase a new chunk. It costs $" + chunkCost);
                        return true;
                    }
                    else {
                        //claim chunk
                        Main.nationsManager.addChunk(currentChunk.getX(), currentChunk.getZ(), nationsPlayer.getNationID());
                        nationsPlayer.removeMoney(chunkCost);
                        playerNation.incrementChunks();
                        player.sendMessage(ChatColor.GREEN + "Claimed this chunk for " + playerNation.getName() + " for $" + chunkCost);
                        nationsPlayer.setCurrentChunk(new NationsChunk(currentChunk.getX(), currentChunk.getZ(), nationsPlayer.getNationID())); //update currentChunk

                        //update all villagers in the chunk
                        for (Entity entity : currentChunk.getEntities()) {
                            if (entity instanceof CraftVillager) {
                                EntityVillager villager = ((CraftVillager) entity).getHandle();
                                if (Main.nationsManager.getVillagerByUUID(villager.getUniqueID()).getNationID() != nationsPlayer.getNationID()) {
                                    Main.nationsManager.getVillagerByUUID(villager.getUniqueID()).setNationID(nationsPlayer.getNationID());
                                    playerNation.incrementPopulation();
                                }
                            }
                        }
                    }

                }
                return true;

            case "create":
                if (args.length < 2) return false;
                //check if player is in a nation
                if (nationsPlayer.getNationID() != -1) {
                    player.sendMessage(ChatColor.RED + "You are already in a nation");
                    return true;
                }
                //check if name is 2-30 characters long
                if (fullName.length() < 2 || fullName.length() > 30)
                    player.sendMessage(ChatColor.RED + "Nation name must be between 2 and 30 characters");
                //check if name is already taken
                else if (nationsManager.getNationByName(fullName.toString()) != null)
                    player.sendMessage(ChatColor.RED + "A nation with that name already exists");
                else {
                    Nation newNation = new Nation(fullName.toString(), player, nationsManager.nextNationID());
                    nationsManager.addNation(newNation);
                    nationsPlayer.setNationID(newNation.getID());
                    newNation.incrementPopulation();
                    nationsPlayer.setRank(NationsManager.Rank.LEADER);
                    newNation.addMember(player.getUniqueId());
                    player.sendMessage(ChatColor.GREEN + "You have created the nation \"" + newNation.getName() + "\"");
                }
                return true;
                
            case "demote":
                if (args.length < 2)
                    return false;

                //check if NationsPlayer is in a nation
                if (playerNation == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a nation");
                    return true;
                }
                //check if NationsPlayer has permission to demote members
                if (!playerNation.getPermissionByRank(nationsPlayer.getRank()).canManageMembers()) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to manage members of your nation");
                    return true;
                }
                //check if NationsPlayer being demoted is online
                Player promotedNationsPlayer = Bukkit.getPlayer(args[1]);
                if (promotedNationsPlayer == null) {
                    player.sendMessage(ChatColor.RED + "That player cannot be found. Make sure the spelling is correct and that the player is online");
                    return true;
                }
                //check if NationsPlayer being demoted is a member of the same nation
                NationsPlayer nationsDemotedNationsPlayer = Main.nationsManager.getPlayerByUUID(promotedNationsPlayer.getUniqueId());
                if (nationsPlayer.getNationID() != nationsDemotedNationsPlayer.getNationID()) {
                    player.sendMessage(ChatColor.RED + "That player is not a member of your nation");
                    return true;
                }
                //check if NationsPlayer being demoted is a leader
                if (nationsDemotedNationsPlayer.getRank() == NationsManager.Rank.LEADER) {
                    player.sendMessage(ChatColor.RED + "You cannot demote yourself since you are the leader of your nation");
                    return true;
                }
                //check if NationsPlayer being demoted is already a member (lowest rank)
                if (nationsDemotedNationsPlayer.getRank() == NationsManager.Rank.MEMBER) {
                    player.sendMessage(ChatColor.RED + "That player is already at the lowest possible rank");
                    return true;
                }
                //demote NationsPlayer
                nationsDemotedNationsPlayer.setRank(NationsManager.Rank.MEMBER);
                Bukkit.getPlayer(nationsDemotedNationsPlayer.getUniqueID()).sendMessage(ChatColor.RED + "You have been demoted from legate to member rank in " + playerNation.getName());
                //send message to all members
                for (UUID uuid : playerNation.getMembers()) {
                    Player msgNationsPlayer = Bukkit.getPlayer(uuid);
                    if (msgNationsPlayer != null && !(msgNationsPlayer.getUniqueId().equals(nationsDemotedNationsPlayer.getUniqueID())))
                        msgNationsPlayer.sendMessage(ChatColor.YELLOW + nationsDemotedNationsPlayer.getName() + " has been demoted to member");
                }
                return true;

            case "enemy":
                if (args.length < 2)
                    return false;

                //check if player is in a nation
                if (nationsPlayer.getNationID() == -1) {
                    player.sendMessage(ChatColor.RED + "You are not in a nation");
                    return true;
                }
                //check if player has permission to manage nation
                if (!playerNation.getPermissionByRank(nationsPlayer.getRank()).canDeclareEnemies()) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to declare enemies");
                    return true;
                }
                //check if enemy nation exists
                if (argNation == null) {
                    player.sendMessage(ChatColor.RED + "No nation of that name exists");
                    return true;
                }
                //declare or undeclare enemy
                fullName = new StringBuilder(fullName.substring(0, 1).toUpperCase() + fullName.substring(1));
                if (!playerNation.isEnemy(argNation.getID())) {
                    playerNation.addEnemy(argNation.getID());

                    //send message to all members of both nations
                    for (UUID enemyUUID : argNation.getMembers()) {
                        Player enemyPlayer = Bukkit.getPlayer(enemyUUID);
                        if (enemyPlayer != null)
                            enemyPlayer.sendMessage(ChatColor.RED + fullName.toString() + " has been declared as an enemy by " + playerNation.getName());
                    }
                    for (UUID memberUUID : playerNation.getMembers()) {
                        Player enemyPlayer = Bukkit.getPlayer(memberUUID);
                        if (enemyPlayer != null)
                            enemyPlayer.sendMessage(ChatColor.RED + fullName.toString() + " has been declared as an enemy by " + playerNation.getName());
                    }
                }
                else {
                    playerNation.removeEnemy(argNation.getID());

                    //send message to all members of both nations
                    for (UUID enemyUUID : argNation.getMembers()) {
                        Player enemyPlayer = Bukkit.getPlayer(enemyUUID);
                        if (enemyPlayer != null)
                            enemyPlayer.sendMessage(ChatColor.GREEN + fullName.toString() + " is no longer an enemy of " + playerNation.getName());
                    }
                    for (UUID memberUUID : playerNation.getMembers()) {
                        Player enemyPlayer = Bukkit.getPlayer(memberUUID);
                        if (enemyPlayer != null)
                            enemyPlayer.sendMessage(ChatColor.GREEN + fullName.toString() + " is no longer an enemy of " + playerNation.getName());
                    }
                }
                return true;

            case "exile":
                if (args.length < 2)
                    return false;

                //check if nationsPlayer is in a nation
                if (playerNation == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a nation");
                    return true;
                }
                //check if nationsPlayer has permission to exile members
                if (!playerNation.getPermissionByRank(nationsPlayer.getRank()).canManageMembers()) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to manage members of your nation");
                    return true;
                }
                //check if nationsPlayer being exiled is online
                Player exiledPlayer = Bukkit.getPlayer(args[1]);
                if (exiledPlayer == null) {
                    player.sendMessage(ChatColor.RED + "That player cannot be found. Make sure the spelling is correct and that the player is online");
                    return true;
                }
                //check if nationsPlayer is trying to exile themselves
                NationsPlayer nationsExiledPlayer = Main.nationsManager.getPlayerByUUID(exiledPlayer.getUniqueId());
                if (nationsPlayer.getUniqueID().equals(nationsExiledPlayer.getUniqueID())) {
                    player.sendMessage(ChatColor.RED + "You cannot exile yourself from this nation. Use \"/nation leave\" instead.");
                    return true;
                }
                //check if nationsPlayer being exiled is a member of the same nation
                if (nationsPlayer.getNationID() != nationsExiledPlayer.getNationID()) {
                    player.sendMessage(ChatColor.RED + "That player is not a member of your nation");
                    return true;
                }
                //check if nationsPlayer being exiled is the leader of the nation
                if (nationsExiledPlayer.getRank() == NationsManager.Rank.LEADER) {
                    player.sendMessage(ChatColor.RED + "You cannot exile the leader of your nation");
                    return true;
                }
                //exile nationsPlayer
                nationsExiledPlayer.setNationID(-1);
                nationsExiledPlayer.setRank(NationsManager.Rank.NONMEMBER);
                playerNation.decrementPopulation();
                Bukkit.getPlayer(nationsExiledPlayer.getUniqueID()).sendMessage(ChatColor.RED + "You have been exiled from " + playerNation.getName() + "!");
                //send message to all members
                for (UUID uuid : playerNation.getMembers()) {
                    Player msgPlayer = Bukkit.getPlayer(uuid);
                    if (msgPlayer != null && !(msgPlayer.getUniqueId().equals(nationsExiledPlayer.getUniqueID())))
                        msgPlayer.sendMessage(ChatColor.RED + nationsExiledPlayer.getName() + " has been exiled from " + playerNation.getName());
                }
                return true;

            case "info":
                if (args.length < 2) {
                    if (nationsPlayer.getNationID() == -1)
                        return false;
                    else argNation = playerNation;
                }
                if (args.length == 1 && argNation == null)
                    player.sendMessage(ChatColor.RED + "No nation of that name exists");
                else {
                    StringBuilder legateStr = new StringBuilder();
                    StringBuilder memberStr = new StringBuilder();
                    StringBuilder enemiesStr = new StringBuilder();
                    for (UUID uuid : argNation.getMembers()) {
                        NationsPlayer member = nationsManager.getPlayerByUUID(uuid);
                        if (member.getRank() == NationsManager.Rank.LEGATE)
                            legateStr.append(", ").append(member.getName());
                        else if (member.getRank() == NationsManager.Rank.MEMBER)
                            memberStr.append(", ").append(member.getName());
                    }
                    if (legateStr.length() > 0)
                        legateStr = new StringBuilder(legateStr.substring(2));
                    if (memberStr.length() > 0)
                        memberStr = new StringBuilder(memberStr.substring(2));
                    for (int enemyID : argNation.getEnemies()) {
                        Nation enemy = nationsManager.getNationByID(enemyID);
                        enemiesStr.append(", ").append(enemy.getName());
                    }
                    if (enemiesStr.length() > 0)
                        enemiesStr = new StringBuilder(enemiesStr.substring(2));

                    player.sendMessage(ChatColor.GOLD + "--------------------\n"
                            + ChatColor.WHITE + argNation.getName()
                            + ChatColor.GOLD + "\n--------------------\n"
                            + ChatColor.YELLOW + "Leader: " + ChatColor.WHITE + argNation.getOwner().getName()
                            + ChatColor.YELLOW + "\nPopulation: " + ChatColor.WHITE + argNation.getPopulation()
                            + ChatColor.YELLOW + "\nLegates: " + ChatColor.WHITE + legateStr
                            + ChatColor.YELLOW + "\nMembers: " + ChatColor.WHITE + memberStr
                            + ChatColor.YELLOW + "\nEnemies: " + ChatColor.WHITE + enemiesStr);
                }
                return true;
                
            case "invite":
                if (args.length < 2)
                    return false;

                //check if nationsPlayer is in a nation
                if (playerNation == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a nation");
                    return true;
                }
                //check if nationsPlayer has permission to invite members
                if (!playerNation.getPermissionByRank(nationsPlayer.getRank()).canManageMembers()) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to invite members to your nation");
                    return true;
                }
                //check if nationsPlayer being invited is online
                Player invitedPlayer = Bukkit.getPlayer(args[1]);
                if (invitedPlayer == null) {
                    player.sendMessage(ChatColor.RED + "That player cannot be found. Make sure the spelling is correct and that the player is online");
                    return true;
                }
                //check if nationsPlayer being invited is a member of the same nation
                NationsPlayer nationsInvitedPlayer = Main.nationsManager.getPlayerByUUID(invitedPlayer.getUniqueId());
                if (nationsPlayer.getNationID() == nationsInvitedPlayer.getNationID()) {
                    player.sendMessage(ChatColor.RED + "That player is already a member of your nation");
                    return true;
                }
                //check if nationsPlayer being invited is already in a nation
                if (nationsInvitedPlayer.getNationID() != -1) {
                    player.sendMessage(ChatColor.YELLOW + "That player is already in a nation");
                    return true;
                }
                //check if nationsPlayer already has a pending invite
                for (UUID uuid : playerNation.getInvitedPlayers()) {
                    if (uuid.equals(nationsInvitedPlayer.getUniqueID())) {
                        player.sendMessage(ChatColor.RED + nationsInvitedPlayer.getName() + " already has a pending invite to " + playerNation.getName());
                        return true;
                    }
                }
                //invite nationsPlayer
                playerNation.addInvitedPlayer(invitedPlayer.getUniqueId());
                player.sendMessage(ChatColor.GREEN + args[1] + " has been invited to " + playerNation.getName());
                return true;

            case "join":
                if (args.length < 2) return false;
                //check if player is already in a nation
                if (nationsPlayer.getNationID() != -1) {
                    player.sendMessage(ChatColor.RED + "You are already in a nation");
                    return true;
                }
                //check if the nation exists
                if (argNation == null) {
                    player.sendMessage(ChatColor.RED + "No nation of that name exists");
                    return true;
                }
                //check if player has an invite to the nation
                Nation inviteNation = nationsManager.getNationByName(fullName.toString());
                if (!inviteNation.isPlayerInvited(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You have not been invited to join this nation");
                    return true;
                }
                //add player to the nation
                nationsPlayer.setNationID(argNation.getID());
                nationsPlayer.setRank(NationsManager.Rank.MEMBER);
                argNation.incrementPopulation();
                argNation.addMember(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "You have joined the nation \"" + argNation.getName() + "\"");
                //send message to all members
                for (UUID uuid : argNation.getMembers()) {
                    Player msgPlayer = Bukkit.getPlayer(uuid);
                    if (msgPlayer != null && !(msgPlayer.getUniqueId().equals(player.getUniqueId())))
                        msgPlayer.sendMessage(ChatColor.GREEN + nationsPlayer.getName() + " has joined " + argNation.getName());
                }
                return true;

            case "leave":
                //check if player is in a nation
                if (nationsPlayer.getNationID() == -1) {
                    player.sendMessage(ChatColor.RED + "You are not in a nation");
                    return true;
                }
                //confirmation
                if (args.length < 2) {
                    if (nationsPlayer.getRank() == NationsManager.Rank.LEADER)
                        player.sendMessage(ChatColor.RED + "WARNING: You are the leader of this nation. If you leave, it will be completely disbanded, and you will lose all your claimed land with no refund!");
                    player.sendMessage(ChatColor.RED + "Confirm that you want to leave this nation with: /nation leave [name]");
                    return true;
                }
                //leave nation
                if (fullName.toString().equals(nationsManager.getNationByID(nationsPlayer.getNationID()).getName())) {
                    nationsPlayer.setNationID(-1);
                    playerNation.decrementPopulation();
                    if (nationsPlayer.getRank() == NationsManager.Rank.LEADER)
                        nationsManager.removeNation(playerNation.getID());
                    nationsPlayer.setRank(NationsManager.Rank.NONMEMBER);
                    playerNation.removeMember(player.getUniqueId());
                    player.sendMessage(ChatColor.YELLOW + "You have left the nation \"" + playerNation.getName() + "\"");
                    //send message to all members
                    for (UUID uuid : playerNation.getMembers()) {
                        Player msgPlayer = Bukkit.getPlayer(uuid);
                        if (msgPlayer != null && !(msgPlayer.getUniqueId().equals(player.getUniqueId())))
                            msgPlayer.sendMessage(ChatColor.YELLOW + nationsPlayer.getName() + " has left " + playerNation.getName());
                    }
                }
                else return false;
                return true;

            case "list":
                if (nationsManager.getNations().keySet().size() > 0) {
                    StringBuilder list = new StringBuilder(ChatColor.GOLD + "List of nations:" + ChatColor.WHITE);
                    for (Nation nationItem : nationsManager.getNations().values())
                        list.append(", ").append(nationItem.getName());
                    list = new StringBuilder(list.substring(0, 18) + list.substring(21));
                    player.sendMessage(list.toString());
                } else
                    player.sendMessage(ChatColor.YELLOW + "There are no nations");
                return true;

            case "permissions":
            case "perms":
                //check if player is in a nation
                if (nationsPlayer.getNationID() == -1) {
                    player.sendMessage(ChatColor.RED + "You are not in a nation");
                    return true;
                }
                //check if player has permission to edit permissions
                if (!playerNation.getPermissionByRank(nationsPlayer.getRank()).canManageMembers()) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to manage members of your nation");
                    return true;
                }
                ranksMenu(player);
                return true;
                
            case "promote":
                if (args.length < 2)
                    return false;

                //check if nationsPlayer is in a nation
                if (playerNation == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a nation");
                    return true;
                }
                //check if nationsPlayer has permission to promote members
                if (!playerNation.getPermissionByRank(nationsPlayer.getRank()).canManageMembers()) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to manage members of your nation");
                    return true;
                }
                //check if nationsPlayer being promoted is online
                Player promotedPlayer = Bukkit.getPlayer(args[1]);
                if (promotedPlayer == null) {
                    player.sendMessage(ChatColor.RED + "That player cannot be found. Make sure the spelling is correct and that the player is online");
                    return true;
                }
                //check if nationsPlayer being promoted is a member of the same nation
                NationsPlayer nationsPromotedPlayer = Main.nationsManager.getPlayerByUUID(promotedPlayer.getUniqueId());
                if (nationsPlayer.getNationID() != nationsPromotedPlayer.getNationID()) {
                    player.sendMessage(ChatColor.RED + "That player is not a member of your nation");
                    return true;
                }
                //check if nationsPlayer being promoted is already a leader or legate
                if (nationsPromotedPlayer.getRank() == NationsManager.Rank.LEADER || nationsPromotedPlayer.getRank() == NationsManager.Rank.LEGATE) {
                    player.sendMessage(ChatColor.RED + "That player is already at the highest possible rank");
                    return true;
                }
                //promote nationsPlayer
                nationsPromotedPlayer.setRank(NationsManager.Rank.LEGATE);
                Bukkit.getPlayer(nationsPromotedPlayer.getUniqueID()).sendMessage(ChatColor.GREEN + "You have been promoted from member to legate in " + playerNation.getName());
                //send message to all members
                for (UUID uuid : playerNation.getMembers()) {
                    Player msgPlayer = Bukkit.getPlayer(uuid);
                    if (msgPlayer != null && !(msgPlayer.getUniqueId().equals(nationsPromotedPlayer.getUniqueID())))
                        msgPlayer.sendMessage(ChatColor.YELLOW + nationsPromotedPlayer.getName() + " has been promoted to legate");
                }
                return true;

            case "rename":
                if (args.length < 2) return false;

                //check if player is in a nation
                if (nationsPlayer.getNationID() == -1) {
                    player.sendMessage(ChatColor.RED + "You are not in a nation");
                    return true;
                }
                //check if player is the leader of their nation
                if (nationsPlayer.getRank() != NationsManager.Rank.LEADER) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to rename your nation");
                    return true;
                }
                //check if name is 2-30 characters long
                if (fullName.length() < 2 || fullName.length() > 30) {
                    player.sendMessage(ChatColor.RED + "Nation name must be between 2 and 30 characters");
                    return true;
                }
                //check if name is already taken
                if (nationsManager.getNationByName(fullName.toString()) != null) {
                    player.sendMessage(ChatColor.RED + "A nation with that name already exists");
                    return true;
                }
                //rename nation
                playerNation.setName(fullName.toString());
                player.sendMessage(ChatColor.GREEN + "Renamed your nation to \"" + playerNation.getName() + "\"");
                return true;
                
            case "unclaim":
            case "uc":
                //check if nationsPlayer owns a nation
                if (nationsPlayer.getNationID() == -1) {
                    player.sendMessage(ChatColor.RED + "You must be in a nation to claim and unclaim chunks");
                    return true;
                }

                //check if nationsPlayer has permission to claim land
                if (!playerNation.getPermissionByRank(nationsPlayer.getRank()).canClaimLand()) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to unclaim land from your nation");
                    return true;
                }

                Chunk currentChunk = player.getLocation().getChunk();
                //check if chunk is claimed
                for (NationsChunk chunk : Main.nationsManager.getChunks()) {
                    if (chunk.getX() == currentChunk.getX() && chunk.getZ() == currentChunk.getZ()) {
                        //check if nationsPlayer's nation owns this chunk
                        if (chunk.getNationID() == nationsPlayer.getNationID()) {
                            //unclaim chunk
                            Main.nationsManager.getChunks().remove(chunk);
                            Main.nationsManager.getNationByID(nationsPlayer.getNationID()).decrementChunks();
                            int chunkCost = (NationsManager.chunkCost * Main.nationsManager.getNationByID(nationsPlayer.getNationID()).getNumChunks()) + NationsManager.chunkCost;
                            nationsPlayer.addMoney(chunkCost);
                            player.sendMessage(ChatColor.GREEN + "Unclaimed this chunk from " + Main.nationsManager.getNationByID(nationsPlayer.getNationID()).getName() + ". You received $" + chunkCost);
                            nationsPlayer.setCurrentChunk(new NationsChunk(currentChunk.getX(), currentChunk.getZ(), -1)); //update currentChunk
                            //update all villagers in the chunk
                            for (Entity entity : currentChunk.getEntities()) {
                                if (entity instanceof CraftVillager) {
                                    EntityVillager villager = ((CraftVillager) entity).getHandle();
                                    if (Main.nationsManager.getVillagerByUUID(villager.getUniqueID()).getNationID() == nationsPlayer.getNationID()) {
                                        Main.nationsManager.getVillagerByUUID(villager.getUniqueID()).setNationID(-1);
                                        Main.nationsManager.getNationByID(nationsPlayer.getNationID()).decrementPopulation();
                                    }
                                }
                            }
                        }
                        else player.sendMessage(ChatColor.RED + "Your nation does not own this chunk");
                        return true;
                    }
                }

                //when this chunk is not claimed
                player.sendMessage(ChatColor.RED + "This chunk is not claimed");
                return true;
        }
        return false;
    }

    //GUI to choose rank
    public static void ranksMenu(Player player) {
        InventoryGUI gui = new InventoryGUI(player, "Ranks", 1, true);
        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 3);
        //legate
        InventoryGUIButton legateButton = new InventoryGUIButton(gui, "Legate", null, Material.DIAMOND);
        legateButton.setOnClick(e -> permsMenu(player, NationsManager.Rank.LEGATE));
        gui.addButton(legateButton);
        //member
        InventoryGUIButton memberButton = new InventoryGUIButton(gui, ChatColor.WHITE + "Member", null, Material.PLAYER_HEAD);
        memberButton.setOnClick(e -> permsMenu(player, NationsManager.Rank.MEMBER));
        gui.addButton(memberButton);
        //non member
        InventoryGUIButton nonMemberButton = new InventoryGUIButton(gui, "Nonmember", null, Material.BARRIER);
        nonMemberButton.setOnClick(e -> permsMenu(player, NationsManager.Rank.NONMEMBER));
        gui.addButton(nonMemberButton);
        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 3);
        gui.showMenu();
    }

    //GUI to edit permissions for nation
    public static void permsMenu(Player player, NationsManager.Rank rank) {
        Nation nation = nationsManager.getNationByID(nationsManager.getPlayerByUUID(player.getUniqueId()).getNationID());
        NationsPermission perms = nation.getPermissionByRank(rank);
        InventoryGUI gui = new InventoryGUI(player, rank + " Permissions", 1, true);
        //back button
        InventoryGUIButton backButton = new InventoryGUIButton(gui, "Back", null, Material.ARROW);
        backButton.setOnClick(e -> {
            gui.removeAllClickEvents();
            ranksMenu(player);
        });
        gui.addButton(backButton);

        gui.addButton(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE));

        //modify blocks
        InventoryGUIButton modifyBlocksButton = new InventoryGUIButton(gui, "Place/Break Blocks", isAllowed(perms.canModifyBlocks()), Material.IRON_PICKAXE);
        modifyBlocksButton.setOnClick(e -> {
            perms.setModifyBlocks(!perms.canModifyBlocks());
            modifyBlocksButton.setDescription(isAllowed(perms.canModifyBlocks()));
            nation.setPermissionByRank(rank, perms);
        });
        gui.addButton(modifyBlocksButton);

        //open containers
        InventoryGUIButton openContainersButton = new InventoryGUIButton(gui, "Open Containers", isAllowed(perms.canOpenContainers()), Material.CHEST);
        openContainersButton.setOnClick(e -> {
            perms.setOpenContainers(!perms.canOpenContainers());
            openContainersButton.setDescription(isAllowed(perms.canOpenContainers()));
            nation.setPermissionByRank(rank, perms);
        });
        gui.addButton(openContainersButton);

        //attack peaceful mobs
        InventoryGUIButton attackMobsButton = new InventoryGUIButton(gui, "Attack Peaceful Mobs", isAllowed(perms.canAttackEntities()), Material.GOLDEN_SWORD);
        attackMobsButton.setOnClick(e -> {
            perms.setAttackEntities(!perms.canAttackEntities());
            attackMobsButton.setDescription(isAllowed(perms.canAttackEntities()));
            nation.setPermissionByRank(rank, perms);
        });
        gui.addButton(attackMobsButton);

        //claim land
        InventoryGUIButton claimLandButton = new InventoryGUIButton(gui, "Claim Land", isAllowed(perms.canClaimLand()), Material.GRASS_BLOCK);
        claimLandButton.setOnClick(e -> {
            perms.setClaimLand(!perms.canClaimLand());
            claimLandButton.setDescription(isAllowed(perms.canClaimLand()));
            nation.setPermissionByRank(rank, perms);
        });
        gui.addButton(claimLandButton);

        //manage members
        InventoryGUIButton manageMembersButton = new InventoryGUIButton(gui, ChatColor.WHITE + "Manage Members", isAllowed(perms.canManageMembers()), Material.PLAYER_HEAD);
        manageMembersButton.setOnClick(e -> {
            perms.setManageMembers(!perms.canManageMembers());
            manageMembersButton.setDescription(isAllowed(perms.canClaimLand()));
            nation.setPermissionByRank(rank, perms);
        });
        gui.addButton(manageMembersButton);

        //declare enemies
        InventoryGUIButton declareEnemiesButton = new InventoryGUIButton(gui, "Declare Enemies", isAllowed(perms.canDeclareEnemies()), Material.TNT);
        declareEnemiesButton.setOnClick(e -> {
            perms.setDeclareEnemies(!perms.canDeclareEnemies());
            declareEnemiesButton.setDescription(isAllowed(perms.canDeclareEnemies()));
            nation.setPermissionByRank(rank, perms);
        });
        gui.addButton(declareEnemiesButton);

        gui.addButton(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE));

        gui.showMenu();
    }

    public static String isAllowed(boolean perm) {
        return perm ? ChatColor.GREEN + "ALLOWED" : ChatColor.RED + "DENIED";
    }
}