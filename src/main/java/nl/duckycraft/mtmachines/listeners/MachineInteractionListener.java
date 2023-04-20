package nl.duckycraft.mtmachines.listeners;

import nl.duckycraft.mtmachines.MachinePlugin;
import nl.duckycraft.mtmachines.manager.MachineManager;
import nl.duckycraft.mtmachines.menus.MachineMenu;
import nl.duckycraft.mtmachines.objects.Machine;
import nl.duckycraft.mtmachines.utils.ChatUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class MachineInteractionListener implements Listener {

    @EventHandler
    public void machineInteractEvent(PlayerInteractEvent event) {
        if(event.getClickedBlock() == null) return;
        if(!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if(!(event.getHand() == EquipmentSlot.HAND)) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        MachineManager manager = MachinePlugin.getInstance().getManager();
        Machine machine = manager.getMachine(block.getLocation());
        if(machine == null) return;
        event.setCancelled(true);

        if(machine.isRunning()) {
            player.sendMessage(ChatUtils.color("&cDeze machine is momenteel al bezig!"));
            return;
        }

        if(!machine.getMembers().contains(player.getUniqueId())) {
            player.sendMessage(ChatUtils.color("&cJij hebt geen toegang tot deze machine!"));
            return;
        }
        new MachineMenu(player, machine);
    }

}
