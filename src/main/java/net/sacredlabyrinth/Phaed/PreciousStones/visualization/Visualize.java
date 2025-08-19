package net.sacredlabyrinth.Phaed.PreciousStones.visualization;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.ChatHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Visualize extends BukkitRunnable {
    private PreciousStones plugin;
    private Queue<Location> visualizationQueue = new LinkedList<>();
    private final int timerID;
    private final Player player;
    private final boolean reverting;
    private final boolean skipRevert;
    private final int seconds;

    public Visualize(List<Location> blocks, Player player, boolean reverting, boolean skipRevert, int seconds) {
        this.visualizationQueue.addAll(blocks);
        this.plugin = PreciousStones.getInstance();
        this.reverting = reverting;
        this.player = player;
        this.skipRevert = skipRevert;
        this.seconds = seconds;

        timerID = this.runTaskTimerAsynchronously(plugin, 1, PreciousStones.getInstance().getSettingsManager().getVisualizeTicksBetweenSends()).getTaskId();
    }

    @Override
    public void run() {
        int i = 0;
        while (i < PreciousStones.getInstance().getSettingsManager().getVisualizeSendSize() && !visualizationQueue.isEmpty()) {
            Location loc = visualizationQueue.poll();
            if (loc == null) continue;

            if (reverting) {
                player.sendBlockChange(loc, loc.getBlock().getBlockData());
                player.sendMessage(ChatHelper.format("visualizationAutodisabled"));
            } else {
                Location top = loc.clone();
                top.setY(loc.getWorld().getHighestBlockYAt(loc));
                player.sendBlockChange(top, Material.GOLD_BLOCK.createBlockData());
            }
            i++;
        }

        if (visualizationQueue.isEmpty()) {
            Bukkit.getServer().getScheduler().cancelTask(timerID);

            if (!reverting && !skipRevert) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.getVisualizationManager().revert(player), 20L * seconds);
            }
        }
    }
}
