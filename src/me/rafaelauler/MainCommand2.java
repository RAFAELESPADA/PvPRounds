package me.rafaelauler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;

public class MainCommand2 implements CommandExecutor, Listener {

    /*
     * These public maps are kept for compatibility with other classes in the plugin.
     * New code should use saveeffects to preserve every active potion effect.
     */
    public static final Map<String, ItemStack[]> saveinv = new HashMap<>();
    public static final Map<String, ItemStack[]> savearmor = new HashMap<>();
    public static final Map<String, Location> saveworld = new HashMap<>();
    public static final Map<String, GameMode> savegamemode = new HashMap<>();
    public static final Map<String, Scoreboard> savescore = new HashMap<>();
    public static final Map<String, Integer> savelevel = new HashMap<>();
    public static final Map<String, Float> saveexp = new HashMap<>();
    public static final Map<String, Integer> savehunger = new HashMap<>();
    public static final Map<String, Integer> saveair = new HashMap<>();
    public static final Map<String, Float> saveexhaustion = new HashMap<>();
    public static final Map<String, Integer> savefireticks = new HashMap<>();
    public static final Map<String, Double> savehealth = new HashMap<>();
    public static final Map<String, Double> saveabsorption = new HashMap<>();
    public static final Map<String, Double> savemaxhealth = new HashMap<>();
    public static final Map<String, Double> savemaxabsorption = new HashMap<>();
    public static final Map<String, Integer> savetotalexperience = new HashMap<>();
    public static final Map<String, Boolean> saveallowflight = new HashMap<>();
    public static final Map<String, Boolean> saveflying = new HashMap<>();
    public static final Map<String, Float> savewalkspeed = new HashMap<>();
    public static final Map<String, Float> saveflyspeed = new HashMap<>();
    private static final Set<String> pendingRestore = new HashSet<>();
    public static final Map<String, Float> savesaturation = new HashMap<>();
    public static final Map<String, List<PotionEffect>> saveeffects = new HashMap<>();

    @Deprecated
    public static final Map<String, PotionEffect> saveeffect = new HashMap<>();

    public static final ArrayList<String> game = new ArrayList<>();
    public static final ArrayList<Player> player = new ArrayList<>();

    /*
     * Retained from the original class for compatibility. These fields are not
     * required for the join/leave flow.
     */
    private final List<String> commands = Arrays.asList(
            "admin", "list", "create", "delete", "1v1", "score", "setspawn",
            "spawn", "join", "leave", "reset", "coins", "setchallenge", "kit",
            "kitunlocker", "shop", "resetkit", "stats", "reload", "update"
    );
    private final Map<String, Location> maps = new HashMap<>();

    private final Main plugin;

    public MainCommand2() {
        this(Main.getInstance());
    }

    public MainCommand2(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!commandLabel.equalsIgnoreCase("pvprounds2")
                && !commandLabel.equalsIgnoreCase("pvr2")) {
            return false;
        }

        if (args.length == 0) {
            sendHelp(sender, commandLabel);
            return true;
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);

