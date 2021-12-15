package com.github.dawsonvilamaa.nationsandvillagesplugin.commands;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class sell implements Command {
    /**
     * @param sender
     * @param args
     */
    public static boolean run(Player sender, String[] args) {
        if (args.length < 1) {
            return false;
        }
        if (args[0].equals("cancel")) {
            if (!Main.nationsManager.isPlayerChoosingMerchant(sender.getUniqueId()))
                sender.sendMessage(ChatColor.RED + "You do not have a sell order pending");
            else {
                Main.nationsManager.getPlayersChoosingMerchants().remove(sender.getUniqueId());
                sender.sendMessage(ChatColor.YELLOW + "Canceled your sell order");
            }
            return true;
        }
        if (sender.getInventory().getItemInMainHand().getType() == Material.AIR) {
            sender.sendMessage(ChatColor.RED + "You must be holding something in your hand to sell");
            return true;
        }
        try {
            int price = Integer.parseInt(args[0]);
            ItemStack item = sender.getInventory().getItemInMainHand();
            String itemName = item.getType().name();
            ShopItem shopItem = new ShopItem(item.clone(), price, item.getAmount(), sender.getUniqueId());
            sender.sendMessage(ChatColor.GREEN + "You are selling " + ChatColor.BOLD + "" + item.getAmount() + "x " + itemName + ChatColor.RESET + "" + ChatColor.GREEN + " for " + ChatColor.BOLD + "$" + shopItem.getPrice());
            sender.sendMessage(ChatColor.GREEN + "Interact with the merchant you want to sell this item through");
            sender.playSound(sender.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.7f);

            //give player a minute to choose a villager to sell item through
            ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(1);
            Main.nationsManager.getPlayersChoosingMerchants().put(sender.getUniqueId(), new AbstractMap.SimpleEntry(shopItem, executorService));
            executorService.schedule(() -> {
                Map.Entry<ShopItem, ScheduledThreadPoolExecutor> entry = Main.nationsManager.getPlayersChoosingMerchants().get(sender.getUniqueId());
                if (entry.getKey().equals(shopItem) && entry.getValue().equals(executorService)) {
                    sender.sendMessage(ChatColor.RED + "You ran out of time to give your item to a merchant");
                    Main.nationsManager.getPlayersChoosingMerchants().remove(sender.getUniqueId());
                }
            }, 1, TimeUnit.MINUTES);
        }
        catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
