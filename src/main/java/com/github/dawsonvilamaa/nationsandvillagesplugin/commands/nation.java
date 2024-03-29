package com.github.dawsonvilamaa.nationsandvillagesplugin.commands;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.NationsManager;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.Nation;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsChunk;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsPermission;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsPlayer;
import com.github.dawsonvilamaa.nationsandvillagesplugin.gui.InventoryGUI;
import com.github.dawsonvilamaa.nationsandvillagesplugin.gui.InventoryGUIButton;
import com.github.dawsonvilamaa.nationsandvillagesplugin.npcs.NationsIronGolem;
import com.github.dawsonvilamaa.nationsandvillagesplugin.npcs.NationsVillager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftIronGolem;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
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
                //check if player is in a nation
                if (nationsPlayer.getNationID() == -1) {
                    player.sendMessage(ChatColor.RED + "You must own a nation to claim chunks");
                    return true;
                }
                //check if player is autoclaiming
                if (nationsPlayer.getAutoClaimMode() != NationsPlayer.AUTOCLAIM_MODE.AUTOCLAIM) {
                    //turn on autoclaim
                    nationsPlayer.setAutoClaimMode(NationsPlayer.AUTOCLAIM_MODE.AUTOCLAIM);
                    player.sendMessage(ChatColor.YELLOW + "Auto-claiming for " + playerNation.getName() + " has been " + ChatColor.GREEN + "ENABLED");
                    NationsChunk currentChunk = nationsPlayer.getCurrentChunk();
                    //claim chunk
                    if (Main.nationsManager.getChunkByCoords(currentChunk.getX(), currentChunk.getZ(), currentChunk.getWorld()) == null)
                        nation.run(player, new String[] {"claim"});
                }
                else {
                    //turn off autoclaim
                    nationsPlayer.setAutoClaimMode(NationsPlayer.AUTOCLAIM_MODE.NONE);
                    player.sendMessage(ChatColor.YELLOW + "Auto-claiming for " + playerNation.getName() + " has been " + ChatColor.RED + "DISABLED");
                }
                return true;
                
            case "autounclaim":
            case "auc":
                //check if player is in a nation
                if (nationsPlayer.getNationID() == -1) {
                    player.sendMessage(ChatColor.RED + "You must own a nation to claim and unclaim chunks");
                    return true;
                }
                //check if player is autounclaiming
                if (nationsPlayer.getAutoClaimMode() != NationsPlayer.AUTOCLAIM_MODE.AUTOUNCLAIM) {
                    //turn on autounclaim
                    nationsPlayer.setAutoClaimMode(NationsPlayer.AUTOCLAIM_MODE.AUTOUNCLAIM);
                    player.sendMessage(ChatColor.YELLOW + "Auto-unclaiming for " + playerNation.getName() + " has been " + ChatColor.GREEN + "ENABLED");
                    NationsChunk currentChunk = nationsPlayer.getCurrentChunk();
                    NationsChunk claimedChunk = Main.nationsManager.getChunkByCoords(currentChunk.getX(), currentChunk.getZ(), currentChunk.getWorld());
                    //unclaim chunk
                    if (claimedChunk != null && claimedChunk.getNationID() == nationsPlayer.getNationID())
                        nation.run(player, new String[] {"unclaim"});
                }
                else {
                    //turn off autounclaim
                    nationsPlayer.setAutoClaimMode(NationsPlayer.AUTOCLAIM_MODE.NONE);
                    player.sendMessage(ChatColor.YELLOW + "Auto-unclaiming for " + playerNation.getName() + " has been " + ChatColor.RED + "DISABLED");
                }
                return true;
                
            case "claim":
            case "c":
                //check if nationsPlayer owns a nation
                if (nationsPlayer.getNationID() == -1) {
                    player.sendMessage(ChatColor.RED + "You must be in a nation to claim land");
                    return true;
                }

                //check if nationsPlayer has permission to claim land
                if (!playerNation.getConfig().getPermissionByRank(nationsPlayer.getRank()).canClaimLand()) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to claim land for your nation");
                    return true;
                }

                Chunk currentChunk = player.getLocation().getChunk();
                //check if chunk is already claimed
                if (Main.nationsManager.getChunkByCoords(currentChunk.getX(), currentChunk.getZ(), currentChunk.getWorld()) != null) {
                    player.sendMessage(ChatColor.RED + "This chunk is already claimed");
                    return true;
                }

                //check if nationsPlayer has enough money to buy a new chunk
                int chunkCost = (playerNation.getNumChunks() * NationsManager.chunkCost) + NationsManager.chunkCost;
                if (nationsPlayer.getMoney() < chunkCost) {
                    player.sendMessage(ChatColor.RED + "You do not have enough money to purchase a new chunk. It costs $" + chunkCost);
                    return true;
                }

                //claim chunk
                Main.nationsManager.addChunk(currentChunk.getX(), currentChunk.getZ(), currentChunk.getWorld(), nationsPlayer.getNationID());
                nationsPlayer.removeMoney(chunkCost);
                playerNation.incrementChunks();
                player.sendMessage(ChatColor.GREEN + "Claimed this chunk for " + playerNation.getName() + " for $" + chunkCost);
                nationsPlayer.setCurrentChunk(new NationsChunk(currentChunk.getX(), currentChunk.getZ(), currentChunk.getWorld(), nationsPlayer.getNationID())); //update currentChunk

                //update all villagers and iron golems in the chunk
                for (Entity entity : currentChunk.getEntities()) {
                    if (entity instanceof CraftVillager) {
                        NationsVillager nationsVillager = nationsManager.getVillagerByUUID(entity.getUniqueId());
                        if (nationsVillager != null || nationsVillager.getNationID() != nationsPlayer.getNationID()) {
                            nationsVillager.setNationID(nationsPlayer.getNationID());
                            playerNation.incrementPopulation();
                        }
                    }
                    else if (entity instanceof CraftIronGolem) {
                        NationsIronGolem nationsIronGolem = nationsManager.getGolemByUUID(entity.getUniqueId());
                        if (nationsIronGolem != null || nationsIronGolem.getNationID() != nationsPlayer.getNationID())
                            nationsIronGolem.setNationID(nationsPlayer.getNationID());
                    }
                }
                return true;

            case "config":
            case "settings":
                //check if player is in a nation
                if (nationsPlayer.getNationID() == -1) {
                    player.sendMessage(ChatColor.RED + "You are not in a nation");
                    return true;
                }

                //check if player has permission to access config for their nation
                if (!playerNation.getConfig().getPermissionByRank(nationsPlayer.getRank()).canAccessConfig()) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to change settings for your nation");
                    return true;
                }

                //show menu
                configMenu(player);
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
                else if (nationsManager.getNationByName(fullName.toString()) != null) {
                    player.sendMessage(ChatColor.RED + "A nation with that name already exists");
                    return true;
                }

                //create nation
                Nation newNation = new Nation(fullName.toString(), player, nationsManager.nextNationID());
                nationsManager.addNation(newNation);
                nationsPlayer.setNationID(newNation.getID());
                newNation.incrementPopulation();
                nationsPlayer.setRank(NationsManager.Rank.LEADER);
                newNation.addMember(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "You have created the nation \"" + newNation.getName() + "\"");
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
                if (!playerNation.getConfig().getPermissionByRank(nationsPlayer.getRank()).canManageMembers()) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to manage members of your nation");
                    return true;
                }

                //check if NationsPlayer being demoted is online
                Player demotedPlayer = Bukkit.getPlayer(args[1]);
                if (demotedPlayer == null) {
                    player.sendMessage(ChatColor.RED + "That player cannot be found. Make sure the spelling is correct and that the player is online");
                    return true;
                }

                //check if NationsPlayer being demoted is a member of the same nation
                NationsPlayer demotedNationsPlayer = Main.nationsManager.getPlayerByUUID(demotedPlayer.getUniqueId());
                if (nationsPlayer.getNationID() != demotedNationsPlayer.getNationID()) {
                    player.sendMessage(ChatColor.RED + "That player is not a member of your nation");
                    return true;
                }

                //check if NationsPlayer being demoted is a leader
                if (demotedNationsPlayer.getRank() == NationsManager.Rank.LEADER) {
                    player.sendMessage(ChatColor.RED + "You cannot demote the leader of a nation");
                    return true;
                }

                //check if NationsPlayer being demoted is already a member (lowest rank)
                if (demotedNationsPlayer.getRank() == NationsManager.Rank.MEMBER) {
                    player.sendMessage(ChatColor.RED + "That player is already at the lowest possible rank");
                    return true;
                }

                //demote NationsPlayer
                demotedNationsPlayer.setRank(NationsManager.Rank.MEMBER);
                Bukkit.getPlayer(demotedNationsPlayer.getUniqueID()).sendMessage(ChatColor.RED + "You have been demoted from legate to member rank in " + playerNation.getName());

                //send message to all members
                for (UUID uuid : playerNation.getMemberUUIDs()) {
                    Player msgNationsPlayer = Bukkit.getPlayer(uuid);
                    if (msgNationsPlayer != null && !(msgNationsPlayer.getUniqueId().equals(demotedNationsPlayer.getUniqueID())))
                        msgNationsPlayer.sendMessage(ChatColor.YELLOW + demotedNationsPlayer.getName() + " has been demoted to member");
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

                //check if player has permission to declare an enemy
                if (!playerNation.getConfig().getPermissionByRank(nationsPlayer.getRank()).canDeclareEnemies()) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to declare enemies");
                    return true;
                }

                //check if enemy nation exists
                if (argNation == null) {
                    player.sendMessage(ChatColor.RED + "No nation of that name exists");
                    return true;
                }

                //check if player is trying to declare own nation as an enemy
                if (playerNation.getID() == argNation.getID()) {
                    player.sendMessage(ChatColor.RED + "You cannot declare your own nation as an enemy");
                    return true;
                }

                //declare or undeclare enemy
                fullName = new StringBuilder(fullName.substring(0, 1).toUpperCase() + fullName.substring(1));
                if (!playerNation.isEnemy(argNation.getID())) {
                    playerNation.addEnemy(argNation.getID());

                    //send message to all members of both nations
                    ArrayList<UUID> playerUUIDs = new ArrayList<>();
                    playerUUIDs.addAll(playerNation.getMemberUUIDs());
                    playerUUIDs.addAll(argNation.getMemberUUIDs());
                    for (UUID playerUUID : playerUUIDs) {
                        Player onlinePlayer = Bukkit.getPlayer(playerUUID);
                        if (onlinePlayer != null)
                            onlinePlayer.sendMessage(ChatColor.RED + fullName.toString() + " has been declared as an enemy by " + playerNation.getName());
                    }
                }
                else {
                    playerNation.removeEnemy(argNation.getID());

                    //send message to all members of both nations
                    ArrayList<UUID> playerUUIDs = new ArrayList<>();
                    playerUUIDs.addAll(playerNation.getMemberUUIDs());
                    playerUUIDs.addAll(argNation.getMemberUUIDs());
                    for (UUID playerUUID : playerUUIDs) {
                        Player onlinePlayer = Bukkit.getPlayer(playerUUID);
                        if (onlinePlayer != null)
                            onlinePlayer.sendMessage(ChatColor.GREEN + fullName.toString() + " is no longer an enemy of " + playerNation.getName());
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
                if (!playerNation.getConfig().getPermissionByRank(nationsPlayer.getRank()).canManageMembers()) {
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
                for (UUID uuid : playerNation.getMemberUUIDs()) {
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
                StringBuilder legateStr = new StringBuilder();
                StringBuilder memberStr = new StringBuilder();
                StringBuilder enemiesStr = new StringBuilder();
                for (UUID uuid : argNation.getMemberUUIDs()) {
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
                        + ChatColor.YELLOW + "Leader: " + ChatColor.WHITE + Bukkit.getOfflinePlayer(argNation.getOwnerUUID()).getName()
                        + ChatColor.YELLOW + "\nPopulation: " + ChatColor.WHITE + argNation.getPopulation()
                        + ChatColor.YELLOW + "\nLegates: " + ChatColor.WHITE + legateStr
                        + ChatColor.YELLOW + "\nMembers: " + ChatColor.WHITE + memberStr
                        + ChatColor.YELLOW + "\nEnemies: " + ChatColor.WHITE + enemiesStr);
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
                if (!playerNation.getConfig().getPermissionByRank(nationsPlayer.getRank()).canManageMembers()) {
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

                //check if player has been invited to the nation
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
                for (UUID uuid : argNation.getMemberUUIDs()) {
                    Player msgPlayer = Bukkit.getPlayer(uuid);
                    if (msgPlayer != null && !(msgPlayer.getUniqueId().equals(player.getUniqueId())))
                        msgPlayer.sendMessage(ChatColor.GREEN + nationsPlayer.getName() + " has joined " + argNation.getName());
                }

                //remove invite
                argNation.removeInvitedPlayer(player.getUniqueId());

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
                    for (UUID uuid : playerNation.getMemberUUIDs()) {
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
                
            case "promote":
                if (args.length < 2)
                    return false;

                //check if nationsPlayer is in a nation
                if (playerNation == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a nation");
                    return true;
                }

                //check if nationsPlayer has permission to promote members
                if (!playerNation.getConfig().getPermissionByRank(nationsPlayer.getRank()).canManageMembers()) {
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
                for (UUID uuid : playerNation.getMemberUUIDs()) {
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
                if (!playerNation.getConfig().getPermissionByRank(nationsPlayer.getRank()).canClaimLand()) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to unclaim land from your nation");
                    return true;
                }

                currentChunk = player.getLocation().getChunk();
                //check if chunk is claimed
                if (Main.nationsManager.getChunkByCoords(currentChunk.getX(), currentChunk.getZ(), currentChunk.getWorld()) == null) {
                    player.sendMessage(ChatColor.RED + "This chunk is not claimed");
                    return true;
                }

                //unclaim chunk
                Main.nationsManager.removeChunk(currentChunk.getX(), currentChunk.getZ(), currentChunk.getWorld());
                chunkCost = (playerNation.getNumChunks() * NationsManager.chunkCost);
                nationsPlayer.addMoney(chunkCost);
                playerNation.decrementChunks();
                player.sendMessage(ChatColor.YELLOW + "Unclaimed this chunk from " + playerNation.getName() + " and received $" + chunkCost);
                nationsPlayer.setCurrentChunk(new NationsChunk(currentChunk.getX(), currentChunk.getZ(), currentChunk.getWorld(), -1)); //update currentChunk

                //update all villagers and iron golems in the chunk
                for (Entity entity : currentChunk.getEntities()) {
                    if (entity instanceof CraftVillager) {
                        NationsVillager nationsVillager = nationsManager.getVillagerByUUID(entity.getUniqueId());
                        if (nationsVillager != null) {
                            nationsVillager.setNationID(-1);
                            playerNation.decrementPopulation();
                        }
                    }
                    else if (entity instanceof CraftIronGolem) {
                        NationsIronGolem nationsIronGolem = nationsManager.getGolemByUUID(entity.getUniqueId());
                        if (nationsIronGolem != null)
                            nationsIronGolem.setNationID(-1);
                    }
                }
                return true;
        }
        return false;
    }

    //GUI for nation config/settings
    public static void configMenu(Player player) {
        Nation nation = nationsManager.getNationByID(nationsManager.getPlayerByUUID(player.getUniqueId()).getNationID());
        InventoryGUI gui = new InventoryGUI(player, "Nation Settings", 1, true);

        //permissions button - opens rank menu
        InventoryGUIButton permsButton = new InventoryGUIButton(gui, "Manage Permissions", "Edit permissions for different ranks of your nation", Material.PLAYER_HEAD);
        permsButton.setOnClick(e -> ranksMenu(player));
        gui.addButton(permsButton);

        //iron golem attack button
        String golemButtonDescription = " - Toggle iron golems attacking\nplayers and villagers of enemy nations";
        InventoryGUIButton golemButton = new InventoryGUIButton(gui, "Iron Golems Attack Enemies", (nation.getConfig().getGolemsAttackEnemies() ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED") + ChatColor.RESET + golemButtonDescription, Material.POPPY);
        golemButton.setOnClick(e -> {
            nation.getConfig().setGolemsAttackEnemies(!nation.getConfig().getGolemsAttackEnemies());
            golemButton.setDescription((nation.getConfig().getGolemsAttackEnemies() ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED") + ChatColor.RESET + golemButtonDescription);
        });
        gui.addButton(golemButton);

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 7);
        gui.showMenu();
    }

    //GUI to choose rank
    public static void ranksMenu(Player player) {
        InventoryGUI gui = new InventoryGUI(player, "Ranks", 1, true);
        gui.addButton(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE));
        //back button
        InventoryGUIButton backButton = new InventoryGUIButton(gui, "Back", null, Material.ARROW);
        backButton.setOnClick(e -> {
            gui.removeAllClickEvents();
            configMenu(player);
        });
        gui.addButton(backButton);
        gui.addButton(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE));
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
        NationsPermission perms = nation.getConfig().getPermissionByRank(rank);
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
            nation.getConfig().setPermissionByRank(rank, perms);
        });
        gui.addButton(modifyBlocksButton);

        //open containers
        InventoryGUIButton openContainersButton = new InventoryGUIButton(gui, "Open Containers", isAllowed(perms.canOpenContainers()), Material.CHEST);
        openContainersButton.setOnClick(e -> {
            perms.setOpenContainers(!perms.canOpenContainers());
            openContainersButton.setDescription(isAllowed(perms.canOpenContainers()));
            nation.getConfig().setPermissionByRank(rank, perms);
        });
        gui.addButton(openContainersButton);

        //attack peaceful mobs
        InventoryGUIButton attackMobsButton = new InventoryGUIButton(gui, "Attack Peaceful Mobs", isAllowed(perms.canAttackEntities()), Material.GOLDEN_SWORD);
        attackMobsButton.setOnClick(e -> {
            perms.setAttackEntities(!perms.canAttackEntities());
            attackMobsButton.setDescription(isAllowed(perms.canAttackEntities()));
            nation.getConfig().setPermissionByRank(rank, perms);
        });
        gui.addButton(attackMobsButton);

        //claim land
        InventoryGUIButton claimLandButton = new InventoryGUIButton(gui, "Claim Land", isAllowed(perms.canClaimLand()), Material.GRASS_BLOCK);
        claimLandButton.setOnClick(e -> {
            perms.setClaimLand(!perms.canClaimLand());
            claimLandButton.setDescription(isAllowed(perms.canClaimLand()));
            nation.getConfig().setPermissionByRank(rank, perms);
        });
        gui.addButton(claimLandButton);

        //manage members
        InventoryGUIButton manageMembersButton = new InventoryGUIButton(gui, ChatColor.WHITE + "Manage Members", isAllowed(perms.canManageMembers()), Material.PLAYER_HEAD);
        manageMembersButton.setOnClick(e -> {
            perms.setManageMembers(!perms.canManageMembers());
            manageMembersButton.setDescription(isAllowed(perms.canClaimLand()));
            nation.getConfig().setPermissionByRank(rank, perms);
        });
        gui.addButton(manageMembersButton);

        //declare enemies
        InventoryGUIButton declareEnemiesButton = new InventoryGUIButton(gui, "Declare Enemies", isAllowed(perms.canDeclareEnemies()), Material.TNT);
        declareEnemiesButton.setOnClick(e -> {
            perms.setDeclareEnemies(!perms.canDeclareEnemies());
            declareEnemiesButton.setDescription(isAllowed(perms.canDeclareEnemies()));
            nation.getConfig().setPermissionByRank(rank, perms);
        });
        gui.addButton(declareEnemiesButton);

        gui.addButton(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE));

        gui.showMenu();
    }

    public static String isAllowed(boolean perm) {
        return perm ? ChatColor.GREEN + "ALLOWED" : ChatColor.RED + "DENIED";
    }
}