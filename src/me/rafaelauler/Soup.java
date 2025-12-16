package me.rafaelauler;


import org.bukkit.Bukkit;
/*    */ 
/*    */ import org.bukkit.Material;
/*    */
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/*    */
/*    */ 
/*    */ public class Soup implements Listener
/*    */ {
/*    */   private Main main;
/* 18 */   public int vida = 7;
/* 19 */   public int fome = 7;
/*    */   
/*    */   public Soup(Main main) {
/* 22 */     this.main = main;
/*    */   }
/*    */   
/*    */   @SuppressWarnings("deprecation")
@EventHandler
/*    */   public void UsarSopa(PlayerInteractEvent e) {
/* 27 */     if (e.getItem() == null) {
/* 28 */       return;
/*    */     }

Player p = e.getPlayer();
/* 33 */       if (p.getHealth() < 20.0D && MainCommand.game.contains(p.getName()) && (p.getItemInHand().getType() == Material.MUSHROOM_SOUP)) {
	e.setCancelled(true);
/* 35 */        
/* 36 */         if (p.getMaxHealth() >= 20) {
/* 37 */         p.setHealth(p.getHealth() + this.vida >= p.getMaxHealth() ? p.getMaxHealth() : p.getHealth() + this.vida);
p.setFoodLevel(20);
} else {
	// 11
	 p.setHealth(p.getHealth() + this.vida >= p.getMaxHealth() ? p.getMaxHealth() : p.getHealth() + this.vida);
}
    e.getItem().setType(Material.BOWL);


	
/*    */       }
/*    */     }
/*    */   @SuppressWarnings("deprecation")
@EventHandler
/*    */   public void UsarSopa2(PlayerInteractEvent e) {
/* 27 */     if (e.getItem() == null) {
/* 28 */       return;
/*    */     }

Player p = e.getPlayer();
/* 33 */       if (p.getHealth() < 20.0D && MainCommand2.game.contains(p.getName()) && (p.getItemInHand().getType() == Material.MUSHROOM_SOUP)) {
	e.setCancelled(true);
/* 35 */        
/* 36 */         if (p.getMaxHealth() >= 20) {
/* 37 */         p.setHealth(p.getHealth() + this.vida >= p.getMaxHealth() ? p.getMaxHealth() : p.getHealth() + this.vida);
p.setFoodLevel(20);
} else {
	// 11
	 p.setHealth(p.getHealth() + this.vida >= p.getMaxHealth() ? p.getMaxHealth() : p.getHealth() + this.vida);
}
    e.getItem().setType(Material.BOWL);


	
/*    */       }
/*    */     }
@SuppressWarnings("deprecation")
public static void setOffhandItem(Player p, ItemStack item) {
		
		if (versionToNumber() == 18) {
			p.setItemInHand(item);
		} else if (versionToNumber() > 18) {
			p.getInventory().setItemInHand(item);
			p.setItemInHand(item);
		} else {
			p.setItemInHand(item);
		}
		
	}
public static int versionToNumber() {

	String version = Bukkit.getVersion();

	if (version.contains("1.8")) {
		return 18;
	} else if (version.contains("1.9")) {
		return 19;
	} else if (version.contains("1.10")) {
		return 110;
	} else if (version.contains("1.11")) {
		return 111;
	} else if (version.contains("1.12")) {
		return 112;
	} else if (version.contains("1.13")) {
		return 113;
	} else if (version.contains("1.14")) {
		return 114;
	} else if (version.contains("1.15")) {
		return 115;
	} else if (version.contains("1.16")) {
		return 116;
	} else if (version.contains("1.17")) {
		return 117;
	} else if (version.contains("1.18")) {
		return 118;
	}
		return 500;
		
	}
/*    */   }

/*    */ 



