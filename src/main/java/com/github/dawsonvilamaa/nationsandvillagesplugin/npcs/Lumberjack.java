package com.github.dawsonvilamaa.nationsandvillagesplugin.npcs;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsPlayer;
import com.github.dawsonvilamaa.nationsandvillagesplugin.gui.InventoryGUI;
import com.github.dawsonvilamaa.nationsandvillagesplugin.gui.InventoryGUIButton;
import com.github.dawsonvilamaa.nationsandvillagesplugin.listeners.NationsVillagerListener;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftItem;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

public class Lumberjack extends NationsVillager {
    static final int RADIUS = 20;
    static final int MIN_RADIUS = 4;

    private Inventory inventory;
    private BukkitTask runnable;

    /**
     * @param uuid
     * @param nationID
     */
    public Lumberjack(UUID uuid, int nationID) {
        super(uuid);
        setName("Lumberjack");
        setNationID(nationID);
        setJob(Job.LUMBERJACK);
        this.inventory = Bukkit.createInventory((InventoryHolder) Bukkit.getEntity(getUniqueID()), 54, "Lumberjack");
        this.runnable = null;
        setOnClick(e -> {
            NationsPlayer nationsPlayer = Main.nationsManager.getPlayerByUUID(e.getPlayer().getUniqueId());
            //check if player is in same nation as villager
            if (nationsPlayer.getNationID() == getNationID())
                lumberjackOptionsMenu(e.getPlayer(), e);
            e.setCancelled(true);
        });
        startJob();
    }

    /**
     * @param jsonLumberjack
     */
    public Lumberjack(JSONObject jsonLumberjack) {
        super(jsonLumberjack);
        this.inventory = Bukkit.createInventory((InventoryHolder) Bukkit.getEntity(getUniqueID()), 54);
        this.runnable = null;
        Iterator<JSONObject> iterator = ((JSONArray) jsonLumberjack.get("inventory")).iterator();
        while (iterator.hasNext()) {
            JSONObject jsonItem = iterator.next();
            Material material = Material.valueOf(jsonItem.get("itemMaterial").toString());
            ItemStack item = new ItemStack(material);
            item.setAmount(Integer.parseInt(jsonItem.get("itemAmount").toString()));
            item.getItemMeta().setDisplayName(jsonItem.get("itemDisplayName").toString());
            HashMap<Enchantment, Integer> enchantments = new HashMap<>();
            JSONArray jsonEnchantments = new JSONArray();
            Iterator enchantmentIterator = jsonEnchantments.iterator();
            while (enchantmentIterator.hasNext()) {
                JSONObject jsonEnchantment = (JSONObject) enchantmentIterator.next();
                enchantments.put(Enchantment.getByKey(NamespacedKey.minecraft(jsonEnchantment.get("name").toString())), Integer.parseInt(jsonEnchantment.get("level").toString()));
            }
            item.addEnchantments(enchantments);
            this.inventory.addItem(item);
        }
        setOnClick(e -> {
            NationsPlayer nationsPlayer = Main.nationsManager.getPlayerByUUID(e.getPlayer().getUniqueId());
            //check if player is in same nation as villager
            if (nationsPlayer.getNationID() == getNationID())
                lumberjackOptionsMenu(e.getPlayer(), e);
            e.setCancelled(true);
        });
        startJob();
    }

    /**
     * @return inventory
     */
    public Inventory getInventory() {
        return this.inventory;
    }

