package me.rafaelauler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.scoreboard.Scoreboard;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;

/**
 * Scoreboard configurável dos torneios PvPRounds usando a API do TAB 6.12.
 *
 * Configuração no config.yml:
 * torneio-scoreboard:
 *   enabled: true
 *   update-interval-ticks: 20
 *   title: "&b&lSKYZERMC"
 *   site: "&bskyzermc.com.br"
 *   lines:
 *     - "&8&m----------------"
 *     - "&bTorneio &f#{torneio}"
 *     - " "
 *     - "&7Estado: {estado}"
 *     - "&7Inscritos: &f{inscritos}/{maximo}"
 *     - "&7Na fila: &e{fila}"
 *     - "&7Em combate: &c{combates}"
 *     - "&7Espectadores: &f{espectadores}"
 *     - " "
 *     - "&7Começa em: &e{tempo}s"
 *     - "&7Adversário: &c{adversario}"
 *     - "&7Sua vida: {vida}"
 *     - "{site}"
 *
 * Placeholders próprios disponíveis nas linhas:
 * {torneio}, {jogador}, {estado}, {inscritos}, {maximo}, {fila},
 * {combates}, {espectadores}, {tempo}, {adversario}, {vida}, {site}.
 */
public final class TorneioScoreboard {

    private final Plugin plugin;
    private final FileConfiguration config;
    private final Automatic torneio1;
    private final Automatic2 torneio2;

    private final Map<UUID, Scoreboard> scoreboards = new HashMap<>();
    private final Map<UUID, String> torneioDoJogador = new HashMap<>();

    private BukkitTask task;
    private ScoreboardManager scoreboardManager;

    public TorneioScoreboard(Plugin plugin, Automatic torneio1, Automatic2 torneio2) {
        this(plugin, torneio1, torneio2, plugin == null ? null : plugin.getConfig());
    }

    public TorneioScoreboard(Plugin plugin, Automatic torneio1, Automatic2 torneio2, FileConfiguration config) {
        if (plugin == null) {
            throw new IllegalArgumentException("plugin não pode ser null");
        }
        if (config == null) {
            throw new IllegalArgumentException("configuração da scoreboard não pode ser null");
        }
        if (torneio1 == null) {
            throw new IllegalArgumentException("Automatic não pode ser null");
        }
        if (torneio2 == null) {
            throw new IllegalArgumentException("Automatic2 não pode ser null");
        }

        this.plugin = plugin;
        this.config = config;
        this.torneio1 = torneio1;
        this.torneio2 = torneio2;
    }

