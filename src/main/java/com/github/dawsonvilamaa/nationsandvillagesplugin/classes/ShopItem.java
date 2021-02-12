package com.github.dawsonvilamaa.nationsandvillagesplugin.classes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ShopItem {
    private ItemStack item;
    private int price;
    private int amount;
    private UUID sellerUUID;

    /**
     * @param item
     * @param price
     * @param amount
     * @param sellerUUID
     */
    public ShopItem(ItemStack item, int price, int amount, UUID sellerUUID) {
        this.item = item;
        this.price = price;
        this.amount = amount;
        this.sellerUUID = sellerUUID;
    }

    /**
     * @return item
     */
    public ItemStack getItem() {
        return this.item;
    }

    /**
     * @param item
     */
    public void setItem(ItemStack item) {
        this.item = item;
    }

    /**
     * @return material
     */
    public Material getMaterial() {
        return this.item.getType();
    }

    /**
     * @return price
     */
    public int getPrice() {
        return this.price;
    }

    /**
     * @param price
     */
    public void setPrice(int price) {
        this.price = price;
    }

    /**
     * @return amount;
     */
    public int getAmount() {
        return this.amount;
    }

    /**
     * @param amount
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void incrementAmount() {
        this.amount++;
    }

    public void decrementAmount() {
        this.amount--;
    }

    /**
     * @return sellerUUID
     */
    public UUID getSellerUUID() {
        return sellerUUID;
    }

    /**
     * Compares ItemStack and price
     * @param compare
     * @return equal
     */
    public boolean equals(ShopItem compare) {
        if (this.item.equals(compare.getItem()) && this.price == compare.getPrice())
            return true;
        return false;
    }
}
