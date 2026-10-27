package me.rafaelauler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.wavemc.core.util.UpdateScheduler;

public class Main extends JavaPlugin implements Listener {

    private EventManager eventmanager;
    private EventManager2 eventmanager2;

    public static Plugin plugin;
    public static Main instance;
    private File cf1;

    public static String pluginName = "PvPRounds";
    public static File file_x1 = new File("plugins/PvPRounds", "1v1.yml");
    public static FileConfiguration cfg_x1 = YamlConfiguration.loadConfiguration(file_x1);

    private SalasMinigamesMenu salasMenu;
    private TorneioScoreboard torneiosb;
    private FileConfiguration scoreboardConfig;

    // Mantém as mesmas instâncias registradas como listeners e usadas pela scoreboard.
    private Automatic automaticListener;
    private Automatic2 automatic2Listener;

    public static Main getInstance() {
        return instance;
    }

    public static Main getInstace() {
        return instance;
    }

    public static Main getInstace23() {
        return instance;
    }

    public EventManager getEventManager() {
        return this.eventmanager;
    }

    public EventManager2 getEventManager2() {
        return this.eventmanager2;
    }

    @Override
    public void onEnable() {
        instance = this;
        plugin = this;

        saveDefaultConfig();
        this.cf1 = new File(getDataFolder(), "config.yml");

        // Inicializa os gerenciadores antes de registrar eventos e comandos.
        eventmanager = new EventManager();
        eventmanager2 = new EventManager2();

        // Garante que o arquivo 1v1.yml exista e atualiza sua configuração estática.
        if (!file_x1.exists()) {
            saveResource("1v1.yml", false);
        }
        try {
            YamlConfiguration config1v1 = new YamlConfiguration();
            config1v1.load(file_x1);
            cfg_x1 = config1v1;
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().severe("Erro ao carregar 1v1.yml: " + e.getMessage());
            e.printStackTrace();
        }

        // Restore data saved for participants who disconnected before the previous shutdown.
        MainCommand.loadPendingRestoreStates();
        MainCommand2.loadPendingRestoreStates();

        // Carrega a configuração separada da scoreboard.
        File scoreboardFile = new File(getDataFolder(), "torneio-scoreboard-config.yml");
        if (!scoreboardFile.exists()) {
            try {
                saveResource("torneio-scoreboard-config.yml", false);
            } catch (IllegalArgumentException e) {
                getLogger().warning("torneio-scoreboard-config.yml não está dentro do JAR. "
                        + "Coloque o arquivo em src/main/resources/torneio-scoreboard-config.yml.");
            }
        }
        scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
        if (!scoreboardConfig.contains("torneio-scoreboard")) {
            getLogger().warning("A seção 'torneio-scoreboard' não foi encontrada em torneio-scoreboard-config.yml.");
        }

        // Cria uma única instância de cada listener e compartilha com a scoreboard.
        automaticListener = new Automatic();
        automatic2Listener = new Automatic2();

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new Eventos(), this);
        Bukkit.getPluginManager().registerEvents(automaticListener, this);
        Bukkit.getPluginManager().registerEvents(automatic2Listener, this);
        Bukkit.getPluginManager().registerEvents(new Soup(), this);

        // Comandos existentes.
        if (getCommand("pvprounds") != null) {
            getCommand("pvprounds").setExecutor(new MainCommand());
        }
        if (getCommand("pvr") != null) {
            getCommand("pvr").setExecutor(new MainCommand());
        }
        if (getCommand("pvprounds2") != null) {
            getCommand("pvprounds2").setExecutor(new MainCommand2());
        }
        if (getCommand("pvr2") != null) {
            getCommand("pvr2").setExecutor(new MainCommand2());
        }
        if (getCommand("setrounds") != null) {
            getCommand("setrounds").setExecutor(new SetRounds());
        }
        if (getCommand("setrounds2") != null) {
            getCommand("setrounds2").setExecutor(new SetRounds2());
        }

