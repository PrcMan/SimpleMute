package me.prcman.simplemute;

import java.util.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.event.Listener;
/**
 *
 * @author Jakub
 */
public class SMManager extends JavaPlugin {
    public HashMap<String, Long> mList = new HashMap<String,Long>();
    public HashMap<String, String> mReason = new HashMap<String, String>();
    private final SMListeners smListeners = new SMListeners(this);
    public boolean configLoaded = false;
    public static SMConfig config;
    public static final String PLUGIN_NAME = "MinemaxMute";
    public static final String LOG_HEADER = "[" + PLUGIN_NAME + "]";
    static final Logger log = Logger.getLogger("Minecraft");
    private SMFile mFile = new SMFile(this);
    SMLoop smLoop;
    
     @Override
    public void onEnable() {
        loadConfig();
        mFile.loadMuteList();
        mFile.loadMuteReasonList();
        getCommand("mute").setExecutor(new SMMuteC(this) {});
        getCommand("unmute").setExecutor(new SMUnMuteC(this));
        getCommand("mutelist").setExecutor(new SMMuteListC(this));
        getServer().getPluginManager().registerEvents(new smListeners(), this);
        getServer().getPluginManager().registerEvents(new SMChatListen(this), this);
        smLoop = new SMLoop(this);
    }

    @Override
    public void onDisable() {
        smLoop.end();
        mFile.saveMuteList();
        mFile.saveMuteReasonList();
        mList.clear();
        mReason.clear();
    }
    
    void loadConfig() {
        if (!this.configLoaded) {
            getConfig().options().copyDefaults(true);
            saveConfig();
            logInfo("Configuration loaded.");
            config = new SMConfig(this);
        } else {
            reloadConfig();
            getConfig().options().copyDefaults(false);
            config = new SMConfig(this);
            logInfo("Configuration reloaded.");
        }
        configLoaded = true;
    }

    public void logInfo(String _message) {
        log.log(Level.INFO, String.format("%s %s", LOG_HEADER, _message));
    }

    public void logError(String _message) {
        log.log(Level.SEVERE, String.format("%s %s", LOG_HEADER, _message));
    }
    
    public void logDebug(String _message) {
        if (config.debugEnabled()) {
            log.log(Level.INFO, String.format("%s [DEBUG] %s", LOG_HEADER, _message));
        }
    }

    public SMConfig getMConfig() {
        return config;
    }
    
     public void mutePlayer(Player player, Long muteTime, CommandSender sender, String reason) {
        long curTime = System.currentTimeMillis();
        long expTime = curTime + (muteTime * 60 * 1000);
        String pName = player.getName();
        mList.put(pName, expTime);
        mReason.put(pName, reason);
        String senderMessage = config.msgPlayerNowMuted();
        senderMessage = senderMessage.replaceAll("%PLAYER%", pName);
        senderMessage = senderMessage.replaceAll("%DURATION%", expireTime(pName));        
        if (!reason.isEmpty()) {
            senderMessage = senderMessage + ChatColor.YELLOW + ". " + config.msgReason() + ": " + ChatColor.RED + reason;
        }
        if (config.shouldNotify()) {
            getServer().broadcastMessage(senderMessage);
        } else {
            sender.sendMessage(senderMessage);            
            if (!config.msgYouHaveBeenMuted().isEmpty()) {
                player.sendMessage(config.msgYouHaveBeenMuted().replaceAll("%DURATION%", expireTime(pName)));
            }
        }
    }
     
     public void mutePlayer(String pName, Long muteTime, CommandSender sender, String reason) {
        Player player = Bukkit.getServer().getPlayerExact(pName);
        long curTime = System.currentTimeMillis();
        long expTime = curTime + (muteTime * 60 * 1000);
        mList.put(pName, expTime);
        mReason.put(pName, reason);        
        String senderMessage = config.msgPlayerNowMuted();
        senderMessage = senderMessage.replaceAll("%PLAYER%", pName);
        senderMessage = senderMessage.replaceAll("%DURATION%", expireTime(pName));        
        if (!reason.isEmpty()) {
            senderMessage = senderMessage + ChatColor.YELLOW + ". " + config.msgReason() + ": " + ChatColor.RED + reason;            
        }
        if (config.shouldNotify()) {
            getServer().broadcastMessage(senderMessage);
        } else {
            sender.sendMessage(senderMessage);
            if (!config.msgYouHaveBeenMuted().isEmpty()) {
                if (player != null) {
                    player.sendMessage(config.msgYouHaveBeenMuted().replaceAll("%DURATION%", expireTime(pName)));
                }
            }
        }
    }
     
