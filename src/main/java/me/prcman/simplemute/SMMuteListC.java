package me.prcman.simplemute;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Jakub
 */
public class SMMuteListC implements CommandExecutor {
    private final SMManager plugin;

    public SMMuteListC(SMManager instance) {
        this.plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            if (!sender.hasPermission("minemax.mutelist")) {
                if (!plugin.getMConfig().msgNoPerm().isEmpty()) {
                    sender.sendMessage(plugin.getMConfig().msgNoPerm());
                }
                return true;
            }
        }
        if (plugin.mList.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "[Mute List] [" + ChatColor.WHITE + 0 + ChatColor.YELLOW + "]");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "[Mute List] [" + ChatColor.WHITE + plugin.mList.size() + ChatColor.YELLOW + "]");
            for (String pName : plugin.mList.keySet()) {
                sender.sendMessage(ChatColor.AQUA + pName + ChatColor.WHITE + ": " 
                        + ChatColor.YELLOW + plugin.expireTime(pName)
                        + ChatColor.RED + " Duvod: " + plugin.mReason.get(pName));            
            }
        }
        return true;
    }
}