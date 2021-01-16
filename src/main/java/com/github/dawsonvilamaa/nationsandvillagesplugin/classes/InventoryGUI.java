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
    private HashMap<Integer, Consumer<InventoryClickEvent>> events;

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
        this.events = new HashMap<>();
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
     * Adds an item to the inventory with a given name, description, and material. Multiple of the same item can be added using this method.
     * @param button
     * @param amount
     * @return
     */
    public void addButtons(InventoryGUIButton button, int amount) {
        for (int i = 0; i < amount; i++)
            addButton(button);
    }

    /**
     * @return slot
     */
    public int getSlot() {
        return this.slot;
    }

    /**
     * @return events
     */
    public HashMap<Integer, Consumer<InventoryClickEvent>> getEvents() {
        return this.events;
    }

    /**
     * Opens this GUI for a given player
     * @param player
     */
    public void showMenu(Player player) {
        player.openInventory(this.inventory);
    }

    /**
     * Removes all click events from the buttons in this GUI
     */
    public void removeAllClickEvents() {
        for (InventoryGUIButton button : this.buttons)
            button.setOnClick(null);
    }

    //prevent players from taking items in GUI menus
    @EventHandler
    public void onItemClick(InventoryClickEvent e) {
        //check if item clicked is actually an item
        if (e.getWhoClicked().equals(this.player) && e.getCurrentItem() != null) {
            for (InventoryGUIButton button : this.buttons) {
                if (button.getName() != null && button.getName().equals(e.getCurrentItem().getItemMeta().getDisplayName())) {
                    button.onClick(e);
                    e.setCancelled(true);
                }
            }
            e.setCancelled(true);
        }
    }

    //remove all click events on buttons when the menu is closed
    @EventHandler
    public void onMenuClose(InventoryCloseEvent e) {
        if (e.getView().getTitle().equals(this.name) && e.getPlayer().equals(this.player)) {
            for (InventoryGUIButton button : this.buttons)
                button.setOnClick(null);
        }
    }
}