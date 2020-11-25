package com.github.dawsonvilamaa.nationsandvillagesplugin.commands;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.Nation;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class nation implements Command {

    /**
     * @param player
     * @param cmd
     * @param args
     */
    public static boolean run(Player player, String cmd, String[] args) {
        if (args.length == 0) return false;
        switch (args[0]) {
            case "create":
                if (args.length < 2) return false;
                String fullName = "";
                for (int i = 1; i < args.length; i++) fullName += args[i] + " ";
                fullName = fullName.substring(0, fullName.length() - 1);
                //check if name is 2-30 characters long
                if (fullName.length() < 2 || fullName.length() > 30)
                    player.sendMessage(ChatColor.RED + "Nation name must be between 2 and 30 characters");
                else {
                    //check if name is already taken
                    if (Main.nationsManager.getNationByName(fullName) != null)
                        player.sendMessage(ChatColor.RED + "A nation with that name already exists");
                    else {
                        Main.nationsManager.addNation(new Nation(fullName, player));
                        player.sendMessage(ChatColor.GREEN + "You have successfully created the nation \"" + fullName + "\"");
                    }
                }
                return true;

            case "info":
                if (args.length < 2) return false;
                String fullNationName = "";
                for (int i = 1; i < args.length; i++) fullNationName += args[i] + " ";
                fullNationName = fullNationName.substring(0, fullNationName.length() - 1);
                Nation infoNation = Main.nationsManager.getNationByName(fullNationName);
                if (infoNation == null) player.sendMessage(ChatColor.RED + "No nation of that name exists");
                else {
                    player.sendMessage(ChatColor.GOLD + "--------------------\n"
                            + ChatColor.WHITE + infoNation.getName()
                            + ChatColor.GOLD + "\n--------------------\n"
                            + ChatColor.WHITE + "Owner: " + infoNation.getOwner().getName()
                            + "\nPopulation: " + infoNation.getPopulation()
                            + "\nVillages: " + infoNation.getVillages().size());
                }
                return true;

            case "list":
                String list = ChatColor.GOLD + "List of nations:" + ChatColor.WHITE;
                for (Nation nation : Main.nationsManager.getNations()) list += "\n" + nation.getName();
                player.sendMessage(list);
                return true;
        }
        return false;
    }
}
