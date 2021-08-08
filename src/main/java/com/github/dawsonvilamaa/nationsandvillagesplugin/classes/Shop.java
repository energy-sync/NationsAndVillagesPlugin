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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

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
     * @param jsonShop
     */
    public Shop(JSONObject jsonShop) {
        this.ownerUUID = UUID.fromString(jsonShop.get("ownerUUID").toString());
        this.villagerUUID = UUID.fromString(jsonShop.get("villagerUUID").toString());
        this.items = new ArrayList<>();
        JSONArray jsonItems = (JSONArray) jsonShop.get("items");
        Iterator<JSONObject> iterator = jsonItems.iterator();
        while (iterator.hasNext())
            this.items.add(new ShopItem(iterator.next()));
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
        InventoryGUI gui = new InventoryGUI(player, "Shop", 6, true);
        for (ShopItem shopItem : this.items) {
            InventoryGUIButton button = new InventoryGUIButton(gui, shopItem.getItem().getItemMeta().getDisplayName(), ChatColor.GREEN + "$" + shopItem.getPrice(), shopItem.getMaterial());
            button.getItem().addEnchantments(shopItem.getItem().getEnchantments());
            button.getItem().setAmount(shopItem.getAmount());
            //owner button behavior
            if (player.getUniqueId().equals(this.getOwnerUUID())) {
                button.setOnClick(e -> {
                    manageItemMenu(player, shopItem);
                });
            }
            //customer button behavior
            else {
                button.setOnClick(e -> {
                    if (Main.nationsManager.getPlayerByUUID(player.getUniqueId()).getMoney() < shopItem.getPrice()) {
                        player.sendMessage(ChatColor.RED + "You do not have enough money to purchase this item");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 1.0f, 0.5f);
                    }
                    else confirmBuyMenu(player, shopItem);
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

    //GUI for removing an item from the shop
    public void manageItemMenu(Player player, ShopItem shopItem) {
        InventoryGUI gui = new InventoryGUI(player, "Remove Item From Shop?", 1, true);
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
        previewButton.getItem().addEnchantments(shopItem.getItem().getEnchantments());
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

    //GUI for confirming the purchase of an item
    public void confirmBuyMenu(Player player, ShopItem shopItem) {
        InventoryGUI gui = new InventoryGUI(player, "Confirm Purchase", 1, true);
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
        previewButton.getItem().addEnchantments(shopItem.getItem().getEnchantments());
        gui.addButton(previewButton);

        gui.addButton(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE));

        //confirm button
        InventoryGUIButton confirmButton = new InventoryGUIButton(gui, ChatColor.GREEN + "" + ChatColor.BOLD + "Confirm", null, Material.LIME_STAINED_GLASS_PANE);
        confirmButton.setOnClick(e -> {
            ItemStack item = removeItem(shopItem);
            if (item != null) {
                player.getInventory().addItem(item);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                player.sendMessage(ChatColor.YELLOW + "You bought " + ChatColor.BOLD + shopItem.getAmount() + "x " + shopItem.getMaterial().name() + ChatColor.RESET + ChatColor.YELLOW + " for " + ChatColor.BOLD + "$" + shopItem.getPrice());
                player.closeInventory();
                Main.nationsManager.getPlayerByUUID(player.getUniqueId()).removeMoney(shopItem.getPrice());
                Main.nationsManager.getPlayerByUUID(shopItem.getSellerUUID()).addMoney(shopItem.getPrice());
                Player seller = Bukkit.getPlayer(shopItem.getSellerUUID());
                if (seller != null)
                    seller.sendMessage(ChatColor.GREEN + player.getName() + " bought " + ChatColor.BOLD + shopItem.getAmount() + "x " + shopItem.getMaterial().name() + ChatColor.RESET + ChatColor.GREEN + " for " + ChatColor.BOLD + "$" + shopItem.getPrice());
            }
        });
        gui.addButton(confirmButton);

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 2);

        gui.showMenu();
    }

    /**
     * Returns this Shop in JSON format
     * @return jsonShop
     */
    public JSONObject toJSON() {
        JSONObject jsonShop = new JSONObject();
        jsonShop.put("ownerUUID", this.ownerUUID.toString());
        jsonShop.put("villagerUUID", this.villagerUUID.toString());
        JSONArray jsonItems = new JSONArray();
        for (ShopItem shopItem : this.items)
            jsonItems.add(shopItem.toJSON());
        jsonShop.put("items", jsonItems);
        return jsonShop;
    }
}
