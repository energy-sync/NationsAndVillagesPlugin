package com.github.dawsonvilamaa.nationsandvillagesplugin.classes;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.NationsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.util.Consumer;

import java.util.ArrayList;
import java.util.HashMap;

public class InventoryGUI implements Listener, InventoryHolder {

    private Player player;
    private String name;
    private Inventory inventory;
    private ArrayList<InventoryGUIButton> buttons;
    private int slot;
    private int maxItems;

    public InventoryGUI() {

    }

    /**
     * Creates an inventory GUI with a given name and number of rows. Rows must be a value from 1-6
     * @param player
     * @param name
     * @param rows
     */
    public InventoryGUI(Player player, String name, int rows) {
        this.player = player;
        this.name = name;
        if (rows < 1) rows = 1;
        if (rows > 6) rows = 6;
        this.inventory = Bukkit.createInventory(null, 9 * rows, name);
        this.buttons = new ArrayList<>();
        this.slot = 0;
        this.maxItems = (9 * rows) - 1;
        Bukkit.getPluginManager().registerEvents(this, Main.plugin);
    }

    /**
     * @return name
     */
    public String getName() {
        return this.name;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    /**
     * Adds an item to the inventory with a given name, description, and material
     * @param button
     * @return button
     */
    public InventoryGUIButton addButton(InventoryGUIButton button) {
        //create item
        if (this.slot <= this.maxItems) {
            this.inventory.setItem(slot, button.getItem());
            this.slot++;
            this.buttons.add(button);
        }
        return button;
    }

    /**
     * Adds an item to the inventory with a given name, description, and material. Also can attach an event to the button when clicked
     * @param button
     * @param consumer
     * @return button
     */
    public InventoryGUIButton addButton(InventoryGUIButton button, Consumer<InventoryClickEvent> consumer) {
        //create item
        if (this.slot <= this.maxItems) {
            this.inventory.setItem(slot, button.getItem());
            this.slot++;
            this.buttons.add(button);
            Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("NationsAndVillagesPlugin"));
        }
        return button;
    }

    /**
     * Adds an item to the inventory with a given name, description, and material. Multiple of the same item can be added using this method.
     * @param button
     * @param amount
     * @return
     */
    public void addButton(InventoryGUIButton button, int amount) {
        for (int i = 0; i < amount; i++)
            addButton(button);
    }

    /**
     * Opens this GUI for a given player
     * @param player
     */
    public void showMenu(Player player) {
        player.openInventory(this.inventory);
    }

    @EventHandler
    public void onItemClick(InventoryClickEvent e) {
        //check if item clicked is actually an item
        if (e.getWhoClicked().equals(this.player) && e.getCurrentItem() != null) {
            for (InventoryGUIButton button : this.buttons) {
                if (button.getName().equals(e.getCurrentItem().getItemMeta().getDisplayName())) {
                    button.onClick(e);
                }
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getPlayer().equals(this.player))
            HandlerList.unregisterAll(this);
    }
}