package net.sacredlabyrinth.Phaed.PreciousStones.managers;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.CuboidEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.entries.PlayerEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.visualization.Visualization;
import net.sacredlabyrinth.Phaed.PreciousStones.visualization.Visualize;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * @author phad
 */
public class VisualizationManager {
    private PreciousStones plugin;
    private HashMap<String, Integer> counts = new HashMap<>();
    private HashMap<String, Visualization> visualizations = new HashMap<>();

    /**
     *
     */
    public VisualizationManager() {
        plugin = PreciousStones.getInstance();
    }

    /**
     * Visualize and display a single field
     *
     * @param player
     * @param field
     */
    public void visualizeSingleField(Player player, Field field) {
        addVisualizationField(player, field);
        displayVisualization(player, false);
    }

    /**
     * Visualize and display a single field for 2 seconds
     *
     * @param player
     * @param field
     */
    public void visualizeSingleFieldFast(Player player, Field field) {
        addVisualizationField(player, field);
        displayVisualization(player, false, 2);
    }

    /**
     * If the player is in the middle of a visualization
     *
     * @param player
     * @return
     */
    public boolean pendingVisualization(Player player) {
        return visualizations.containsKey(player.getName());
    }

    /**
     * Reverts all current visualizations
     */
    @SuppressWarnings("deprecation")
    public void revertAll() {
        for (Entry<String, Visualization> visualization : visualizations.entrySet()) {
            Visualization vis = visualization.getValue();
            Player player = Bukkit.getServer().getPlayerExact(visualization.getKey());

            if (player != null) {
                Visualize visualize = new Visualize(vis.getBlocks(), player, true, false, 0);
            }
        }
        visualizations.clear();
        counts.clear();
    }

    /**
     * Adds a fields perimeter to a player's visualization buffer
     *
     * @param player
     * @param field
     */
    public void addVisualizationField(Player player, Field field) {
        Visualization vis = visualizations.get(player.getName());

        if (vis == null) {
            vis = new Visualization();
        }

        if (plugin.getCuboidManager().hasOpenCuboid(player)) {
            return;
        }

        PlayerEntry data = plugin.getPlayerManager().getPlayerEntry(player);
        if (data.getDensity() == 0) {
            return;
        }

        vis.addField(field);
        int minx = field.getX() - field.getRadius();
        int maxx = field.getX() + field.getRadius();
        int minz = field.getZ() - field.getRadius();
        int maxz = field.getZ() + field.getRadius();
        if (field.hasFlag(FieldFlag.CUBOID)) {
            minx = field.getMinx();
            maxx = field.getMaxx();
            minz = field.getMinz();
            maxz = field.getMaxz();
        }

        World world = player.getWorld();
        List<Location> groundBlocks = new ArrayList<>();
        for (int z = minz; z <= maxz; z++) {
            int y = world.getHighestBlockYAt(minx, z);
            groundBlocks.add(new Location(world, minx, y, z));

            y = world.getHighestBlockYAt(maxx, z);
            groundBlocks.add(new Location(world, maxx, y, z));
        }
        for (int x = minx + 1; x < maxx; x++) {
            int y = world.getHighestBlockYAt(x, minz);
            groundBlocks.add(new Location(world, x, y, minz));

            y = world.getHighestBlockYAt(x, maxz);
            groundBlocks.add(new Location(world, x, y, maxz));
        }

        visualizations.put(player.getName(), vis);
        new Visualize(groundBlocks, player, false, false, plugin.getSettingsManager().getVisualizeSeconds());
    }

    private boolean turnCounter(String name, int size) {
        if (counts.containsKey(name)) {
            int count = counts.get(name);
            count += 1;

            if (count >= size) {
                counts.put(name, 0);
                return true;
            }

            counts.put(name, count);
        } else {
            counts.put(name, 1);
        }

        return false;
    }

