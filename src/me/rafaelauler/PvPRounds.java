package me.rafaelauler;

import java.util.Locale;

import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

/**
 * PlaceholderAPI expansion for PvPRounds.
 *
 * Supported placeholders:
 * %pvprounds_players_room_1%
 * %pvprounds_players_room_2%
 */
public class PvPRounds extends PlaceholderExpansion {

    private final Main plugin;

    public PvPRounds(Main main) {
        this.plugin = main;
    }

    @Override
    public boolean persist() {
        // Keep this expansion registered when PlaceholderAPI is reloaded.
        return true;
    }

    @Override
    public boolean canRegister() {
        return plugin != null;
    }

    @Override
    public String getAuthor() {
        if (plugin == null || plugin.getDescription() == null) {
            return "Unknown";
        }

        if (plugin.getDescription().getAuthors() == null
                || plugin.getDescription().getAuthors().isEmpty()) {
            return "Unknown";
        }

        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public String getIdentifier() {
        return "pvprounds";
    }

    @Override
    public String getVersion() {
        return plugin == null || plugin.getDescription() == null
                ? "unknown"
                : plugin.getDescription().getVersion();
    }

    /**
     * Modern PlaceholderAPI request method. The room-count placeholders do not
     * require a player, so they also work in menus or requests without context.
     */
    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        return resolvePlaceholder(identifier);
    }

    /**
     * Backwards-compatible method for PlaceholderAPI integrations that use
     * the older Player-based callback.
     */
    @Override
    @Deprecated
    public String onPlaceholderRequest(Player player, String identifier) {
        return resolvePlaceholder(identifier);
    }

    private String resolvePlaceholder(String identifier) {
        if (identifier == null || plugin == null) {
            return null;
        }

        String key = identifier.toLowerCase(Locale.ROOT);

        switch (key) {
            case "players_room_1": {
                Automatic room1 = plugin.getAutomaticListener();
                return room1 == null ? "0" : String.valueOf(room1.getPlayers().size());
            }

            case "players_room_2": {
                Automatic2 room2 = plugin.getAutomatic2Listener();
                return room2 == null ? "0" : String.valueOf(room2.getPlayers().size());
            }

            default:
                // Unknown placeholders should be left for PlaceholderAPI/other expansions.
                return null;
        }
    }
}
