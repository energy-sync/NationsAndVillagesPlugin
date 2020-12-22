package com.github.dawsonvilamaa.nationsandvillagesplugin.listeners;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsChunk;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsPlayer;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsVillager;
import net.minecraft.server.v1_16_R3.EntityVillager;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
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

    //used to get info from villager, for debugging user
    @EventHandler
    public void onNationsVillagerInteract(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof CraftVillager) {
            EntityVillager villager = ((CraftVillager) e.getRightClicked()).getHandle();
            if (villager instanceof NationsVillager && e.getHand().equals(EquipmentSlot.HAND)) {
                NationsVillager nationsVillager = (NationsVillager) villager;
                String infoStr = ChatColor.YELLOW + "--------------------\n"
                        + ChatColor.WHITE + nationsVillager.getName() + "\n"
                        + ChatColor.YELLOW + "--------------------\n"
                        + ChatColor.WHITE + "Nation: ";
                if (nationsVillager.getNationID() != -1) infoStr += Main.nationsManager.getNationByID(nationsVillager.getNationID());
                else infoStr += "none";
                infoStr += "\nVillager: ";
                if (nationsVillager.getVillage() != null) infoStr += nationsVillager.getVillage().getName();
                else infoStr += "none";
                e.getPlayer().sendMessage(infoStr);
            }
        }
    }

    //detect when player enters and exits claimed chunks
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        NationsPlayer player = Main.nationsManager.getPlayerByUUID(e.getPlayer().getUniqueId());
        Chunk chunk = e.getPlayer().getLocation().getChunk();
        if (player.getCurrentChunk().getX() != chunk.getX() || player.getCurrentChunk().getZ() != chunk.getZ()) {
            NationsChunk nationsChunk = Main.nationsManager.getChunkByCoords(chunk.getX(), chunk.getZ());
            if (nationsChunk != null) {
                //entering claimed land
                if (player.getCurrentChunk().getNationID() != nationsChunk.getNationID()) {
                    player.setCurrentChunk(new NationsChunk(chunk.getX(), chunk.getZ(), nationsChunk.getNationID()));
                    e.getPlayer().sendTitle(ChatColor.YELLOW + Main.nationsManager.getNationByID(nationsChunk.getNationID()).getName(),ChatColor.GREEN + "Entering", 3, 50, 3);
                }
            }
            else {
                //entering unclaimed land
                if (player.getCurrentChunk().getNationID() != -1) {
                    e.getPlayer().sendTitle(ChatColor.YELLOW + Main.nationsManager.getNationByID(player.getCurrentChunk().getNationID()).getName(), ChatColor.RED + "Leaving", 3, 50, 3);
                    player.setCurrentChunk(new NationsChunk(chunk.getX(), chunk.getZ(), -1));
                }
            }
        }
    }
}