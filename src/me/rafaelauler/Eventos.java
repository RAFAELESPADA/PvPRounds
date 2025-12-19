package me.rafaelauler;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;


public class Eventos implements Listener {

	  @EventHandler
	  public void onSignChange2(SignChangeEvent e)
	  {
	    if (e.getLine(0).equalsIgnoreCase("[pr]") && (e.getLine(1).equalsIgnoreCase("join")) && e.getPlayer().hasPermission("kitpvp.createsigns"))
	    {
	      e.setLine(0, "§e[PVPRounds]");
	      e.setLine(1, "§bJoin");
	      e.setLine(2, "§6Right Click");
	      e.setLine(2, "§3Me");
	    }
	  }
	  @EventHandler
	  public void onSignChange22(SignChangeEvent e)
	  {
	    if (e.getLine(0).equalsIgnoreCase("[pr2]") && (e.getLine(1).equalsIgnoreCase("join")) && e.getPlayer().hasPermission("kitpvp.createsigns"))
	    {
	      e.setLine(0, "§e[PVPRounds2]");
	      e.setLine(1, "§bJoin");
	      e.setLine(2, "§6Right Click");
	      e.setLine(2, "§3Me");
	    }
	  }
	  

	  
	  @EventHandler
	  public void inv(PlayerInteractEvent e)
	  {
		  Player p = e.getPlayer();
		  if ((e.getAction() == Action.RIGHT_CLICK_BLOCK) && (e.getClickedBlock() != null) && (
			      (e.getClickedBlock().getType() == Material.SIGN) || (e.getClickedBlock().getType() == Material.WALL_SIGN)))
			    {
			      Sign s = (Sign)e.getClickedBlock().getState();
			      String[] lines = s.getLines();
			      if ((lines.length > 0) && (lines[0].equals("§e[PVPRounds]") && 
			        (lines.length > 1) && (lines[1].equals("§bJoin") && 
			        (lines.length > 2) && (lines[2].equals("§6Right Click") && 
			        (lines.length > 3) && (lines[3].equals("§3Me")))))) {
			    	  p.performCommand("pvprounds join");
			      }
			    }
	  }
		  @EventHandler
		  public void inv2(PlayerInteractEvent e)
		  {
			  Player p = e.getPlayer();
			  if ((e.getAction() == Action.RIGHT_CLICK_BLOCK) && (e.getClickedBlock() != null) && (
				      (e.getClickedBlock().getType() == Material.SIGN) || (e.getClickedBlock().getType() == Material.WALL_SIGN)))
				    {
				      Sign s = (Sign)e.getClickedBlock().getState();
				      String[] lines = s.getLines();
				      if ((lines.length > 0) && (lines[0].equals("§e[PVPRounds2]") && 
				        (lines.length > 1) && (lines[1].equals("§bJoin") && 
				        (lines.length > 2) && (lines[2].equals("§6Right Click") && 
				        (lines.length > 3) && (lines[3].equals("§3Me")))))) {
				    	  p.performCommand("pvprounds2 join");
				      }
		  }
	  }

	  @EventHandler
	  public void aoconstruir(BlockPlaceEvent e)
	  {
	    if (MainCommand.game.contains(e.getPlayer().getName())) {
	      e.setCancelled(true);
	    }
	  }
	  @EventHandler
	  public void aocgonstruir(FoodLevelChangeEvent e)
	  {
	    if (MainCommand.game.contains(e.getEntity().getName())) {
	      e.setCancelled(true);
	    }
	  }

	  @EventHandler
	  public void aocgonstruir2(FoodLevelChangeEvent e)
	  {
	    if (MainCommand2.game.contains(e.getEntity().getName())) {
	      e.setCancelled(true);
	    }
	  }
	  
	  @EventHandler
	  public void aoconstruir(BlockBreakEvent e)
	  {
	    if (MainCommand.game.contains(e.getPlayer().getName())) {
	      e.setCancelled(true);
	    }
	  }
	  @EventHandler
	  public void aoconst2ruir(BlockPlaceEvent e)
	  {
	    if (MainCommand2.game.contains(e.getPlayer().getName())) {
	      e.setCancelled(true);
	    }
	  }
	  
	  @EventHandler
	  public void aoconst2ruir(BlockBreakEvent e)
	  {
	    if (MainCommand2.game.contains(e.getPlayer().getName())) {
	      e.setCancelled(true);
	    }
	  }
	  }	  
