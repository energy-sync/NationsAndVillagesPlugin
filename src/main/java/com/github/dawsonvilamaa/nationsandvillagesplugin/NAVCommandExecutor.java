package com.github.dawsonvilamaa.nationsandvillagesplugin;

import com.github.dawsonvilamaa.nationsandvillagesplugin.commands.money;
import com.github.dawsonvilamaa.nationsandvillagesplugin.commands.nation;
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
                case "money":
                case "balance":
                case "bal":
                    return money.run(cleanPlayer, cleanCmd, args);

                case "nation":
                    return nation.run(cleanPlayer, cleanCmd, args);

                default:
                    return false;
            }
        }
        else return true;
    }
}
