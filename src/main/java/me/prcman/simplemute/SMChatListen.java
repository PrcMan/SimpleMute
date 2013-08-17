package me.prcman.simplemute;


import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

/**
 *
 * @author Jakub
 */
public class SMChatListen implements Listener {
    public SMChatListen(SMManager p)
    {
        plugin = p;
    }

    public void playerChat(PlayerChatEvent event)
    {
        Player player = event.getPlayer();
        if(player.hasPermission("shutup.bypass"))
            return;
        String sPlayer = player.getName();
        players = event.getRecipients();
        Player playersOnline[] = Bukkit.getOnlinePlayers();
        Player tempPlayer = null;
        String sTempPlayer = "";
        for(int i = 0; i < playersOnline.length; i++)
        {
            tempPlayer = playersOnline[i];
            if(tempPlayer != player)
            {
                sTempPlayer = tempPlayer.getName().toLowerCase();
                if(plugin.getConfig().getStringList((new StringBuilder("players.")).append(sTempPlayer).append(".ignoring").toString()) != null)
                {
                    ignoring = plugin.getConfig().getStringList((new StringBuilder("players.")).append(sTempPlayer).append(".ignoring").toString());
                    for(int p = 0; p < ignoring.size(); p++)
                    {
                        String ignored = (String)ignoring.get(p);
                        if(ignored.equalsIgnoreCase(sPlayer))
                        {
                            event.getRecipients().remove(tempPlayer);
                            players = event.getRecipients();
                        }
                    }

                }
            }
        }

    }

    private SMManager plugin;
    public static List ignoring = new ArrayList();
    public static Set players = new HashSet();

}
