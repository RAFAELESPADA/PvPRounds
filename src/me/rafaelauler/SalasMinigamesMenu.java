
package me.rafaelauler;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class SalasMinigamesMenu implements Listener {

    private final JavaPlugin plugin;

    public SalasMinigamesMenu(JavaPlugin plugin) {
        this.plugin = plugin;

        plugin.saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void abrir(Player player) {
        String titulo = cor(
                plugin.getConfig().getString(
                        "menu.titulo",
                        "&8Salas de Minigames"
                )
        );

        Inventory menu = Bukkit.createInventory(
                new MenuHolder(),
                27,
                titulo
        );


menu.setItem(11, criarItem(
        player,
        "salas.sala1",
        "%pvprounds_players_room_1%"
));

menu.setItem(15, criarItem(
        player,
        "salas.sala2",
        "%pvprounds_players_room_2%"
));


        player.openInventory(menu);
    }

    private ItemStack criarItem(String caminho, String placeholder) {
        String materialNome = plugin.getConfig().getString(
                caminho + ".material",
                "OAK_LOG"
        );

        Material material = Material.matchMaterial(materialNome);

        if (material == null || !material.isItem()) {
            material = Material.OAK_LOG;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String nome = plugin.getConfig().getString(
                caminho + ".nome",
                "&aSala"
        );

        meta.setDisplayName(processar(nome, placeholder));

        List<String> loreConfig = plugin.getConfig().getStringList(
                caminho + ".lore"
        );

        List<String> lore = new ArrayList<>();

        for (String linha : loreConfig) {
            lore.add(processar(linha, placeholder));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private String processar(String texto, String placeholder) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            texto = PlaceholderAPI.setPlaceholders(
                    null,
                    texto
            );

            // O jogador é definido na abertura do menu.
            // Este método recebe o placeholder da sala.
            texto = texto.replace(
                    placeholder,
                    placeholder
            );
        }

        return cor(texto);
    }

    private String cor(String texto) {
        return ChatColor.translateAlternateColorCodes('&', texto);
    }

    @EventHandler
    public void aoClicar(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder()
                instanceof MenuHolder)) {
            return;
        }

        event.setCancelled(true);

        if (event.getClickedInventory() == null
                || event.getClickedInventory()
                != event.getView().getTopInventory()) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        switch (event.getRawSlot()) {
            case 11:
                player.closeInventory();
                executarComando(player, "salas.sala1.comando", "pvr join");
                break;

            case 15:
                player.closeInventory();
                executarComando(player, "salas.sala2.comando", "pvr2 join");
                break;

            default:
                break;
        }
    }

    private void executarComando(
            Player player,
            String caminho,
            String comandoPadrao
    ) {
        String comando = plugin.getConfig().getString(
                caminho,
                comandoPadrao
        );

        if (comando == null || comando.isBlank()) {
            return;
        }

        player.performCommand(comando.startsWith("/")
                ? comando.substring(1)
                : comando);
    }

private ItemStack criarItem(
        Player player,
        String caminho,
        String placeholder
) {
    String materialNome = plugin.getConfig().getString(
            caminho + ".material",
            "OAK_LOG"
    );

    Material material = Material.matchMaterial(materialNome);

    if (material == null || !material.isItem()) {
        material = Material.OAK_LOG;
    }

    ItemStack item = new ItemStack(material);
    ItemMeta meta = item.getItemMeta();

    String nome = plugin.getConfig().getString(
            caminho + ".nome",
            "&aSala"
    );

    meta.setDisplayName(processar(player, nome));

    List<String> loreConfig = plugin.getConfig().getStringList(
            caminho + ".lore"
    );

    List<String> lore = new ArrayList<>();

    for (String linha : loreConfig) {
        linha = linha.replace("%sala_jogadores%", placeholder);
        lore.add(processar(player, linha));
    }

    meta.setLore(lore);
    item.setItemMeta(meta);

    return item;
}

private String processar(Player player, String texto) {
    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
        texto = PlaceholderAPI.setPlaceholders(player, texto);
    }

    return cor(texto);
}

    private static class MenuHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
