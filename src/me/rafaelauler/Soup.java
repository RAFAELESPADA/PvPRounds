
package me.rafaelauler;

import java.util.Map;

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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onUseSoup(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR
                && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.MUSHROOM_STEW) {
            return;
        }

        String name = player.getName();
        boolean inRoom1 = MainCommand.game.contains(name);
        boolean inRoom2 = MainCommand2.game.contains(name);

        if (!inRoom1 && !inRoom2) {
            return;
        }

        double maxHealth = player.getMaxHealth();
        double health = player.getHealth();
        int food = player.getFoodLevel();

        boolean heal = vida > 0 && health < maxHealth;
        boolean restoreFood = fome > 0 && food < 20;

        // Não consumir se nenhum atributo puder ser restaurado.
        if (!heal && !restoreFood) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        if (heal) {
            player.setHealth(Math.min(maxHealth, health + vida));
        }

        if (restoreFood) {
            player.setFoodLevel(Math.min(20, food + fome));
        }

        consumeSoup(player);

        player.playSound(
                player.getLocation(),
                Sound.ENTITY_GENERIC_EAT,
                0.6f,
                1.2f
        );
    }

    private void consumeSoup(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();

        if (held.getType() != Material.MUSHROOM_STEW) {
            return;
        }

        if (held.getAmount() <= 1) {
            player.getInventory().setItemInMainHand(
                    new ItemStack(Material.BOWL)
            );
            return;
        }

        held.setAmount(held.getAmount() - 1);

        Map<Integer, ItemStack> leftovers =
                player.getInventory().addItem(new ItemStack(Material.BOWL));

        leftovers.values().forEach(leftover ->
                player.getWorld().dropItemNaturally(
                        player.getLocation(), leftover
                )
        );
    }
}