     public void unMutePlayer(String pName, CommandSender sender) {
        Player player = Bukkit.getServer().getPlayerExact(pName);
        String senderMessage = config.msgSenderUnMuted().replaceAll("%PLAYER%", pName);
        boolean success = unMutePlayer(pName);
        if (success) {
            if (config.shouldNotify()) {
                getServer().broadcastMessage(senderMessage);
            } else {
                logInfo(pName + " has been unmuted!");
                if (!config.msgYouHaveBeenMuted().isEmpty()) {
                    if (player != null) {
                        player.sendMessage(config.msgYouHaveBeenUnMuted());
                    }
                }
                sender.sendMessage(senderMessage);
            }            
        } else {
            sender.sendMessage(config.msgUnableToUnMute().replaceAll("%PLAYER%", pName));
        }
    }

    public boolean unMutePlayer(String p) {  
        logDebug("Unmuting: " + p);
        String pName = p;
        for (String s : mList.keySet()) {
            if (s.equalsIgnoreCase(pName)) {
                pName = s;
            }
        }
        if (mReason.containsKey(pName)) {
            mReason.remove(pName);
        } 
        if (mList.containsKey(pName)) {
            mList.remove(pName);
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isMuted(Player player) {
        String pName = player.getName();
        if (mList.containsKey(pName)) {
            long curTime = System.currentTimeMillis();
            long expTime = mList.get(pName);
            if (expTime > curTime) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public String expireTime(Player player) {
        String pName = player.getName();
        return expireTime(pName);
    }
    
    public boolean isBlockedCmd(String cmd) {
        return getMConfig().blockedCmds().contains(cmd);
    }

    public String expireTime(String pName) {
        DecimalFormat formatter = new DecimalFormat("0.00");
        if (mList.containsKey(pName)) {
            long curTime = System.currentTimeMillis();
            long expTime = mList.get(pName);
            float diffTime = ((expTime - curTime) / 1000f) / 60f;
            if (diffTime > 5256000) {
                return config.msgForever();
            }
            if (diffTime > 525600) {
                return (formatter.format(diffTime / 525600f)) + " " + config.msgYears();
            }
            if (diffTime > 1440) {
                return (formatter.format(diffTime / 1440f)) + " " + config.msgDays();
            }
            if (diffTime > 60) {
                return (formatter.format(diffTime / 60f)) + " " + config.msgHOurs();
            }
            if (diffTime < 1f) {
                return (formatter.format(diffTime * 60f)) + " " + config.msgSeconds();
            }
            return (formatter.format(diffTime)) + " " + config.msgMinutes();
        } else {
            return config.msgZeroSeconds();
        }
    }
    
    public Player lookupPlayer(String pName) {
        Player player;
        if (config.reqFullName()) {
            player = Bukkit.getPlayerExact(pName);
        } else {
            player = Bukkit.getPlayerExact(pName);
            if (player == null) {
                for (Player pl : Bukkit.getOnlinePlayers()) {
                    if (pl.getName().toLowerCase().startsWith(pName.toLowerCase())) {
                        player = Bukkit.getPlayer(pName);
                        break;
                    }
                }
            }
        }
        return player;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[])
    {
        if(cmd.getName().equalsIgnoreCase("shutup"))
        {
            if(args.length == 0)
            {
                sender.sendMessage((new StringBuilder()).append(ChatColor.GOLD).append("/Shutup [player]").toString());
                return true;
            }
            String lowerPlayer = sender.getName().toLowerCase();
            if(getConfig().get((new StringBuilder("players.")).append(lowerPlayer).append(".ignoring").toString()) == null)
            {
                ignoring.clear();
                ignoring.add(args[0].toLowerCase());
                getConfig().set((new StringBuilder("players.")).append(lowerPlayer).append(".ignoring").toString(), ignoring);
                saveConfig();
                sender.sendMessage((new StringBuilder()).append(ChatColor.AQUA).append("Ignorujes ").append(args[0]).toString());
                return true;
            }
            ignoring = getConfig().getStringList((new StringBuilder("players.")).append(lowerPlayer).append(".ignoring").toString());
            if(ignoring.contains(args[0].toLowerCase()))
            {
                sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You are already ignoring this player.").toString());
                return true;
            } else
            {
                ignoring.add(args[0].toLowerCase());
                getConfig().set((new StringBuilder("players.")).append(lowerPlayer).append(".ignoring").toString(), ignoring);
                saveConfig();
                sender.sendMessage((new StringBuilder()).append(ChatColor.AQUA).append("You are now ignoring ").append(args[0]).toString());
                return true;
            }
        }
        if(cmd.getName().equalsIgnoreCase("talk"))
        {
            if(args.length == 0)
            {
                sender.sendMessage((new StringBuilder()).append(ChatColor.GOLD).append("/talk [player]").toString());
                return true;
            }
            String lowerPlayer = sender.getName().toLowerCase();
            if(getConfig().get((new StringBuilder("players.")).append(lowerPlayer).append(".ignoring").toString()) == null)
            {
                sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You are not ignoring anyone.").toString());
                return true;
            }
            ignoring = getConfig().getStringList((new StringBuilder("players.")).append(lowerPlayer).append(".ignoring").toString());
            if(ignoring.contains(args[0].toLowerCase()))
            {
                ignoring.remove(args[0].toLowerCase());
                getConfig().set((new StringBuilder("players.")).append(lowerPlayer).append(".ignoring").toString(), ignoring);
                saveConfig();
                sender.sendMessage((new StringBuilder()).append(ChatColor.AQUA).append("You have unignored ").append(args[0]).toString());
                return true;
            } else
            {
                sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You are not ignoring this player.").toString());
                return true;
            }
        }
        if(cmd.getName().equalsIgnoreCase("shutuplist"))
        {
            List ignored;
            String message;
            if(args.length > 0)
                if(sender.hasPermission("shutup.admin"))
                {
                    String sP = args[0].toLowerCase();
                    ignored = getConfig().getStringList((new StringBuilder("players.")).append(sP).append(".ignoring").toString());
                    message = (new StringBuilder(String.valueOf(sP))).append(" is ignoring:").toString();
                    for(Iterator iterator = ignored.iterator(); iterator.hasNext();)
                    {
                        String p = (String)iterator.next();
                        message = (new StringBuilder(String.valueOf(message))).append(" ").append(p).toString();
                    }

                    sender.sendMessage((new StringBuilder()).append(ChatColor.AQUA).append(message).toString());
                    return true;
                } else
                {
                    sender.sendMessage((new StringBuilder()).append(ChatColor.RED).append("You do not have permissions for this command.").toString());
                    return true;
                }
            String lowerPlayer = sender.getName().toLowerCase();
            if(getConfig().get((new StringBuilder("players.")).append(lowerPlayer).append(".ignoring").toString()) == null)
            {
                sender.sendMessage((new StringBuilder()).append(ChatColor.AQUA).append("You are not ignoring anyone.").toString());
                return true;
            }
            ignored = getConfig().getStringList((new StringBuilder("players.")).append(lowerPlayer).append(".ignoring").toString());
            message = "You are ignoring:";
            for(Iterator iterator1 = ignored.iterator(); iterator1.hasNext();)
            {
                String p = (String)iterator1.next();
                message = (new StringBuilder(String.valueOf(message))).append(" ").append(p).toString();
            }

            sender.sendMessage((new StringBuilder()).append(ChatColor.AQUA).append(message).toString());
            return true;
        } else
        {
            return false;
        }
    }

    public static List ignoring = new ArrayList();
}
