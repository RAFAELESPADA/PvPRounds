package me.rafaelauler;
 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

	 private EventManager2 eventmanager2;
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
public EventManager2 getEventManager2() {
    return this.eventmanager2;
  }

/*     */   public void onEnable()
/*     */   {
	  
	getCommand("pvprounds").setExecutor(new MainCommand());

	getCommand("pvr").setExecutor(new MainCommand());
	getCommand("pvprounds2").setExecutor(new MainCommand2());

	getCommand("pvr2").setExecutor(new MainCommand2());
	 Bukkit.getPluginManager().registerEvents(new Eventos(), this);
	 /* 109 */     Metrics metrics = new Metrics(this);
	 metrics.addCustomChart(new Metrics.DrilldownPie("serverAddress", () -> {
			Map<String, Map<String, Integer>> map = new HashMap<>();
			Map<String, Integer> entry = new HashMap<>();
			if (getConfig().getBoolean("SendIPAddressData")) entry.put(Bukkit.getServer().getIp(), 1);
			else entry.put("Hidden", 1);
			
			map.put("Port " + Bukkit.getServer().getPort(), entry);
			
			return map;
		}));
	 Bukkit.getPluginManager().registerEvents(new Automatic(), this);
	 ;
	 Bukkit.getPluginManager().registerEvents(new Automatic2(), this);
	 Bukkit.getPluginManager().registerEvents(new Soup(this), this);
	getCommand("setrounds").setExecutor(new SetRounds());
	getCommand("setrounds2").setExecutor(new SetRounds2());
	/*     */     
/* 121 */     instance = this;
/* 122 */     plugin = this;
(getInstance()).eventmanager = new EventManager();

(getInstance()).eventmanager2 = new EventManager2();
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
	/* 131 */     if (!file_x1.exists()) {
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