    /**
     * Starts the lumberjack's job of chopping down all trees within RADIUS blocks
     */
    public void startJob() {
        this.runnable = new BukkitRunnable() {
            @Override
            public void run() {
                Entity entity = Bukkit.getEntity(getUniqueID());
                if (entity != null && entity.getLocation().getChunk().isLoaded()) {
                    //finds closest tree to lumberjack, which is at least one log connected to at least one leaf block
                    boolean isTree = false;
                    Location entityLoc = entity.getLocation();
                    Block closestBlock = null;
                    double closestDist = -1;
                    for (int x = -RADIUS; x <= RADIUS; x++) {
                        for (int y = -RADIUS; y <= RADIUS; y++) {
                            for (int z = -RADIUS; z <= RADIUS; z++) {
                                Location blockLoc = new Location(entity.getWorld(), x + entityLoc.getBlockX(), y + entityLoc.getBlockY(), z + entityLoc.getBlockZ());
                                String blockMaterialStr = blockLoc.getBlock().getType().toString();
                                if (blockMaterialStr.contains("LOG")) {
                                    //check for leaves
                                    for (int a = -1; a <= 1; a++) {
                                        for (int b = -1; b <= 1; b++) {
                                            for (int c = -1; c <= 1; c++) {
                                                if (isTree == false) {
                                                    Location leafBlockLoc = new Location(entity.getWorld(), blockLoc.getBlockX() + a, blockLoc.getBlockY(), blockLoc.getBlockZ() + c);
                                                    String leafBlockMaterialStr = entity.getWorld().getBlockAt(leafBlockLoc).getType().toString();
                                                    if (leafBlockMaterialStr.contains("LEAVES"))
                                                        isTree = true;
                                                }
                                            }
                                        }
                                    }
                                    if (isTree == true) {
                                        //find closest log
                                        double dist = dist(entity.getLocation(), blockLoc);
                                        if (closestBlock == null || dist < closestDist) {
                                            closestBlock = blockLoc.getBlock();
                                            closestDist = dist;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isTree == true) {
                        //finds root log
                        Block root = closestBlock;
                        HashSet<Block> saplings = new HashSet<>();
                        boolean done = false;
                        if (closestBlock != null) {
                            while (!done) {
                                Block newRoot = null;
                                int y = root.getY() - 1;
                                Location blockLoc = root.getLocation();
                                for (int x = blockLoc.getBlockX() - 1; x <= blockLoc.getBlockX() + 1; x++) {
                                    for (int z = blockLoc.getBlockZ() - 1; z <= blockLoc.getBlockZ() + 1; z++) {
                                        Block pRoot = root.getWorld().getBlockAt(x, y, z);
                                        if (pRoot.getType().toString().contains("LOG"))
                                            newRoot = pRoot;
                                    }
                                }
                                if (newRoot != null)
                                    root = newRoot;
                                else done = true;
                            }

                            //set sapling blocks
                            Location rootLoc = root.getLocation();
                            int y = rootLoc.getBlockY();
                            for (int x = rootLoc.getBlockX() - 1; x <= rootLoc.getBlockX() + 1; x++) {
                                for (int z = root.getZ() - 1; z <= rootLoc.getBlockZ() + 1; z++) {
                                    Block block = root.getWorld().getBlockAt(x, y, z);
                                    if (root.getWorld().getBlockAt(x, y, z).getType().toString().contains("LOG"))
                                        saplings.add(block);
                                }
                            }

                            //cut down tree
                            if (closestDist <= MIN_RADIUS) {
                                Block block = closestBlock;
                                String blockType = block.getType().toString();
                                treecapitate(closestBlock, new HashSet<>());
                                //plant sapling
                                HashSet<Block> finalSaplings = saplings;
                                new BukkitRunnable() {
                                    public void run() {
                                        for (Block root : finalSaplings) {
                                            Material saplingType;
                                            if (blockType.equals("DARK_OAK_LOG"))
                                                saplingType = Material.DARK_OAK_SAPLING;
                                            else saplingType = Material.valueOf(blockType.substring(0, blockType.indexOf("_") + 1) + "SAPLING");
                                            root.setType(saplingType);
                                            root.getWorld().playSound(block.getLocation(), Sound.BLOCK_GRASS_PLACE, 0.4f, 0.8f);
                                        }
                                    }
                                }.runTaskLater(Main.plugin, 20);
                            }
                            else walkToLocation(closestBlock.getLocation());
                        }
                    }

                    //pick up items
                    else {
                        ArrayList<Entity> entities = (ArrayList<Entity>) entity.getNearbyEntities(RADIUS, RADIUS, RADIUS);
                        CraftItem closestItem = null;
                        double closestItemDist = -1;
                        //find closest item
                        for (Entity e : entities) {
                            if (e.getType() == EntityType.DROPPED_ITEM) {
                                CraftItem item = (CraftItem) e;
                                Material material = item.getItemStack().getType();
                                if (material.toString().contains("LOG") || material.toString().contains("SAPLING") || material == Material.STICK || material == Material.APPLE) {
                                    double dist = dist(entity.getLocation(), e.getLocation());
                                    if (closestItem == null || dist < closestItemDist) {
                                        closestItem = item;
                                        closestItemDist = dist;
                                    }
                                }
                            }
                        }

                        //pick up or walk to item
                        if (closestItem != null) {
                            if (closestItemDist <= 2) {
                                HashMap<Integer, ItemStack> leftoverItems = inventory.addItem(closestItem.getItemStack());
                                if (leftoverItems.values().size() == 0) {
                                    closestItem.getWorld().playSound(closestItem.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.7f, 1.0f);
                                    closestItem.remove();
                                }
                            }
                            else walkToLocation(closestItem.getLocation());
                        }
                    }
                }
            }
        }.runTaskTimer(Main.plugin, 20, 5);
    }

    /**
     * Stops the lumberjack from cutting down trees
     */
    public void stopJob() {
        this.runnable.cancel();
        Entity entity = Bukkit.getEntity(getUniqueID());
        for (ItemStack item : this.inventory.getContents())
            if (item != null)
                entity.getWorld().dropItemNaturally(entity.getLocation(), item);
    }

    /**
     * Breaks all logs and leaves around the starting block and recursively calls for each log or leaf block around it
     * @param block
     * @param brokenBlocks
     */
    public void treecapitate(Block block, HashSet<Location> brokenBlocks) {
        String blockType = block.getType().toString();
        if (blockType.indexOf("LOG") != -1)
            block.getWorld().playSound(block.getLocation(), Sound.BLOCK_WOOD_BREAK, 0.5f, 1.0f);
        ItemStack blockItemStack = new ItemStack(block.getType());
        block.getWorld().spawnParticle(Particle.BLOCK_CRACK, block.getLocation().getBlockX() + 0.5, block.getLocation().getBlockY() + 0.5, block.getLocation().getBlockZ() + 0.5, 12, block.getBlockData());
        block.breakNaturally(blockItemStack);
        brokenBlocks.add(block.getLocation());
        HashSet<Block> nextBlocks = new HashSet<>();
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    Block nextBlock = block.getRelative(x, y, z);
                    String nextBlockType = nextBlock.getType().toString();
                    if (nextBlockType.indexOf("LOG") != -1)
                        nextBlocks.add(nextBlock);
                }
            }
        }

        for (Block nextBlock : nextBlocks) {
            new BukkitRunnable() {
                public void run() {
                    if (!brokenBlocks.contains(nextBlock.getLocation()))
                        treecapitate(nextBlock, brokenBlocks);
                    cancel();
                }
            }.runTaskLater(Main.plugin, 1);
        }
    }

    /**
     * Returns the items in the villagers inventory in JSON formate
     * @return jsonInventory
     */
    public JSONArray inventoryToJSON() {
        JSONArray jsonInventory = new JSONArray();
        for (ItemStack item : this.inventory) {
            if (item != null) {
                JSONObject jsonItem = new JSONObject();
                jsonItem.put("itemMaterial", item.getType().toString());
                jsonItem.put("itemAmount", String.valueOf(item.getAmount()));
                jsonItem.put("itemDisplayName", item.getItemMeta().getDisplayName());
                JSONArray jsonEnchantments = new JSONArray();
                for (Enchantment enchantment : item.getEnchantments().keySet()) {
                    JSONObject jsonEnchantment = new JSONObject();
                    String enchantmentKey = enchantment.getKey().toString();
                    jsonEnchantment.put("name", enchantmentKey.substring(enchantmentKey.indexOf("") + 1));
                    jsonEnchantment.put("level", String.valueOf(item.getEnchantments().get(enchantment)));
                    jsonEnchantments.add(jsonEnchantment);
                }
                jsonItem.put("enchantments", jsonEnchantments);
                jsonInventory.add(jsonItem);
            }
        }
        return jsonInventory;
    }

    /**
     * @param loc1
     * @param loc2
     * @return distance
     */
    private double dist(Location loc1, Location loc2) {
        return Math.sqrt(Math.pow(loc2.getX() - loc1.getX(), 2) + Math.pow(loc2.getY() - loc1.getY(), 2) + Math.pow(loc2.getZ() - loc1.getZ(), 2));
    }

    //Menus

    //GUI for opening lumberjack's inventory or assigning the villager a different job
    public void lumberjackOptionsMenu(Player player, PlayerInteractEntityEvent e) {
        InventoryGUI gui = new InventoryGUI(player, "Lumberjack Options", 1, true);

        //open inventory button
        InventoryGUIButton openInventoryButton = new InventoryGUIButton(gui, "Open inventory", null, Material.CHEST);
        openInventoryButton.setOnClick(f -> {
            player.openInventory(this.inventory);
        });
        gui.addButton(openInventoryButton);

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
