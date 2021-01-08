package com.github.dawsonvilamaa.nationsandvillagesplugin;

import com.github.dawsonvilamaa.nationsandvillagesplugin.commands.demote;
import com.github.dawsonvilamaa.nationsandvillagesplugin.commands.*;
import com.github.dawsonvilamaa.nationsandvillagesplugin.exceptions.NationNotFoundException;
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

                case "demote":
                    return demote.run(cleanPlayer, args);

                case "exile":
                    return exile.run(cleanPlayer, args);

                case "invite":
                    try {
                        return invite.run(cleanPlayer, args);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                case "nation":
                    return nation.run(cleanPlayer, args);

                case "promote":
                    return promote.run(cleanPlayer, args);

                case "unclaim":
                    return unclaim.run(cleanPlayer, args);

                default:
                    return false;
            }
        }
        else return true;
    }
}