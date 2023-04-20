package nl.duckycraft.mtmachines.listeners;

import nl.duckycraft.mtmachines.MachinePlugin;
import nl.duckycraft.mtmachines.utils.ChatUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class MachineBreakListener implements Listener {

    @EventHandler
    public void machineBreakEvent(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (MachinePlugin.getInstance().getManager().getMachine(block.getLocation()) != null) {
            event.setCancelled(true);
            if (!player.hasPermission("machine.delete")) return;
            player.sendMessage(ChatUtils.color("&cJe probeert een blok te breken waar een machine aan staat gekoppeld, gebruik \"/machine delete\" om deze te verwijderen"));
        }
    }

}
