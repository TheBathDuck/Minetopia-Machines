package nl.duckycraft.mtmachines.commands;

import nl.duckycraft.mtmachines.utils.SerializeUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SerializeCommand implements CommandExecutor {



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();

        Bukkit.getLogger().info(SerializeUtils.toBase64(item));
        return false;
    }
}
