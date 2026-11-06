package me.rafaelauler;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class Soup implements Listener {

    public int vida = 7;
    public int fome = 7;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onUseSoup(PlayerInteractEvent event) {

        // Evita executar duas vezes por causa da mão secundária
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR
                && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() != Material.MUSHROOM_STEW) {
            return;
        }

        String name = player.getName();

        boolean inRoom1 = MainCommand.game.contains(name);
        boolean inRoom2 = MainCommand2.game.contains(name);

        if (!inRoom1 && !inRoom2) {
            return;
        }

        // Impede o comportamento normal da sopa
        event.setCancelled(true);

        double health = player.getHealth();
        double maxHealth = player.getMaxHealth();

        int food = player.getFoodLevel();

        boolean needsHealth = health < maxHealth;
        boolean needsFood = food < 20;

        // Não consome sopa se estiver com vida e fome cheias
        if (!needsHealth && !needsFood) {
            return;
        }

        // Recupera vida
        if (needsHealth) {
            double newHealth = Math.min(maxHealth, health + vida);
            player.setHealth(newHealth);
        }

        // Recupera fome
        if (needsFood) {
            int newFood = Math.min(20, food + fome);
            player.setFoodLevel(newFood);
        }

        // Consome sopa e entrega tigela
        consumeSoup(player);

        // Som de consumo
        player.playSound(
                player.getLocation(),
                Sound.ENTITY_GENERIC_EAT,
                0.6f,
                1.2f
        );
    }

    private void consumeSoup(Player player) {

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() != Material.MUSHROOM_STEW) {
            return;
        }

        if (item.getAmount() <= 1) {

            player.getInventory().setItemInMainHand(
                    new ItemStack(Material.BOWL)
            );

        } else {

            item.setAmount(item.getAmount() - 1);

            java.util.Map<Integer, ItemStack> leftovers =
                    player.getInventory().addItem(
                            new ItemStack(Material.BOWL)
                    );

            for (ItemStack leftover : leftovers.values()) {
                player.getWorld().dropItemNaturally(
                        player.getLocation(),
                        leftover
                );
            }
        }
    }
}