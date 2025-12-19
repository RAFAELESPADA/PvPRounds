package me.rafaelauler;


/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;

import org.bukkit.Bukkit;
/*     */ import org.bukkit.ChatColor;
/*     */ import org.bukkit.GameMode;
/*     */ import org.bukkit.Location;
/*     */ import org.bukkit.command.Command;
/*     */ import org.bukkit.command.CommandExecutor;
/*     */ import org.bukkit.command.CommandSender;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.event.Listener;
/*     */ import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;



 
/*     */ public class MainCommand2
/*     */   implements CommandExecutor, Listener
/*     */ {
/*  43 */   public static HashMap<String, ItemStack[]> saveinv = new HashMap();
/*  44 */   public static HashMap<String, ItemStack[]> savearmor = new HashMap();
/*  45 */   public static HashMap<String, Location> saveworld = new HashMap();
/*  46 */   public static HashMap<String, GameMode> savegamemode = new HashMap();
public static HashMap<String, Scoreboard> savescore = new HashMap();
public static HashMap<String, Integer> savelevel = new HashMap();
public static HashMap<String, Integer> savehunger = new HashMap();
public static HashMap<String, PotionEffect> saveeffect = new HashMap();
public static HashMap<String, Integer> saveair = new HashMap();

/*     */   static Main plugin;
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */   public MainCommand2(Main BukkitMain)
/*     */   {
/*  62 */     plugin = BukkitMain;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*  76 */   HashMap<String, Location> maps = new HashMap();
/*  77 */   public static ArrayList<String> game = new ArrayList();
public static ArrayList<Player> player = new ArrayList();
/*  78 */   List<String> commands = Arrays.asList(new String[] { "admin", "list", "create", "delete", "1v1", "score", "setspawn", "spawn", "join", "leave", "reset", "coins", "setchallenge", "kit", "kitunlocker", "shop", "resetkit", "stats", "reload", "update" });
/*     */   
/*     */   public MainCommand2() {}
/*     */   

/*     */   
/*     */   public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
/*  91 */     if (commandLabel.equalsIgnoreCase("pvprounds2") || (commandLabel.equalsIgnoreCase("pvr2")))
/*     */     {
/*  93 */       if (args.length == 0)
/*     */       {

/* 106 */         sender.sendMessage(ChatColor.DARK_AQUA + "§m-----------" + ChatColor.AQUA + " PVPROUNDS ROOM 2 COMMANDS " + ChatColor.DARK_AQUA + ChatColor.STRIKETHROUGH + "-------------");
/* 107 */         sender.sendMessage(ChatColor.DARK_AQUA + "§eCreated by Rafael Auler");
/* 108 */         sender.sendMessage("");
/* 109 */         sender.sendMessage(ChatColor.DARK_AQUA + " - " + ChatColor.AQUA + "/" + commandLabel + ChatColor.DARK_AQUA + " - " + ChatColor.GRAY + "Main command");
/* 110 */         sender.sendMessage(ChatColor.DARK_AQUA + " - " + ChatColor.AQUA + "/" + commandLabel + " " + ChatColor.GREEN + "join" + ChatColor.DARK_AQUA + " - " + ChatColor.GRAY + "Join the PVP TOURNAMENT ROOM #2!");
/* 111 */         sender.sendMessage(ChatColor.DARK_AQUA + " - " + ChatColor.AQUA + "/" + commandLabel + " " + ChatColor.GREEN + "leave" + ChatColor.DARK_AQUA + " - " + ChatColor.GRAY + "Leave the PVP TOURNAMENT ROOM #2!");

/* 116 */         sender.sendMessage(ChatColor.DARK_AQUA + " - " + ChatColor.AQUA + "/" + commandLabel + " " + ChatColor.GREEN + "info" + ChatColor.DARK_AQUA + " - " + ChatColor.GRAY + "Shows plugin info");
/* 120 */         sender.sendMessage(ChatColor.DARK_AQUA + "§m------------------------------------------");
/* 102 */         return true;
/*     */       }
/* 104 */       if (args[0].equalsIgnoreCase("info"))
/*     */       {
	/* 27 */       sender.sendMessage("§4§l\u274C §2§lCREDITS §f§lAND §e§lINFORMATION §4§l \u274C");
	/* 28 */       sender.sendMessage("§6\u279C §cPlugin Name: §ePvPRounds");
	/* 29 */       sender.sendMessage("§6\u279C §cPlugin Version: §e " + Main.getInstance().getDescription().getVersion());
	/* 30 */       sender.sendMessage("§6\u279C §cAuthor: §ezEnderX5_ , Rafael Auler");
	/* 31 */       sender.sendMessage("§6\u279C §cAuthor Channel: http://bit.ly/2kC345B");
	/* 32 */       sender.sendMessage("§6\u279C §cSpigot Profile: http://bit.ly/2j5qvnM");
	/* 33 */       sender.sendMessage("§6\u279C §cPlugin Page: COMMING SOON");
	/* 34 */       sender.sendMessage("§cThanks for use this plugin i really appreaciate IT");
	/* 35 */       sender.sendMessage("§cIf you like it consider giving a §e§l\u2605\u2605\u2605\u2605\u2605 §cReview");
	/* 36 */       sender.sendMessage("§cPS: §eSubscribe to my channel and follow me on Spigot Thanks! §9§l=)");
/* 121 */         return true;
/*     */       }
if (args[0].equalsIgnoreCase("stop"))
/*     */       {
	/* 27 */       sender.sendMessage("§4§l\u274C You stopped the tournament pvp room #1");
	               Automatic2 r2 = new Automatic2();
	            		   r2.destroy();
/* 121 */         return true;
/*     */       }
/* 167 */       if (args[0].equalsIgnoreCase("join"))
/*     */       {
/* 169 */         if ((sender instanceof Player))
/*     */         {
/* 171 */           if (game.contains(sender.getName()))
/*     */           {
/* 173 */sender.sendMessage("You are already on the PvPRound room 2!");            
	return true;
/*     */           }
/*     */           if (Main.cfg_x1.getString("x2.coords.spawn.world") == null) {
	sender.sendMessage(ChatColor.YELLOW + "PvPRounds spawn is not seted yet!");
	return true;
}
if (Automatic2.star) {
	sender.sendMessage("The tournament is occouring! Please wait to it finish before you join!");
	return true;
}
/*     */ Player p = (Player)sender;
/*     */ 
/* 179 */           p.sendMessage(Main.getInstance().getConfig().getString("Joined").replaceAll("&", "§"));

/*     */ TitleAPI.sendTitle(p, 80, 80, 80, Main.getInstance().getConfig().getString("JoinTitle").replaceAll("&", "§"), Main.getInstance().getConfig().getString("JoinSubTitle").replaceAll("&", "§"));
/*     */ 
/*     */ 
/* 185 */           game.add(p.getName());
/*     */     Automatic2.players.add(p);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 

}
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ Player p = (Player)sender;
/*     */ 
;
org.bukkit.World w = Bukkit.getServer().getWorld(Main.cfg_x1.getString("x2.coords.spawn.world"));
/*  98 */     p.teleport(new Location(w, Main.cfg_x1.getDouble("x2.coords.spawn.x"), 
/*  99 */       Main.cfg_x1.getDouble("x2.coords.spawn.y"), Main.cfg_x1.getDouble("x2.coords.spawn.z")));
		/* 205 */           saveworld.put(p.getName(), p.getLocation());
		                    savescore.put(p.getName(), p.getScoreboard());
		/* 206 */           saveinv.put(p.getName(), p.getInventory().getContents());
		/* 207 */           savearmor.put(p.getName(), p.getInventory().getArmorContents());
		/* 208 */           savegamemode.put(p.getName(), p.getGameMode());
		/*     */           savelevel.put(p.getName(), p.getLevel());
		savehunger.put(p.getName(), p.getFoodLevel());
		saveair.put(p.getName(), p.getRemainingAir());
/*     */           
/*     */ 
/* 219 */           p.getInventory().clear();
p.getInventory().setArmorContents(null);
/* 107 */       p.updateInventory();
/*     */           
/*     */ 

/*     */ 
/* 235 */           p.setExp(0.0F);
/* 236 */           p.setExhaustion(20.0F);
/* 237 */           p.setFireTicks(0);
/* 238 */           p.setFoodLevel(20000);

}
if (args[0].equalsIgnoreCase("leave"))
/*     */       {
	Player p = (Player)sender;
	if ((!game.contains(p.getName()))) {
		
			p.sendMessage(ChatColor.RED + "You must be on the game room");
		return true;
	}
	
	/*     */       
	/*     */ 
	/*     */ 
	/*     */     Automatic2.players.remove(p);
	/* 283 */       game.remove(p.getName());
	/* 284 */       game.remove(p.getName());
	/* 285 */       game.remove(p.getName());
	/* 286 */       game.remove(p.getName());
	/* 287 */       game.remove(p.getName());
	/* 288 */       game.remove(p.getName());
	/* 289 */       game.remove(p.getName());
	/* 290 */       game.remove(p.getName());
	/* 291 */       game.remove(p.getName());
	/* 292 */       game.remove(p.getName());
	/* 293 */       game.remove(p.getName());
	/* 294 */       game.remove(p.getName());game.remove(p.getName());
	/* 295 */       game.remove(p.getName());
	/* 296 */       game.remove(p.getName());
	/* 297 */       game.remove(p.getName());
                    p.sendMessage(ChatColor.RED + Main.getInstace().getConfig().getString("TournamentLeave"));
	/* 304 */       p.getInventory().clear();
	/* 305 */       p.teleport((Location)saveworld.get(p.getName()));
	/* 306 */       p.getInventory().setContents((ItemStack[])saveinv.get(p.getName()));
	/* 307 */       p.setGameMode((GameMode)savegamemode.get(p.getName()));
	p.setScoreboard(savescore.get(p.getName()));
	p.setLevel(savelevel.get(p.getName()));
	p.setFoodLevel(savehunger.get(p.getName()));
	p.setRemainingAir(saveair.get(p.getName()));
	  org.bukkit.World w = Bukkit.getServer().getWorld(Main.cfg_x1.getString("x2.coords.quit.world"));
	  /*  98 */     p.teleport(new Location(w, Main.cfg_x1.getDouble("x2.coords.quit.x"), 
	  /*  99 */       Main.cfg_x1.getDouble("x2.coords.quit.y"), Main.cfg_x1.getDouble("x2.coords.quit.z")));
  
	/* 308 */       p.getInventory().setArmorContents((ItemStack[])savearmor.get(p.getName()));

	/*     */   
	/* 311 */       p.updateInventory();

	/*     */     }

/*     */       
return false;
}
return false;
}
}

