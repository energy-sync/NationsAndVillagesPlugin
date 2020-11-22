package com.github.dawsonvilamaa.nationsandvillagesplugin.listeners;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsVillager;
import net.minecraft.server.v1_16_R3.EntityVillager;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
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
                if (nationsVillager.getNation() != null) infoStr += nationsVillager.getNation().getName();
                else infoStr += "none";
                infoStr += "\nVillager: ";
                if (nationsVillager.getVillage() != null) infoStr += nationsVillager.getVillage().getName();
                else infoStr += "none";
                e.getPlayer().sendMessage(infoStr);
            }
        }
    }
}