package com.github.dawsonvilamaa.nationsandvillagesplugin.classes;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
     * @param jsonShopItem
     */
    public ShopItem(JSONObject jsonShopItem) {
        Material material = Material.valueOf(jsonShopItem.get("itemMaterial").toString());
        ItemStack itemStack = new ItemStack(material);
        itemStack.setAmount(Integer.parseInt(jsonShopItem.get("itemAmount").toString()));
        itemStack.getItemMeta().setDisplayName(jsonShopItem.get("itemDisplayName").toString());
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        JSONArray jsonEnchantments = (JSONArray) jsonShopItem.get("enchantments");
        Iterator<JSONObject> iterator = jsonEnchantments.iterator();
        while (iterator.hasNext()) {
            JSONObject jsonEnchantment = iterator.next();
            enchantments.put(Enchantment.getByKey(NamespacedKey.minecraft(jsonEnchantment.get("name").toString())), Integer.parseInt(jsonEnchantment.get("level").toString()));
        }
        itemStack.addEnchantments(enchantments);
        this.item = itemStack;
        this.price = Integer.parseInt(jsonShopItem.get("price").toString());
        this.amount = Integer.parseInt(jsonShopItem.get("amount").toString());
        this.sellerUUID = UUID.fromString(jsonShopItem.get("sellerUUID").toString());
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

    /**
     * Returns this ShopItem in JSON format
     * @return jsonShopItem
     */
    public JSONObject toJSON() {
        JSONObject jsonShopItem = new JSONObject();
        jsonShopItem.put("itemMaterial", this.item.getType().toString());
        jsonShopItem.put("itemAmount", String.valueOf(this.item.getAmount()));
        jsonShopItem.put("itemDisplayName", this.item.getItemMeta().getDisplayName());
        JSONArray jsonEnchantments = new JSONArray();
        for (Enchantment enchantment : this.item.getEnchantments().keySet()) {
            JSONObject jsonEnchantment = new JSONObject();
            String enchantmentKey = enchantment.getKey().toString();
            jsonEnchantment.put("name", enchantmentKey.substring(enchantmentKey.indexOf(":") + 1));
            jsonEnchantment.put("level", String.valueOf(this.item.getEnchantments().get(enchantment)));
            jsonEnchantments.add(jsonEnchantment);
        }
        jsonShopItem.put("enchantments", jsonEnchantments);
        jsonShopItem.put("price", String.valueOf(this.price));
        jsonShopItem.put("amount", String.valueOf(this.amount));
        jsonShopItem.put("sellerUUID", this.sellerUUID.toString());
        return jsonShopItem;
    }
}