    /**
     * Visualizes a single field's outline
     *
     * @param player
     * @param field
     */
    public void visualizeSingleOutline(Player player, Field field, boolean revert) {
        Visualization vis = visualizations.get(player.getName());

        if (vis == null) {
            vis = new Visualization();
        }

        // save current outline and clear out the visualization

        List<Location> newBlocks = new ArrayList<>();

        int minx = field.getX() - field.getRadius() - 1;
        int maxx = field.getX() + field.getRadius() + 1;
        int minz = field.getZ() - field.getRadius() - 1;
        int maxz = field.getZ() + field.getRadius() + 1;
        int miny = field.getY() - (Math.max(field.getHeight() - 1, 0) / 2) - 1;
        int maxy = field.getY() + (Math.max(field.getHeight() - 1, 0) / 2) + 1;

        if (field.hasFlag(FieldFlag.CUBOID)) {
            minx = field.getMinx() - 1;
            maxx = field.getMaxx() + 1;
            minz = field.getMinz() - 1;
            maxz = field.getMaxz() + 1;
            miny = field.getMiny() - 1;
            maxy = field.getMaxy() + 1;
        }

        // add  the blocks for the new outline

        for (int x = minx; x <= maxx; x++) {
            Location loc = new Location(player.getWorld(), x, miny, maxz);
            newBlocks.add(loc);

            loc = new Location(player.getWorld(), x, maxy, minz);
            newBlocks.add(loc);

            loc = new Location(player.getWorld(), x, miny, minz);
            newBlocks.add(loc);

            loc = new Location(player.getWorld(), x, maxy, maxz);
            newBlocks.add(loc);
        }

        for (int y = miny; y <= maxy; y++) {
            Location loc = new Location(player.getWorld(), minx, y, maxz);
            newBlocks.add(loc);

            loc = new Location(player.getWorld(), maxx, y, minz);
            newBlocks.add(loc);

            loc = new Location(player.getWorld(), minx, y, minz);
            newBlocks.add(loc);

            loc = new Location(player.getWorld(), maxx, y, maxz);
            newBlocks.add(loc);
        }

        for (int z = minz; z <= maxz; z++) {
            Location loc = new Location(player.getWorld(), minx, maxy, z);
            newBlocks.add(loc);

            loc = new Location(player.getWorld(), maxx, miny, z);
            newBlocks.add(loc);

            loc = new Location(player.getWorld(), minx, miny, z);
            newBlocks.add(loc);

            loc = new Location(player.getWorld(), maxx, maxy, z);
            newBlocks.add(loc);
        }

        // visualize all the new blocks that are left to visualize

        Visualize visualize = new Visualize(newBlocks, player, false, !revert, plugin.getSettingsManager().getVisualizeSeconds());
        visualizations.put(player.getName(), vis);
    }

    /**
     * Adds a fields outline to a player's visualization buffer
     *
     * @param player
     * @param ce
     */
    public void displayFieldOutline(Player player, CuboidEntry ce) {
        Visualization vis = visualizations.get(player.getName());

        if (vis == null) {
            vis = new Visualization();
        }

        // save current outline and clear out the visualization

        List<Location> oldBlocks = new ArrayList<>(vis.getOutlineBlocks());
        List<Location> newBlocks = new ArrayList<>();

        int offset = ce.selectedCount() > 1 ? 1 : 0;

        int minx = ce.getMinx() - offset;
        int miny = ce.getMiny() - offset;
        int minz = ce.getMinz() - offset;
        int maxx = ce.getMaxx() + offset;
        int maxy = ce.getMaxy() + offset;
        int maxz = ce.getMaxz() + offset;

        // add  the blocks for the new outline

        for (int x = minx; x <= maxx; x++) {
            Location loc = new Location(player.getWorld(), x, miny, maxz);
            newBlocks.add(loc);

            loc = new Location(player.getWorld(), x, maxy, minz);
            newBlocks.add(loc);

            loc = new Location(player.getWorld(), x, miny, minz);
            newBlocks.add(loc);

            loc = new Location(player.getWorld(), x, maxy, maxz);
            newBlocks.add(loc);
        }

        for (int y = miny; y <= maxy; y++) {
            Location loc = new Location(player.getWorld(), minx, y, maxz);
            newBlocks.add(loc);

            loc = new Location(player.getWorld(), maxx, y, minz);
            newBlocks.add(loc);

            loc = new Location(player.getWorld(), minx, y, minz);
            newBlocks.add(loc);

            loc = new Location(player.getWorld(), maxx, y, maxz);
            newBlocks.add(loc);
        }

        for (int z = minz; z <= maxz; z++) {
            Location loc = new Location(player.getWorld(), minx, maxy, z);
            newBlocks.add(loc);

            loc = new Location(player.getWorld(), maxx, miny, z);
            newBlocks.add(loc);

            loc = new Location(player.getWorld(), minx, miny, z);
            newBlocks.add(loc);

            loc = new Location(player.getWorld(), maxx, maxy, z);
            newBlocks.add(loc);
        }

        // revert the blocks that are no longer in the new set and should be reverted

        List<Location> revertible = new ArrayList<>(oldBlocks);
        revertible.removeAll(newBlocks);

        Visualize revert = new Visualize(revertible, player, true, false, plugin.getSettingsManager().getVisualizeSeconds());

        // visualize all the new blocks that are left to visualize

        List<Location> missing = new ArrayList<>(newBlocks);
        missing.removeAll(oldBlocks);

        Visualize visualize = new Visualize(missing, player, false, true, plugin.getSettingsManager().getVisualizeSeconds());

        vis.setOutlineBlocks(newBlocks);
        visualizations.put(player.getName(), vis);
    }

    /**
     * Whether the block is currently visualized as outline
     *
     * @param player
     * @param block
     * @return
     */
    public boolean isOutlineBlock(Player player, Block block) {
        Visualization vis = visualizations.get(player.getName());

        if (vis == null) {
            vis = new Visualization();
        }

        return vis.getOutlineBlocks().contains(new BlockEntry(block));
    }

