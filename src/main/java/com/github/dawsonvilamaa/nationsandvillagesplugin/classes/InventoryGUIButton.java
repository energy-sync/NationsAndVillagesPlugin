package com.github.dawsonvilamaa.nationsandvillagesplugin.classes;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Consumer;

import java.awt.*;
import java.util.Arrays;

public class InventoryGUIButton {
    private String name;
    private String description;
    private Material material;
    private ItemStack item;
    private InventoryGUI parentGUI;
    private Consumer<InventoryClickEvent> onClick;

    public InventoryGUIButton() {
        this.name = null;
        this.description = null;
        this.material = null;
        this.item = null;
        this.parentGUI = null;
        onClick = null;
    }

    /**
     * @param parentGUI
     * @param name
     * @param description
     * @param material
     */
    public InventoryGUIButton(InventoryGUI parentGUI, String name, String description, Material material) {
        this.name = name;
        this.description = description;
        this.material = material;
        ItemStack newItem = new ItemStack(material, 1);
        ItemMeta meta = newItem.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(description));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        newItem.setItemMeta(meta);
        this.item = newItem;
        this.parentGUI = parentGUI;
        this.onClick = null;
    }

    /**
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return material
     */
    public Material getMaterial() {
        return this.material;
    }

    /**
     * @return item
     */
    public ItemStack getItem() {
        return this.item;
    }

    /**
     * @return
     */
    public InventoryGUI getParentGUI() {
        return this.parentGUI;
    }

    /**
     * @param e
     */
    public void onClick(InventoryClickEvent e) {
        if (this.onClick != null)
            this.onClick.accept(e);
    }

    /**
     * @param consumer
     */
    public void setOnClick(Consumer<InventoryClickEvent> consumer) {
        this.onClick = consumer;
    }

    /**
     * @param compare
     * @return is equal
     */
    public boolean equals(InventoryGUIButton compare) {
        if (this.name.equals(compare.getName()) && this.description.equals(compare.getDescription()) && this.material == compare.material)
            return true;
        return false;
    }
}