        switch (subCommand) {
            case "join":
                return join(sender);

            case "leave":
                return leave(sender);

            case "stop":
                if (!sender.hasPermission("pvprounds.admin")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to stop the Room 2 tournament.");
                    return true;
                }

                Automatic2 automatic = getAutomatic2();
                if (automatic == null) {
                    sender.sendMessage(color("&cThe Room 2 tournament is not available."));
                    return true;
                }

                automatic.destroy();
                sender.sendMessage(color("&4&l✖ &cYou stopped the PvPRounds tournament in Room 2."));
                return true;

            case "info":
                sendInfo(sender);
                return true;

            default:
                sendHelp(sender, commandLabel);
                return true;
        }
    }

    private boolean join(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(color("&cThis command can only be used by a player."));
            return true;
        }

        Player joiningPlayer = (Player) sender;
        String playerName = joiningPlayer.getName();

        Automatic2 automatic = getAutomatic2();
        if (automatic == null) {
            joiningPlayer.sendMessage(color("&cThe Room 2 tournament is not available right now."));
            return true;
        }

        if (game.contains(playerName) || automatic.getPlayers().contains(joiningPlayer)) {
            joiningPlayer.sendMessage(color("&cYou are already in PvPRounds Room 2."));
            return true;
        }

        if (pendingRestore.contains(playerName)) {
            joiningPlayer.sendMessage(color("&eYour previous tournament state is being restored. Please wait a moment."));
            return true;
        }

        if (hasSavedPlayerState(playerName)) {
            joiningPlayer.sendMessage(color("&eYour Room 2 player state is already registered or awaiting cleanup. Please contact an administrator if this persists."));
            return true;
        }

        if (MainCommand.isPlayerRegisteredOrPendingRestore(joiningPlayer)) {
            joiningPlayer.sendMessage(color("&cYou cannot join Room 2 while you are registered in or awaiting restoration from Room 1."));
            return true;
        }

        if (Automatic2.star || automatic.isEndingTournament()) {
            joiningPlayer.sendMessage(color(automatic.isEndingTournament()
                    ? "&eThe tournament is finishing. Please wait before joining."
                    : "&eThe tournament is already in progress. Please wait until it ends before joining."));
            return true;
        }

        if (automatic.getPlayers().size() >= automatic.getMaxPlayers()) {
            joiningPlayer.sendMessage(color("&cPvPRounds Room 2 is full."));
            return true;
        }

        if (Main.cfg_x1 == null) {
            joiningPlayer.sendMessage(color("&cThe PvPRounds configuration has not been loaded."));
            return true;
        }

        String spawnWorldName = Main.cfg_x1.getString("x2.coords.spawn.world");
        if (spawnWorldName == null || spawnWorldName.trim().isEmpty()) {
            joiningPlayer.sendMessage(color("&eThe Room 2 spawn has not been configured yet."));
            return true;
        }

        World spawnWorld = Bukkit.getWorld(spawnWorldName);
        if (spawnWorld == null) {
            joiningPlayer.sendMessage(color(
                    "&cThe Room 2 spawn world '" + spawnWorldName + "' is not loaded."
            ));
            return true;
        }

        Location spawn = new Location(
                spawnWorld,
                Main.cfg_x1.getDouble("x2.coords.spawn.x"),
                Main.cfg_x1.getDouble("x2.coords.spawn.y"),
                Main.cfg_x1.getDouble("x2.coords.spawn.z"),
                (float) Main.cfg_x1.getDouble("x2.coords.spawn.yaw"),
                (float) Main.cfg_x1.getDouble("x2.coords.spawn.pitch")
        );

        // Save the player's original state before putInEvent clears their inventory/effects.
        savePlayerState(joiningPlayer);

        automatic.putInEvent(joiningPlayer);

        // Only mark the player as joined if the tournament actually accepted them.
        if (!automatic.getPlayers().contains(joiningPlayer)) {
            clearSavedState(playerName);
            joiningPlayer.sendMessage(color("&cYou could not join the tournament. Please try again."));
            return true;
        }

        game.add(playerName);
        enforceTournamentRestrictions(joiningPlayer);
        joiningPlayer.teleport(spawn);

        joiningPlayer.getInventory().clear();
        joiningPlayer.getInventory().setArmorContents(new ItemStack[4]);
        joiningPlayer.setExp(0.0F);
        joiningPlayer.setLevel(0);
        joiningPlayer.setExhaustion(0.0F);
        joiningPlayer.setFireTicks(0);
        joiningPlayer.setFoodLevel(20);
        joiningPlayer.setRemainingAir(joiningPlayer.getMaximumAir());
        joiningPlayer.updateInventory();

        joiningPlayer.sendMessage(color(configMessage("Joined", "&eYou joined the tournament.")));

        String title = configMessage("JoinTitle", "&eYou joined");
        String subtitle = configMessage("JoinSubTitle", "&fThe tournament");
        TitleAPI.sendTitle(joiningPlayer, 80, 80, 80, color(title), color(subtitle));

        return true;
    }

    private boolean leave(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(color("&cThis command can only be used by a player."));
            return true;
        }

        Player leavingPlayer = (Player) sender;
        String playerName = leavingPlayer.getName();

        if (!game.contains(playerName)) {
            leavingPlayer.sendMessage(color("&cYou are not participating in PvPRounds Room 2."));
            return true;
        }

        Automatic2 automatic = getAutomatic2();
        if (automatic != null) {
            automatic.removeFromEvent(leavingPlayer);
        }

        game.remove(playerName);
        player.remove(leavingPlayer);

        restorePlayerState(leavingPlayer);
        leavingPlayer.sendMessage(color(configMessage("TournamentLeave", "&eYou left the tournament.")));

        if (automatic != null && Automatic2.star) {
            automatic.queuedPlayers();
        }

        return true;
    }

    /** Enforces fair tournament rules after the original state has been saved. */
    private void enforceTournamentRestrictions(Player player) {
        if (player.isFlying()) {
            player.setFlying(false);
        }
        player.setAllowFlight(false);
        player.setGameMode(GameMode.SURVIVAL);
    }

    private void savePlayerState(Player player) {
        String name = player.getName();

        saveworld.put(name, player.getLocation().clone());
        saveinv.put(name, cloneItems(player.getInventory().getContents()));
        savearmor.put(name, cloneItems(player.getInventory().getArmorContents()));
        savegamemode.put(name, player.getGameMode());
        savescore.put(name, player.getScoreboard());
        savelevel.put(name, player.getLevel());
        saveexp.put(name, player.getExp());
        savehunger.put(name, player.getFoodLevel());
        saveair.put(name, player.getRemainingAir());
        saveexhaustion.put(name, player.getExhaustion());
        savefireticks.put(name, player.getFireTicks());
        savehealth.put(name, player.getHealth());
        saveabsorption.put(name, player.getAbsorptionAmount());
        saveallowflight.put(name, player.getAllowFlight());
        saveflying.put(name, player.isFlying());
        savewalkspeed.put(name, player.getWalkSpeed());
        saveflyspeed.put(name, player.getFlySpeed());
        savetotalexperience.put(name, player.getTotalExperience());

        AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttribute != null) {
            savemaxhealth.put(name, maxHealthAttribute.getBaseValue());
        }
        AttributeInstance maxAbsorptionAttribute = player.getAttribute(Attribute.MAX_ABSORPTION);
        if (maxAbsorptionAttribute != null) {
            savemaxabsorption.put(name, maxAbsorptionAttribute.getBaseValue());
        }
        savesaturation.put(name, player.getSaturation());
        saveeffects.put(name, new ArrayList<>(player.getActivePotionEffects()));
    }

    private static void restorePlayerState(Player player) {
        String name = player.getName();

        player.closeInventory();
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);

        Location returnLocation = getConfiguredQuitLocation();
        if (returnLocation == null) {
            returnLocation = saveworld.get(name);
        }
        if (returnLocation != null && returnLocation.getWorld() != null) {
            player.teleport(returnLocation);
        }

        ItemStack[] inventory = saveinv.get(name);
        if (inventory != null) {
            player.getInventory().setContents(cloneItems(inventory));
        }

        ItemStack[] armor = savearmor.get(name);
        if (armor != null) {
            player.getInventory().setArmorContents(cloneItems(armor));
        }

        GameMode gameMode = savegamemode.get(name);
        if (gameMode != null) {
            player.setGameMode(gameMode);
        }

        Double originalMaxHealth = savemaxhealth.get(name);
        AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (originalMaxHealth != null && maxHealthAttribute != null
                && Double.isFinite(originalMaxHealth) && originalMaxHealth > 0.0D) {
            maxHealthAttribute.setBaseValue(originalMaxHealth);
        }

        Double originalMaxAbsorption = savemaxabsorption.get(name);
        AttributeInstance maxAbsorptionAttribute = player.getAttribute(Attribute.MAX_ABSORPTION);
        if (originalMaxAbsorption != null && maxAbsorptionAttribute != null
                && Double.isFinite(originalMaxAbsorption) && originalMaxAbsorption >= 0.0D) {
            maxAbsorptionAttribute.setBaseValue(originalMaxAbsorption);
        }

        Float originalWalkSpeed = savewalkspeed.get(name);
        if (originalWalkSpeed != null && Float.isFinite(originalWalkSpeed)) {
            player.setWalkSpeed(Math.max(-1.0F, Math.min(1.0F, originalWalkSpeed)));
        }
        Float originalFlySpeed = saveflyspeed.get(name);
        if (originalFlySpeed != null && Float.isFinite(originalFlySpeed)) {
            player.setFlySpeed(Math.max(-1.0F, Math.min(1.0F, originalFlySpeed)));
        }

        // Restore flight only after the original game mode has been applied.
        if (player.isFlying()) {
            player.setFlying(false);
        }
        Boolean originalAllowFlight = saveallowflight.get(name);
        if (originalAllowFlight != null) {
            player.setAllowFlight(originalAllowFlight);
        }
        Boolean originalFlying = saveflying.get(name);
        if (Boolean.TRUE.equals(originalFlying) && player.getAllowFlight()) {
            player.setFlying(true);
        }

        Scoreboard scoreboard = savescore.get(name);
        if (scoreboard != null) {
            player.setScoreboard(scoreboard);
        }

        Integer level = savelevel.get(name);
        if (level != null) {
            player.setLevel(level);
        }

        Float experience = saveexp.get(name);
        if (experience != null) {
            player.setExp(Math.max(0.0F, Math.min(1.0F, experience)));
        }

        Integer hunger = savehunger.get(name);
        if (hunger != null) {
            player.setFoodLevel(Math.max(0, Math.min(20, hunger)));
        }

        Integer air = saveair.get(name);
        if (air != null) {
            player.setRemainingAir(Math.max(0, Math.min(player.getMaximumAir(), air)));
        }

        Float exhaustion = saveexhaustion.get(name);
        if (exhaustion != null) {
            player.setExhaustion(Math.max(0.0F, exhaustion));
        }

        Float saturation = savesaturation.get(name);
        if (saturation != null) {
            player.setSaturation(Math.max(0.0F, Math.min(20.0F, saturation)));
        }

        for (PotionEffect effect : new ArrayList<>(player.getActivePotionEffects())) {
            player.removePotionEffect(effect.getType());
        }

        List<PotionEffect> effects = saveeffects.get(name);
        if (effects != null) {
            for (PotionEffect effect : effects) {
                player.addPotionEffect(effect);
            }
        }

        Double originalHealth = savehealth.get(name);
        if (originalHealth != null && Double.isFinite(originalHealth)) {
            AttributeInstance currentMaxHealth = player.getAttribute(Attribute.MAX_HEALTH);
            double maximum = currentMaxHealth == null ? 20.0D : currentMaxHealth.getValue();
            double safeHealth = Math.max(0.1D, Math.min(originalHealth, maximum));
            player.setHealth(safeHealth);
        }

        Double originalAbsorption = saveabsorption.get(name);
        if (originalAbsorption != null && Double.isFinite(originalAbsorption)) {
            player.setAbsorptionAmount(Math.max(0.0D, originalAbsorption));
        }

        Integer originalTotalExperience = savetotalexperience.get(name);
        if (originalTotalExperience != null) {
            player.setTotalExperience(Math.max(0, originalTotalExperience));
        }

        // Clear any accumulated fall distance after teleporting back to the lobby.
        player.setFallDistance(0.0F);

        Integer originalFireTicks = savefireticks.get(name);
        if (originalFireTicks != null) {
            player.setFireTicks(Math.max(0, originalFireTicks));
        }
        player.updateInventory();
        clearSavedState(name);
    }

    private static Location getConfiguredQuitLocation() {
        if (Main.cfg_x1 == null) {
            return null;
        }

        String quitWorldName = Main.cfg_x1.getString("x2.coords.quit.world");
        if (quitWorldName == null || quitWorldName.trim().isEmpty()) {
            return null;
        }

        World quitWorld = Bukkit.getWorld(quitWorldName);
        if (quitWorld == null) {
            return null;
        }

        return new Location(
                quitWorld,
                Main.cfg_x1.getDouble("x2.coords.quit.x"),
                Main.cfg_x1.getDouble("x2.coords.quit.y"),
                Main.cfg_x1.getDouble("x2.coords.quit.z"),
                (float) Main.cfg_x1.getDouble("x2.coords.quit.yaw"),
                (float) Main.cfg_x1.getDouble("x2.coords.quit.pitch")
        );
    }

    private static ItemStack[] cloneItems(ItemStack[] items) {
        if (items == null) {
            return null;
        }

        ItemStack[] cloned = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            cloned[i] = items[i] == null ? null : items[i].clone();
        }
        return cloned;
    }

    private static void clearSavedState(String playerName) {
        saveinv.remove(playerName);
        savearmor.remove(playerName);
        saveworld.remove(playerName);
        savegamemode.remove(playerName);
        savescore.remove(playerName);
        savelevel.remove(playerName);
        saveexp.remove(playerName);
        savehunger.remove(playerName);
        saveair.remove(playerName);
        saveexhaustion.remove(playerName);
        savefireticks.remove(playerName);
        savehealth.remove(playerName);
        saveabsorption.remove(playerName);
        savemaxhealth.remove(playerName);
        savemaxabsorption.remove(playerName);
        savetotalexperience.remove(playerName);
        saveallowflight.remove(playerName);
        saveflying.remove(playerName);
        savewalkspeed.remove(playerName);
        saveflyspeed.remove(playerName);
        savesaturation.remove(playerName);
        saveeffects.remove(playerName);
        saveeffect.remove(playerName);
        if (pendingRestore.remove(playerName)) {
            persistPendingRestoreStates();
        }
    }

    /** True when this player belongs to this room or has state awaiting restoration. */
    public static boolean isPlayerRegisteredOrPendingRestore(Player player) {
        if (player == null) return false;
        String name = player.getName();
        return game.contains(name) || pendingRestore.contains(name) || hasSavedPlayerState(name);
    }

    public static boolean hasSavedPlayerState(String name) {
        return name != null && (saveinv.containsKey(name) || savearmor.containsKey(name)
                || saveworld.containsKey(name) || savegamemode.containsKey(name)
                || savescore.containsKey(name) || savelevel.containsKey(name) || saveexp.containsKey(name)
                || savehunger.containsKey(name) || saveair.containsKey(name) || saveexhaustion.containsKey(name)
                || savesaturation.containsKey(name) || savefireticks.containsKey(name)
                || savehealth.containsKey(name) || saveabsorption.containsKey(name)
                || savemaxhealth.containsKey(name) || savemaxabsorption.containsKey(name)
                || savetotalexperience.containsKey(name) || saveallowflight.containsKey(name)
                || saveflying.containsKey(name) || savewalkspeed.containsKey(name)
                || saveflyspeed.containsKey(name) || saveeffects.containsKey(name));
    }

    /** Called when a participant disconnects; preserve their state for the next login. */
    public static void markPendingRestore(Player player) {
        if (player == null) return;
        String name = player.getName();
        game.remove(name);
        MainCommand2.player.remove(player);
        if (hasSavedPlayerState(name) && pendingRestore.add(name)) {
            persistPendingRestoreStates();
        }
    }

    public static boolean hasPendingRestore(Player player) {
        return player != null && pendingRestore.contains(player.getName());
    }

    public static boolean restorePendingState(Player player) {
        if (player == null || !pendingRestore.contains(player.getName())) return false;
        restorePlayerState(player);
        return true;
    }

    public static boolean restoreSavedState(Player player) {
        if (player == null || !hasSavedPlayerState(player.getName())) return false;
        restorePlayerState(player);
        return true;
    }

    /** Clears a conflicting stale snapshot without applying it over a restored inventory. */
    public static void discardPendingState(Player player) {
        if (player == null) return;
        String name = player.getName();
        game.remove(name);
        MainCommand2.player.remove(player);
        clearSavedState(name);
    }

    public static void loadPendingRestoreStates() {
        File file = pendingRestoreFile();
        if (file == null || !file.isFile()) return;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        org.bukkit.configuration.ConfigurationSection section = cfg.getConfigurationSection("players");
        if (section == null) return;
        for (String name : section.getKeys(false)) {
            String path = "players." + name;
            readSavedState(cfg, path, name);
            pendingRestore.add(name);
        }
    }

    public static void persistPendingRestoreStates() {
        File file = pendingRestoreFile();
        if (file == null) return;
        YamlConfiguration cfg = new YamlConfiguration();
        for (String name : pendingRestore) {
            writeSavedState(cfg, "players." + name, name);
        }
        try {
            File folder = file.getParentFile();
            if (folder != null && !folder.exists() && !folder.mkdirs()) {
                Main main = Main.getInstance();
                if (main != null) main.getLogger().warning("Could not create folder for Room 2 pending player states.");
                return;
            }
            cfg.save(file);
        } catch (Exception exception) {
            Main main = Main.getInstance();
            if (main != null) main.getLogger().severe("Could not save Room 2 pending player states: " + exception.getMessage());
        }
    }

    private static File pendingRestoreFile() {
        Main main = Main.getInstance();
        return main == null ? null : new File(main.getDataFolder(), "pending-restore-room2.yml");
    }

    private static void writeSavedState(YamlConfiguration cfg, String path, String name) {
        if (saveinv.containsKey(name)) cfg.set(path + ".inventory", Arrays.asList(cloneItems(saveinv.get(name))));
        if (savearmor.containsKey(name)) cfg.set(path + ".armor", Arrays.asList(cloneItems(savearmor.get(name))));
        if (saveworld.containsKey(name)) cfg.set(path + ".location", saveworld.get(name));
        if (savegamemode.containsKey(name) && savegamemode.get(name) != null) cfg.set(path + ".gamemode", savegamemode.get(name).name());
        if (savelevel.containsKey(name)) cfg.set(path + ".level", savelevel.get(name));
        if (saveexp.containsKey(name)) cfg.set(path + ".exp", saveexp.get(name));
        if (savehunger.containsKey(name)) cfg.set(path + ".hunger", savehunger.get(name));
        if (saveair.containsKey(name)) cfg.set(path + ".air", saveair.get(name));
        if (saveexhaustion.containsKey(name)) cfg.set(path + ".exhaustion", saveexhaustion.get(name));
        if (savesaturation.containsKey(name)) cfg.set(path + ".saturation", savesaturation.get(name));
        if (savefireticks.containsKey(name)) cfg.set(path + ".fireTicks", savefireticks.get(name));
        if (savehealth.containsKey(name)) cfg.set(path + ".health", savehealth.get(name));
        if (saveabsorption.containsKey(name)) cfg.set(path + ".absorption", saveabsorption.get(name));
        if (savemaxhealth.containsKey(name)) cfg.set(path + ".maxHealthBase", savemaxhealth.get(name));
        if (savemaxabsorption.containsKey(name)) cfg.set(path + ".maxAbsorptionBase", savemaxabsorption.get(name));
        if (savetotalexperience.containsKey(name)) cfg.set(path + ".totalExperience", savetotalexperience.get(name));
        if (saveallowflight.containsKey(name)) cfg.set(path + ".allowFlight", saveallowflight.get(name));
        if (saveflying.containsKey(name)) cfg.set(path + ".flying", saveflying.get(name));
        if (savewalkspeed.containsKey(name)) cfg.set(path + ".walkSpeed", savewalkspeed.get(name));
        if (saveflyspeed.containsKey(name)) cfg.set(path + ".flySpeed", saveflyspeed.get(name));
        if (saveeffects.containsKey(name)) cfg.set(path + ".effects", new ArrayList<>(saveeffects.get(name)));
    }

    private static void readSavedState(YamlConfiguration cfg, String path, String name) {
        ItemStack[] inventory = itemArray(cfg.getList(path + ".inventory"));
        ItemStack[] armor = itemArray(cfg.getList(path + ".armor"));
        if (inventory != null) saveinv.put(name, inventory);
        if (armor != null) savearmor.put(name, armor);
        Location location = cfg.getLocation(path + ".location");
        if (location != null) saveworld.put(name, location);
        String mode = cfg.getString(path + ".gamemode");
        if (mode != null) {
            try { savegamemode.put(name, GameMode.valueOf(mode)); }
            catch (IllegalArgumentException ignored) { }
        }
        if (cfg.contains(path + ".level")) savelevel.put(name, cfg.getInt(path + ".level"));
        if (cfg.contains(path + ".exp")) saveexp.put(name, (float) cfg.getDouble(path + ".exp"));
        if (cfg.contains(path + ".hunger")) savehunger.put(name, cfg.getInt(path + ".hunger"));
        if (cfg.contains(path + ".air")) saveair.put(name, cfg.getInt(path + ".air"));
        if (cfg.contains(path + ".exhaustion")) saveexhaustion.put(name, (float) cfg.getDouble(path + ".exhaustion"));
        if (cfg.contains(path + ".saturation")) savesaturation.put(name, (float) cfg.getDouble(path + ".saturation"));
        if (cfg.contains(path + ".fireTicks")) savefireticks.put(name, cfg.getInt(path + ".fireTicks"));
        if (cfg.contains(path + ".health")) savehealth.put(name, cfg.getDouble(path + ".health"));
        if (cfg.contains(path + ".absorption")) saveabsorption.put(name, cfg.getDouble(path + ".absorption"));
        if (cfg.contains(path + ".maxHealthBase")) savemaxhealth.put(name, cfg.getDouble(path + ".maxHealthBase"));
        if (cfg.contains(path + ".maxAbsorptionBase")) savemaxabsorption.put(name, cfg.getDouble(path + ".maxAbsorptionBase"));
        if (cfg.contains(path + ".totalExperience")) savetotalexperience.put(name, cfg.getInt(path + ".totalExperience"));
        if (cfg.contains(path + ".allowFlight")) saveallowflight.put(name, cfg.getBoolean(path + ".allowFlight"));
        if (cfg.contains(path + ".flying")) saveflying.put(name, cfg.getBoolean(path + ".flying"));
        if (cfg.contains(path + ".walkSpeed")) savewalkspeed.put(name, (float) cfg.getDouble(path + ".walkSpeed"));
        if (cfg.contains(path + ".flySpeed")) saveflyspeed.put(name, (float) cfg.getDouble(path + ".flySpeed"));
        List<PotionEffect> effects = new ArrayList<>();
        List<?> rawEffects = cfg.getList(path + ".effects");
        if (rawEffects != null) {
            for (Object rawEffect : rawEffects) {
                try {
                    if (rawEffect instanceof PotionEffect) {
                        effects.add((PotionEffect) rawEffect);
                    } else if (rawEffect instanceof Map<?, ?>) {
                        // Bukkit serializes ConfigurationSerializable objects as maps in YAML.
                        Map<String, Object> serialized = new HashMap<>();
                        for (Map.Entry<?, ?> entry : ((Map<?, ?>) rawEffect).entrySet()) {
                            if (entry.getKey() != null) {
                                serialized.put(String.valueOf(entry.getKey()), entry.getValue());
                            }
                        }
                        PotionEffect restored = deserializePotionEffect(serialized);
                        if (restored != null) {
                            effects.add(restored);
                        }
                    }
                } catch (IllegalArgumentException | ClassCastException ex) {
                    Main main = Main.getInstance();
                    if (main != null) {
                        main.getLogger().warning("Could not restore a saved potion effect for " + name + ": " + ex.getMessage());
                    }
                }
            }
        }
        if (!effects.isEmpty()) saveeffects.put(name, effects);
    }

    /**
     * Rebuilds a potion effect from Bukkit's serialized YAML map without relying
     * on PotionEffect.deserialize(), which is not available in every API version.
     */
    private static PotionEffect deserializePotionEffect(Map<String, Object> data) {
        Object rawType = data.get("effect");
        if (rawType == null) rawType = data.get("type");
        if (rawType == null) return null;

        PotionEffectType type;
        if (rawType instanceof PotionEffectType) {
            type = (PotionEffectType) rawType;
        } else {
            String typeName = String.valueOf(rawType);
            type = PotionEffectType.getByName(typeName.toUpperCase(java.util.Locale.ROOT));
            if (type == null && typeName.contains(":")) {
                type = PotionEffectType.getByName(typeName.substring(typeName.indexOf(':') + 1).toUpperCase(java.util.Locale.ROOT));
            }
        }
        if (type == null) return null;

        int duration = readInt(data, "duration", 0);
        int amplifier = readInt(data, "amplifier", 0);
        if (duration <= 0) return null;

        boolean ambient = readBoolean(data, "ambient", false);
        boolean particles = readBoolean(data, "has-particles", readBoolean(data, "particles", true));
        boolean icon = readBoolean(data, "has-icon", readBoolean(data, "icon", particles));
        return new PotionEffect(type, duration, amplifier, ambient, particles, icon);
    }

    private static int readInt(Map<String, Object> data, String key, int fallback) {
        Object value = data.get(key);
        if (value instanceof Number) return ((Number) value).intValue();
        if (value != null) {
            try { return Integer.parseInt(String.valueOf(value)); }
            catch (NumberFormatException ignored) { }
        }
        return fallback;
    }

    private static boolean readBoolean(Map<String, Object> data, String key, boolean fallback) {
        Object value = data.get(key);
        return value instanceof Boolean ? (Boolean) value : value == null ? fallback : Boolean.parseBoolean(String.valueOf(value));
    }

    private static ItemStack[] itemArray(List<?> values) {
        if (values == null) return null;
        ItemStack[] items = new ItemStack[values.size()];
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            items[i] = value instanceof ItemStack ? ((ItemStack) value).clone() : null;
        }
        return items;
    }

    private Automatic2 getAutomatic2() {
        Main instance = Main.getInstance();
        return instance == null ? null : instance.getAutomatic2Listener();
    }

    private String configMessage(String path, String fallback) {
        Main currentPlugin = plugin != null ? plugin : Main.getInstance();
        if (currentPlugin == null || currentPlugin.getConfig() == null) {
            return fallback;
        }

        String value = currentPlugin.getConfig().getString(path);
        return value == null ? fallback : value;
    }

    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message == null ? "" : message);
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(ChatColor.DARK_AQUA + "§m-----------"
                + ChatColor.AQUA + " PVPROUNDS ROOM 2 COMMANDS "
                + ChatColor.DARK_AQUA + ChatColor.STRIKETHROUGH + "-------------");
        sender.sendMessage(ChatColor.YELLOW + "Created by Rafael Auler");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.DARK_AQUA + " - " + ChatColor.AQUA + "/" + label
                + ChatColor.DARK_AQUA + " - " + ChatColor.GRAY + "Main command");
        sender.sendMessage(ChatColor.DARK_AQUA + " - " + ChatColor.AQUA + "/" + label
                + " join" + ChatColor.DARK_AQUA + " - " + ChatColor.GRAY + "Join PvP Tournament Room #2");
        sender.sendMessage(ChatColor.DARK_AQUA + " - " + ChatColor.AQUA + "/" + label
                + " leave" + ChatColor.DARK_AQUA + " - " + ChatColor.GRAY + "Leave PvP Tournament Room #2");
        sender.sendMessage(ChatColor.DARK_AQUA + " - " + ChatColor.AQUA + "/" + label
                + " info" + ChatColor.DARK_AQUA + " - " + ChatColor.GRAY + "Show plugin information");
        sender.sendMessage(ChatColor.DARK_AQUA + " - " + ChatColor.AQUA + "/" + label
                + " stop" + ChatColor.DARK_AQUA + " - " + ChatColor.GRAY + "Stop the tournament");
        sender.sendMessage(ChatColor.DARK_AQUA + "§m------------------------------------------");
    }

    private void sendInfo(CommandSender sender) {
        Main currentPlugin = plugin != null ? plugin : Main.getInstance();
        sender.sendMessage("§4§l✖ §2§lCREDITS §f§lAND §e§lINFORMATION §4§l✖");
        sender.sendMessage("§6➜ §cPlugin Name: §ePvPRounds");
        sender.sendMessage("§6➜ §cPlugin Version: §e"
                + (currentPlugin == null ? "unknown" : currentPlugin.getDescription().getVersion()));
        sender.sendMessage("§6➜ §cAuthor: §ezEnderX5_, Rafael Auler");
        sender.sendMessage("§6➜ §cAuthor Channel: http://bit.ly/2kC345B");
        sender.sendMessage("§6➜ §cSpigot Profile: http://bit.ly/2j5qvnM");
        sender.sendMessage("§6➜ §cPlugin Page: COMING SOON");
        sender.sendMessage("§cThanks for using this plugin!");
    }
}
