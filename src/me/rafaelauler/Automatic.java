package me.rafaelauler;



import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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

import net.wavemc.core.bukkit.item.ItemBuilder;
import net.wavemc.core.util.UpdateEvent;

public class Automatic implements Listener {
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
  public Automatic() {
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
            	  
                  broadcast("§bPvP Round will start in 30 seconds");
              }
              if (time == 15 && !star) {
                  broadcast("§bPvPRound will start in 15 seconds");
              } 
              if (time == 10 && !star) {
                  broadcast("§bPvPRound will start in 10 seconds");

              }
              if (time == 5 && !star) {
                  broadcast("§bPvPRound will start in 5 seconds");

              }
              if (players.size() == 9 && time >= 20 && !this.full && !star) {
                time = 30;

                broadcast("§bThe time changed because the minigame is almost full");
                this.full = true;
              } 
              if (time <= 0 && !star) {
                this.gameType = GameType.GAMIMG;
               broadcast("§aThe minigame PvPRounds will now start!");
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
            	 Bukkit.getConsoleSender().sendMessage("[DEBUG] SENDING TWO RANDOM PLAYERS TO FIGHT!");
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

           	 Bukkit.getConsoleSender().sendMessage("[DEBUG] " + player.getName() + " joined the event!");
        	    player.getInventory().setArmorContents(null);
        	    for (PotionEffect pot : player.getActivePotionEffects())
        	      player.removePotionEffect(pot.getType()); 
        	  }
          
          
          @EventHandler
          public void onPlayerQuit(PlayerQuitEvent e) {
            if (players.contains(e.getPlayer())) {
              players.remove(e.getPlayer());
              if (playersInPvp.contains(e.getPlayer())) {
                e.getPlayer().damage(9999.0D);
                playersInPvp.remove(e.getPlayer());
                pvp = false;

          	  Bukkit.dispatchCommand(e.getPlayer(), "pvprounds leave");
                Automatic.this.broadcast("§bThe player " + e.getPlayer().getName() + " §bdied because he left!");
                return;
              } 
              if (Automatic.this.getGameType() == Automatic.GameType.GAMIMG)
                Automatic.this.broadcast("§bThe player " + e.getPlayer().getName() + " left and died!"); 

        	  Bukkit.dispatchCommand(e.getPlayer(), "pvprounds leave");
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
              p.sendMessage("§e§lPvPRound §fYou got killed by "  + d.getName() + "!");
              Automatic.this.broadcast("§bThe player " + p.getName() + " got killed by "  + d.getName() + "!");
              Automatic.this.broadcast("§b"+ players.size() + " players left.");

        	  Bukkit.dispatchCommand(p, "pvprounds leave");
org.bukkit.World w = Bukkit.getServer().getWorld(Main.cfg_x1.getString("x1.coords.spawn.world"));
/*  98 */     d.teleport(new Location(w, Main.cfg_x1.getDouble("x1.coords.spawn.x"), 
/*  99 */       Main.cfg_x1.getDouble("x1.coords.spawn.y"), Main.cfg_x1.getDouble("x1.coords.spawn.z")));
			 	 d.getInventory().clear();
			 	 d.getInventory().setArmorContents(null);
			 	   Bukkit.getConsoleSender().sendMessage(d.getName() + " killed " + p.getName() + " in the event 1v1");
              Automatic.this.broadcast("§bSearching for the next players...");
             
              queuedPlayers();
            } 
          }
          
          @EventHandler(priority = EventPriority.MONITOR)
          public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        	  if (!star) {
                  e.setCancelled(true);
                }
            if (!(e.getDamager() instanceof Player))
              return; 
            if (!Automatic.this.isSpec((Player)e.getDamager()))
              return; 
            if (!iniciou) {
            	return;
            }
            