    /** Inicia a atualização automática. Chame uma única vez no onEnable. */
    public void start() {
        if (task != null) {
            return;
        }

        if (!config.getBoolean("torneio-scoreboard.enabled", true)) {
            plugin.getLogger().info("Scoreboard do torneio desativada na configuração.");
            return;
        }

        if (Bukkit.getPluginManager().getPlugin("TAB") == null
                || !Bukkit.getPluginManager().isPluginEnabled("TAB")) {
            plugin.getLogger().warning("TAB não encontrado ou não está habilitado; scoreboard do torneio não iniciada.");
            return;
        }

        try {
            scoreboardManager = TabAPI.getInstance().getScoreboardManager();
        } catch (Throwable ex) {
            plugin.getLogger().severe("Não foi possível acessar a API do TAB: " + ex.getMessage());
            return;
        }

        if (scoreboardManager == null) {
            plugin.getLogger().warning("A feature scoreboard está desativada na configuração do TAB.");
            return;
        }

        long intervalo = Math.max(1L,
                config.getLong("torneio-scoreboard.update-interval-ticks", 20L));
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, intervalo);
    }

    /** Para a tarefa e restaura a scoreboard padrão do TAB para os jogadores. */
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        for (UUID uuid : new ArrayList<>(scoreboards.keySet())) {
            remover(uuid);
        }
    }

    /** Atualiza a scoreboard imediatamente, se necessário. */
    public void atualizarAgora() {
        tick();
    }

    /** Recarrega as opções do config.yml na próxima atualização. */
    public void recarregar() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        stop();
        start();
    }

    private void tick() {
        if (scoreboardManager == null || !Bukkit.getPluginManager().isPluginEnabled("TAB")) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean noTorneio1 = identificarTorneio1(player);
            boolean noTorneio2 = identificarTorneio2(player);

            // Se houver estado duplicado por engano, prioriza o torneio 1.
            if (noTorneio1) {
                atualizarJogador(player, torneio1, null, "1");
            } else if (noTorneio2) {
                atualizarJogador(player, null, torneio2, "2");
            } else {
                remover(player.getUniqueId());
            }
        }

        for (UUID uuid : new ArrayList<>(scoreboards.keySet())) {
            if (Bukkit.getPlayer(uuid) == null) {
                remover(uuid);
            }
        }
    }

    private boolean identificarTorneio1(Player player) {
        return torneio1.getPlayers().contains(player)
                || torneio1.getPlayersInPvp().contains(player)
                || torneio1.getSpecs().contains(player);
    }

    private boolean identificarTorneio2(Player player) {
        return torneio2.getPlayers().contains(player)
                || torneio2.getPlayersInPvp().contains(player)
                || torneio2.getSpecs().contains(player);
    }

    private void atualizarJogador(Player player, Automatic t1, Automatic2 t2, String idTorneio) {
        UUID uuid = player.getUniqueId();
        String idAnterior = torneioDoJogador.get(uuid);

        if (idAnterior != null && !idAnterior.equals(idTorneio)) {
            remover(uuid);
        }

        Scoreboard board = scoreboards.get(uuid);
        boolean novaScoreboard = board == null;

        if (board == null) {
            List<String> linhasIniciais = Arrays.asList("Carregando...", " ");
            String nome = "pvr_" + idTorneio + "_" + uuid.toString().replace("-", "");
            board = scoreboardManager.createScoreboard(nome, colorizar(titulo()), linhasIniciais);
            if (board == null) {
                return;
            }
            scoreboards.put(uuid, board);
            torneioDoJogador.put(uuid, idTorneio);
        }

        TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(uuid);
        if (tabPlayer == null) {
            return;
        }

        // Atualiza título e linhas ANTES de exibir a scoreboard. Isso evita que o
        // cliente receba inicialmente apenas o título enquanto as linhas ainda
        // estão vazias/carregando.
        board.setTitle(colorizar(titulo()));
        board.setLines(criarLinhas(player, t1, t2, idTorneio));

        if (novaScoreboard || !scoreboardManager.hasCustomScoreboard(tabPlayer)) {
            scoreboardManager.showScoreboard(tabPlayer, board);
        }
    }

    private List<String> criarLinhas(Player player, Automatic t1, Automatic2 t2, String idTorneio) {
        boolean segundo = t2 != null;
        List<Player> inscritos = segundo ? t2.getPlayers() : t1.getPlayers();
        List<Player> emCombate = segundo ? t2.getPlayersInPvp() : t1.getPlayersInPvp();
        List<Player> espectadores = segundo ? t2.getSpecs() : t1.getSpecs();
        int maximo = segundo ? t2.getMaxPlayers() : t1.getMaxPlayers();
        int tempo = segundo ? t2.getTime() : t1.getTime();
        boolean iniciou = segundo ? Automatic2.iniciou : Automatic.iniciou;
        boolean comecou = segundo ? Automatic2.star : Automatic.star;
        boolean espectador = espectadores.contains(player);
        boolean lutando = emCombate.contains(player);

        Player adversario = lutando ? encontrarAdversario(player, emCombate) : null;
        if (espectador && emCombate.size() >= 2) {
            adversario = emCombate.get(0);
        }

        Map<String, String> valores = new HashMap<>();
        valores.put("{torneio}", idTorneio);
        valores.put("{jogador}", player.getName());
        valores.put("{estado}", estado(iniciou, comecou, espectador, lutando));
        valores.put("{inscritos}", String.valueOf(inscritos.size()));
        valores.put("{maximo}", String.valueOf(maximo));
        valores.put("{fila}", String.valueOf(Math.max(0, inscritos.size() - emCombate.size())));
        valores.put("{combates}", String.valueOf(emCombate.size() / 2));
        valores.put("{espectadores}", String.valueOf(espectadores.size()));
        valores.put("{tempo}", String.valueOf(Math.max(0, tempo)));
        valores.put("{adversario}", adversario == null ? "—" : limitar(adversario.getName(), 16));
        valores.put("{vida}", lutando ? vida(player) : "—");
        valores.put("{site}", config.getString("torneio-scoreboard.site", "&bskyzermc.com.br"));

        List<String> configuradas = config.getStringList("torneio-scoreboard.lines");
        if (configuradas == null || configuradas.isEmpty()) {
            configuradas = linhasPadrao();
        }

        List<String> linhas = new ArrayList<>();
        for (String linha : configuradas) {
            if (linha == null) {
                linhas.add(" ");
                continue;
            }

            String resultado = linha;
            for (Map.Entry<String, String> entry : valores.entrySet()) {
                resultado = resultado.replace(entry.getKey(), entry.getValue());
            }
            linhas.add(colorizar(resultado));
        }

        // A sidebar do Minecraft suporta até 15 linhas. Limita para evitar excesso.
        if (linhas.size() > 15) {
            linhas = new ArrayList<>(linhas.subList(0, 15));
        }
        return linhas;
    }

    private List<String> linhasPadrao() {
        return Arrays.asList(
                "&8&m----------------",
                "&bTorneio &f#{torneio}",
                " ",
                "&7Estado: {estado}",
                "&7Inscritos: &f{inscritos}/{maximo}",
                "&7Na fila: &e{fila}",
                "&7Em combate: &c{combates}",
                "&7Espectadores: &f{espectadores}",
                " ",
                "&7Tempo: &e{tempo}s",
                "&7Adversário: &c{adversario}",
                "&7Sua vida: {vida}",
                "{site}"
        );
    }

    private String titulo() {
        return config.getString("torneio-scoreboard.title", "&b&lSKYZERMC");
    }

    private String estado(boolean iniciou, boolean comecou, boolean espectador, boolean lutando) {
        if (espectador) return "&dEspectando";
        if (lutando) return "&cEm combate";
        if (comecou) return "&eEm andamento";
        if (iniciou) return "&aPreparando";
        return "&7Aguardando";
    }

    private Player encontrarAdversario(Player player, List<Player> emCombate) {
        for (Player participante : emCombate) {
            if (participante != null && !participante.getUniqueId().equals(player.getUniqueId())) {
                return participante;
            }
        }
        return null;
    }

    private String vida(Player player) {
        double hp = Math.max(0.0D, Math.min(20.0D, player.getHealth()));
        String cor = hp > 12.0D ? "&a" : hp > 6.0D ? "&e" : "&c";
        return colorizar(cor + String.format(Locale.US, "%.1f", hp) + "❤");
    }

    private String limitar(String texto, int limite) {
        if (texto == null) return "-";
        return texto.length() <= limite ? texto : texto.substring(0, limite);
    }

    private String colorizar(String texto) {
        if (texto == null) return "";
        return ChatColor.translateAlternateColorCodes('&', texto);
    }

    private void remover(UUID uuid) {
        Scoreboard board = scoreboards.remove(uuid);
        torneioDoJogador.remove(uuid);

        TabPlayer tabPlayer = null;
        try {
            if (TabAPI.getInstance() != null) {
                tabPlayer = TabAPI.getInstance().getPlayer(uuid);
            }
        } catch (Throwable ignored) {
            // TAB pode estar descarregando durante o desligamento.
        }

        if (tabPlayer != null && scoreboardManager != null) {
            try {
                if (scoreboardManager.hasCustomScoreboard(tabPlayer)) {
                    scoreboardManager.resetScoreboard(tabPlayer);
                }
            } catch (Throwable ex) {
                plugin.getLogger().fine("Não foi possível restaurar a scoreboard de " + uuid + ": "
                        + ex.getMessage());
            }
        }

        if (board != null) {
            try {
                board.unregister();
            } catch (Throwable ignored) {
                // Pode já ter sido desregistrada pelo TAB.
            }
        }
    }
}
