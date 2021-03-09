package com.github.dawsonvilamaa.nationsandvillagesplugin.listeners;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.NationsManager;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.Nation;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsChunk;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsPlayer;
import com.github.dawsonvilamaa.nationsandvillagesplugin.commands.claim;
import com.github.dawsonvilamaa.nationsandvillagesplugin.commands.unclaim;
import com.github.dawsonvilamaa.nationsandvillagesplugin.npcs.NationsVillager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.entity.*;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PlayerListener implements Listener {
    private Main plugin;

    /**
     * @param plugin
     */
    public PlayerListener(Main plugin) {
        this.plugin = plugin;
    }

    //Adds players to data when they join if not already on the list, disables autoclaiming and autounclaiming
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        NationsPlayer nationsPlayer = Main.nationsManager.getPlayerByUUID(e.getPlayer().getUniqueId());
        if (nationsPlayer == null) {
            nationsPlayer = Main.nationsManager.addPlayer(e.getPlayer());
            e.getPlayer().sendMessage(ChatColor.GREEN + "You received $" + Main.nationsManager.startingMoney + " for being a new player");
        }
        nationsPlayer.setAutoClaim(false);
        nationsPlayer.setAutoUnclaim(false);
        //check if username changed
        if (!(nationsPlayer.getName().equals(e.getPlayer().getName())))
            nationsPlayer.setName(e.getPlayer().getName());
    }

    //detect when player enters and exits claimed chunks, auto claims and unclaims chunks if applicable
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        NationsPlayer player = Main.nationsManager.getPlayerByUUID(e.getPlayer().getUniqueId());
        Chunk chunk = e.getPlayer().getLocation().getChunk();
        //check if player moved into a new chunk
        if (player.getCurrentChunk().getX() != chunk.getX() || player.getCurrentChunk().getZ() != chunk.getZ()) {
            NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ());
            if (nationsChunk != null) {
                //entering claimed land
                if (player.getCurrentChunk().getNationID() != nationsChunk.getNationID()) {
                    player.setCurrentChunk(new NationsChunk(chunk.getX(), chunk.getZ(), nationsChunk.getNationID()));
                    e.getPlayer().sendTitle(ChatColor.YELLOW + Main.nationsManager.getNationByID(nationsChunk.getNationID()).getName(),ChatColor.GREEN + "Entering", 3, 50, 3);
                }
                //autounclaim
                if (player.isAutoUnclaiming() && player.getNationID() == nationsChunk.getNationID())
                    unclaim.run(e.getPlayer(), new String[0]);
            }
            else {
                //entering unclaimed land
                if (player.getCurrentChunk().getNationID() != -1)
                    e.getPlayer().sendTitle(ChatColor.YELLOW + Main.nationsManager.getNationByID(player.getCurrentChunk().getNationID()).getName(), ChatColor.RED + "Leaving", 3, 50, 3);
                player.setCurrentChunk(new NationsChunk(chunk.getX(), chunk.getZ(), -1));
                player.setCurrentChunk(new NationsChunk(chunk.getX(), chunk.getZ(), -1));
                //autoclaim
                if (player.isAutoClaiming())
                    claim.run(e.getPlayer(), new String[0]);
            }
        }
    }


    //PERMISSIONS

    //checks if player is allowed to place blocks in current chunk
    @EventHandler
    public void  onBlockPlace(BlockPlaceEvent e) {
        NationsPlayer player = Main.nationsManager.getPlayerByUUID(e.getPlayer().getUniqueId());
        Chunk chunk = e.getBlockPlaced().getChunk();
        NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ());
        //check if player is inside a claimed chunk
        if (nationsChunk != null) {
            Nation nation = Main.nationsManager.getNationByID(nationsChunk.getNationID());
            boolean canPlaceBlocks = true;
            //check player's permissions if they are in their own nation
            if (player.getNationID() == nationsChunk.getNationID())
                canPlaceBlocks = nation.getPermissionByRank(player.getRank()).canModifyBlocks();
            //check player's permission if they are not in their own nation
            else canPlaceBlocks = nation.getPermissionByRank(NationsManager.Rank.NONMEMBER).canModifyBlocks();
            if (canPlaceBlocks == false) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to place blocks here");
            }
        }
    }

    //checks if player is allowed to break blocks in current chunk
    @EventHandler
    public void  onBlockBreak(BlockBreakEvent e) {
        NationsPlayer player = Main.nationsManager.getPlayerByUUID(e.getPlayer().getUniqueId());
        Chunk chunk = e.getBlock().getChunk();
        NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ());
        //check if player is inside a claimed chunk
        if (nationsChunk != null) {
            Nation nation = Main.nationsManager.getNationByID(nationsChunk.getNationID());
            boolean canBreakBlocks = true;
            //check player's permissions if they are in their own nation
            if (player.getNationID() == nationsChunk.getNationID())
                canBreakBlocks = nation.getPermissionByRank(player.getRank()).canModifyBlocks();
                //check player's permission if they are not in their own nation
            else canBreakBlocks = nation.getPermissionByRank(NationsManager.Rank.NONMEMBER).canModifyBlocks();
            if (canBreakBlocks == false) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to destroy blocks here");
            }
        }
    }

    //checks if player is allowed to break item frames and paintings in current chunk
    @EventHandler
    public void onBreakHangingEntity(HangingBreakByEntityEvent e) {
        if (e.getRemover() instanceof CraftPlayer && (e.getEntity() instanceof CraftItemFrame || e.getEntity() instanceof CraftPainting)) {
            Chunk chunk = e.getEntity().getLocation().getChunk();
            NationsPlayer player = Main.nationsManager.getPlayerByUUID(e.getRemover().getUniqueId());
            NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ());
            //check if player is inside a claimed chunk
            if (nationsChunk != null) {
                Nation nation = Main.nationsManager.getNationByID(nationsChunk.getNationID());
                boolean canBreakBlocks = true;
                //check player's permissions if they are in their own nation
                if (player.getNationID() == nationsChunk.getNationID())
                    canBreakBlocks = nation.getPermissionByRank(player.getRank()).canModifyBlocks();
                    //check player's permission if they are not in their own nation
                else canBreakBlocks = nation.getPermissionByRank(NationsManager.Rank.NONMEMBER).canModifyBlocks();
                if (canBreakBlocks == false) {
                    e.setCancelled(true);
                    e.getRemover().sendMessage(ChatColor.RED + "You do not have permission to destroy blocks here");
                }
            }
        }
    }

    //checks if player is allowed to break boats and minecarts in current chunk
    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent e) {
        if (e.getAttacker() instanceof CraftPlayer && (e.getVehicle() instanceof CraftMinecart || e.getVehicle() instanceof CraftBoat)) {
            Chunk chunk = e.getVehicle().getLocation().getChunk();
            NationsPlayer player = Main.nationsManager.getPlayerByUUID(e.getAttacker().getUniqueId());
            NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ());
            //check if player is inside a claimed chunk
            if (nationsChunk != null) {
                Nation nation = Main.nationsManager.getNationByID(nationsChunk.getNationID());
                boolean canBreakBlocks = true;
                //check player's permissions if they are in their own nation
                if (player.getNationID() == nationsChunk.getNationID())
                    canBreakBlocks = nation.getPermissionByRank(player.getRank()).canModifyBlocks();
                    //check player's permission if they are not in their own nation
                else canBreakBlocks = nation.getPermissionByRank(NationsManager.Rank.NONMEMBER).canModifyBlocks();
                if (canBreakBlocks == false) {
                    e.setCancelled(true);
                    e.getAttacker().sendMessage(ChatColor.RED + "You do not have permission to break vehicles here");
                }
            }
        }
    }

    //checks if player is allowed to modify armor stands in current chunk
    @EventHandler
    public void onModifyArmorStand(PlayerArmorStandManipulateEvent e) {
        Chunk chunk = e.getRightClicked().getLocation().getChunk();
        NationsPlayer player = Main.nationsManager.getPlayerByUUID(e.getPlayer().getUniqueId());
        NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ());
        //check if player is inside a claimed chunk
        if (nationsChunk != null) {
            Nation nation = Main.nationsManager.getNationByID(nationsChunk.getNationID());
            boolean canBreakBlocks = true;
            //check player's permissions if they are in their own nation
            if (player.getNationID() == nationsChunk.getNationID())
                canBreakBlocks = nation.getPermissionByRank(player.getRank()).canModifyBlocks();
            //check player's permission if they are not in their own nation
            else canBreakBlocks = nation.getPermissionByRank(NationsManager.Rank.NONMEMBER).canModifyBlocks();
            if (canBreakBlocks == false) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to modify armor stands here");
            }
        }
    }

    //checks if player is allowed to open containers in current chunk
    @EventHandler
    public void  onContainerOpen(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null && (e.getClickedBlock().getState() instanceof InventoryHolder || e.getClickedBlock().getType() == Material.ANVIL)) {
            Chunk chunk = e.getClickedBlock().getChunk();
            NationsPlayer player = Main.nationsManager.getPlayerByUUID(e.getPlayer().getUniqueId());
            NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ());
            //check if player is inside a claimed chunk
            if (nationsChunk != null) {
                Nation nation = Main.nationsManager.getNationByID(nationsChunk.getNationID());
                boolean canOpenContainers = true;
                //check player's permissions if they are in their own nation
                if (player.getNationID() == nationsChunk.getNationID())
                    canOpenContainers = nation.getPermissionByRank(player.getRank()).canOpenContainers();
                //check player's permission if they are not in their own nation
                else canOpenContainers = nation.getPermissionByRank(NationsManager.Rank.NONMEMBER).canOpenContainers();
                if (canOpenContainers == false) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to open containers here");
                }
            }
        }
    }

    //checks if player is allowed to open chests and hoppers in minecarts
    @EventHandler
    public void onContainerEntityOpen(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof CraftMinecartChest || e.getRightClicked() instanceof CraftMinecartHopper) {
            Chunk chunk = e.getRightClicked().getLocation().getChunk();
            NationsPlayer player = Main.nationsManager.getPlayerByUUID(e.getPlayer().getUniqueId());
            NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ());
            //check if player is inside a claimed chunk
            if (nationsChunk != null) {
                Nation nation = Main.nationsManager.getNationByID(nationsChunk.getNationID());
                boolean canOpenContainers = true;
                //check player's permissions if they are in their own nation
                if (player.getNationID() == nationsChunk.getNationID())
                    canOpenContainers = nation.getPermissionByRank(player.getRank()).canOpenContainers();
                    //check player's permission if they are not in their own nation
                else canOpenContainers = nation.getPermissionByRank(NationsManager.Rank.NONMEMBER).canOpenContainers();
                if (canOpenContainers == false) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to open containers here");
                }
            }
        }
    }

    //checks if player is allowed to attack non-hostile enemies
    @EventHandler
    public void onAttackEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof CraftPlayer && !(e.getEntity() instanceof Monster || e.getEntity() instanceof CraftGhast || e.getEntity() instanceof CraftHoglin || e.getEntity() instanceof CraftMagmaCube || e.getEntity() instanceof CraftPhantom || e.getEntity() instanceof CraftShulker || e.getEntity() instanceof CraftSlime || e.getEntity() instanceof CraftEnderDragon || e.getEntity() instanceof CraftVillager)) {
            Chunk chunk = e.getEntity().getLocation().getChunk();
            NationsPlayer player = Main.nationsManager.getPlayerByUUID(e.getDamager().getUniqueId());
            NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ());
            //check if player is inside a claimed chunk
            if (nationsChunk != null) {
                Nation nation = Main.nationsManager.getNationByID(nationsChunk.getNationID());
                boolean canAttackEntities = true;
                //check player's permissions if they are in their own nation
                if (player.getNationID() == nationsChunk.getNationID())
                    canAttackEntities = nation.getPermissionByRank(player.getRank()).canAttackEntities();
                    //check player's permission if they are not in their own nation
                else canAttackEntities = nation.getPermissionByRank(NationsManager.Rank.NONMEMBER).canAttackEntities();
                if (canAttackEntities == false) {
                    e.setCancelled(true);
                    e.getDamager().sendMessage(ChatColor.RED + "You do not have permission to attack peaceful mobs here");
                }
            }
        }
    }
}