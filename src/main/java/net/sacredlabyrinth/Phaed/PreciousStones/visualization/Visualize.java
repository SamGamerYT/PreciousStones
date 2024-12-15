package net.sacredlabyrinth.Phaed.PreciousStones.visualization;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author phaed
 */
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

    public void run() {
        int i = 0;

        while (i < PreciousStones.getInstance().getSettingsManager().getVisualizeSendSize() && !visualizationQueue.isEmpty()) {
            Location loc = visualizationQueue.poll();

            if (!loc.equals(player.getLocation()) && !loc.equals(player.getLocation().add(0, 1, 0))) {
                if (!reverting) {
                    player.spawnParticle(Particle.REDSTONE, loc, 10, 0.5, 0.5, 0.5, 0, new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1));
                }
            }
            i++;
        }

        if (visualizationQueue.isEmpty()) {
            Bukkit.getServer().getScheduler().cancelTask(timerID);

            if (!reverting) {
                if (!skipRevert) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.getVisualizationManager().revert(player), 20L * seconds);
                }
            }
        }
    }
}