    /**
     * @param player
     * @param field
     */
    public void addFieldMark(Player player, Field field) {
        Visualization vis = visualizations.get(player.getName());

        if (vis == null) {
            vis = new Visualization();
        }

        vis.addField(field);

        World world = plugin.getServer().getWorld(field.getWorld());

        if (world != null) {
            for (int y = 0; y < 256; y++) {
                Material typeId = world.getBlockAt(field.getX(), y, field.getZ()).getType();

                if (plugin.getSettingsManager().isThroughType(typeId)) {
                    vis.addBlock(new Location(world, field.getX(), y, field.getZ()));
                }
            }
        }

        visualizations.put(player.getName(), vis);
    }

    /**
     * Adds and displays a visualized block to the player
     *
     * @param player
     * @param material
     * @param block
     */
    @SuppressWarnings("deprecation")
    public void displaySingle(Player player, Material material, Block block) {
        Visualization vis = visualizations.get(player.getName());

        if (vis == null) {
            vis = new Visualization();
        }

        vis.addBlock(block);
        visualizations.put(player.getName(), vis);

        player.sendBlockChange(block.getLocation(), material, (byte) 0);
    }

    /**
     * Revert a single a visualized block to the player
     *
     * @param player
     * @param block
     */
    public void revertSingle(Player player, Block block) {
        Visualization vis = visualizations.get(player.getName());

        if (vis == null) {
            vis = new Visualization();
        }

        vis.addBlock(block);
        visualizations.put(player.getName(), vis);

        player.sendBlockChange(block.getLocation(), block.getBlockData());
    }

    /**
     * Displays contents of a player's visualization buffer to the player
     *
     * @param player
     * @param minusOverlap
     */
    public void displayVisualization(final Player player, boolean minusOverlap) {
        displayVisualization(player, minusOverlap, plugin.getSettingsManager().getVisualizeSeconds());
    }

    /**
     * Displays contents of a player's visualization buffer to the player
     *
     * @param player
     * @param minusOverlap
     */
    public void displayVisualization(final Player player, boolean minusOverlap, int seconds) {
        Visualization vis = visualizations.get(player.getName());

        if (vis != null) {
            if (minusOverlap) {
                for (Iterator<Location> iter = vis.getBlocks().iterator(); iter.hasNext(); ) {
                    Location loc = iter.next();

                    for (Field field : vis.getFields()) {
                        if (field.envelops(loc)) {
                            iter.remove();
                            break;
                        }
                    }
                }

                Visualize visualize = new Visualize(vis.getBlocks(), player, false, false, seconds);
            } else {
                Visualize visualize = new Visualize(vis.getBlocks(), player, false, false, seconds);
            }
        }
    }

    public void revert(Player player) {
        Visualization vis = visualizations.get(player.getName());

        if (vis == null) {
            return;
        }
        if (plugin.getCuboidManager().hasOpenCuboid(player)) {
            return;
        }

        PlayerEntry data = plugin.getPlayerManager().getPlayerEntry(player);
        if (data.getDensity() == 0) {
            return;
        }

        List<Location> groundBlocks = new ArrayList<>();

        for (Field field : vis.getFields()) {
            int minx = field.getX() - field.getRadius();
            int maxx = field.getX() + field.getRadius();
            int minz = field.getZ() - field.getRadius();
            int maxz = field.getZ() + field.getRadius();

            if (field.hasFlag(FieldFlag.CUBOID)) {
                minx = field.getMinx();
                maxx = field.getMaxx();
                minz = field.getMinz();
                maxz = field.getMaxz();
            }

            World world = player.getWorld();

            // Bordas Z
            for (int z = minz; z <= maxz; z++) {
                int y1 = world.getHighestBlockYAt(minx, z);
                groundBlocks.add(new Location(world, minx, y1, z));

                int y2 = world.getHighestBlockYAt(maxx, z);
                groundBlocks.add(new Location(world, maxx, y2, z));
            }

            // Bordas X
            for (int x = minx + 1; x < maxx; x++) {
                int y1 = world.getHighestBlockYAt(x, minz);
                groundBlocks.add(new Location(world, x, y1, minz));

                int y2 = world.getHighestBlockYAt(x, maxz);
                groundBlocks.add(new Location(world, x, y2, maxz));
            }
        }

        // Agora realmente reverte os blocos para o estado original
        for (Location loc : groundBlocks) {
            player.sendBlockChange(loc, loc.getBlock().getBlockData());
        }

        visualizations.remove(player.getName());
    }

    /**
     * Reverts the player's outline blocks
     *
     * @param player
     */
    public void revertOutline(Player player) {
        Visualization vis = visualizations.get(player.getName());

        if (vis != null) {
            visualizations.remove(player.getName());
            Visualize visualize = new Visualize(vis.getOutlineBlocks(), player, true, false, 0);
        }
    }
}
