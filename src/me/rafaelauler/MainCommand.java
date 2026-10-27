package me.rafaelauler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

public class MainCommand implements CommandExecutor, Listener {

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
    public static final Map<String, Float> savesaturation = new HashMap<>();
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
    public static final Map<String, List<PotionEffect>> saveeffects = new HashMap<>();

    /*
     * Kept for compatibility with older plugin classes.
     * All potion effects are restored using saveeffects.
     */
    @Deprecated
    public static final Map<String, PotionEffect> saveeffect = new HashMap<>();

    static Main plugin;

    public static final ArrayList<String> game = new ArrayList<>();
    public static final ArrayList<Player> player = new ArrayList<>();

    public MainCommand(Main main) {
        plugin = main;
    }

    public MainCommand() {
        plugin = Main.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!commandLabel.equalsIgnoreCase("pvprounds")
                && !commandLabel.equalsIgnoreCase("pvr")) {
            return false;
        }

        if (args.length == 0) {
            sendHelp(sender, commandLabel);
            return true;
        }

        String subCommand = args[0].toLowerCase(java.util.Locale.ROOT);

        switch (subCommand) {
            case "join":
                return join(sender);

            case "leave":
                return leave(sender);

            case "stop":
                if (!sender.hasPermission("pvprounds.admin")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to stop the Room 1 tournament.");
                    return true;
                }

                Automatic automatic = getAutomatic();
                if (automatic == null) {
                    sender.sendMessage(ChatColor.RED + "The Room 1 tournament is currently unavailable.");
                    return true;
                }
                automatic.destroy();
                sender.sendMessage(ChatColor.DARK_RED + "✖ You stopped the PvPRounds tournament in Room 1.");
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
            sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
            return true;
        }

        Player player = (Player) sender;
        String name = player.getName();

        if (game.contains(name)) {
            player.sendMessage(ChatColor.RED + "You are already in PvPRounds Room 1.");
            return true;
        }

        if (pendingRestore.contains(name)) {
            player.sendMessage(ChatColor.YELLOW + "Your previous tournament state is being restored. Please wait a moment.");
            return true;
        }

        if (MainCommand2.isPlayerRegisteredOrPendingRestore(player)) {
            player.sendMessage(ChatColor.RED + "You cannot join Room 1 while you are registered in or awaiting restoration from Room 2.");
            return true;
        }

        Automatic automatic = getAutomatic();
        if (automatic == null) {
            player.sendMessage(ChatColor.RED + "The Room 1 tournament is currently unavailable.");
            return true;
        }

        if (automatic.getPlayers().contains(player) || hasSavedPlayerState(name)) {
            player.sendMessage(ChatColor.YELLOW + "Your Room 1 player state is already registered or awaiting cleanup. Please contact an administrator if this persists.");
            return true;
        }

        if (Automatic.star || automatic.isEndingTournament()) {
            player.sendMessage(ChatColor.YELLOW
                    + (automatic.isEndingTournament()
                    ? "The tournament is finishing. Please wait before joining."
                    : "The tournament is already in progress. Please wait until it ends before joining."));
            return true;
        }

        if (automatic.getPlayers().size() >= automatic.getMaxPlayers()) {
            player.sendMessage(ChatColor.RED + "Room 1 is full.");
            return true;
        }

