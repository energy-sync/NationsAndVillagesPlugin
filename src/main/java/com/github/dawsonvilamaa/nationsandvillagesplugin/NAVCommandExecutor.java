package com.github.dawsonvilamaa.nationsandvillagesplugin;

import com.github.dawsonvilamaa.nationsandvillagesplugin.commands.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NAVCommandExecutor implements CommandExecutor {
    private final Main plugin;

    public NAVCommandExecutor(Main plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            //makes "clean" version of sender, cmd, and args to send to command handling files
            Player cleanPlayer = (Player) sender;
            String cleanCmd = cmd.getName().toLowerCase();

            switch (cleanCmd) {
                case "autoclaim":
                    return autoclaim.run(cleanPlayer, args);

                case "autounclaim":
                    return autounclaim.run(cleanPlayer, args);

                case "balance":
                    return money.run(cleanPlayer, args);

                case "claim":
                    return claim.run(cleanPlayer, args);

                case "nation":
                    return nation.run(cleanPlayer, args);

                case "unclaim":
                    return unclaim.run(cleanPlayer, args);

                default:
                    return false;
            }
        }
        else return true;
    }
}