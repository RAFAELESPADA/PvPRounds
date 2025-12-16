package me.rafaelauler;



import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.wavemc.core.bukkit.item.ItemBuilder;
import net.wavemc.core.util.UpdateEvent;

public class Automatic2 implements Listener {
  private Main main;
  
  private int time;
  
  private GameType gameType;
  
  private Listener listener;
  
  public static List<Player> players;
  
  private int maxPlayers;
  public static boolean iniciou;
  public static boolean star = false;
  private boolean full;
  
  private boolean pvp;
  
  private List<Player> playersInPvp;
  
  private List<Player> specs;
  public static final List<String> playersIN = new ArrayList<>();
  public Automatic2() {
    this.main = Main.getInstance();
    time = 32;
    players = new ArrayList<>();
    this.gameType = GameType.STARTING;
    this.maxPlayers = 60;
    this.full = false;
    this.pvp = false;
    this.specs = new ArrayList<>();
    playersInPvp = new ArrayList<>();
  }
          @EventHandler
          public void onUpdate(UpdateEvent e) {
            if (e.getType() != UpdateEvent.UpdateType.SEGUNDO) {
              return; 
            }
            
            if (players.size() >= 2 && !iniciou) {
            	iniciou = true;
            }
            else if (!iniciou) {
            	time = 30;
            	return;
            }
              if (MainCommand.game.isEmpty() && iniciou) {
            	  destroy();
              }
              if (time == 30 && !star) {
            	  broadcast(Main.getInstance().getConfig().getString("TournamentStart").replaceAll("&", "§").replace("%time%", "30"));
            	  TextComponent textComponent4 = new TextComponent(Main.getInstance().getConfig().getString("TournamentStartGlobal").replaceAll("&", "§").replace("%time%", "30"));
                  textComponent4.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Main.getInstance().getConfig().getString("ClickToJoin").replaceAll("&", "§")).create()));
                  textComponent4.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pvr2 join"));
                  for (Player n2 : Bukkit.getWorld("x2.coords.spawn.world").getPlayers()) { 
                	  n2.spigot().sendMessage(textComponent4);
              } 
              }
              for (Player p : players) {
            	  if (p.getWorld() != Bukkit.getServer().getWorld(Main.cfg_x1.getString("x2.coords.spawn.world"))) {
            		  p.performCommand("pvprounds leave");
            	  }
              }
              if (time == 15 && !star) {
            	  broadcast(Main.getInstance().getConfig().getString("TournamentStart").replaceAll("&", "§").replace("%time%", "15"));
            	  TextComponent textComponent4 = new TextComponent(Main.getInstance().getConfig().getString("TournamentStartGlobal").replaceAll("&", "§").replace("%time%", "15"));
                  textComponent4.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Main.getInstance().getConfig().getString("ClickToJoin").replaceAll("&", "§")).create()));
                  textComponent4.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pvr2 join"));
                  for (Player n2 : Bukkit.getWorld("x2.coords.spawn.world").getPlayers()) { 
                	  n2.spigot().sendMessage(textComponent4);
              } 
              } 
              if (time == 10 && !star) {
            	  broadcast(Main.getInstance().getConfig().getString("TournamentStart").replaceAll("&", "§").replace("%time%", "10"));

            	 TextComponent textComponent4 = new TextComponent(Main.getInstance().getConfig().getString("TournamentStartGlobal").replaceAll("&", "§").replace("%time%", "30"));
                  textComponent4.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Main.getInstance().getConfig().getString("ClickToJoin").replaceAll("&", "§")).create()));
                  textComponent4.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pvr2 join"));
                  for (Player n2 : Bukkit.getWorld("x2.coords.spawn.world").getPlayers()) { 
                	  n2.spigot().sendMessage(textComponent4);
              } 
              }
              if (time == 5 && !star) {
            	  broadcast(Main.getInstance().getConfig().getString("TournamentStart").replaceAll("&", "§").replace("%time%", "5"));

            	  TextComponent textComponent4 = new TextComponent(Main.getInstance().getConfig().getString("TournamentStartGlobal").replaceAll("&", "§").replace("%time%", "30"));
                  textComponent4.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Main.getInstance().getConfig().getString("ClickToJoin").replaceAll("&", "§")).create()));
                  textComponent4.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pvr2 join"));
                  for (Player n2 : Bukkit.getWorld("x2.coords.spawn.world").getPlayers()) { 
                	  n2.spigot().sendMessage(textComponent4);
              } 
              }
              if (players.size() == 9 && time >= 20 && !this.full && !star) {
                time = 30;

          	  broadcast(Main.getInstance().getConfig().getString("TournamentTimeChanged").replaceAll("&", "§"));
                this.full = true;
              } 
              if (time <= 0 && !star) {
                this.gameType = GameType.GAMIMG;
            	  broadcast(Main.getInstance().getConfig().getString("TournamentStarted").replaceAll("&", "§"));

            	  Bukkit.broadcastMessage(Main.getInstance().getConfig().getString("TournamentStartedGlobal"));
               star = true;
               pvp = true;
               queuedPlayers();
               time = 32;
              } 
              if (!star) {
            	  if (time > 0) {
             time = time - 1;
              }
             if (!pvp && star) {
             queuedPlayers();
             }
            } 
          }
          public void putInEvent2(Player player) {
        	  if (players.contains(player)) {
        		  return;
        	  }
        	    players.add(player);
        	}
          public void putInEvent(Player player) {
        	  if (players.contains(player)) {
        		  return;
        	  }
        	    players.add(player);
        	    player.getInventory().clear();

          	  broadcast(Main.getInstance().getConfig().getString("PlayerJoinedMatch").replaceAll("&", "§").replace("%player%", player.getName()));
        	    player.getInventory().setArmorContents(null);
        	    for (PotionEffect pot : player.getActivePotionEffects())
        	      player.removePotionEffect(pot.getType()); 
        	  }
          
          
          @EventHandler
          public void onPlayerQuit(PlayerQuitEvent e) {
            if (players.contains(e.getPlayer())) {
              players.remove(e.getPlayer());
          	  e.getPlayer().chat("/pvprounds2 leave");
              if (playersInPvp.contains(e.getPlayer())) {
                e.getPlayer().damage(9999.0D);
                playersInPvp.remove(e.getPlayer());
                pvp = false;
          	  broadcast(Main.getInstance().getConfig().getString("PlayerLeaveServer").replaceAll("&", "§").replace("%player%", e.getPlayer().getName()));
        	  
                return;
              } 
              if (Automatic2.this.getGameType() == Automatic2.GameType.GAMIMG)
              	  broadcast(Main.getInstance().getConfig().getString("PlayerLeaveServerDeath").replaceAll("&", "§").replace("%player%", e.getPlayer().getName())); 

        	  Bukkit.dispatchCommand(e.getPlayer(), "pvprounds2 leave");
              queuedPlayers();
            } 
          }
          
          @EventHandler
          public void onPlayerDeath(PlayerDeathEvent e) {
            if (!(e.getEntity() instanceof Player))
              return; 
            if (e.getEntity().getKiller() == null)
              return; 
            Player p = e.getEntity();
            Player d = e.getEntity().getKiller();
            if ((players.contains(d) || players.contains(p)) && 
              playersInPvp.contains(d) && playersInPvp.contains(p) && (MainCommand.game.contains(d.getName()) && MainCommand.game.contains(p.getName()))) {
            	if (!iniciou) {
            		return;
            	}
              playersInPvp.remove(p);
              players.remove(p);
              p.spigot().respawn();
              e.getDrops().clear();
              pvp = false;
              p.sendMessage(Main.getInstance().getConfig().getString("PlayerKilledMessage").replaceAll("&", "§").replace("%player%", p.getName()));
              Automatic2.this.broadcast(Main.getInstance().getConfig().getString("PlayerKilledBroadcast").replaceAll("&", "§").replace("%player%", p.getName()).replace("%killer%", d.getName()));
              Automatic2.this.broadcast(Main.getInstance().getConfig().getString("PlayersLeft").replaceAll("&", "§").replace("%left%", String.valueOf(players.size())));
        	  Bukkit.dispatchCommand(p, "pvprounds2 leave");
        	  if (players.size() > 1) {
org.bukkit.World w = Bukkit.getServer().getWorld(Main.cfg_x1.getString("x1.coords.spawn.world"));
/*  98 */     d.teleport(new Location(w, Main.cfg_x1.getDouble("x1.coords.spawn.x"), 
/*  99 */       Main.cfg_x1.getDouble("x1.coords.spawn.y"), Main.cfg_x1.getDouble("x1.coords.spawn.z")));
			 	 d.getInventory().clear();
			 	 d.getInventory().setArmorContents(null);
			 	   Bukkit.getConsoleSender().sendMessage(d.getName() + " killed " + p.getName() + " in the event 1v1");
			 	  Automatic2.this.broadcast(Main.getInstance().getConfig().getString("Searching").replaceAll("&", "§"));
		              }
        	  org.bukkit.World w = Bukkit.getServer().getWorld(Main.cfg_x1.getString("x1.coords.quit.world"));
        	  /*  98 */     p.teleport(new Location(w, Main.cfg_x1.getDouble("x1.coords.quit.x"), 
        	  /*  99 */       Main.cfg_x1.getDouble("x1.coords.quit.y"), Main.cfg_x1.getDouble("x1.coords.quit.z")));
          
              queuedPlayers();
            } 
          }
          
          @EventHandler(priority = EventPriority.MONITOR)
          public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        	  
            if (!(e.getDamager() instanceof Player))
              return;
            if (!(e.getEntity() instanceof Player)) {
            	return;
            }
            Player p = (Player)e.getEntity();
            if (!star && MainCommand.game.contains(p.getName())) {
                e.setCancelled(true);
              }
            if (!Automatic2.this.isSpec((Player)e.getDamager()))
              return; 
            if (!iniciou) {
            	return;
            }
            
            e.setCancelled(true);
          }
          
          @EventHandler
          public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent e) {
            Player p = e.getPlayer();
            if (!Automatic2.this.isInEvent(p))
              return; 
            if (Automatic2.this.isInPvP(p) && iniciou) {
              e.setCancelled(true);
              p.sendMessage(Main.getInstance().getConfig().getString("CommandBlockedInBattle").replaceAll("&", "§"));
              return;
            } 
            if (e.getMessage().toLowerCase().startsWith("/") && !e.getMessage().toLowerCase().contains("/tell") && !e.getMessage().toLowerCase().contains("/pvprounds") && !p.hasPermission("kombo.cmd.report") && iniciou) {
              e.setCancelled(true);
		 	  p.sendMessage(Main.getInstance().getConfig().getString("CommandBlocked").replaceAll("&", "§"));
              return;
            } 
          }
  
  
  public boolean isInEvent(Player player) {
    return getPlayers().contains(player);
  }
  public void removeFromEvent(Player player) {
	  if (isInEvent(player)) {
	    getPlayers().remove(player);

      	 Bukkit.getConsoleSender().sendMessage("[DEBUG] " + player.getName() + " left the event!");
	    Bukkit.getConsoleSender().sendMessage(player + " got removed from event!");
	  }
  }

  
  public void queuedPlayers() {
    Player firstPlayer = null;
    Player secondPlayer = null;
    
    pvp = true;
    for (Player players12 : players) {

        Bukkit.getConsoleSender().sendMessage("[EVENT] Players in event: " + players12.getName());
      if (!MainCommand.game.contains(players12.getName())) {

    	    players.remove(players12);
      }
    }
    
      
    
    firstPlayer = null;
    
    	if (!players.isEmpty()) {
    secondPlayer = players.get((new Random()).nextInt(players.size()));
      firstPlayer = players.get((new Random()).nextInt(players.size()));
      Bukkit.getConsoleSender().sendMessage("[EVENT] FIRST PLAYER: " + firstPlayer.getName() + " VS SECOND PLAYER: " + secondPlayer.getName());
      playersInPvp.clear();
    	}
    	else {
    		
    	     destroy(); 	
    	}
    
      if (firstPlayer != secondPlayer) {
    firstPlayer.closeInventory();
    secondPlayer.closeInventory();
    Bukkit.getConsoleSender().sendMessage("[EVENT] FIRST PLAYER: " + firstPlayer.getName() + " VS SECOND PLAYER: " + secondPlayer.getName());
    
    send1v1(firstPlayer, secondPlayer);
      } else if (firstPlayer == secondPlayer && players.size() == 1 && !iniciou) {
    	
    	  time = 30;
    	  star = false;
  }
      else {
    	  {
    		  if (players.size() == 1 && iniciou) {
    			  Bukkit.getConsoleSender().sendMessage("[EVENT] PLAYER: " + firstPlayer.getName() + " IS THE WINNER");
    	      
    			  destroy();
    			  firstPlayer.chat("/pvprounds2 leave");
      		Bukkit.broadcastMessage(Main.getInstance().getConfig().getString("EventWinner").replaceAll("&", "§").replace("%player%", firstPlayer.getName()));
    			  return;
    		  }
    	  }
    	  if (players.size() >= 2) {

              queuedPlayers();
    	  }
      }
    	}
  
  public void broadcast(String message) {
    for (Player players2 : players) {
      players2.sendMessage(String.valueOf(Main.getInstance().getConfig().getString("Prefix").replaceAll("&", "§")) + message);

     TitleAPI.sendTitle(players2, 40, 40, 40, Main.getInstance().getConfig().getString("Prefix").replaceAll("&", "§"), message);
    }
    for (Player players2 : this.specs) {
      players2.sendMessage(String.valueOf((Main.getInstance().getConfig().getString("Prefix").replaceAll("&", "§")) + message));
    TitleAPI.sendTitle(players2, 40, 40, 40, Main.getInstance().getConfig().getString("Prefix").replaceAll("&", "§"), message);
  }
  }
  
  public void send1v1(Player firstPlayer, Player  secondPlayer) {
    playersInPvp.clear();
    playersInPvp.add(firstPlayer);
    playersInPvp.add(secondPlayer);
	  Automatic2.this.broadcast(Main.getInstance().getConfig().getString("Fight").replaceAll("&", "§").replace("%player1%", firstPlayer.getName()).replace("%player2%", secondPlayer.getName()));
    firstPlayer.setHealth(20.0D);
    secondPlayer.setHealth(20.0D);
	org.bukkit.World w1 = Bukkit.getServer().getWorld(Main.cfg_x1.getString("x2.coords.loc_1.world"));
	org.bukkit.World w2 = Bukkit.getServer().getWorld(Main.cfg_x1.getString("x2.coords.loc_2.world"));
/* 152 */     firstPlayer.teleport(new Location(w1, Main.cfg_x1.getDouble("x2.coords.loc_1.x"), 
/* 153 */       Main.cfg_x1.getDouble("x2.coords.loc_1.y"), Main.cfg_x1.getDouble("x2.coords.loc_1.z"), 
/* 154 */       Float.valueOf((float)Main.cfg_x1.getDouble("x2.coords.loc_1.yaw")).floatValue(), Float.valueOf((float)Main.cfg_x1.getDouble("x2.coords.loc_1.pitch")).floatValue()));
/* 155 */     secondPlayer.teleport(new Location(w2, Main.cfg_x1.getDouble("x2.coords.loc_2.x"), 
/* 156 */       Main.cfg_x1.getDouble("x2.coords.loc_2.y"), Main.cfg_x1.getDouble("x2.coords.loc_2.z"), 
/* 157 */       Float.valueOf((float)Main.cfg_x1.getDouble("x2.coords.loc_2.yaw")).floatValue(), Float.valueOf((float)Main.cfg_x1.getDouble("x2.coords.loc_2.pitch")).floatValue()));
/*     */     
    firstPlayer.sendMessage(Main.getInstance().getConfig().getString("FightAgainst").replaceAll("&", "§").replace("%player%", secondPlayer.getName()));
    secondPlayer.sendMessage(Main.getInstance().getConfig().getString("FightAgainst").replaceAll("&", "§").replace("%player%", firstPlayer.getName()));
    for (PotionEffect pot : firstPlayer.getActivePotionEffects())
      firstPlayer.removePotionEffect(pot.getType()); 
    for (PotionEffect pot : secondPlayer.getActivePotionEffects())
      secondPlayer.removePotionEffect(pot.getType()); 
    firstPlayer.getInventory().clear();
    firstPlayer.getInventory().setArmorContents(new ItemStack[4]);
    firstPlayer.closeInventory();
    secondPlayer.getInventory().clear();
    secondPlayer.getInventory().setArmorContents(new ItemStack[4]);
    secondPlayer.closeInventory();
	ItemStack helmet = new ItemStack(Material.IRON_HELMET);
	ItemStack chest = new ItemStack(Material.IRON_CHESTPLATE);
	ItemStack leg = new ItemStack(Material.IRON_LEGGINGS);
	ItemStack boost = new ItemStack(Material.IRON_BOOTS);
    firstPlayer.getInventory().setHelmet(helmet);
    firstPlayer.getInventory().setChestplate(chest);
    firstPlayer.getInventory().setBoots(boost);
    firstPlayer.getInventory().setLeggings(leg);
    secondPlayer.getInventory().setHelmet(helmet);
    secondPlayer.getInventory().setChestplate(chest);
    secondPlayer.getInventory().setBoots(boost);
    secondPlayer.getInventory().setLeggings(leg);
    firstPlayer.getInventory().setItem(0, new ItemBuilder(Main.getInstance().getConfig().getString("SwordName").replaceAll("&", "§"), Material.DIAMOND_SWORD).addEnchant(Enchantment.DAMAGE_ALL, 1)
			.nbt("cancel-drop")
			.toStack()
	);
    secondPlayer.getInventory().setItem(0, new ItemBuilder(Main.getInstance().getConfig().getString("SwordName").replaceAll("&", "§"), Material.DIAMOND_SWORD).addEnchant(Enchantment.DAMAGE_ALL, 1)
			.nbt("cancel-drop")
			.toStack()
	);
    for (int x = 0; x < 8; x++) {
      firstPlayer.getInventory().addItem(new ItemStack[] { new ItemStack(Material.MUSHROOM_SOUP) });
      secondPlayer.getInventory().addItem(new ItemStack[] { new ItemStack(Material.MUSHROOM_SOUP) });
    } 
    this.pvp = true;
  }
  
  public void setGameType(GameType newtr) {
	  this.gameType = newtr;
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
    return this.maxPlayers;
  }
  
  
  
  public boolean isInPvP(Player player) {
    return (playersInPvp.contains(player) && getGameType() == GameType.GAMIMG);
  }
  
  public void destroy() {
      setGameType(GameType.STARTING);
      iniciou = false;
      star = false;
      for (String s : new ArrayList<>(MainCommand.game)) {
    	  Player p = Bukkit.getPlayer(s);
    	  Bukkit.dispatchCommand(p, "pvprounds leave");
    	  org.bukkit.World w = Bukkit.getServer().getWorld(Main.cfg_x1.getString("x2.coords.quit.world"));
    	  /*  98 */     p.teleport(new Location(w, Main.cfg_x1.getDouble("x2.coords.quit.x"), 
    	  /*  99 */       Main.cfg_x1.getDouble("x2.coords.quit.y"), Main.cfg_x1.getDouble("x2.coords.quit.z")));
      }
      players.clear();
      time = 32;
      pvp = false;
      playersInPvp.clear();
      getPlayers().clear();
    HandlerList.unregisterAll(this.listener);
   Main.getInstance().getEventManager().setRdmAutomatic(null);
  }

  public void setMaxPlayers(int maxPlayers) {
    this.maxPlayers = maxPlayers;
  }
  
  public void setTime(int time) {
    this.time = time;
  }
  public void desmakeVanish(Player p) {
	    if (p == null) {
	      return; 
	    }
	    for (Player player : Bukkit.getOnlinePlayers()) {
	      if (!player.getName().equals(p.getName()))
	        player.showPlayer(p); 
	    } 
  }
  
  public List<Player> getSpecs() {
    return this.specs;
  }
  
  public boolean isSpec(Player p) {
    return this.specs.contains(p);
  }
  
  
  public enum GameType {
    STARTING, GAMIMG , STOPPED;
  }
}