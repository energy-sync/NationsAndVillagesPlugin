package com.github.dawsonvilamaa.nationsandvillagesplugin.listeners;

import com.github.dawsonvilamaa.nationsandvillagesplugin.Main;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsVillager;
import net.minecraft.server.v1_16_R3.EntityVillager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftZombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
            EntityVillager villager = ((CraftVillager) e.getEntity()).getHandle();
            if (villager instanceof NationsVillager) {
                NationsVillager nationsVillager = (NationsVillager) villager;
                if (e.getDamager() instanceof CraftPlayer)
                    nationsVillager.speakToPlayer((CraftPlayer) e.getDamager(), playerAttackMessages[random.nextInt(playerAttackMessages.length)]);
                else if (e.getDamager() instanceof CraftZombie)
                    nationsVillager.shout(zombieAttackMessages[random.nextInt(zombieAttackMessages.length)]);
            }
        }
    }

    //renames villager if renamed with name tag
    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof CraftVillager) {
            EntityVillager villager = ((CraftVillager) e.getRightClicked()).getHandle();
            ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
            if (villager instanceof NationsVillager
                && e.getHand().equals(EquipmentSlot.HAND)
                && item.getType() == Material.NAME_TAG && item.getItemMeta().hasDisplayName() == true) {
                    NationsVillager nationsVillager = (NationsVillager) villager;
                    nationsVillager.setName(item.getItemMeta().getDisplayName());
            }
        }
    }
}
