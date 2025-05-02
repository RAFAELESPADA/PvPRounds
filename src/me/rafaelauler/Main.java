package me.rafaelauler;
 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/*     */ import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
/*     */ import org.bukkit.event.Listener;
/*     */ import org.bukkit.plugin.Plugin;
/*     */ import org.bukkit.plugin.java.JavaPlugin;






/*     */ 
/*     */ 
/*     */ public class Main
/*     */   extends JavaPlugin
/*     */   implements Listener
/*     */ {

	 private EventManager eventmanager;
/*     */ /*     */   public static Plugin plugin;
/*     */   public static Main instance;

/*     */   private File cf1;
/*  77 */   public static String pluginName = "PvPRounds";
/*     */   
/*  98 */   public static File file_x1 = new File("plugins/PvPRounds", "1v1.yml");
/*     */ 
/* 364 */   public static FileConfiguration cfg_x1 = YamlConfiguration.loadConfiguration(file_x1);  
/*     */   public static Main getInstance()
/*     */   {
/*  82 */     return instance;
/*     */   }
/*     */   
/*     */   public static Main getInstace()
/*     */   {
/*  87 */     return instance;
/*     */   }
public static Main getInstace23()
/*     */   {
/*  87 */     return instance;
/*     */   }
public EventManager getEventManager() {
    return this.eventmanager;
  }

/*     */   public void onEnable()
/*     */   {
	getCommand("pvprounds").setExecutor(new MainCommand());

	getCommand("setrounds").setExecutor(new SetRounds());
	/*     */     
/* 121 */     instance = this;
/* 122 */     plugin = this;
(getInstance()).eventmanager = new EventManager();
   instance = this;
   plugin = this;
	ConsoleCommandSender cmd = Bukkit.getConsoleSender();
	if (!Coins.setupPermissions()) {
		cmd.sendMessage("PvPRounds - Disabled due to no Vault dependency found! PvPRounds VERSION" + getDescription().getVersion());
        cmd.sendMessage("Install vault to PvPRounds work!");
        getServer().getPluginManager().disablePlugin(this);
        return;
    }
	if (!Coins.setupEconomy()) {
		cmd.sendMessage("PvPRounds - Disabled due to no Vault dependency found! PvPRounds VERSION" + getDescription().getVersion());
        cmd.sendMessage("Install vault to PvPRounds work!");
        getServer().getPluginManager().disablePlugin(this);
        return;
    }
	saveDefaultConfig();
	File cf = new File(getDataFolder(), "config.yml");
	/* 127 */     if (!cf.exists()) {
	/* 128 */       saveResource("config.yml", false);
	/*     */     }
	/* 130 */     this.cf1 = new File(getDataFolder(), "config.yml");
	/* 131 */     if (file_x1.exists()) {
	/* 132 */       saveResource("1v1.yml", false);
	/*     */     }

/* 139 */       try {
	cfg_x1.load(file_x1);
} catch (FileNotFoundException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
} catch (IOException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
} catch (InvalidConfigurationException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
}
}
