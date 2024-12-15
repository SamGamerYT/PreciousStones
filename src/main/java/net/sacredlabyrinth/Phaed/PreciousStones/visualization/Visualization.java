package net.sacredlabyrinth.Phaed.PreciousStones.visualization;

import net.sacredlabyrinth.Phaed.PreciousStones.entries.BlockEntry;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * @author phaed
 */
public class Visualization {
    private List<Location> blocks = new ArrayList<>();
    private List<Location> outlineBlocks = new ArrayList<>();
    private List<Field> fields = new ArrayList<>();

    /**
     * @param block
     */
    public void addBlock(Block block) {
        blocks.add(block.getLocation());
    }

    public void addBlock(Location loc) {
        if (!blocks.contains(loc)) {
            blocks.add(loc);
        }
    }

    /**
     * @param field
     */
    public void addField(Field field) {
        fields.add(field);
    }

    /**
     * Remove the latest added block
     *
     * @return
     */
    public void undoBlock() {
        if (blocks.size() > 1) {
            blocks.remove(blocks.size() - 1);
        }
    }

    /**
     * @return the locations
     */
    public List<Location> getBlocks() {
        return blocks;
    }

    /**
     * @return the fields
     */
    public List<Field> getFields() {
        List<Field> f = new ArrayList<>();
        f.addAll(fields);
        return f;
    }

    public void setBlocks(List<Location> bds) {
        this.blocks = bds;
    }

    public List<Location> getOutlineBlocks() {
        return outlineBlocks;
    }

    public void setOutlineBlocks(List<Location> outlineBlocks) {
        this.outlineBlocks = outlineBlocks;
    }
}
