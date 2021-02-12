package com.github.dawsonvilamaa.nationsandvillagesplugin.listeners;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsPlayer;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.ShopItem;
import com.github.dawsonvilamaa.nationsandvillagesplugin.gui.InventoryGUI;
import com.github.dawsonvilamaa.nationsandvillagesplugin.gui.InventoryGUIButton;
import com.github.dawsonvilamaa.nationsandvillagesplugin.npcs.Merchant;
import com.github.dawsonvilamaa.nationsandvillagesplugin.npcs.NationsVillager;
import net.minecraft.server.v1_16_R3.EntityVillager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class NationsVillagerListener implements Listener {
    private Main plugin;
    private static String[] playerAttackMessages = {"Hey! Stop hurting me!", "Ow! Stop hurting me!", "Ouch! Stop hurting me!", "Hey! Stop hitting me!", "Ow! Stop hitting me!", "Ouch! Stop hitting me!", "Ow!", "Ouch!"};
    private static String[] zombieAttackMessages = {"Help! A zombie is attacking me!", "Someone help! A zombie is attacking me!", "Help! There's a zombie!", "Someone help! There's a zombie!", "Zombie!"};
    private static Random random = new Random();

    /**
     * @param plugin
     */
    public NationsVillagerListener(Main plugin) {
        this.plugin = plugin;
    }

    //distress messages from player and zombie attacks
    @EventHandler
    public void onTakeDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof CraftVillager) {
            if (random.nextBoolean()) {
                EntityVillager villager = ((CraftVillager) e.getEntity()).getHandle();
                NationsVillager nationsVillager = Main.nationsManager.getVillagers().get(villager.getUniqueID());
                if (e.getDamager() instanceof CraftPlayer)
                    nationsVillager.speakToPlayer((CraftPlayer) e.getDamager(), playerAttackMessages[random.nextInt(playerAttackMessages.length)]);
                else if (e.getDamager() instanceof CraftZombie)
                    nationsVillager.shout(zombieAttackMessages[random.nextInt(zombieAttackMessages.length)]);
            }
        }
    }

    //renames villager if renamed with name tag, or does click event otherwise
    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof CraftVillager) {
            Player player = e.getPlayer();
            EntityVillager villager = ((CraftVillager) e.getRightClicked()).getHandle();
            NationsVillager nationsVillager = Main.nationsManager.getVillagers().get(villager.getUniqueID());
            ItemStack item = player.getInventory().getItemInMainHand();
            NationsPlayer nationsPlayer = Main.nationsManager.getPlayerByUUID(player.getUniqueId());
            CraftVillager craftVillager = (CraftVillager) e.getRightClicked();
            boolean doOnClick = false;

            if (e.getHand().equals(EquipmentSlot.HAND)) {
                //rename villager with name tag
                if (item.getType() == Material.NAME_TAG && item.getItemMeta().hasDisplayName() == true)
                    nationsVillager.setName(item.getItemMeta().getDisplayName());

                //merchant stuff
                else if (nationsVillager instanceof Merchant) {
                    Merchant merchant = (Merchant) nationsVillager;
                    //add item to shop if this is a merchant and the player is the owner of the shop
                    if (Main.nationsManager.isPlayerChoosingMerchant(player.getUniqueId()) && merchant.getShop().getOwnerUUID().equals(player.getUniqueId())) {
                        if (merchant.getShop().isFull())
                            player.sendMessage(ChatColor.RED + "This merchant cannot hold any more items");
                        else {
                            ItemStack soldItem = Main.nationsManager.getPlayersChoosingMerchants().get(player.getUniqueId()).getKey().getItem();
                            //remove item from inventory
                            boolean itemInInventory = false;
                            if (item.getType() != Material.AIR
                                    && item.getType() == soldItem.getType()
                                    && item.getAmount() == soldItem.getAmount()
                                    && item.getItemMeta().getDisplayName().equals(soldItem.getItemMeta().getDisplayName())
                                    && item.getEnchantments().equals(soldItem.getEnchantments())) {
                                player.getInventory().setItemInMainHand(null);
                                itemInInventory = true;
                            }
                            else {
                                for (int i = player.getInventory().getContents().length - 1; i >= 0; i--) {
                                    ItemStack currentItem = player.getInventory().getItem(i);
                                    if (currentItem != null
                                            && currentItem.getType() == soldItem.getType()
                                            && currentItem.getAmount() == soldItem.getAmount()
                                            && currentItem.getItemMeta().getDisplayName().equals(soldItem.getItemMeta().getDisplayName())
                                            && currentItem.getEnchantments().equals(soldItem.getEnchantments())) {
                                        player.getInventory().setItem(i, null);
                                        itemInInventory = true;
                                        break;
                                    }
                                }
                            }
                            if (itemInInventory == true) {
                                merchant.getShop().addItem(Main.nationsManager.getPlayersChoosingMerchants().get(player.getUniqueId()).getKey());
                                Main.nationsManager.getPlayersChoosingMerchants().remove(player.getUniqueId());
                                merchant.getShop().getInventoryGUI(player).showMenu();
                                player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.5f);
                                e.setCancelled(true);
                            }
                        }
                    }
                    else doOnClick(e);
                }

                //assign job menu if player is in same nation
                else if (nationsVillager.getNationID() == nationsPlayer.getNationID() && craftVillager.getProfession() == Villager.Profession.NONE) {
                    e.setCancelled(true);
                    assignJobMenu(e);
                }

                //other cases of custom interact event
                else if ((nationsVillager.getJob() != NationsVillager.Jobs.NONE && nationsVillager.getOnClick() != null) || doOnClick)
                    doOnClick(e);
            }
        }
    }

    //drops all items if villager is a merchant
    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if (e.getEntity() instanceof CraftVillager) {
            EntityVillager villager = ((CraftVillager) e.getEntity()).getHandle();
            NationsVillager nationsVillager = Main.nationsManager.getVillagers().get(villager.getUniqueID());
            if (nationsVillager instanceof Merchant) {
                for (ShopItem shopItem : ((Merchant) nationsVillager).getShop().getItems()) {
                    e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), shopItem.getItem());
                }
            }
        }
    }

    private void doOnClick(PlayerInteractEntityEvent e) {
        e.setCancelled(true);
        Main.nationsManager.getVillagerByUUID(e.getRightClicked().getUniqueId()).onClick(e);
    }

    //GUI for assigning a job to a villager
    public void assignJobMenu(PlayerInteractEntityEvent e) {
        Player player = e.getPlayer();
        NationsVillager nationsVillager = Main.nationsManager.getVillagerByUUID(e.getRightClicked().getUniqueId());
        InventoryGUI gui = new InventoryGUI(player, "Assign Job to " + nationsVillager.getName(), 1);
        //merchant button
        InventoryGUIButton merchantButton = new InventoryGUIButton(gui, "Merchant", "Sell items to other players through a merchant", Material.EMERALD);
        merchantButton.setOnClick(f -> {
            Main.nationsManager.getVillagers().put(nationsVillager.getUniqueID(), new Merchant(nationsVillager.getUniqueID(), player.getUniqueId()));
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.7f);
            player.sendMessage(ChatColor.GREEN + "Assigned this villager to be a merchant");
            player.sendMessage(ChatColor.GREEN + "Use /sell to sell items through merchants");
            player.closeInventory();
        });
        gui.addButton(merchantButton);

        //lumberjack button
        InventoryGUIButton lumberjackButton = new InventoryGUIButton(gui, "Lumberjack", "Gets wood from nearby trees", Material.IRON_AXE);
        lumberjackButton.setOnClick(f -> {

        });
        gui.addButton(lumberjackButton);

        //farmer button
        InventoryGUIButton farmerButton = new InventoryGUIButton(gui, "Farmer", "Farms nearby crops", Material.WHEAT);
        farmerButton.setOnClick(f -> {

        });
        gui.addButton(farmerButton);

        //miner button
        InventoryGUIButton minerButton = new InventoryGUIButton(gui, "Miner", "Mines stuff", Material.IRON_PICKAXE);
        minerButton.setOnClick(f -> {

        });
        gui.addButton(minerButton);

        //guard button
        InventoryGUIButton guardButton = new InventoryGUIButton(gui, "Guard", "Defends from mobs and enemy players", Material.IRON_SWORD);
        guardButton.setOnClick(f -> {

        });
        gui.addButton(guardButton);

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 4);
        gui.showMenu();
    }
}