        String spawnWorldName = Main.cfg_x1.getString("x1.coords.spawn.world");
        if (spawnWorldName == null || spawnWorldName.trim().isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "The PvPRounds spawn has not been set yet.");
            return true;
        }

        World spawnWorld = Bukkit.getWorld(spawnWorldName);
        if (spawnWorld == null) {
            player.sendMessage(ChatColor.RED + "The spawn world '" + spawnWorldName + "' is not loaded.");
            return true;
        }

        Location spawn = new Location(
                spawnWorld,
                Main.cfg_x1.getDouble("x1.coords.spawn.x"),
                Main.cfg_x1.getDouble("x1.coords.spawn.y"),
                Main.cfg_x1.getDouble("x1.coords.spawn.z"),
                (float) Main.cfg_x1.getDouble("x1.coords.spawn.yaw"),
                (float) Main.cfg_x1.getDouble("x1.coords.spawn.pitch")
        );

        // Save the player state BEFORE putInEvent, because this method clears the inventory.
        savePlayerState(player);

        automatic.putInEvent(player);

        // putInEvent may reject the player; only register them in the command list if accepted.
        if (!automatic.getPlayers().contains(player)) {
            clearSavedState(name);
            player.sendMessage(ChatColor.RED + "Could not join the tournament.");
            return true;
        }

        game.add(name);
        enforceTournamentRestrictions(player);
        player.teleport(spawn);
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.setExp(0.0F);
        player.setLevel(0);
        player.setExhaustion(0.0F);
        player.setFireTicks(0);
        player.setFoodLevel(20);
        player.setRemainingAir(player.getMaximumAir());
        player.updateInventory();

        String joined = configMessage("Joined", "&eYou joined the tournament.");
        player.sendMessage(color(joined));

        String title = configMessage("JoinTitle", "&eYou joined");
        String subtitle = configMessage("JoinSubTitle", "&fThe tournament");
        TitleAPI.sendTitle(player, 80, 80, 80, color(title), color(subtitle));

        return true;
    }

    private boolean leave(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
            return true;
        }

        Player player = (Player) sender;
        String name = player.getName();

        if (!game.contains(name)) {
            player.sendMessage(ChatColor.RED + "You are not participating in Room 1.");
            return true;
        }

        Automatic automatic = getAutomatic();

        // Remove the participant from the tournament lists before restoring the player.
        if (automatic != null) {
            automatic.removeFromEvent(player);
        }

        game.remove(name);
        MainCommand.player.remove(player);

        restorePlayerState(player);

        player.sendMessage(color(configMessage("TournamentLeave", "&eYou left the tournament!")));

        if (automatic != null && Automatic.star) {
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
        savesaturation.put(name, player.getSaturation());
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

        List<PotionEffect> effects = new ArrayList<>(player.getActivePotionEffects());
        saveeffects.put(name, effects);
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

        Float exp = saveexp.get(name);
        if (exp != null) {
            player.setExp(exp);
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
        String worldName = Main.cfg_x1.getString("x1.coords.quit.world");
        if (worldName == null || worldName.trim().isEmpty()) {
            return null;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }

        return new Location(
                world,
                Main.cfg_x1.getDouble("x1.coords.quit.x"),
                Main.cfg_x1.getDouble("x1.coords.quit.y"),
                Main.cfg_x1.getDouble("x1.coords.quit.z"),
                (float) Main.cfg_x1.getDouble("x1.coords.quit.yaw"),
                (float) Main.cfg_x1.getDouble("x1.coords.quit.pitch")
        );
    }

    private static ItemStack[] cloneItems(ItemStack[] items) {
        if (items == null) {
            return null;
        }

        ItemStack[] clone = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            clone[i] = items[i] == null ? null : items[i].clone();
        }
        return clone;
    }

    private static void clearSavedState(String name) {
        saveinv.remove(name);
        savearmor.remove(name);
        saveworld.remove(name);
        savegamemode.remove(name);
        savescore.remove(name);
        savelevel.remove(name);
        saveexp.remove(name);
        savehunger.remove(name);
        saveair.remove(name);
        saveexhaustion.remove(name);
        savesaturation.remove(name);
        savefireticks.remove(name);
        savehealth.remove(name);
        saveabsorption.remove(name);
        savemaxhealth.remove(name);
        savemaxabsorption.remove(name);
        savetotalexperience.remove(name);
        saveallowflight.remove(name);
        saveflying.remove(name);
        savewalkspeed.remove(name);
        saveflyspeed.remove(name);
        saveeffects.remove(name);
        saveeffect.remove(name);
        if (pendingRestore.remove(name)) {
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

    /** Called when a participant disconnects; keep their saved state for the next login. */
    public static void markPendingRestore(Player player) {
        if (player == null) return;
        String name = player.getName();
        game.remove(name);
        MainCommand.player.remove(player);
        if (hasSavedPlayerState(name) && pendingRestore.add(name)) {
            persistPendingRestoreStates();
        }
    }

    public static boolean hasPendingRestore(Player player) {
        return player != null && pendingRestore.contains(player.getName());
    }

    /** Restore a disconnected player's saved state after they reconnect. */
    public static boolean restorePendingState(Player player) {
        if (player == null || !pendingRestore.contains(player.getName())) return false;
        restorePlayerState(player);
        return true;
    }

    /** Used during controlled shutdown to restore online players still holding tournament state. */
    public static boolean restoreSavedState(Player player) {
        if (player == null || !hasSavedPlayerState(player.getName())) return false;
        restorePlayerState(player);
        return true;
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
            String path = "players." + name;
            writeSavedState(cfg, path, name);
        }
        try {
            File folder = file.getParentFile();
            if (folder != null && !folder.exists() && !folder.mkdirs()) {
                Main main = Main.getInstance();
                if (main != null) main.getLogger().warning("Could not create folder for Room 1 pending player states.");
                return;
            }
            cfg.save(file);
        } catch (Exception exception) {
            Main main = Main.getInstance();
            if (main != null) main.getLogger().severe("Could not save Room 1 pending player states: " + exception.getMessage());
        }
    }

    private static File pendingRestoreFile() {
        Main main = Main.getInstance();
        return main == null ? null : new File(main.getDataFolder(), "pending-restore-room1.yml");
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
        String gameModeName = cfg.getString(path + ".gamemode");
        if (gameModeName != null) {
            try { savegamemode.put(name, GameMode.valueOf(gameModeName)); }
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

    private Automatic getAutomatic() {
        Main main = Main.getInstance();
        return main == null ? null : main.getAutomaticListener();
    }

    private String configMessage(String path, String fallback) {
        Main main = Main.getInstance();
        if (main == null || main.getConfig() == null) {
            return fallback;
        }
        String value = main.getConfig().getString(path);
        return value == null ? fallback : value;
    }

    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message == null ? "" : message);
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(ChatColor.DARK_AQUA + "§m-----------" + ChatColor.AQUA
                + " PVPROUNDS ROOM 1 COMMANDS " + ChatColor.DARK_AQUA + "§m-------------");
        sender.sendMessage(ChatColor.YELLOW + "Created by Rafael Auler");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.AQUA + "/" + label + ChatColor.GRAY + " - Help");
        sender.sendMessage(ChatColor.AQUA + "/" + label + " join" + ChatColor.GRAY + " - Join Room 1");
        sender.sendMessage(ChatColor.AQUA + "/" + label + " leave" + ChatColor.GRAY + " - Leave Room 1");
        sender.sendMessage(ChatColor.AQUA + "/" + label + " info" + ChatColor.GRAY + " - Plugin information");
        sender.sendMessage(ChatColor.AQUA + "/" + label + " stop" + ChatColor.GRAY + " - Stop the tournament");
        sender.sendMessage(ChatColor.DARK_AQUA + "§m------------------------------------------");
    }

    private void sendInfo(CommandSender sender) {
        Main main = Main.getInstance();
        sender.sendMessage("§4§l✖ §2§lCREDITS §f§lAND §e§lINFORMATION");
        sender.sendMessage("§6➜ §cPlugin: §ePvPRounds");
        sender.sendMessage("§6➜ §cVersion: §e" + (main == null ? "unknown" : main.getDescription().getVersion()));
        sender.sendMessage("§6➜ §cAuthor: §eRafael Auler");
    }
}
