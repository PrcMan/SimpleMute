
package me.prcman.simplemute;

import static me.prcman.simplemute.SMManager.config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
/**
 *
 * @author Jakub
 */
public class SMListeners implements Listener {
     private final SMManager plugin;

    public SMListeners(SMManager instance) {
        this.plugin = instance;
    }
    
     @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (plugin.isMuted(player)) {
            event.setCancelled(true); 
            if (!config.msgYouAreMuted().isEmpty()) {
                player.sendMessage(config.msgYouAreMuted().replaceAll("%DURATION%", plugin.expireTime(player)));
            }
            if (plugin.getMConfig().adminListen()) {
                String bCastMessage = ChatColor.WHITE + "[" + ChatColor.RED + "Mute" + ChatColor.WHITE + "]";
                bCastMessage = bCastMessage + "<" + player.getName() + "> ";
                bCastMessage = bCastMessage + ChatColor.GRAY + event.getMessage();
                Bukkit.getServer().broadcast(bCastMessage, plugin.getMConfig().broadcastNode());
            }
        } else {
            plugin.unMutePlayer(player.getName());
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerCommandPreprocessEvent (PlayerCommandPreprocessEvent  event) {
        Player player = event.getPlayer();
        String attemptedCmd = event.getMessage().split(" ")[0];        
        if (plugin.isMuted(player) && plugin.isBlockedCmd(attemptedCmd)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.YELLOW + "Bol si " + ChatColor.RED + "umlcany" + ChatColor.YELLOW + "! Dlzka: " + ChatColor.WHITE + plugin.expireTime(player));
            if (plugin.getMConfig().adminListen()) {
                String bCastMessage = ChatColor.WHITE + "[" + ChatColor.RED + "Mute" + ChatColor.WHITE + "]";
                bCastMessage = bCastMessage + "<" + player.getName() + "> ";
                bCastMessage = bCastMessage + ChatColor.GRAY + event.getMessage();
                Bukkit.getServer().broadcast(bCastMessage, plugin.getMConfig().broadcastNode());
            } else {
            }
        } 
    }
}
