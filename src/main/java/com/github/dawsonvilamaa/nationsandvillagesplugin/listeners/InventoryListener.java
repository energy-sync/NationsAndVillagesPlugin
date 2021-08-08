package com.github.dawsonvilamaa.nationsandvillagesplugin.listeners;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.gui.InventoryGUI;
import com.github.dawsonvilamaa.nationsandvillagesplugin.gui.InventoryGUIButton;
import com.github.dawsonvilamaa.nationsandvillagesplugin.npcs.Guard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class InventoryListener implements Listener {
    Main plugin;

    /**
     * @param plugin
     */
    public InventoryListener(Main plugin) {
        this.plugin = plugin;
    }

    //prevent players from taking items in GUI menus
    @EventHandler
    public void onItemClick(InventoryClickEvent e) {
        InventoryGUI gui = Main.nationsManager.getMenuByPlayerUUID(e.getWhoClicked().getUniqueId());
        if (gui != null && e.getWhoClicked().equals(gui.getPlayer()) && e.getCurrentItem() != null && e.getView().getTitle().equals(gui.getName())) {
            InventoryGUIButton button = gui.getButtons().get(e.getRawSlot());
            if (button != null) {
                if (button.getOnClick() != null)
                    button.onClick(e);
                if (button.isLocked())
                    e.setCancelled(true);
            }
            else if (gui.isLocked())
                e.setCancelled(true);
        }
    }

    //remove all click events on buttons when the menu is closed
    @EventHandler
    public void onMenuClose(InventoryCloseEvent e) {
        InventoryGUI gui = Main.nationsManager.getMenuByPlayerUUID(e.getPlayer().getUniqueId());
        if (gui != null && e.getView().getTitle().equals(gui.getName()) && e.getPlayer().equals(gui.getPlayer())) {
            if (e.getView().getTitle().equals("Equip Weapon and Armor")) {
                //update guard's equipment
                ((Guard) gui.getVillager()).confirmEquip((Player) e.getPlayer(), gui);
                //stop item check
                gui.stopRunnable();
            }
            Main.nationsManager.removeMenu(e.getPlayer().getUniqueId());
        }
    }
}
