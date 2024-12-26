package net.sacredlabyrinth.Phaed.PreciousStones.listeners;

import dev.norska.dsw.api.DeluxeSellwandPreSellEvent;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.helpers.ChatHelper;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class SellWandListener implements Listener {
    private final PreciousStones plugin;

    public SellWandListener() {
        plugin = PreciousStones.getInstance();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSellWand(DeluxeSellwandPreSellEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (player == null || block == null) {
            return;
        }

        Field field = plugin.getForceFieldManager().getField(block);

        if (field != null && !field.isOwner(player.getName()) && !field.isAllowed(player.getName())) {
            event.setCancelled(true);
            ChatHelper.send(player, "notAllowedToUseSellWand");
        }
    }
}
