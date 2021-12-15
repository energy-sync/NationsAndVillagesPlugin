package com.github.dawsonvilamaa.nationsandvillagesplugin.listeners;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsPlayer;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.ShopItem;
import com.github.dawsonvilamaa.nationsandvillagesplugin.gui.InventoryGUI;
import com.github.dawsonvilamaa.nationsandvillagesplugin.gui.InventoryGUIButton;
import com.github.dawsonvilamaa.nationsandvillagesplugin.npcs.Guard;
import com.github.dawsonvilamaa.nationsandvillagesplugin.npcs.Lumberjack;
import com.github.dawsonvilamaa.nationsandvillagesplugin.npcs.Merchant;
import com.github.dawsonvilamaa.nationsandvillagesplugin.npcs.NationsVillager;
import net.minecraft.server.v1_16_R3.EntityVillager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftZombie;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
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
            NationsVillager nationsVillager = Main.nationsManager.getVillagerByUUID(e.getEntity().getUniqueId());

            EntityType damagerEntityType = e.getDamager().getType();
            if (random.nextBoolean() && Main.nationsManager.getVillagerByUUID(e.getEntity().getUniqueId()).getJob() != NationsVillager.Job.GUARD) {
                //shout for help if attacked by a player or zombie
                if (e.getDamager() instanceof CraftPlayer)
                    nationsVillager.speakToPlayer((CraftPlayer) e.getDamager(), playerAttackMessages[random.nextInt(playerAttackMessages.length)]);
                else if (e.getDamager() instanceof CraftZombie)
                    nationsVillager.shout(zombieAttackMessages[random.nextInt(zombieAttackMessages.length)]);
            }

            //health tag
            nationsVillager.updateNameTag();
        }
    }

    //updates health in name tag when healed
    @EventHandler
    public void onGainHealthEvent(EntityRegainHealthEvent e) {
        if (e.getEntity() instanceof CraftVillager) {
            NationsVillager nationsVillager = Main.nationsManager.getVillagerByUUID(e.getEntity().getUniqueId());
            nationsVillager.updateNameTag();
        }
    }

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

                //assign job menu if player is in same nation and does not have a job
                else if (nationsPlayer.getNationID() != -1 && nationsVillager.getNationID() == nationsPlayer.getNationID() && craftVillager.getProfession() == Villager.Profession.NONE && nationsVillager.getJob() == NationsVillager.Job.NONE) {
                    e.setCancelled(true);
                    assignJobMenu(e);
                }

                //other cases of custom interact event
                else if ((nationsVillager.getJob() != NationsVillager.Job.NONE && nationsVillager.getOnClick() != null) || doOnClick)
                    doOnClick(e);
            }
        }
    }

    //drops all items if villager is a merchant, lumberjack, or guard
    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        Entity entity = e.getEntity();
        if (entity instanceof CraftVillager) {
            EntityVillager villager = ((CraftVillager) entity).getHandle();
            NationsVillager nationsVillager = Main.nationsManager.getVillagerByUUID(villager.getUniqueID());
            nationsVillager.stopRunnable();
            endJob(nationsVillager);
            if (nationsVillager instanceof Merchant) {
                for (ShopItem shopItem : ((Merchant) nationsVillager).getShop().getItems())
                    entity.getWorld().dropItemNaturally(entity.getLocation(), shopItem.getItem());
            }
            else if (nationsVillager instanceof Lumberjack) {
                ((Lumberjack) nationsVillager).stopJob();
                for (ItemStack item : ((Lumberjack) nationsVillager).getInventory().getContents()) {
                    if (item != null)
                        entity.getWorld().dropItemNaturally(entity.getLocation(), item);
                }
            }
            else if (nationsVillager instanceof Guard) {
                Guard guard = (Guard) nationsVillager;

                //skeletons and strays still attack the location of the guard after it dies, so manually set attack targets to null
                for (Entity enemyEntity : e.getEntity().getNearbyEntities(NationsVillager.ENEMY_DETECTION_RANGE, NationsVillager.ENEMY_DETECTION_RANGE, NationsVillager.ENEMY_DETECTION_RANGE)) {
                    if (enemyEntity.getType() == EntityType.SKELETON && ((Monster) enemyEntity).getTarget() != null && ((Monster) enemyEntity).getTarget().equals(entity))
                        ((Monster) enemyEntity).setTarget(null);
                }
            }
        }
    }

    private void doOnClick(PlayerInteractEntityEvent e) {
        e.setCancelled(true);
        Main.nationsManager.getVillagerByUUID(e.getRightClicked().getUniqueId()).onClick(e);
    }

    //GUI for assigning a job to a villager
    public static void assignJobMenu(PlayerInteractEntityEvent e) {
        Player player = e.getPlayer();
        NationsVillager nationsVillager = Main.nationsManager.getVillagerByUUID(e.getRightClicked().getUniqueId());
        InventoryGUI gui = new InventoryGUI(player, "Assign Job to " + nationsVillager.getName(), 1, true);

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 2);

        //merchant button
        InventoryGUIButton merchantButton = new InventoryGUIButton(gui, "Merchant", "Sell items to other players through a merchant", Material.EMERALD);
        merchantButton.setOnClick(f -> {
            if (nationsVillager.getJob() != NationsVillager.Job.MERCHANT) {
                endJob(nationsVillager);
                Main.nationsManager.getVillagers().put(nationsVillager.getUniqueID(), new Merchant(nationsVillager.getUniqueID(), player.getUniqueId()));
                player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.7f);
                player.sendMessage(ChatColor.GREEN + "Assigned this villager to be a merchant");
                player.sendMessage(ChatColor.GREEN + "Use /sell to sell items through merchants");
                player.closeInventory();
            }
        });
        gui.addButton(merchantButton);

        //lumberjack button
        InventoryGUIButton lumberjackButton = new InventoryGUIButton(gui, "Lumberjack", "Gets wood from nearby trees", Material.IRON_AXE);
        lumberjackButton.setOnClick(f -> {
            if (nationsVillager.getJob() != NationsVillager.Job.LUMBERJACK) {
                endJob(nationsVillager);
                Main.nationsManager.getVillagers().put(nationsVillager.getUniqueID(), new Lumberjack(nationsVillager.getUniqueID(), nationsVillager.getNationID()));
                player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.7f);
                player.sendMessage(ChatColor.GREEN + "Assigned this villager to be a lumberjack");
                player.closeInventory();
            }
        });
        gui.addButton(lumberjackButton);

        //guard button
        InventoryGUIButton guardButton = new InventoryGUIButton(gui, "Guard", "Defends from mobs and enemy players", Material.IRON_SWORD);
        guardButton.setOnClick(f -> {
            if (nationsVillager.getJob() != NationsVillager.Job.GUARD) {
                endJob(nationsVillager);
                Main.nationsManager.getVillagers().put(nationsVillager.getUniqueID(), new Guard(nationsVillager.getUniqueID(), nationsVillager.getNationID(), e.getRightClicked().getLocation()));
                player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.7f);
                player.sendMessage(ChatColor.GREEN + "Assigned this villager to be a guard");
                player.closeInventory();
            }
        });
        gui.addButton(guardButton);

        //miner button
        InventoryGUIButton minerButton = new InventoryGUIButton(gui, "Miner", "Mines stuff", Material.IRON_PICKAXE);
        minerButton.setOnClick(f -> {

        });
        gui.addButton(minerButton);

        //farmer button
        InventoryGUIButton farmerButton = new InventoryGUIButton(gui, "Farmer", "Farms nearby crops", Material.WHEAT);
        farmerButton.setOnClick(f -> {

        });
        gui.addButton(farmerButton);

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 2);
        gui.showMenu();
    }

    /**
     * Makes villager drop all items in its inventory if it has any and stops villager from doing its job
     * @param nationsVillager
     */
    private static void endJob(NationsVillager nationsVillager) {
        switch (nationsVillager.getJob()) {
            case MERCHANT:
                ((Merchant) nationsVillager).stopJob();
            break;

            case LUMBERJACK:
                ((Lumberjack) nationsVillager).stopJob();
            break;

            case GUARD:
                ((Guard) nationsVillager).stopJob();
            break;
        }
    }
}