            e.setCancelled(true);
          }
          
          @EventHandler
          public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent e) {
            Player p = e.getPlayer();
            if (!Automatic.this.isInEvent(p))
              return; 
            if (Automatic.this.isInPvP(p) && iniciou) {
              e.setCancelled(true);
              p.sendMessage(String.valueOf("§eDo not use commands in battle."));
              return;
            } 
            if (e.getMessage().toLowerCase().startsWith("/") && !e.getMessage().toLowerCase().contains("/tell") && !e.getMessage().toLowerCase().contains("/pvprounds") && !p.hasPermission("kombo.cmd.report") && iniciou) {
              e.setCancelled(true);
              p.sendMessage(String.valueOf("§cTo left the event write /pvprounds leave!"));
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
    Bukkit.getConsoleSender().sendMessage("[EVENT] FILA CHAMADA");
    
    pvp = true;
    for (Player players12 : players) {

        Bukkit.getConsoleSender().sendMessage("[EVENT] Jogadores na lista: " + players12.getName());
      if (!MainCommand.game.contains(players12.getName())) {

    	    Bukkit.getConsoleSender().sendMessage("[EVENT] REMOVENDO PLAYERS QUE NÃO ESTÃO NA VARIAVEL");
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
    			  firstPlayer.chat("/pvprounds leave");
      		Bukkit.broadcastMessage("§6The event PvPRounds ended! The winner is " + firstPlayer.getName());
	            	    		for (Player pg : Bukkit.getOnlinePlayers()) {
	            	    			pg.playSound(pg.getLocation(), Sound.ENTITY_GHAST_DEATH, 10f, 10f);
	            	    		}
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
      players2.sendMessage(String.valueOf("§b§l1V1: §f") + message);

		players2.playSound(players2.getLocation(), Sound.UI_BUTTON_CLICK, 10f, 10f);
     TitleAPI.sendTitle(players2, 40, 40, 40, "§b§l1V1:", message);
    }
    for (Player players2 : this.specs) {
      players2.sendMessage(String.valueOf(("§b§l1V1: §f") + message));
    TitleAPI.sendTitle(players2, 40, 40, 40, "§b§l1V1:", message);
	players2.playSound(players2.getLocation(), Sound.UI_BUTTON_CLICK, 10f, 10f);
  }
  }
  
  public void send1v1(Player firstPlayer, Player  secondPlayer) {
    playersInPvp.clear();
    playersInPvp.add(firstPlayer);
    playersInPvp.add(secondPlayer);
    broadcast("§cThe player §e" + firstPlayer.getName() + " §cwill fight against §e" +  secondPlayer.getName());
    firstPlayer.setHealth(20.0D);
    secondPlayer.setHealth(20.0D);
	org.bukkit.World w1 = Bukkit.getServer().getWorld(Main.cfg_x1.getString("x1.coords.loc_1.world"));
	org.bukkit.World w2 = Bukkit.getServer().getWorld(Main.cfg_x1.getString("x1.coords.loc_2.world"));
/* 152 */     firstPlayer.teleport(new Location(w1, Main.cfg_x1.getDouble("x1.coords.loc_1.x"), 
/* 153 */       Main.cfg_x1.getDouble("x1.coords.loc_1.y"), Main.cfg_x1.getDouble("x1.coords.loc_1.z"), 
/* 154 */       Float.valueOf((float)Main.cfg_x1.getDouble("x1.coords.loc_1.yaw")).floatValue(), Float.valueOf((float)Main.cfg_x1.getDouble("x1.coords.loc_1.pitch")).floatValue()));
/* 155 */     secondPlayer.teleport(new Location(w2, Main.cfg_x1.getDouble("x1.coords.loc_2.x"), 
/* 156 */       Main.cfg_x1.getDouble("x1.coords.loc_2.y"), Main.cfg_x1.getDouble("x1.coords.loc_2.z"), 
/* 157 */       Float.valueOf((float)Main.cfg_x1.getDouble("x1.coords.loc_2.yaw")).floatValue(), Float.valueOf((float)Main.cfg_x1.getDouble("x1.coords.loc_2.pitch")).floatValue()));
/*     */     
    firstPlayer.sendMessage("§eYou will fight against "  + secondPlayer.getName());
    secondPlayer.sendMessage("§eYou will fight against " + firstPlayer.getName());
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
    firstPlayer.getInventory().setItem(0, new ItemBuilder("§7Sword", Material.DIAMOND_SWORD).addEnchant(Enchantment.SHARPNESS, 1)
			.nbt("cancel-drop")
			.toStack()
	);
    secondPlayer.getInventory().setItem(0, new ItemBuilder("§7Sword", Material.DIAMOND_SWORD).addEnchant(Enchantment.SHARPNESS, 1)
			.nbt("cancel-drop")
			.toStack()
	);
    for (int x = 0; x < 8; x++) {
      firstPlayer.getInventory().addItem(new ItemStack[] { new ItemStack(Material.MUSHROOM_STEW) });
      secondPlayer.getInventory().addItem(new ItemStack[] { new ItemStack(Material.MUSHROOM_STEW) });
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