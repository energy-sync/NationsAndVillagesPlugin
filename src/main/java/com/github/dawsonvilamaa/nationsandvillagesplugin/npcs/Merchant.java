package com.github.dawsonvilamaa.nationsandvillagesplugin.npcs;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsPlayer;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.Shop;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.ShopItem;
import com.github.dawsonvilamaa.nationsandvillagesplugin.gui.InventoryGUI;
import com.github.dawsonvilamaa.nationsandvillagesplugin.gui.InventoryGUIButton;
import com.github.dawsonvilamaa.nationsandvillagesplugin.listeners.NationsVillagerListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.json.simple.JSONObject;

import java.util.UUID;

public class Merchant extends NationsVillager {
    private Shop shop;

    public Merchant(UUID merchantUUID, UUID ownerUUID) {
        super(merchantUUID);
        setName("Merchant");
        setNationID(Main.nationsManager.getPlayerByUUID(ownerUUID).getNationID());
        setJob(Job.MERCHANT);
        this.shop = new Shop(ownerUUID, getUniqueID());
        setOnClick(e -> {
            NationsPlayer nationsPlayer = Main.nationsManager.getPlayerByUUID(e.getPlayer().getUniqueId());
            //check if player is the owner of the shop
            if (nationsPlayer.getUniqueID().equals(this.shop.getOwnerUUID()))
                merchantOptionsMenu(e.getPlayer(), e);
            else shop.getInventoryGUI(e.getPlayer()).showMenu();
        });
    }

    public Merchant(JSONObject jsonMerchant) {
        super(jsonMerchant);
        this.shop = new Shop((JSONObject) jsonMerchant.get("shop"));
        setOnClick(e -> {
            NationsPlayer nationsPlayer = Main.nationsManager.getPlayerByUUID(e.getPlayer().getUniqueId());
            //check if player is the owner of the shop
            if (nationsPlayer.getUniqueID().equals(this.shop.getOwnerUUID()))
                merchantOptionsMenu(e.getPlayer(), e);
            else shop.getInventoryGUI(e.getPlayer()).showMenu();
        });
    }

    /**
     * @return shop
     */
    public Shop getShop() {
        return this.shop;
    }

    //drops all the items in the shop
    public void stopJob() {
        Entity entity = Bukkit.getEntity(getUniqueID());
        for (ShopItem shopItem : this.shop.getItems())
            entity.getWorld().dropItemNaturally(entity.getLocation(), shopItem.getItem());
    }

    //Menus

    //GUI for manage existing items in the shop or assigning the villager a different job
    public void merchantOptionsMenu(Player player, PlayerInteractEntityEvent e) {
        InventoryGUI gui = new InventoryGUI(player, "Merchant Options", 1, true);

        //manage shop items button
        InventoryGUIButton manageShopButton = new InventoryGUIButton(gui, "Manage Shop Items", "Edit prices or remove items from the shop", Material.CHEST);
        manageShopButton.setOnClick(f -> {
            this.shop.getInventoryGUI(player).showMenu();
        });
        gui.addButton(manageShopButton);

        //assign job button
        InventoryGUIButton assignJobButton = new InventoryGUIButton(gui, "Assign Job", null, Material.IRON_PICKAXE);
        assignJobButton.setOnClick(f -> {
            NationsVillagerListener.assignJobMenu(e);
        });
        gui.addButton(assignJobButton);

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 7);
        gui.showMenu();
    }
}
