package com.github.dawsonvilamaa.nationsandvillagesplugin.npcs;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsPlayer;
import com.github.dawsonvilamaa.nationsandvillagesplugin.gui.InventoryGUI;
import com.github.dawsonvilamaa.nationsandvillagesplugin.gui.InventoryGUIButton;
import com.github.dawsonvilamaa.nationsandvillagesplugin.listeners.NationsVillagerListener;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Guard extends NationsVillager {
    static final int RADIUS = 20;
    public enum GuardMode { STATIONARY, WANDER, BODYGUARD, FOLLOW };

    GuardMode guardMode;
    Location guardLocation;
    Player followPlayer;
    ItemStack weapon;
    ItemStack[] armor;
    int ticks;

    private BukkitTask runnable;

    /**
     * @param uuid
     * @param nationID
     */
    public Guard(UUID uuid, int nationID, Location guardLocation) {
        super(uuid);
        this.guardMode = GuardMode.STATIONARY;
        this.guardLocation = guardLocation;
        this.followPlayer = null;
        this.weapon = null;
        this.armor = new ItemStack[] { null, null, null, null };
        this.ticks = 0;
        this.runnable = null;
        setName("Guard");
        setNationID(nationID);
        setJob(Job.GUARD);
        setOnClick(e -> {
            NationsPlayer nationsPlayer = Main.nationsManager.getPlayerByUUID(e.getPlayer().getUniqueId());
            //check if player is in same nation as villager
            if (nationsPlayer.getNationID() == getNationID())
                guardOptionsMenu(e.getPlayer(), e);
            e.setCancelled(true);
        });
        startJob();
    }

    /**
     *
     * @param jsonGuard
     */
    public Guard(JSONObject jsonGuard) {
       super(jsonGuard);
       this.guardMode = GuardMode.STATIONARY;
       JSONObject jsonGuardLocation = (JSONObject) jsonGuard.get("guardLocation");
       if (jsonGuardLocation.equals("null"))
           this.guardLocation = null;
       else this.guardLocation = new Location(Bukkit.getWorld(jsonGuardLocation.get("world").toString()), Double.parseDouble(jsonGuardLocation.get("x").toString()), Double.parseDouble(jsonGuardLocation.get("y").toString()), Double.parseDouble(jsonGuardLocation.get("z").toString()));
       this.followPlayer = null;

       this.armor = new ItemStack[] { null, null, null, null };
       JSONObject jsonWeapon = (JSONObject) jsonGuard.get("weapon");
       if (jsonWeapon != null) {
           Material weaponType = Material.valueOf(jsonWeapon.get("material").toString());
           this.weapon = new ItemStack(weaponType);
           this.weapon.getItemMeta().setDisplayName(jsonWeapon.get("displayName").toString());
           Map<Enchantment, Integer> enchantments = new HashMap<>();
           JSONArray jsonEnchantments = (JSONArray) jsonWeapon.get("enchantments");
           Iterator<JSONObject> iterator = jsonEnchantments.iterator();
           while (iterator.hasNext()) {
               JSONObject jsonEnchantment = iterator.next();
               enchantments.put(Enchantment.getByKey(NamespacedKey.minecraft(jsonEnchantment.get("name").toString())), Integer.parseInt(jsonEnchantment.get("level").toString()));
           }
           this.weapon.addEnchantments(enchantments);
       }

       JSONArray jsonArmor = (JSONArray) jsonGuard.get("armor");
       for (int i = 0; i < 4; i++) {
           JSONObject jsonItem = (JSONObject) jsonArmor.get(i);
           if (jsonItem == null)
               this.armor[i] = null;
           else {
               Material armorType = Material.valueOf(jsonItem.get("material").toString());
               this.armor[i] = new ItemStack(armorType);
               this.armor[i].getItemMeta().setDisplayName(jsonItem.get("displayName").toString());
               Map<Enchantment, Integer> enchantments = new HashMap<>();
               JSONArray jsonEnchantments = (JSONArray) jsonItem.get("enchantments");
               Iterator<JSONObject> iterator = jsonEnchantments.iterator();
               while (iterator.hasNext()) {
                   JSONObject jsonEnchantment = iterator.next();
                   enchantments.put(Enchantment.getByKey(NamespacedKey.minecraft(jsonEnchantment.get("name").toString())), Integer.parseInt(jsonEnchantment.get("level").toString()));
               }
               this.armor[i].addEnchantments(enchantments);
           }
       }

       this.ticks = 0;
       this.runnable = null;
       setOnClick(e -> {
           NationsPlayer nationsPlayer = Main.nationsManager.getPlayerByUUID(e.getPlayer().getUniqueId());
           //check if player is in same nation as villager
           if (nationsPlayer.getNationID() == getNationID())
               guardOptionsMenu(e.getPlayer(), e);
           e.setCancelled(true);
       });
       startJob();
    }

    /**
     * @return guardLocation
     */
    public Location getGuardLocation() {
        return this.guardLocation;
    }

    /**
     * @param guardLocation
     */
    public void setGuardLocation(Location guardLocation) {
        this.guardLocation = guardLocation;
    }

    /**
     * @return guardMode
     */
    public GuardMode getGuardMode() {
        return this.guardMode;
    }

    /**
     * @param guardMode
     */
    public void setGuardMode(GuardMode guardMode) {
        this.guardMode = guardMode;
    }

    /**
     * @return followPlayer
     */
    public Player getFollowPlayer() {
        return this.followPlayer;
    }

    /**
     * @param followPlayer
     */
    public void setFollowPlayer(Player followPlayer) {
        this.followPlayer = followPlayer;
    }

    /**
     * @return weapon
     */
    public ItemStack getWeapon() {
        return this.weapon;
    }

    /**
     * @param weapon
     */
    public void setWeapon(ItemStack weapon) {
        this.weapon = weapon;
    }

    /**
     * @return armor
     */
    public ItemStack[] getArmor() {
        return this.armor;
    }

    /**
     * Stars the guard's job of attacking hostile mobs
     */
    public void startJob() {
        final CraftVillager[] villager = {(CraftVillager) Bukkit.getEntity(getUniqueID())};
        this.runnable = new BukkitRunnable() {
            @Override
            public void run() {
                Entity vEntity = Bukkit.getEntity(getUniqueID());
                if (vEntity != null && vEntity.getLocation().getChunk().isLoaded()) {
                    ticks++;
                    //regenerate health
                    if (villager[0] == null)
                        villager[0] = (CraftVillager) Bukkit.getEntity(getUniqueID());
                    if (villager[0] != null) {
                        if (villager[0].getHealth() < 20 && ticks % 8 == 0) {
                            if (villager[0].getMaxHealth() - villager[0].getHealth() < 0.5)
                                villager[0].setHealth(villager[0].getMaxHealth());
                            else villager[0].setHealth(villager[0].getHealth() + 0.5);
                        }
                        Main.nationsManager.getVillagerByUUID(villager[0].getUniqueId()).updateHealthTag();

                        //follow mode
                        if (guardMode == GuardMode.FOLLOW && distance(villager[0].getLocation(), followPlayer.getLocation()) > 3)
                            walkToLocation(followPlayer.getLocation());
                        else {
                            //find nearby hostile enemies
                            List<Entity> nearbyEntities = villager[0].getNearbyEntities(guardMode == GuardMode.FOLLOW ? RADIUS / 2 : RADIUS, 2, guardMode == GuardMode.FOLLOW ? RADIUS / 2 : RADIUS);
                            if (nearbyEntities.size() > 0) {
                                EntityType hostileMobs[] = new EntityType[] { EntityType.BLAZE, EntityType.CREEPER, EntityType.DROWNED, EntityType.ENDERMITE, EntityType.EVOKER, EntityType.HUSK, EntityType.MAGMA_CUBE, EntityType.PILLAGER, EntityType.RAVAGER, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME, EntityType.SPIDER, EntityType.STRAY, EntityType.VINDICATOR, EntityType.WITCH, EntityType.WITHER_SKELETON, EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HOGLIN, EntityType.CAVE_SPIDER, EntityType.ZOGLIN };
                                ArrayList<Map.Entry<Entity, Double>> closestMobs = new ArrayList<>();
                                for (Entity entity : nearbyEntities) {
                                    for (EntityType mob : hostileMobs) {
                                        if (entity.getType() == mob) {
                                            Location villagerLoc = villager[0].getLocation();
                                            Location mobLoc = entity.getLocation();
                                            double distance = distance(villagerLoc, mobLoc);
                                            closestMobs.add(new AbstractMap.SimpleEntry<>(entity, distance));
                                        }
                                    }
                                }
                                if (closestMobs.size() > 0) {
                                    closestMobs.sort(Comparator.comparing(Map.Entry::getValue));
                                    Entity mob = closestMobs.get(0).getKey();
                                    //attack mob
                                    if (closestMobs.get(0).getValue() <= 2.0 && ticks % 4 == 0) {
                                        lookAtLocation(mob.getLocation(), villager[0]);
                                        double damageAmount;
                                        double xDir = villager[0].getLocation().getX() - mob.getLocation().getX() >= 0 ? 1 : -1;
                                        double zDir = villager[0].getLocation().getZ() - mob.getLocation().getZ() >= 0 ? 1 : -1;
                                        Vector knockbackVector = new Vector(0.4 * xDir, -0.25, 0.4 * zDir);
                                        knockbackVector.multiply(-1);
                                        //knockbackVector.normalize();

                                        //base damage
                                        if (weapon == null)
                                            damageAmount = 2;
                                        else {
                                            switch (weapon.getType()) {
                                                case WOODEN_SWORD:
                                                case GOLDEN_SWORD:
                                                    damageAmount = 4;
                                                    break;

                                                case STONE_SWORD:
                                                    damageAmount = 5;
                                                    break;

                                                case IRON_SWORD:
                                                    damageAmount = 6;
                                                    break;

                                                case DIAMOND_SWORD:
                                                    damageAmount = 7;
                                                    break;

                                                case NETHERITE_SWORD:
                                                    damageAmount = 8;
                                                    break;

                                                default:
                                                    damageAmount = 2;
                                            }

                                            //enchantments
                                            for (Enchantment enchantment : weapon.getEnchantments().keySet()) {
                                                int level = weapon.getEnchantmentLevel(enchantment);
                                                switch(enchantment.getKey().toString()) {
                                                    case "minecraft:bane_of_arthropods":
                                                        if (mob.getType() == EntityType.SPIDER || mob.getType() == EntityType.CAVE_SPIDER || mob.getType() == EntityType.SILVERFISH || mob.getType() == EntityType.ENDERMITE) {
                                                            damageAmount += 2.5 * level;
                                                            double slownessLength = ThreadLocalRandom.current().nextDouble(1, 2 + (0.5 * level));
                                                            ((LivingEntity) mob).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) Math.round(slownessLength * 20), 4, false, true));
                                                        }
                                                        break;

                                                    case "minecraft:fire_aspect":
                                                        mob.setFireTicks(80 * level);
                                                        break;

                                                    case "minecraft:knockback":
                                                        knockbackVector.add(new Vector(0.4 * level * xDir * -1, 0, 0.4 * level * zDir * -1));
                                                        break;

                                                    case "minecraft:sharpness":
                                                        damageAmount += 0.5 * level + 0.5;
                                                        break;

                                                    case "minecraft:smite":
                                                        if (mob.getType() == EntityType.SKELETON || mob.getType() == EntityType.ZOMBIE || mob.getType() == EntityType.ZOMBIE_VILLAGER || mob.getType() == EntityType.WITHER || mob.getType() == EntityType.WITHER_SKELETON || mob.getType() == EntityType.STRAY || mob.getType() == EntityType.HUSK || mob.getType() == EntityType.DROWNED || mob.getType() == EntityType.ZOGLIN) {
                                                            damageAmount += 2.5 * level;
                                                        }
                                                        break;
                                                }
                                            }
                                        }

                                        ((LivingEntity) mob).damage(damageAmount);
                                        mob.setVelocity(knockbackVector);
                                        ((Monster) mob).setTarget((LivingEntity) villager[0]);
                                    }
                                    else {
                                        //run to mob
                                        if (guardMode == GuardMode.STATIONARY && distance(guardLocation, mob.getLocation()) <= 20) {
                                            lookAtLocation(mob.getLocation(), villager[0]);
                                            runToLocation(mob.getLocation());
                                        }
                                        else if ((guardMode == GuardMode.BODYGUARD) && distance(mob.getLocation(), followPlayer.getLocation()) <= 10) {
                                            lookAtLocation(mob.getLocation(), villager[0]);
                                            runToLocation(mob.getLocation());
                                        }
                                        else if (guardMode == GuardMode.WANDER && distance(villager[0].getLocation(), mob.getLocation()) <= 15) {
                                            lookAtLocation(mob.getLocation(), villager[0]);
                                            runToLocation(mob.getLocation());
                                        }
                                        else returnToStart(villager[0]);
                                    }
                                }
                                else {
                                    returnToStart(villager[0]);
                                }
                            }
                        }
                        if (ticks >= 8)
                            ticks = 0;
                    }
                }
            }
        }.runTaskTimer(Main.plugin, 20, 5);
    }

    /**
     * Stops the guard's job of attacking hostile mobs
     */
    public void stopJob() {
        this.runnable.cancel();
        CraftVillager villager = (CraftVillager) Bukkit.getEntity(getUniqueID());
        if (this.weapon != null)
            villager.getWorld().dropItemNaturally(villager.getLocation(), this.weapon);
        for (ItemStack item : this.armor) {
            if (item != null)
                villager.getWorld().dropItemNaturally(villager.getLocation(), item);
        }
        villager.getEquipment().setHelmet(null);
        villager.getEquipment().setChestplate(null);
        villager.getEquipment().setLeggings(null);
        villager.getEquipment().setBoots(null);
    }

    /**
     * Makes the villager look at a location
     * @param location
     * @param villager
     */
    private void lookAtLocation(Location location, Entity villager) {
        Vector lookAt = location.toVector().subtract(villager.getLocation().toVector());
        Location loc = villager.getLocation();
        loc.setDirection(lookAt);
        villager.teleport(loc);
    }

    private void returnToStart(Entity villager) {
        //run to guard position
        if (guardMode == GuardMode.STATIONARY && distance(villager.getLocation(), guardLocation) > 3) {
            walkToLocation(guardLocation);
        }
        //run to player
        else if (guardMode == GuardMode.BODYGUARD && distance(villager.getLocation(), followPlayer.getLocation()) > 5) {
            runToLocation(followPlayer.getLocation());
        }
    }

    /**
     * @return guardLocation
     */
    public JSONObject guardLocationToJSON() {
        if (this.guardLocation == null)
            return null;
        else {
            JSONObject jsonGuardLocation = new JSONObject();
            jsonGuardLocation.put("world", this.guardLocation.getWorld().getName());
            jsonGuardLocation.put("x", String.valueOf(this.guardLocation.getX()));
            jsonGuardLocation.put("y", String.valueOf(this.guardLocation.getY()));
            jsonGuardLocation.put("z", String.valueOf(this.guardLocation.getZ()));
            return jsonGuardLocation;
        }
    }

    /**
     * Returns the Guard's weapon in JSON format
     * @return jsonWeapon
     */
    public JSONObject weaponToJSON() {
        if (this.weapon == null)
            return null;
        else {
            JSONObject jsonWeapon = new JSONObject();
            jsonWeapon.put("material", this.weapon.getType().toString());
            jsonWeapon.put("displayName", this.weapon.getItemMeta().getDisplayName());
            JSONArray jsonEnchantments = new JSONArray();
            for (Enchantment enchantment : this.weapon.getEnchantments().keySet()) {
                JSONObject jsonEnchantment = new JSONObject();
                String enchantmentKey = enchantment.getKey().toString();
                jsonEnchantment.put("name", enchantmentKey.substring(enchantmentKey.indexOf(":") + 1));
                jsonEnchantment.put("level", String.valueOf(this.weapon.getEnchantments().get(enchantment)));
                jsonEnchantments.add(jsonEnchantment);
            }
            jsonWeapon.put("enchantments", jsonEnchantments);
            return jsonWeapon;
        }
    }

    /**
     * Returns the Guard's armor in JSON format
     * @return
     */
    public JSONArray armorToJSON() {
        JSONArray jsonArmor = new JSONArray();
        for (ItemStack item : this.armor) {
            if (item == null)
                jsonArmor.add(null);
            else {
                JSONObject jsonItem = new JSONObject();
                jsonItem.put("material", item.getType().toString());
                jsonItem.put("displayName", item.getItemMeta().getDisplayName());
                JSONArray jsonEnchantments = new JSONArray();
                for (Enchantment enchantment : item.getEnchantments().keySet()) {
                    JSONObject jsonEnchantment = new JSONObject();
                    String enchantmentKey = enchantment.getKey().toString();
                    jsonEnchantment.put("name", enchantmentKey.substring(enchantmentKey.indexOf(":") + 1));
                    jsonItem.put("level", String.valueOf(item.getEnchantments().get(enchantment)));
                    jsonEnchantments.add(jsonEnchantment);
                }
                jsonItem.put("enchantments", jsonEnchantments);
                jsonArmor.add(jsonItem);
            }
        }
        return jsonArmor;
    }

    //GUI for guard options
    public void guardOptionsMenu(Player player, PlayerInteractEntityEvent e) {
        CraftVillager villager = (CraftVillager) e.getRightClicked();
        InventoryGUI gui = new InventoryGUI(player, "Guard Options", 1, true);

        //equip button
        InventoryGUIButton equipButton = new InventoryGUIButton(gui, "Equip weapon and armor", null, Material.ARMOR_STAND);
        equipButton.setOnClick(f -> {
            equipMenu(player, e);
        });
        gui.addButton(equipButton);

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 2);

        //stationary button
        if (this.guardMode != GuardMode.STATIONARY) {
            InventoryGUIButton stationaryButton = new InventoryGUIButton(gui, "Stay here", "The guard will stand in place and attack nearby mobs", Material.SHIELD);
            stationaryButton.setOnClick(f -> {
                this.guardMode = GuardMode.STATIONARY;
                this.guardLocation = villager.getLocation();
                this.followPlayer = null;
                player.sendMessage(ChatColor.GREEN + getName() + " will stand guard here");
                player.closeInventory();
            });
            gui.addButton(stationaryButton);
        }

        //wander button
        if (this.guardMode != GuardMode.WANDER) {
            InventoryGUIButton wanderButton = new InventoryGUIButton(gui, "Wander", "The guard will wander and attack any mobs it sees", Material.IRON_SWORD);
            wanderButton.setOnClick(f -> {
                this.guardMode = GuardMode.WANDER;
                this.followPlayer = null;
                player.sendMessage(ChatColor.GREEN + getName() + " will patrol the area");
                player.closeInventory();
            });
            gui.addButton(wanderButton);
        }

        //bodyguard button
        if (this.guardMode != GuardMode.BODYGUARD) {
            InventoryGUIButton bodyguardButton = new InventoryGUIButton(gui, "Bodyguard", "The guard will follow you and attack nearby mobs", Material.IRON_CHESTPLATE);
            bodyguardButton.setOnClick(f -> {
                this.guardMode = GuardMode.BODYGUARD;
                this.followPlayer = e.getPlayer();
                player.sendMessage(ChatColor.GREEN + getName() + " will follow and protect you");
                player.closeInventory();
            });
            gui.addButton(bodyguardButton);
        }

        //follow button
        if (this.guardMode != GuardMode.FOLLOW) {
            InventoryGUIButton followButton = new InventoryGUIButton(gui, "Follow me", "The guard will follow you without attacking mobs", Material.IRON_BOOTS);
            followButton.setOnClick(f -> {
                this.guardMode = GuardMode.FOLLOW;
                this.followPlayer = e.getPlayer();
                player.sendMessage(ChatColor.GREEN + getName() + " will follow you");
                player.closeInventory();
            });
            gui.addButton(followButton);
        }

        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE), 2);

        //assign job button
        InventoryGUIButton assignJobButton = new InventoryGUIButton(gui, "Assign Job", null, Material.IRON_PICKAXE);
        assignJobButton.setOnClick(f -> {
            NationsVillagerListener.assignJobMenu(e);
        });
        gui.addButton(assignJobButton);

        gui.showMenu();
    }

    //GUI for equipping villager with weapon and armor
    public void equipMenu(Player player, PlayerInteractEntityEvent e) {
        InventoryGUI gui = new InventoryGUI(player, "Equip Weapon and Armor", 1, false);
        gui.setVillager(this);

        //sword slot
        gui.addButton(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE, false));

        gui.addButton(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE));

        //armor slots
        gui.addButtons(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE, false), 4);

        gui.addButton(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE));

        //confirm button
        InventoryGUIButton confirmButton = new InventoryGUIButton(gui, "Confirm", null, Material.LIME_STAINED_GLASS_PANE);
        confirmButton.setOnClick(f -> {
            if (confirmEquip(player, gui))
                player.closeInventory();
        });
        gui.addButton(confirmButton);

        gui.addButton(new InventoryGUIButton(gui, null, null, Material.WHITE_STAINED_GLASS_PANE));

        //weapon
        if (weapon != null) {
            InventoryGUIButton weaponButton = gui.getButtons().get(0);
            weaponButton.setItem(weapon);
            weaponButton.setLocked(false);
        }
        else gui.setButton(0, new InventoryGUIButton(gui, null, null, Material.AIR, false));

        //armor
        int armorIndex = 2;
        for (ItemStack item : this.armor) {
            if (item != null)
                gui.getInventory().setItem(armorIndex, item);
            else gui.setButton(armorIndex, new InventoryGUIButton(gui, null, null, Material.AIR, false));
            armorIndex++;
        }

        gui.setRunnable(new BukkitRunnable() {
            @Override
            public void run() {
                confirmEquip(player, gui);
            }
        });

        gui.showMenu();
    }

    /**
     *
     * @param player
     * @param gui
     * @return true if valid, false if invalid
     */
    public boolean confirmEquip(Player player, InventoryGUI gui) {
        CraftVillager villager = (CraftVillager) Bukkit.getEntity(this.getUniqueID());
        boolean error = false;

        //weapon
        ItemStack selectedWeapon = gui.getInventory().getContents()[0];
        if (selectedWeapon == null)
            this.weapon = null;
        else if (selectedWeapon != null && selectedWeapon.getType() != Material.AIR) {
            if (selectedWeapon.getType().toString().contains("SWORD"))
                this.weapon = selectedWeapon;
            else {
                HashMap<Integer, ItemStack> extraItems = player.getInventory().addItem(selectedWeapon);
                for (ItemStack item : extraItems.values())
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                gui.getInventory().remove(selectedWeapon);
                player.sendMessage(ChatColor.RED + this.getName() + " only accepts swords for weapons");
                error = true;
            }
        }

        //helmet
        ItemStack selectedHelmet = gui.getInventory().getContents()[2];
        if (selectedHelmet == null) {
            this.armor[0] = null;
            villager.getEquipment().setHelmet(null);
        }
        else if (selectedHelmet.getType() != Material.AIR) {
            if (selectedHelmet != null && selectedHelmet.getType().toString().contains("HELMET")) {
                this.armor[0] = selectedHelmet;
                villager.getEquipment().setHelmet(this.armor[0]);
                villager.getEquipment().setHelmetDropChance(0);
            }
            else {
                HashMap<Integer, ItemStack> extraItems = player.getInventory().addItem(selectedHelmet);
                for (ItemStack item : extraItems.values())
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                gui.getInventory().remove(selectedHelmet);
                player.sendMessage(ChatColor.RED + "Please only place helmets in the first armor slot");
                error = true;
            }
        }

        //chestplate
        ItemStack selectedChestplate = gui.getInventory().getContents()[3];
        if (selectedChestplate == null) {
            this.armor[1] = null;
            villager.getEquipment().setChestplate(null);
        }
        else if (selectedChestplate.getType() != Material.AIR) {
            if (selectedChestplate != null && selectedChestplate.getType().toString().contains("CHESTPLATE")) {
                this.armor[1] = selectedChestplate;
                villager.getEquipment().setChestplate(this.armor[1]);
                villager.getEquipment().setChestplateDropChance(0);
            }
            else {
                HashMap<Integer, ItemStack> extraItems = player.getInventory().addItem(selectedChestplate);
                for (ItemStack item : extraItems.values())
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                gui.getInventory().remove(selectedChestplate);
                player.sendMessage(ChatColor.RED + "Please only place chestplates in the second armor slot");
                error = true;
            }
        }

        //leggings
        ItemStack selectedLeggings = gui.getInventory().getContents()[4];
        if (selectedLeggings == null) {
            this.armor[2] = null;
            villager.getEquipment().setLeggings(null);
        }
        else if (selectedLeggings.getType() != Material.AIR) {
            if (selectedLeggings != null && selectedLeggings.getType().toString().contains("LEGGINGS")) {
                this.armor[2] = selectedLeggings;
                villager.getEquipment().setLeggings(this.armor[2]);
                villager.getEquipment().setLeggingsDropChance(0);
            }
            else {
                HashMap<Integer, ItemStack> extraItems = player.getInventory().addItem(selectedLeggings);
                for (ItemStack item : extraItems.values())
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                gui.getInventory().remove(selectedLeggings);
                player.sendMessage(ChatColor.RED + "Please only place leggings in the third armor slot");
                error = true;
            }
        }

        //boots
        ItemStack selectedBoots = gui.getInventory().getContents()[5];
        if (selectedBoots == null) {
            this.armor[3] = null;
            villager.getEquipment().setBoots(null);
        }
        else if (selectedBoots != null && selectedBoots.getType() != Material.AIR) {
            if (selectedBoots.getType().toString().contains("BOOTS")) {
                this.armor[3] = selectedBoots;
                villager.getEquipment().setBoots(this.armor[3]);
                villager.getEquipment().setBootsDropChance(0);
            }
            else {
                HashMap<Integer, ItemStack> extraItems = player.getInventory().addItem(selectedBoots);
                for (ItemStack item : extraItems.values())
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                gui.getInventory().remove(selectedBoots);
                player.sendMessage(ChatColor.RED + "Please only place boots in the fourth armor slot");
                error = true;
            }
        }

        return !error;
    }

    /**
     * @param loc1
     * @param loc2
     * @return distance
     */
    private double distance(Location loc1, Location loc2) {
        return Math.sqrt(Math.pow(loc2.getX() - loc1.getX(), 2) + Math.pow(loc2.getY() - loc1.getY(), 2) + Math.pow(loc2.getZ() - loc1.getZ(), 2));
    }
}
