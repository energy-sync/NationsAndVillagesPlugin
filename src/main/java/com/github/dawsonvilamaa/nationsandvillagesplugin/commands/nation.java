package com.github.dawsonvilamaa.nationsandvillagesplugin.commands;

import com.github.dawsonvilamaa.nationsandvillagesplugin.NationsManager;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.*;
import com.github.dawsonvilamaa.nationsandvillagesplugin.gui.InventoryGUI;
import com.github.dawsonvilamaa.nationsandvillagesplugin.gui.InventoryGUIButton;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

import static com.github.dawsonvilamaa.nationsandvillagesplugin.Main.nationsManager;

public class nation implements Command {

    /**
     * @param player
     * @param args
     */
    public static boolean run(Player player, String[] args) {
        if (args.length == 0) return false;

        NationsPlayer nationsPlayer = nationsManager.getPlayerByUUID(player.getUniqueId());
        String fullName = "";
        for (int i = 1; i < args.length; i++)
            fullName += args[i] + " ";
        if (fullName.length() > 0)
            fullName = fullName.substring(0, fullName.length() - 1);
        Nation nation = nationsManager.getNationByName(fullName);

        switch (args[0]) {
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
                else {
                    //check if name is already taken
                    if (nationsManager.getNationByName(fullName) != null)
                        player.sendMessage(ChatColor.RED + "A nation with that name already exists");
                    else {
                        Nation newNation = new Nation(fullName, player, nationsManager.nextNationID());
                        nationsManager.addNation(newNation);
                        nationsPlayer.setNationID(newNation.getID());
                        newNation.incrementPopulation();
                        nationsPlayer.setRank(NationsManager.Rank.LEADER);
                        newNation.addMember(player.getUniqueId());
                        player.sendMessage(ChatColor.GREEN + "You have created the nation \"" + newNation.getName() + "\"");
                    }
                }
                return true;

            case "join":
                if (args.length < 2) return false;
                //check if player is already in a nation
                if (nationsPlayer.getNationID() != -1) {
                    player.sendMessage(ChatColor.RED + "You are already in a nation");
                    return true;
                }
                //check if the nation exists
                if (nation == null) {
                    player.sendMessage(ChatColor.RED + "No nation of that name exists");
                    return true;
                }
                //check if player has an invite to the nation
                Nation inviteNation = nationsManager.getNationByName(fullName);
                if (inviteNation.isPlayerInvited(player.getUniqueId()) == false) {
                    player.sendMessage(ChatColor.RED + "You have not been invited to join this nation");
                    return true;
                }
                //add player to the nation
                nationsPlayer.setNationID(nation.getID());
                nationsPlayer.setRank(NationsManager.Rank.MEMBER);
                nation.incrementPopulation();
                nation.addMember(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "You have joined the nation \"" + nation.getName() + "\"");
                //send message to all members
                for (UUID uuid : nation.getMembers()) {
                    Player msgPlayer = Bukkit.getPlayer(uuid);
                    if (msgPlayer != null && !(msgPlayer.getUniqueId().equals(player.getUniqueId())))
                        msgPlayer.sendMessage(ChatColor.GREEN + nationsPlayer.getName() + " has joined " + nation.getName());
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
                        player.sendMessage(ChatColor.RED + "WARNING: You are the leader of this nation. If you leave, it will be completely disbanded, and you will lose all your claimed land!");
                    player.sendMessage(ChatColor.RED + "Confirm that you want to leave this nation with: /nation leave [name]");
                    return true;
                }
                //leave nation
                if (fullName.equals(nationsManager.getNationByID(nationsPlayer.getNationID()).getName())) {
                    nationsPlayer.setNationID(-1);
                    nation.decrementPopulation();
                    if (nationsPlayer.getRank() == NationsManager.Rank.LEADER)
                        nationsManager.removeNation(nation.getID());
                    nationsPlayer.setRank(NationsManager.Rank.NONMEMBER);
                    nation.removeMember(player.getUniqueId());
                    player.sendMessage(ChatColor.YELLOW + "You have left the nation \"" + nation.getName() + "\"");
                    //send message to all members
                    for (UUID uuid : nation.getMembers()) {
                        Player msgPlayer = Bukkit.getPlayer(uuid);
                        if (msgPlayer != null && !(msgPlayer.getUniqueId().equals(player.getUniqueId())))
                            msgPlayer.sendMessage(ChatColor.YELLOW + nationsPlayer.getName() + " has left " + nation.getName());
                    }
                }
                else return false;
                return true;

            case "info":
                if (args.length < 2) {
                    if (nationsPlayer.getNationID() == -1) return false;
                    nation = nationsManager.getNationByID(nationsPlayer.getNationID());
                }
                if (nation == null) player.sendMessage(ChatColor.RED + "No nation of that name exists");
                else {
                    String legateStr = "";
                    String memberStr = "";
                    for (UUID uuid : nation.getMembers()) {
                        NationsPlayer member = nationsManager.getPlayerByUUID(uuid);
                        if (member.getRank() == NationsManager.Rank.LEGATE)
                            legateStr += ", " + member.getName();
                        else if (member.getRank() == NationsManager.Rank.MEMBER)
                            memberStr += ", " + member.getName();
                    }
                    if (legateStr.length() > 0)
                        legateStr = legateStr.substring(2);
                    if (memberStr.length() > 0)
                        memberStr = memberStr.substring(2);

                    player.sendMessage(ChatColor.GOLD + "--------------------\n"
                        + ChatColor.WHITE + nation.getName()
                        + ChatColor.GOLD + "\n--------------------\n"
                        + ChatColor.YELLOW + "Leader: " + ChatColor.WHITE + nation.getOwner().getName()
                        + ChatColor.YELLOW + "\nPopulation: " + ChatColor.WHITE + nation.getPopulation()
                        + ChatColor.YELLOW + "\nLegates: " + ChatColor.WHITE + legateStr
                        + ChatColor.YELLOW + "\nMembers: " + ChatColor.WHITE + memberStr);
                }
                return true;

            case "permissions":
            case "perms":
                //check if player is in a nation
                if (nationsPlayer.getNationID() == -1) {
                    player.sendMessage(ChatColor.RED + "You are not in a nation");
                    return true;
                }
                //check if player has permission to edit permissions
                nation = nationsManager.getNationByID(nationsPlayer.getNationID());
                if (nation.getPermissionByRank(nationsPlayer.getRank()).canManageMembers() == false) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to manage members of your nation");
                    return true;
                }
                ranksMenu(player);
                return true;

            case "rename":
                if (args.length < 2) return false;
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
                if (nationsManager.getNationByName(fullName) != null) {
                    player.sendMessage(ChatColor.RED + "A nation with that name already exists");
                    return true;
                }
                //rename nation
                nation = nationsManager.getNationByID(nationsPlayer.getNationID());
                nation.setName(fullName);
                player.sendMessage(ChatColor.GREEN + "Renamed your nation to \"" + nation.getName() + "\"");
                return true;

            case "list":
                String list = ChatColor.GOLD + "List of nations:" + ChatColor.WHITE;
                for (Nation nationItem : nationsManager.getNations().values())
                    list += ", " + nationItem.getName();
                list = list.substring(2);
                player.sendMessage(list);
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
        InventoryGUI gui = new InventoryGUI(player, rank.toString() + " Permissions", 1, true);
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
            if (perms.canModifyBlocks() == true)
                perms.setModifyBlocks(false);
            else perms.setModifyBlocks(true);
            modifyBlocksButton.setDescription(isAllowed(perms.canModifyBlocks()));
            nation.setPermissionByRank(rank, perms);
        });
        gui.addButton(modifyBlocksButton);
        //open containers
        InventoryGUIButton openContainersButton = new InventoryGUIButton(gui, "Open Containers", isAllowed(perms.canOpenContainers()), Material.CHEST);
        openContainersButton.setOnClick(e -> {
            if (perms.canOpenContainers() == true)
                perms.setOpenContainers(false);
            else perms.setOpenContainers(true);
            openContainersButton.setDescription(isAllowed(perms.canOpenContainers()));
            nation.setPermissionByRank(rank, perms);
        });
        gui.addButton(openContainersButton);
        //attack peaceful mobs
        InventoryGUIButton attackMobsButton = new InventoryGUIButton(gui, "Attack Peaceful Mobs", isAllowed(perms.canAttackEntities()), Material.GOLDEN_SWORD);
        attackMobsButton.setOnClick(e -> {
            if (perms.canAttackEntities() == true)
                perms.setAttackEntities(false);
            else perms.setAttackEntities(true);
            attackMobsButton.setDescription(isAllowed(perms.canAttackEntities()));
            nation.setPermissionByRank(rank, perms);
        });
        gui.addButton(attackMobsButton);
        //claim land
        InventoryGUIButton claimLandButton = new InventoryGUIButton(gui, "Claim Land", isAllowed(perms.canClaimLand()), Material.GRASS_BLOCK);
        claimLandButton.setOnClick(e -> {
            if (perms.canClaimLand() == true)
                perms.setClaimLand(false);
            else perms.setClaimLand(true);
            claimLandButton.setDescription(isAllowed(perms.canClaimLand()));
            nation.setPermissionByRank(rank, perms);
        });
        gui.addButton(claimLandButton);
        //manage members
        InventoryGUIButton manageMembersButton = new InventoryGUIButton(gui, ChatColor.WHITE + "Manage Members", isAllowed(perms.canManageMembers()), Material.PLAYER_HEAD);
        manageMembersButton.setOnClick(e -> {
            if (perms.canManageMembers() == true)
                perms.setManageMembers(false);
            else perms.setManageMembers(true);
            manageMembersButton.setDescription(isAllowed(perms.canClaimLand()));
            nation.setPermissionByRank(rank, perms);
        });
        gui.addButton(manageMembersButton);
        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 2);
        gui.showMenu();
    }

    public static String isAllowed(boolean perm) {
        return perm ? ChatColor.GREEN + "ALLOWED" : ChatColor.RED + "DENIED";
    }
}