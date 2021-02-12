package com.github.dawsonvilamaa.nationsandvillagesplugin.classes;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.gui.InventoryGUI;
import com.github.dawsonvilamaa.nationsandvillagesplugin.gui.InventoryGUIButton;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class Shop {
    private UUID ownerUUID;
    private UUID villagerUUID;
    private ArrayList<ShopItem> items;

    /**
     * @param ownerUUID
     * @param villagerUUID
     */
    public Shop(UUID ownerUUID, UUID villagerUUID) {
        this.ownerUUID = ownerUUID;
        this.villagerUUID = villagerUUID;
        this.items = new ArrayList<>();
    }

    /**
     * @return ownerUUID
     */
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    /**
     * @return villagerUUID
     */
    public UUID getVillagerUUID() {
        return this.villagerUUID;
    }

    /**
     * @return ShopItems
     */
    public Collection<ShopItem> getItems() {
        return this.items;
    }

    /**
     * @param newItem
     */
    public void addItem(ShopItem newItem) {
        this.items.add(newItem);
    }

    /**
     * Removes an item from this shop and returns the ItemStack. Returns null if not removed
     * @param removedShopItem
     * @return item
     */
    public ItemStack removeItem(ShopItem removedShopItem) {
        for (int i = 0; i < this.items.size(); i++) {
            if (this.items.get(i) != null) {
                ItemStack item = this.items.get(i).getItem().clone();
                if (item.getType() == removedShopItem.getMaterial()
                        && item.getAmount() == removedShopItem.getAmount()
                        && item.getItemMeta().getDisplayName().equals(removedShopItem.getItem().getItemMeta().getDisplayName())
                        && item.getEnchantments().equals(removedShopItem.getItem().getEnchantments())) {
                    this.items.remove(i);
                    return item;
                }
            }
        }
        return null;
    }

    /**
     * Generates an InventoryGUI based on the items in this shop and returns it
     * @param player
     * @return
     */
    public InventoryGUI getInventoryGUI(Player player) {
        this.items.removeAll(Collections.singleton(null));
        InventoryGUI gui = new InventoryGUI(player, "Shop", 6);
        for (ShopItem item : this.items) {
            InventoryGUIButton button = new InventoryGUIButton(gui, item.getItem().getItemMeta().getDisplayName(), ChatColor.GREEN + "$" + item.getPrice(), item.getMaterial());
            button.getItem().setAmount(item.getAmount());
            //owner button behavior
            if (player.getUniqueId().equals(this.getOwnerUUID())) {
                button.setOnClick(e -> {
                    manageItemMenu(player, item);
                });
            }
            //customer button behavior
            else {
                button.setOnClick(e -> {
                    Main.nationsManager.getPlayerByUUID(player.getUniqueId()).addMoney(item.getPrice());
                    player.sendMessage(ChatColor.GREEN + "You have been paid $" + item.getPrice());
                });
            }
            gui.addButton(button);
        }
        return gui;
    }

    /**
     * Returns whether this shop is full or not
     * @return isFull
     */
    public boolean isFull() {
        return this.items.size() < 54 ? false : true;
    }

    //Menus

    public void manageItemMenu(Player player, ShopItem shopItem) {
        InventoryGUI gui = new InventoryGUI(player, "Remove Item From Shop?", 1);
        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 2);
        //cancel button
        InventoryGUIButton cancelButton = new InventoryGUIButton(gui, ChatColor.RED + "" + ChatColor.BOLD + "Cancel", null, Material.RED_STAINED_GLASS_PANE);
        cancelButton.setOnClick(e -> {
            getInventoryGUI(player).showMenu();
        });
        gui.addButton(cancelButton);

        gui.addButton(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE));

        //item preview
        InventoryGUIButton previewButton = new InventoryGUIButton(gui, shopItem.getItem().getItemMeta().getDisplayName(), ChatColor.GREEN + "$" + shopItem.getPrice(), shopItem.getMaterial());
        previewButton.getItem().setAmount(shopItem.getAmount());
        gui.addButton(previewButton);

        gui.addButton(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE));

        //confirm button
        InventoryGUIButton confirmButton = new InventoryGUIButton(gui, ChatColor.GREEN + "" + ChatColor.BOLD + "Confirm", "Remove this from the shop", Material.LIME_STAINED_GLASS_PANE);
        confirmButton.setOnClick(e -> {
            ItemStack item = removeItem(shopItem);
            if (item != null) {
                player.getInventory().addItem(item);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
            }
            getInventoryGUI(player).showMenu();
        });
        gui.addButton(confirmButton);

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 2);

        gui.showMenu();
    }
}
