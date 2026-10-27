package me.rafaelauler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.wavemc.core.bukkit.api.HelixActionBar;
import net.wavemc.core.bukkit.item.ItemBuilder;
import net.wavemc.core.util.UpdateEvent;

public class Automatic2 implements Listener {

    private final Main main;
    private int time;
    private GameType gameType;
    private boolean pairingScheduled;
    private boolean full;
    private boolean pvp;
    private boolean endingTournament;
    private BukkitTask victoryReturnTask;
    private BukkitTask victoryFireworksTask;

    /** Lista de participantes inscritos no torneio. */
    public final List<Player> players = new ArrayList<>();

    private final List<Player> playersInPvp = new ArrayList<>();
    private final List<Player> specs = new ArrayList<>();

    /** Players eliminated in a duel, finalized only after the client has respawned. */
    private final Map<UUID, Player> pendingEliminations = new HashMap<>();

    private int maxPlayers = 60;

    /*
     * Mantidos estáticos por compatibilidade com outras classes do plugin.
     * O projeto deve manter somente uma instância ativa de Automatic2.
     */
    public static boolean iniciou = false;
    public static boolean star = false;
    public static final List<String> playersIN = new ArrayList<>();

    public Automatic2() {
        this.main = Main.getInstance();
        this.time = 30;
        this.gameType = GameType.STARTING;
        this.pairingScheduled = false;
        this.full = false;
        this.pvp = false;
        this.endingTournament = false;
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getType() != UpdateEvent.UpdateType.SEGUNDO) {
            return;
        }

        // Do not restart the countdown while the winner celebration is active.
        if (endingTournament) {
            return;
        }

        // Limpa participantes desconectados. A saída normal é tratada pelos comandos.
        removeOfflinePlayers();

        if (star) {
            if (players.isEmpty() || MainCommand2.game.isEmpty()) {
                destroy();
                return;
            }

            if (playersInPvp.isEmpty()) {
                queuedPlayers();
            }
            return;
        }

        // Antes de iniciar, são necessários pelo menos dois participantes.
        if (players.size() < 2) {
            iniciou = false;
            time = 30;
            full = false;
            return;
        }

        iniciou = true;

        // Só valida o mundo do lobby antes do início; não expulsa quem está lutando.
        String spawnWorldName = Main.cfg_x1.getString("x2.coords.spawn.world");
        World spawnWorld = spawnWorldName == null ? null : Bukkit.getWorld(spawnWorldName);

        if (spawnWorld != null) {
            for (Player player : new ArrayList<>(players)) {
                if (player != null && player.isOnline() && player.getWorld() != spawnWorld) {
                    Bukkit.dispatchCommand(player, "pvprounds2 leave");
                }
            }
        }

        if (players.size() < 2) {
            iniciou = false;
            time = 30;
            return;
        }

        String startMessage = getConfigMessage("TournamentStart", "&eTournament starts in %time%s");
        for (Player player : new ArrayList<>(players)) {
            if (player != null && player.isOnline() && !playersInPvp.contains(player)) {
                HelixActionBar.send(player, color(startMessage).replace("%time%", String.valueOf(time)));
            }
        }

        // Mensagens de contagem regressiva, processadas uma vez por segundo.
        if (time == 30 || time == 15 || time == 10 || time == 5) {
            broadcast(color(startMessage).replace("%time%", String.valueOf(time)));
            broadcastJoinMessage(time);
        }

        if (players.size() >= maxPlayers && !full) {
            time = Math.max(time, 30);
            broadcast(color(getConfigMessage("TournamentTimeChanged", "&eThe tournament timer was extended.")));
            full = true;
        }

        if (time <= 0) {
            gameType = GameType.GAMIMG;
            broadcast(color(getConfigMessage("TournamentStarted", "&aThe tournament has started!")));
            star = true;
            pvp = false;
            time = 30;
            queuedPlayers();
            return;
        }

