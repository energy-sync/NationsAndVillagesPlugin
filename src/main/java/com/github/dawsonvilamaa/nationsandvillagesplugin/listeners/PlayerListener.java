package com.github.dawsonvilamaa.nationsandvillagesplugin.listeners;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.NationsManager;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.Nation;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsChunk;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsPlayer;
import com.github.dawsonvilamaa.nationsandvillagesplugin.commands.nation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
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
        nationsPlayer.setAutoClaimMode(NationsPlayer.AUTOCLAIM_MODE.NONE);
        //check if username changed
        if (!(nationsPlayer.getName().equals(e.getPlayer().getName())))
            nationsPlayer.setName(e.getPlayer().getName());
    }

    //detect when player enters and exits claimed chunks, auto claims and unclaims chunks if applicable
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) throws InterruptedException {
        NationsPlayer player = Main.nationsManager.getPlayerByUUID(e.getPlayer().getUniqueId());
        Chunk chunk = e.getPlayer().getLocation().getChunk();
        //check if player moved into a new chunk
        if (player.getCurrentChunk().getX() != chunk.getX() || player.getCurrentChunk().getZ() != chunk.getZ()) {
            NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ(), chunk.getWorld());
            if (nationsChunk != null) {
                //entering claimed land
                if (player.getCurrentChunk().getNationID() != nationsChunk.getNationID() && player.getAutoClaimMode() == NationsPlayer.AUTOCLAIM_MODE.NONE) {
                    player.setCurrentChunk(new NationsChunk(chunk.getX(), chunk.getZ(), chunk.getWorld(), nationsChunk.getNationID()));
                    e.getPlayer().sendTitle(ChatColor.YELLOW + Main.nationsManager.getNationByID(nationsChunk.getNationID()).getName(),ChatColor.GREEN + "Entering", 3, 50, 3);
                }
                //autounclaim
                if (player.getAutoClaimMode() == NationsPlayer.AUTOCLAIM_MODE.AUTOUNCLAIM && player.getNationID() == nationsChunk.getNationID())
                    nation.run(e.getPlayer(), new String[] {"unclaim"});
            }
            else {
                //entering unclaimed land
                if (player.getCurrentChunk().getNationID() != -1){
                    Nation chunkNation = Main.nationsManager.getNationByID(player.getCurrentChunk().getNationID());
                    if (chunkNation != null && player.getAutoClaimMode() == NationsPlayer.AUTOCLAIM_MODE.NONE)
                        e.getPlayer().sendTitle(ChatColor.YELLOW + chunkNation.getName(), ChatColor.RED + "Leaving", 3, 50, 3);
                }
                player.setCurrentChunk(new NationsChunk(chunk.getX(), chunk.getZ(), chunk.getWorld(), -1));
                player.setCurrentChunk(new NationsChunk(chunk.getX(), chunk.getZ(), chunk.getWorld(), -1));
                //autoclaim
                if (player.getAutoClaimMode() == NationsPlayer.AUTOCLAIM_MODE.AUTOCLAIM)
                    nation.run(e.getPlayer(), new String[] {"claim"});
            }
        }
    }


    //PERMISSIONS

    //checks if player is allowed to place blocks in current chunk
    @EventHandler
    public void  onBlockPlace(BlockPlaceEvent e) {
        NationsPlayer player = Main.nationsManager.getPlayerByUUID(e.getPlayer().getUniqueId());
        Chunk chunk = e.getBlockPlaced().getChunk();
        NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ(), chunk.getWorld());
        //check if player is inside a claimed chunk
        if (nationsChunk != null) {
            Nation nation = Main.nationsManager.getNationByID(nationsChunk.getNationID());
            boolean canPlaceBlocks;
            //check player's permissions if they are in their own nation
            if (player.getNationID() == nationsChunk.getNationID())
                canPlaceBlocks = nation.getConfig().getPermissionByRank(player.getRank()).canModifyBlocks();
            //check player's permission if they are not in their own nation
            else canPlaceBlocks = nation.getConfig().getPermissionByRank(NationsManager.Rank.NONMEMBER).canModifyBlocks();
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
        NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ(), chunk.getWorld());
        //check if player is inside a claimed chunk
        if (nationsChunk != null) {
            Nation nation = Main.nationsManager.getNationByID(nationsChunk.getNationID());
            boolean canBreakBlocks = true;
            //check player's permissions if they are in their own nation
            if (player.getNationID() == nationsChunk.getNationID())
                canBreakBlocks = nation.getConfig().getPermissionByRank(player.getRank()).canModifyBlocks();
                //check player's permission if they are not in their own nation
            else canBreakBlocks = nation.getConfig().getPermissionByRank(NationsManager.Rank.NONMEMBER).canModifyBlocks();
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
            NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ(), chunk.getWorld());
            //check if player is inside a claimed chunk
            if (nationsChunk != null) {
                Nation nation = Main.nationsManager.getNationByID(nationsChunk.getNationID());
                boolean canBreakBlocks = true;
                //check player's permissions if they are in their own nation
                if (player.getNationID() == nationsChunk.getNationID())
                    canBreakBlocks = nation.getConfig().getPermissionByRank(player.getRank()).canModifyBlocks();
                    //check player's permission if they are not in their own nation
                else canBreakBlocks = nation.getConfig().getPermissionByRank(NationsManager.Rank.NONMEMBER).canModifyBlocks();
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
            NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ(), chunk.getWorld());
            //check if player is inside a claimed chunk
            if (nationsChunk != null) {
                Nation nation = Main.nationsManager.getNationByID(nationsChunk.getNationID());
                boolean canBreakBlocks = true;
                //check player's permissions if they are in their own nation
                if (player.getNationID() == nationsChunk.getNationID())
                    canBreakBlocks = nation.getConfig().getPermissionByRank(player.getRank()).canModifyBlocks();
                    //check player's permission if they are not in their own nation
                else canBreakBlocks = nation.getConfig().getPermissionByRank(NationsManager.Rank.NONMEMBER).canModifyBlocks();
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
        NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ(), chunk.getWorld());
        //check if player is inside a claimed chunk
        if (nationsChunk != null) {
            Nation nation = Main.nationsManager.getNationByID(nationsChunk.getNationID());
            boolean canBreakBlocks = true;
            //check player's permissions if they are in their own nation
            if (player.getNationID() == nationsChunk.getNationID())
                canBreakBlocks = nation.getConfig().getPermissionByRank(player.getRank()).canModifyBlocks();
            //check player's permission if they are not in their own nation
            else canBreakBlocks = nation.getConfig().getPermissionByRank(NationsManager.Rank.NONMEMBER).canModifyBlocks();
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
            NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ(), chunk.getWorld());
            //check if player is inside a claimed chunk
            if (nationsChunk != null) {
                Nation nation = Main.nationsManager.getNationByID(nationsChunk.getNationID());
                boolean canOpenContainers = true;
                //check player's permissions if they are in their own nation
                if (player.getNationID() == nationsChunk.getNationID())
                    canOpenContainers = nation.getConfig().getPermissionByRank(player.getRank()).canOpenContainers();
                //check player's permission if they are not in their own nation
                else canOpenContainers = nation.getConfig().getPermissionByRank(NationsManager.Rank.NONMEMBER).canOpenContainers();
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
            NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ(), chunk.getWorld());
            //check if player is inside a claimed chunk
            if (nationsChunk != null) {
                Nation nation = Main.nationsManager.getNationByID(nationsChunk.getNationID());
                boolean canOpenContainers = true;
                //check player's permissions if they are in their own nation
                if (player.getNationID() == nationsChunk.getNationID())
                    canOpenContainers = nation.getConfig().getPermissionByRank(player.getRank()).canOpenContainers();
                    //check player's permission if they are not in their own nation
                else canOpenContainers = nation.getConfig().getPermissionByRank(NationsManager.Rank.NONMEMBER).canOpenContainers();
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
        if (e.getDamager() instanceof CraftPlayer && !(e.getEntity() instanceof Monster || e.getEntity() instanceof CraftGhast || e.getEntity() instanceof CraftHoglin || e.getEntity() instanceof CraftMagmaCube || e.getEntity() instanceof CraftPhantom || e.getEntity() instanceof CraftShulker || e.getEntity() instanceof CraftSlime || e.getEntity() instanceof CraftEnderDragon || e.getEntity() instanceof CraftVillager || e.getEntity() instanceof CraftIronGolem)) {
            Chunk chunk = e.getEntity().getLocation().getChunk();
            NationsPlayer player = Main.nationsManager.getPlayerByUUID(e.getDamager().getUniqueId());
            NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ(), chunk.getWorld());
            //check if player is inside a claimed chunk
            if (nationsChunk != null) {
                Nation nation = Main.nationsManager.getNationByID(nationsChunk.getNationID());
                boolean canAttackEntities = true;
                //check player's permissions if they are in their own nation
                if (player.getNationID() == nationsChunk.getNationID())
                    canAttackEntities = nation.getConfig().getPermissionByRank(player.getRank()).canAttackEntities();
                    //check player's permission if they are not in their own nation
                else canAttackEntities = nation.getConfig().getPermissionByRank(NationsManager.Rank.NONMEMBER).canAttackEntities();
                if (canAttackEntities == false) {
                    e.setCancelled(true);
                    e.getDamager().sendMessage(ChatColor.RED + "You do not have permission to attack peaceful mobs here");
                }
            }
        }
    }
}