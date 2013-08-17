package me.prcman.simplemute;

import java.util.ArrayList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
/**
 *
 * @author Jakub
 */
public class SMUnMuteC implements CommandExecutor {
    private final SMManager plugin;

    public SMUnMuteC(SMManager instance) {
        this.plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            if (!sender.hasPermission("minemax.unmute")) {
                if (!plugin.getMConfig().msgNoPerm().isEmpty()) {
                    sender.sendMessage(plugin.getMConfig().msgNoPerm());
                }
                return true;
            }
        }
        
         if (args.length == 1) {    
            String pName = args[0];
            if (pName.equals("*")) {
                ArrayList unMuteList = new ArrayList();            
                for (String pNameInner : plugin.mList.keySet()) {                                  
                        unMuteList.add(pNameInner);                       
                }
                for (Object pNameInner : unMuteList) {
                    plugin.unMutePlayer((String)pNameInner, sender); 
                }
                unMuteList.clear();
            } else {
                plugin.unMutePlayer(pName, sender);
            }
            return true;
        } else {
            return false;
        }
    }
    
    
}