        // PlaceholderAPI.
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Bukkit.getConsoleSender().sendMessage("§e[PvPRounds] §aPlaceholderAPI encontrado!");
            Bukkit.getConsoleSender().sendMessage("§e[PvPRounds] §aConectando ao PlaceholderAPI...");
            new PvPRounds(this).register();
            Bukkit.getConsoleSender().sendMessage("§e[PvPRounds] §aPlaceholderAPI conectado com sucesso!");
        }

        // Inicia a scoreboard depois de carregar a configuração e criar os torneios.
        torneiosb = new TorneioScoreboard(this, automaticListener, automatic2Listener, scoreboardConfig);
        torneiosb.start();

        // Menu de salas de minigames.
        salasMenu = new SalasMinigamesMenu(this);
        if (getCommand("pvprooms") != null) {
            getCommand("pvprooms").setExecutor((sender, command, label, args) -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("This command can only be executed by players.");
                    return true;
                }
                salasMenu.abrir((Player) sender);
                return true;
            });
        } else {
            getLogger().warning("Command /pvprooms not found in plugin.yml!");
        }

        // Métricas.
        Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.DrilldownPie("serverAddress", () -> {
            Map<String, Map<String, Integer>> map = new HashMap<>();
            Map<String, Integer> entry = new HashMap<>();
            if (getConfig().getBoolean("SendIPAddressData")) {
                entry.put(Bukkit.getServer().getIp(), 1);
            } else {
                entry.put("Hidden", 1);
            }
            map.put("Port " + Bukkit.getServer().getPort(), entry);
            return map;
        }));

        // Vault is a hard dependency, but a permissions provider is optional.
        ConsoleCommandSender cmd = Bukkit.getConsoleSender();
        if (!Coins.setupPermissions()) {
            getLogger().warning("No Vault-compatible permissions provider is registered. "
                    + "Permission-service integration will remain unavailable.");
        }
        if (!Coins.setupEconomy()) {
            cmd.sendMessage("§c[PvPRounds] Startup aborted: no Vault-compatible economy provider is registered.");
            cmd.sendMessage("§eInstall and enable an economy plugin that registers its service with Vault.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Bukkit.getScheduler().runTaskTimer(this, new UpdateScheduler(), 20L, 20L);

        Bukkit.getConsoleSender().sendMessage("§aPVPROUNDS HAS BEEN ENABLED!");
    }
    public Automatic getAutomaticListener() {
        return automaticListener;
    }

    public Automatic2 getAutomatic2Listener() {
        return automatic2Listener;
    }
    @EventHandler
    public void onPlayerJoinRestoreTournamentState(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Wait one tick so Bukkit and other login listeners can finish applying the initial state.
        Bukkit.getScheduler().runTaskLater(this, () -> {
            boolean room1Pending = MainCommand.hasPendingRestore(player);
            boolean room2Pending = MainCommand2.hasPendingRestore(player);

            if (room1Pending && room2Pending) {
                getLogger().severe("Player " + player.getName()
                        + " has pending states for both rooms. Restoring Room 1 state and discarding the conflicting Room 2 snapshot.");
                MainCommand.restorePendingState(player);
                MainCommand2.discardPendingState(player);
                player.sendMessage("§eYour saved tournament state has been restored after reconnecting.");
            } else if (room1Pending && MainCommand.restorePendingState(player)) {
                player.sendMessage("§eYour saved tournament state has been restored after reconnecting.");
            } else if (room2Pending && MainCommand2.restorePendingState(player)) {
                player.sendMessage("§eYour saved tournament state has been restored after reconnecting.");
            }
        }, 1L);
    }

    /**
     * Requests an immediate server-side respawn for every player. This also works for legacy
     * clients (including 1.8) that do not support the newer doImmediateRespawn game event.
     * The one-tick delay is intentional: sending the respawn request inside PlayerDeathEvent
     * can race entity death processing and leave the client's camera/model out of sync.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onAnyPlayerDeath(PlayerDeathEvent event) {
        Player dead = event.getEntity();
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!dead.isOnline() || !dead.isDead()) {
                return; // The client or another plugin already completed the respawn.
            }
            try {
                dead.spigot().respawn();
            } catch (Throwable throwable) {
                getLogger().warning("Could not force respawn for " + dead.getName()
                        + "; the client may need to use the respawn control. "
                        + throwable.getClass().getSimpleName());
            }
        }, 1L);
    }

    @Override
    public void onDisable() {
        // Ask both rooms to return online participants through their normal leave flow.
        if (automaticListener != null) {
            automaticListener.destroy();
        }
        if (automatic2Listener != null) {
            automatic2Listener.destroy();
        }

        // Restore any state not handled by the normal leave flow. If legacy data exists
        // for both rooms, restore Room 1 once and discard Room 2's conflicting snapshot.
        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean room1HasState = MainCommand.hasSavedPlayerState(player.getName());
            boolean room2HasState = MainCommand2.hasSavedPlayerState(player.getName());

            if (room1HasState && room2HasState) {
                getLogger().severe("Conflicting saved tournament states for " + player.getName()
                        + ". Restoring Room 1 state and discarding Room 2 state.");
                MainCommand.restoreSavedState(player);
                MainCommand2.discardPendingState(player);
            } else if (room1HasState) {
                MainCommand.restoreSavedState(player);
            } else if (room2HasState) {
                MainCommand2.restoreSavedState(player);
            }
        }

        // Keep disconnected players' snapshots on disk for their next login.
        MainCommand.persistPendingRestoreStates();
        MainCommand2.persistPendingRestoreStates();

        if (torneiosb != null) {
            torneiosb.stop();
        }
        getLogger().info("PvPRounds disabled.");
    }
}