        time--;
    }

    private void broadcastJoinMessage(int seconds) {
        String template = getConfigMessage("TournamentStartGlobal", "&eTournament starts in %time%s");
        TextComponent component = new TextComponent(color(template).replace("%time%", String.valueOf(seconds)));

        String hover = color(getConfigMessage("ClickToJoin", "&aClick to join"));
        component.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(hover).create()
        ));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pvr2 join"));

        String worldName = Main.cfg_x1.getString("x2.coords.quit.world");
        World world = worldName == null ? null : Bukkit.getWorld(worldName);
        broadcast2(component, world);
    }

    public void putInEvent2(Player player) {
        putInEvent(player);
    }

    public void putInEvent(Player player) {
        if (player == null || !player.isOnline() || players.contains(player)) {
            return;
        }
        if (endingTournament || gameType == GameType.STOPPED) {
            player.sendMessage(color("&eThe tournament is finishing. Please wait before joining again."));
            return;
        }
        if (players.size() >= maxPlayers) {
            player.sendMessage("§cO torneio está cheio.");
            return;
        }

        players.add(player);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        for (PotionEffect effect : new ArrayList<>(player.getActivePotionEffects())) {
            player.removePotionEffect(effect.getType());
        }

        String message = getConfigMessage("PlayerJoinedMatch", "&a%player% joined the tournament.");
        broadcast(color(message).replace("%player%", player.getName()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        pendingEliminations.remove(player.getUniqueId());

        String playerName = player.getName();
        boolean trackedByTournament = players.contains(player) || playersInPvp.contains(player) || specs.contains(player);
        boolean trackedByCommand = MainCommand2.game.contains(playerName)
                || MainCommand2.hasSavedPlayerState(playerName) || MainCommand2.hasPendingRestore(player);
        if (!trackedByTournament && !trackedByCommand) {
            return;
        }

        // Remove stale command-list entries while preserving saved state for reconnect.
        MainCommand2.markPendingRestore(player);
        if (!trackedByTournament) {
            return;
        }
        boolean wasFighting = playersInPvp.contains(player);
        Player opponent = null;
        if (wasFighting) {
            for (Player fighter : new ArrayList<>(playersInPvp)) {
                if (fighter != null && !fighter.equals(player)) {
                    opponent = fighter;
                    break;
                }
            }
        }

        players.remove(player);
        specs.remove(player);

        if (wasFighting) {
            // This tournament runs one duel at a time. End the duel for both players
            // so the remaining fighter can return to the queue instead of stalling it.
            playersInPvp.clear();
            pvp = false;
            String message = getConfigMessage("PlayerLeaveServerDeath", "&c%player% left the fight.");
            broadcast(color(message).replace("%player%", player.getName()));

            if (opponent != null && opponent.isOnline()) {
                opponent.sendMessage(color("&eYour opponent disconnected. You will be placed back in the tournament queue."));
            }
        } else {
            String message = getConfigMessage("PlayerLeaveServer", "&e%player% left the tournament.");
            broadcast(color(message).replace("%player%", player.getName()));
        }

        if (star) {
            // Schedule after PlayerQuitEvent finishes, then let queuedPlayers decide
            // whether to start another duel, celebrate the final winner, or destroy an empty tournament.
            Bukkit.getScheduler().runTask(main, () -> {
                if (!star || gameType != GameType.GAMIMG) {
                    return;
                }
                removeOfflinePlayers();
                queuedPlayers();
            });
        } else if (players.size() < 2) {
            iniciou = false;
            time = 30;
        }
    }

    public static void broadcast2(TextComponent text, World world) {
        if (text == null || world == null) {
            return;
        }
        for (Player player : world.getPlayers()) {
            player.spigot().sendMessage(text);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player dead = event.getEntity();

        if (!star || !players.contains(dead) || !playersInPvp.contains(dead)) {
            return;
        }

        event.getDrops().clear();
        Player killer = dead.getKiller();
        Player opponent = null;

        for (Player fighter : new ArrayList<>(playersInPvp)) {
            if (fighter != null && !fighter.equals(dead) && fighter.isOnline()) {
                opponent = fighter;
                break;
            }
        }

        playersInPvp.remove(dead);
        playersInPvp.remove(opponent);
        players.remove(dead);
        pvp = false;

        if (killer != null) {
            dead.sendMessage(color(getConfigMessage("PlayerKilledMessage", "&cYou were eliminated, %player%."))
                    .replace("%player%", dead.getName()));
            String broadcast = getConfigMessage("PlayerKilledBroadcast", "&c%player% was defeated by %killer%.");
            broadcast(color(broadcast).replace("%player%", dead.getName()).replace("%killer%", killer.getName()));
        }

        String leftMessage = getConfigMessage("PlayersLeft", "&ePlayers remaining: %left%");
        broadcast(color(leftMessage).replace("%left%", String.valueOf(players.size())));

        // The respawn request is handled one tick later by Main for every player on the server.
        // Keep this participant pending until PlayerRespawnEvent fires. Teleporting/restoring
        // inventory before that event can leave 1.8 clients with a stale camera/model.
        pendingEliminations.put(dead.getUniqueId(), opponent);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTournamentPlayerRespawn(PlayerRespawnEvent event) {
        Player dead = event.getPlayer();
        UUID uuid = dead.getUniqueId();
        if (!pendingEliminations.containsKey(uuid)) {
            return;
        }

        // Let the respawn packet, entity recreation and initial position update reach the client
        // before leaving the tournament and matchmaking the surviving fighter again.
        Bukkit.getScheduler().runTaskLater(main, () -> {
            if (!pendingEliminations.containsKey(uuid)) {
                return;
            }
            Player winnerOfFight = pendingEliminations.remove(uuid);
            if (dead.isOnline() && MainCommand2.game.contains(dead.getName())) {
                Bukkit.dispatchCommand(dead, "pvprounds2 leave");
            }
            if (winnerOfFight != null && winnerOfFight.isOnline()) {
                broadcast(color(getConfigMessage("Searching", "&eSearching for the next opponent...")));
            }
            queuedPlayers();
        }, 2L);
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTournamentGameModeChange(PlayerGameModeChangeEvent event) {
        if (players.contains(event.getPlayer()) && event.getNewGameMode() != GameMode.SURVIVAL) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(color(getConfigMessage(
                    "CommandBlockedInBattle", "&cGame mode changes are disabled during the tournament.")));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTournamentFlightToggle(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (!players.contains(player)) {
            return;
        }
        event.setCancelled(true);
        if (player.isFlying()) {
            player.setFlying(false);
        }
        if (player.getAllowFlight()) {
            player.setAllowFlight(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        Player damager = resolvePlayerDamager(event.getDamager());

        boolean victimInTournament = players.contains(victim) || specs.contains(victim);
        boolean damagerInTournament = damager != null
                && (players.contains(damager) || specs.contains(damager));

        // Leave unrelated PvP elsewhere on the server untouched.
        if (!victimInTournament && !damagerInTournament) {
            return;
        }

        // Spectators cannot deal or receive combat damage.
        if (specs.contains(victim) || (damager != null && specs.contains(damager))) {
            event.setCancelled(true);
            return;
        }

        // Within this room, damage is allowed only between the two currently paired fighters.
        // This also cancels arrows/tridents/etc. fired by outsiders at tournament players,
        // and prevents tournament players from attacking people outside their active duel.
        boolean validDuelDamage = damager != null
                && !damager.equals(victim)
                && star
                && gameType == GameType.GAMIMG
                && players.contains(victim)
                && players.contains(damager)
                && playersInPvp.contains(victim)
                && playersInPvp.contains(damager);

        if (!validDuelDamage) {
            event.setCancelled(true);
        }
    }

    private Player resolvePlayerDamager(org.bukkit.entity.Entity damagerEntity) {
        if (damagerEntity instanceof Player) {
            return (Player) damagerEntity;
        }

        if (damagerEntity instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) damagerEntity).getShooter();
            if (shooter instanceof Player) {
                return (Player) shooter;
            }
        }
        return null;
    }

    @EventHandler
    public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (!isInEvent(player)) {
            return;
        }

        String command = event.getMessage().toLowerCase(Locale.ROOT);
        boolean isLeaveCommand = command.equals("/pvr2 leave")
                || command.equals("/pvprounds2 leave");

        if (isLeaveCommand && !isInPvP(player)) {
            return;
        }

        if (isInPvP(player) && iniciou) {
            event.setCancelled(true);
            player.sendMessage(color(getConfigMessage("CommandBlockedInBattle", "&cYou cannot use commands during a fight.")));
            return;
        }

        if (!player.hasPermission("pvprounds.bypass") && iniciou
                && !command.startsWith("/tell ")
                && !command.startsWith("/msg ")
                && !command.startsWith("/pvprounds2")
                && !command.startsWith("/pvr2 ")) {
            event.setCancelled(true);
            player.sendMessage(color(getConfigMessage("CommandBlocked", "&cCommands are disabled during the tournament.")));
        }
    }

    public boolean isInEvent(Player player) {
        return player != null && players.contains(player);
    }

    public void removeFromEvent(Player player) {
        if (player == null) {
            return;
        }
        players.remove(player);
        playersInPvp.remove(player);
        specs.remove(player);
    }

    public void queuedPlayers() {
        if (pairingScheduled || !star || gameType != GameType.GAMIMG || !playersInPvp.isEmpty()) {
            return;
        }

        pairingScheduled = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                pairingScheduled = false;
                removeOfflinePlayers();

                if (!star || gameType != GameType.GAMIMG) {
                    return;
                }

                if (players.size() < 2) {
                    if (players.size() == 1) {
                        Player winner = players.get(0);
                        finishWithVictory(winner);
                    } else {
                        destroy();
                    }
                    return;
                }

                Collections.shuffle(players);
                Player first = players.get(0);
                Player second = players.get(1);

                if (first == null || second == null || !first.isOnline() || !second.isOnline()) {
                    queuedPlayers();
                    return;
                }

                send1v1(first, second);
            }
        }.runTaskLater(main, 20L);
    }

    private void removeOfflinePlayers() {
        Iterator<Player> iterator = players.iterator();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            if (player == null || !player.isOnline()) {
                boolean wasFighting = player != null && playersInPvp.contains(player);
                if (player != null) {
                    MainCommand2.markPendingRestore(player);
                }
                iterator.remove();
                if (wasFighting) {
                    // One offline fighter invalidates the current duel; release the opponent too.
                    playersInPvp.clear();
                    pvp = false;
                } else if (player != null) {
                    playersInPvp.remove(player);
                }
                if (player != null) {
                    specs.remove(player);
                }
            }
        }
    }

    public void broadcast(String message) {
        String prefix = color(getConfigMessage("Prefix", ""));
        String finalMessage = prefix + (message == null ? "" : message);

        for (Player player : new ArrayList<>(players)) {
            if (player != null && player.isOnline()) {
                player.sendMessage(finalMessage);
                TitleAPI.sendTitle(player, 40, 40, 40, prefix, message == null ? "" : message);
            }
        }

        for (Player player : new ArrayList<>(specs)) {
            if (player != null && player.isOnline()) {
                player.sendMessage(finalMessage);
                TitleAPI.sendTitle(player, 40, 40, 40, prefix, message == null ? "" : message);
            }
        }
    }

    public void send1v1(Player firstPlayer, Player secondPlayer) {
        if (firstPlayer == null || secondPlayer == null || firstPlayer.equals(secondPlayer)
                || !firstPlayer.isOnline() || !secondPlayer.isOnline()) {
            return;
        }

        String world1Name = Main.cfg_x1.getString("x2.coords.loc_1.world");
        String world2Name = Main.cfg_x1.getString("x2.coords.loc_2.world");
        World world1 = world1Name == null ? null : Bukkit.getWorld(world1Name);
        World world2 = world2Name == null ? null : Bukkit.getWorld(world2Name);

        if (world1 == null || world2 == null) {
            Bukkit.getLogger().severe("[PvPRounds] Room world not found for room #2. Verify 1v1.yml in PvPRounds folder.");
            return;
        }

        Location location1 = new Location(
                world1,
                Main.cfg_x1.getDouble("x2.coords.loc_1.x"),
                Main.cfg_x1.getDouble("x2.coords.loc_1.y"),
                Main.cfg_x1.getDouble("x2.coords.loc_1.z"),
                (float) Main.cfg_x1.getDouble("x2.coords.loc_1.yaw"),
                (float) Main.cfg_x1.getDouble("x2.coords.loc_1.pitch")
        );
        Location location2 = new Location(
                world2,
                Main.cfg_x1.getDouble("x2.coords.loc_2.x"),
                Main.cfg_x1.getDouble("x2.coords.loc_2.y"),
                Main.cfg_x1.getDouble("x2.coords.loc_2.z"),
                (float) Main.cfg_x1.getDouble("x2.coords.loc_2.yaw"),
                (float) Main.cfg_x1.getDouble("x2.coords.loc_2.pitch")
        );

        playersInPvp.clear();
        playersInPvp.add(firstPlayer);
        playersInPvp.add(secondPlayer);

        // Normalize the player state before teleporting; changing game mode only after the
        // teleport can leave legacy clients with stale camera/entity state for a few ticks.
        resetPlayerBeforeTeleport(firstPlayer);
        resetPlayerBeforeTeleport(secondPlayer);
        clearEffects(firstPlayer);
        clearEffects(secondPlayer);

        firstPlayer.setHealth(20.0D);
        secondPlayer.setHealth(20.0D);
        firstPlayer.setFoodLevel(20);
        secondPlayer.setFoodLevel(20);
        firstPlayer.setFireTicks(0);
        secondPlayer.setFireTicks(0);

        // Não iniciar a luta se o Paper rejeitar algum teleporte.
        boolean firstTeleported = firstPlayer.teleport(location1);
        boolean secondTeleported = secondPlayer.teleport(location2);
        if (!firstTeleported || !secondTeleported) {
            playersInPvp.clear();
            pvp = false;
            Bukkit.getLogger().warning("[PvPRounds] Falha ao teleportar um jogador da sala #2; luta cancelada.");
            return;
        }

        // ViaRewind/ViaVersion podem deixar clientes 1.8.8 com a posição local
        // dessincronizada após o teleporte. Reenvia a posição autoritativa do
        // servidor alguns ticks depois, já com o jogador preparado para a luta.
        Bukkit.getScheduler().runTaskLater(main, () -> {
            if (!firstPlayer.isOnline() || !secondPlayer.isOnline()) {
                return;
            }
            firstPlayer.teleport(location1);
            secondPlayer.teleport(location2);
            firstPlayer.setFallDistance(0.0F);
            secondPlayer.setFallDistance(0.0F);
            firstPlayer.updateInventory();
            secondPlayer.updateInventory();
        }, 2L);

        broadcast(color(getConfigMessage("Fight", "&e%player1% vs %player2%"))
                .replace("%player1%", firstPlayer.getName())
                .replace("%player2%", secondPlayer.getName()));

        firstPlayer.sendMessage(color(getConfigMessage("FightAgainst", "&eYour opponent is %player%"))
                .replace("%player%", secondPlayer.getName()));
        secondPlayer.sendMessage(color(getConfigMessage("FightAgainst", "&eYour opponent is %player%"))
                .replace("%player%", firstPlayer.getName()));

        prepareFighter(firstPlayer);
        prepareFighter(secondPlayer);
        pvp = true;
    }

    private void resetPlayerBeforeTeleport(Player player) {
        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setSpectatorTarget(null);
        }
        if (player.isFlying()) {
            player.setFlying(false);
        }
        player.setAllowFlight(false);
        player.setGameMode(GameMode.SURVIVAL);
        player.setVelocity(new org.bukkit.util.Vector(0.0D, 0.0D, 0.0D));
        player.setFallDistance(0.0F);
        player.setFireTicks(0);
    }

    private void prepareFighter(Player player) {
        // Reapply state after teleport as an additional client synchronization safeguard.
        resetPlayerBeforeTeleport(player);

        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.closeInventory();

        player.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
        player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));

        String swordName = color(getConfigMessage("SwordName", "&bTournament Sword"));
        ItemStack sword = new ItemBuilder(swordName, Material.DIAMOND_SWORD)
                .addEnchant(Enchantment.SHARPNESS, 1)
                .nbt("cancel-drop")
                .toStack();

        player.getInventory().setItem(0, sword);
        for (int i = 0; i < 34; i++) {
            player.getInventory().addItem(new ItemStack(Material.MUSHROOM_STEW));
        }
        player.updateInventory();
    }

    private void clearEffects(Player player) {
        for (PotionEffect effect : new ArrayList<>(player.getActivePotionEffects())) {
            player.removePotionEffect(effect.getType());
        }
    }

    public void setGameType(GameType newType) {
        this.gameType = newType;
    }

    public GameType getGameType() {
        return this.gameType;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Player> getPlayersInPvp() {
        return playersInPvp;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public boolean isInPvP(Player player) {
        return player != null && playersInPvp.contains(player) && gameType == GameType.GAMIMG;
    }

    /**
     * Celebra o vencedor do segundo torneio e só depois o devolve ao lobby.
     * O tempo da comemoração é configurado em VictoryDelay (segundos).
     */
    private void finishWithVictory(Player winner) {
        if (endingTournament || winner == null || !winner.isOnline()) {
            if (!endingTournament) {
                destroy();
            }
            return;
        }

        endingTournament = true;
        star = false;
        iniciou = false;
        pairingScheduled = false;
        pvp = false;
        gameType = GameType.STOPPED;
        playersInPvp.clear();

        String winnerName = winner.getName();
        String title = color(getConfigMessage("VictoryTitle", "&6&lVITÓRIA!"));
        String subtitle = color(getConfigMessage("VictorySubtitle", "&e%player% venceu o PvP Tournament!"))
                .replace("%player%", winnerName);
        String victoryMessage = color(getConfigMessage("VictoryMessage", "&a%player% venceu o PvP Tournament!"))
                .replace("%player%", winnerName);
        String broadcastMessage = color(getConfigMessage("WinnerBroadcast",
                "&6&lPvPRounds 2 &8» &eO vencedor é &a%player%&e!"))
                .replace("%player%", winnerName);

        Bukkit.broadcastMessage(broadcastMessage);
        winner.sendMessage(victoryMessage);
        winner.sendTitle(title, subtitle, 10, 80, 20);

        Coins.addCoins(winner, 10000);
        String soundName = getConfigMessage("VictorySound", "UI_TOAST_CHALLENGE_COMPLETE");
        try {
            winner.playSound(winner.getLocation(), Sound.valueOf(soundName.toUpperCase(Locale.ROOT)), 1.0f, 1.0f);
        } catch (IllegalArgumentException ignored) {
            Bukkit.getLogger().warning("[PvPRounds2] VictorySound inválido: " + soundName);
        }

        // Fogos opcionais durante a comemoração.
        if (main != null && main.getConfig().getBoolean("VictoryFireworks", true)) {
            victoryFireworksTask = new BukkitRunnable() {
                private int launches = 0;

                @Override
                public void run() {
                    if (!winner.isOnline() || launches >= 5) {
                        cancel();
                        return;
                    }

                    Location location = winner.getLocation().clone().add(0, 1, 0);
                    World world = location.getWorld();
                    if (world != null) {
                        Firework firework = world.spawn(location, Firework.class);
                        FireworkMeta meta = firework.getFireworkMeta();
                        meta.setPower(1);
                        meta.addEffect(FireworkEffect.builder()
                                .with(FireworkEffect.Type.BALL_LARGE)
                                .withColor(Color.YELLOW, Color.ORANGE, Color.LIME)
                                .withFade(Color.WHITE)
                                .trail(true)
                                .flicker(true)
                                .build());
                        firework.setFireworkMeta(meta);
                    }
                    launches++;
                }
            }.runTaskTimer(main, 0L, 20L);
        }

        long victoryDelaySeconds = Math.max(0L,
                main != null && main.getConfig() != null
                        ? main.getConfig().getLong("VictoryDelay", 5L) : 5L);

        // Ao terminar o delay, devolve os participantes ao lobby.
        victoryReturnTask = Bukkit.getScheduler().runTaskLater(main, () -> {
            victoryReturnTask = null;
            if (victoryFireworksTask != null) {
                victoryFireworksTask.cancel();
                victoryFireworksTask = null;
            }
            if (!endingTournament) {
                return;
            }
            if (winner.isOnline()) {
                winner.sendMessage(color(getConfigMessage("VictoryReturnMessage",
                        "&eA comemoração terminou. Você será enviado ao lobby.")));
            }
            destroy();
            endingTournament = false;
        }, victoryDelaySeconds * 20L);
    }

    public boolean isEndingTournament() {
        return endingTournament;
    }

    public void destroy() {
        // Cancel pending celebration tasks so an old timer cannot destroy a new tournament.
        if (victoryReturnTask != null) {
            victoryReturnTask.cancel();
            victoryReturnTask = null;
        }
        if (victoryFireworksTask != null) {
            victoryFireworksTask.cancel();
            victoryFireworksTask = null;
        }
        endingTournament = false;

        // Marca o torneio como encerrado antes de executar os comandos de saída.
        star = false;
        iniciou = false;
        pairingScheduled = false;
        gameType = GameType.STARTING;
        pvp = false;

        for (Player player : new ArrayList<>(players)) {
            if (player == null || !player.isOnline()) {
                continue;
            }
            if (MainCommand2.game.contains(player.getName())) {
                Bukkit.dispatchCommand(player, "pvprounds2 leave");
            } else {
                // Recover participants if the command list became inconsistent.
                MainCommand2.restoreSavedState(player);
            }
        }

        players.clear();
        playersInPvp.clear();
        specs.clear();
        time = 30;
        full = false;

        // Não zera o EventManager: a instância persistente continua registrada e pode reiniciar.
    }

    public void setMaxPlayers(int maxPlayers) {
        if (maxPlayers < 2) {
            throw new IllegalArgumentException("maxPlayers deve ser pelo menos 2.");
        }
        this.maxPlayers = maxPlayers;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = Math.max(0, time);
    }

    public void desmakeVanish(Player player) {
        if (player == null) {
            return;
        }
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player)) {
                online.showPlayer(player);
            }
        }
    }

    public List<Player> getSpecs() {
        return specs;
    }

    public boolean isSpec(Player player) {
        return player != null && specs.contains(player);
    }

    private String getConfigMessage(String path, String fallback) {
        if (main == null || main.getConfig() == null) {
            return fallback;
        }
        String value = main.getConfig().getString(path);
        return value == null ? fallback : value;
    }

    private String color(String text) {
        return text == null ? "" : text.replace('&', '§');
    }

    public enum GameType {
        STARTING,
        GAMIMG,
        STOPPED
    }
}
