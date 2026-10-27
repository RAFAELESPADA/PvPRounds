package me.rafaelauler;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;

/** Safe wrappers around Vault economy and permission services. */
public final class Coins {

    public static HashMap<String, Double> bal = new HashMap<>();
    public static Economy econ;
    public static Permission perms;

    public Coins() {
    }

    public static HashMap<String, Double> getCoinsMap() {
        return bal;
    }

    public static boolean setupEconomy() {
        econ = null;
        if (Bukkit.getServer() == null
                || Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> registration =
                Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (registration == null || registration.getProvider() == null) {
            return false;
        }

        econ = registration.getProvider();
        return true;
    }

    /** Permission service is optional; its absence must not cause a NullPointerException. */
    public static boolean setupPermissions() {
        perms = null;
        if (Bukkit.getServer() == null
                || Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Permission> registration =
                Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        if (registration == null || registration.getProvider() == null) {
            return false;
        }

        perms = registration.getProvider();
        return true;
    }

    public static void addCoins(Player player, int amount) {
        if (player == null || econ == null || amount <= 0) {
            return;
        }
        EconomyResponse response = econ.depositPlayer(player, amount);
        if (!response.transactionSuccess()) {
            Main main = Main.getInstance();
            if (main != null) {
                main.getLogger().warning("Could not deposit coins for " + player.getName() + ": " + response.errorMessage);
            }
        }
    }

    public static int getCoins(Player player) {
        if (player == null || econ == null) {
            return 0;
        }
        double balance = econ.getBalance(player);
        if (!Double.isFinite(balance) || balance <= 0.0D) {
            return 0;
        }
        return (int) Math.min(Integer.MAX_VALUE, Math.floor(balance));
    }

    public static void removeCoins(Player player, int amount) {
        if (player == null || econ == null || amount <= 0) {
            return;
        }
        EconomyResponse response = econ.withdrawPlayer(player, amount);
        if (!response.transactionSuccess()) {
            Main main = Main.getInstance();
            if (main != null) {
                main.getLogger().warning("Could not withdraw coins from " + player.getName() + ": " + response.errorMessage);
            }
        }
    }
}
