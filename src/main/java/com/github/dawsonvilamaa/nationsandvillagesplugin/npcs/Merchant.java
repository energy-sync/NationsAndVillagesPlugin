package com.github.dawsonvilamaa.nationsandvillagesplugin.npcs;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsPlayer;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.Shop;
import com.github.dawsonvilamaa.nationsandvillagesplugin.gui.InventoryGUI;
import com.github.dawsonvilamaa.nationsandvillagesplugin.gui.InventoryGUIButton;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import java.util.UUID;

public class Merchant extends NationsVillager {
    private Shop shop;

    public Merchant(UUID merchantUUID, UUID ownerUUID) {
        super(merchantUUID);
        setName("Merchant");
        setJob(Job.MERCHANT);
        this.shop = new Shop(ownerUUID, getUniqueID());
        CraftVillager entity = (CraftVillager) Bukkit.getEntity(getUniqueID());
        setOnClick(e -> {
            NationsPlayer nationsPlayer = Main.nationsManager.getPlayerByUUID(e.getPlayer().getUniqueId());
            //check if player is the owner of the shop
            if (nationsPlayer.getUniqueID().equals(this.shop.getOwnerUUID())) {
                merchantOptionsMenu(e.getPlayer());
            }
            else shop.getInventoryGUI(e.getPlayer()).showMenu();
        });
    }

    public Merchant(JSONObject jsonMerchant) {
        super(jsonMerchant);
        this.shop = new Shop((JSONObject) jsonMerchant.get("shop"));
        setOnClick(e -> {
            NationsPlayer nationsPlayer = Main.nationsManager.getPlayerByUUID(e.getPlayer().getUniqueId());
            //check if player is the owner of the shop
            if (nationsPlayer.getUniqueID().equals(this.shop.getOwnerUUID())) {
                merchantOptionsMenu(e.getPlayer());
            }
            else shop.getInventoryGUI(e.getPlayer()).showMenu();
        });
    }

    /**
     * @return shop
     */
    public Shop getShop() {
        return this.shop;
    }

    //Menus

    //GUI for manage existing items in the shop or assigning the villager a different job
    public void merchantOptionsMenu(Player player) {
        InventoryGUI gui = new InventoryGUI(player, "Merchant Options", 1);

        //manage shop items button
        InventoryGUIButton manageShopButton = new InventoryGUIButton(gui, "Manage Shop Items", "Edit prices or remove items from the shop", Material.CHEST);
        manageShopButton.setOnClick(e -> {
            this.shop.getInventoryGUI(player).showMenu();
        });
        gui.addButton(manageShopButton);

        //assign job button
        InventoryGUIButton assignJobButton = new InventoryGUIButton(gui, "Assign Job", null, Material.IRON_PICKAXE);
        assignJobButton.setOnClick(e -> {
            Bukkit.broadcastMessage("clicked on assign job");
        });
        gui.addButton(assignJobButton);

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 7);
        gui.showMenu();
    }
}
