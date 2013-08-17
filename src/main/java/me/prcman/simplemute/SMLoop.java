package me.prcman.simplemute;
import java.util.ArrayList;
/**
 *
 * @author Jakub
 */
public class SMLoop {
    private final SMManager plugin;
    private int taskID;

    public SMLoop(SMManager instance) {
        plugin = instance;
        plugin.logInfo("SMManager main loop running.");

        taskID = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable() {
            @Override
            public void run() {
                plugin.logDebug("Checking mute list.");
                if (plugin.mList.isEmpty()) {
                    return;
                }
                ArrayList unMuteList = new ArrayList();
                for (String pName : plugin.mList.keySet()) {
                    if (plugin.mList.containsKey(pName)) {
                        long curTime = System.currentTimeMillis();
                        long expTime = plugin.mList.get(pName);
                        plugin.logDebug(expTime + " <=> " + curTime);
                        if (expTime <= curTime) {
                            unMuteList.add(pName);
                            plugin.logDebug("Unmuting " + pName);
                        }
                    }
                }
                for (Object pName : unMuteList) {
                    plugin.unMutePlayer((String) pName);
                }
                unMuteList.clear();
            }
        }, 200, 200);
    }

    public void end() {
        this.plugin.getServer().getScheduler().cancelTask(taskID);
    }
}
