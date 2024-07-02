package com.starshootercity.keepslots;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class KeepSlots extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    private final NamespacedKey uuidKey = new NamespacedKey(this, "kept-slot-uuid");
    private final NamespacedKey indexKey = new NamespacedKey(this, "kept-slot-index");

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        for (int i = 0; i < event.getPlayer().getInventory().getSize(); i++) {
            ItemStack item = event.getPlayer().getInventory().getItem(i);
            if (item == null) continue;
            PersistentDataContainer container = event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), item).getPersistentDataContainer();
            container.set(uuidKey, PersistentDataType.STRING, event.getPlayer().getUniqueId().toString());
            container.set(indexKey, PersistentDataType.INTEGER, i);
        }
        event.getDrops().clear();
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            String uuid = event.getItem().getPersistentDataContainer().get(uuidKey, PersistentDataType.STRING);
            Integer index = event.getItem().getPersistentDataContainer().get(indexKey, PersistentDataType.INTEGER);
            if (uuid == null || index == null) return;
            if (uuid.equals(player.getUniqueId().toString()) || getConfig().getBoolean("apply-to-all-players")) {
                ItemStack item = player.getInventory().getItem(index);
                if (item == null || item.getType() == Material.AIR) {
                    event.setCancelled(true);
                    ItemStack received = event.getItem().getItemStack();
                    received.setAmount(event.getItem().getItemStack().getAmount() - event.getRemaining());
                    if (event.getRemaining() == 0) {
                        player.playPickupItemAnimation(event.getItem());
                        event.getItem().remove();
                    }
                    else {
                        player.playPickupItemAnimation(event.getItem(), event.getItem().getItemStack().getAmount() - event.getRemaining());
                        ItemStack remaining = event.getItem().getItemStack();
                        remaining.setAmount(event.getRemaining());
                        event.getItem().setItemStack(remaining);
                    }
                    player.getInventory().setItem(index, received);
                }
            }
        }
    }
}