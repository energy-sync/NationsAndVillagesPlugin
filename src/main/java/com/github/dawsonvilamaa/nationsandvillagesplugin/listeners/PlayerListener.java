package com.github.dawsonvilamaa.nationsandvillagesplugin.listeners;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsChunk;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsPlayer;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsVillager;
import com.github.dawsonvilamaa.nationsandvillagesplugin.commands.claim;
import com.github.dawsonvilamaa.nationsandvillagesplugin.commands.unclaim;
import net.minecraft.server.v1_16_R3.EntityVillager;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;

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
    }

    //used to get info from villager, for debugging user
    @EventHandler
    public void onNationsVillagerInteract(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof CraftVillager) {
            EntityVillager villager = ((CraftVillager) e.getRightClicked()).getHandle();
            if (e.getHand().equals(EquipmentSlot.HAND)) {
                NationsVillager nationsVillager = Main.nationsManager.getVillagerByUUID(villager.getUniqueID());
                String infoStr = ChatColor.YELLOW + "--------------------\n"
                        + ChatColor.WHITE + nationsVillager.getName() + "\n"
                        + ChatColor.YELLOW + "--------------------\n"
                        + ChatColor.WHITE + "Nation: ";
                if (nationsVillager.getNationID() != -1) infoStr += Main.nationsManager.getNationByID(nationsVillager.getNationID()).getName();
                else infoStr += "none";
                e.getPlayer().sendMessage(infoStr);
            }
        }
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
}