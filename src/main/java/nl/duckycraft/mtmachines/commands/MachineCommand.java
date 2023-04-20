package nl.duckycraft.mtmachines.commands;

import nl.duckycraft.mtmachines.MachinePlugin;
import nl.duckycraft.mtmachines.manager.MachineManager;
import nl.duckycraft.mtmachines.menus.MachineDeleteMenu;
import nl.duckycraft.mtmachines.objects.Machine;
import nl.duckycraft.mtmachines.utils.ChatUtils;
import nl.duckycraft.mtmachines.utils.ItemBuilder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.StringUtil;

public class MachineCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatUtils.color("&cAlleen spelers kunnen dit uitvoeren"));
            return false;
        }
        Player player = (Player) sender;
        MachineManager manager = MachinePlugin.getInstance().getManager();

        if(args.length <= 0) {
            sendHelp(sender, label);
            return false;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if(!player.hasPermission("machine.create")) {
                player.sendMessage(ChatUtils.color("&cJij mist de permissie machine.create"));
                return false;
            }
            Block targetBlock = player.getTargetBlock(null, 30);
            if (targetBlock == null) {
                player.sendMessage(ChatUtils.color("&cJe kijkt niet naar een geldig block."));
                return false;
            }

            Machine machine = manager.getMachine(targetBlock.getLocation());
            if(machine != null) {
                player.sendMessage(ChatUtils.color("&cEr bestaat al een machine op deze locatie!"));
                return false;
            }

            manager.createMachine(targetBlock.getLocation());
            player.sendMessage(ChatUtils.color("&aJe hebt een machine gecreëerd!"));
            return false;
        }

        if(args[0].equalsIgnoreCase("brandstof")) {
            if(!player.hasPermission("machine.brandstof")) {
                player.sendMessage(ChatUtils.color("&cJij mist de permissie machine.brandstof"));
                return false;
            }
            player.getInventory().addItem(new ItemBuilder(Material.COAL)
                    .setDurability(1)
                    .setColoredName("&cBrandstof")
                    .build()
            );
            player.sendMessage(ChatUtils.color("&aJe hebt succesvol brandstof ontvangen."));
            return false;
        }

        Block targetBlock = player.getTargetBlock(null, 30);
        if (targetBlock == null) {
            player.sendMessage(ChatUtils.color("&cJe kijkt niet naar een geldig block."));
            return false;
        }
        Machine machine = manager.getMachine(targetBlock.getLocation());
        if (machine == null) {
            player.sendMessage(ChatUtils.color("&cJe kijkt niet naar een geldige machine"));
            return false;
        }

        // machine addmember
        if (args[0].equalsIgnoreCase("addmember")) {
            if(!player.hasPermission("machine.addmember")) {
                player.sendMessage(ChatUtils.color("&cJij mist de permissie machine.addmember"));
                return false;
            }
            Player targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null) {
                player.sendMessage(ChatUtils.color("&cDeze speler is niet online."));
                return false;
            }
            if (machine.getMembers().contains(targetPlayer.getUniqueId())) {
                player.sendMessage(ChatUtils.color("&cDeze persoon is al toegevoegd aan deze machine, gebruik /machine removemember <speler> om de persoon te verwijderen."));
                return false;
            }
            machine.addMember(targetPlayer.getUniqueId());
            player.sendMessage(ChatUtils.color("&aJe hebt &e" + targetPlayer.getName() + " &atoegevoegd aan de machine!"));
            return false;
        }

        // machine list
        if (args[0].equalsIgnoreCase("list")) {
            if(!player.hasPermission("machine.removemember")) {
                player.sendMessage(ChatUtils.color("&cJij mist de permissie machine.list"));
                return false;
            }
            player.sendMessage(ChatUtils.color("&aMachine Members:"));
            machine.getMembers().forEach(uuid -> {
                Bukkit.getScheduler().runTaskAsynchronously(MachinePlugin.getInstance(), () -> {
                    OfflinePlayer offlineMember = manager.getOfflineCache().get(uuid);
                    if(offlineMember == null) {
                        offlineMember = Bukkit.getOfflinePlayer(uuid);
                        manager.getOfflineCache().put(uuid, offlineMember);
                    }
                    player.sendMessage(ChatUtils.color(" &a- &e" + offlineMember.getName()));
                });
            });
        }

        // machine removemember
        if (args[0].equalsIgnoreCase("removemember")) {
            if(!player.hasPermission("machine.removemember")) {
                player.sendMessage(ChatUtils.color("&cJij mist de permissie machine.removemember"));
                return false;
            }
            Player targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null) {
                player.sendMessage(ChatUtils.color("&cDeze speler is niet online."));
                return false;
            }
            if (!machine.getMembers().contains(targetPlayer.getUniqueId())) {
                player.sendMessage(ChatUtils.color("&cDeze speler is niet toegevoegd op je machine."));
                return false;
            }
            machine.removeMember(targetPlayer.getUniqueId());
            player.sendMessage(ChatUtils.color("&aJe hebt &e" + targetPlayer.getName() + " &averwijderd van deze machine."));
            return false;
        }

        // machine delete
        if (args[0].equalsIgnoreCase("delete")) {
            if(!player.hasPermission("machine.delete")) {
                player.sendMessage(ChatUtils.color("&cJij mist de permissie machine.delete"));
                return false;
            }
            manager.unloadMachine(machine);
            manager.delete(machine);
            player.sendMessage(ChatUtils.color("&cJe hebt deze machine verwijderd."));
            return false;
        }

        // machine additem
        if(args[0].equalsIgnoreCase("additem")) {
            if(!player.hasPermission("machine.additem")) {
                player.sendMessage(ChatUtils.color("&cJij mist de permissie machine.additem"));
                return false;
            }

            if(args.length != 3 || !isInteger(args[1]) || !isInteger(args[2])) {
                player.sendMessage(ChatUtils.color("&cFout, gebruik: \"/machine additem <brandstof-kosten> <produceer-tijd>\""));
                return false;
            }

            ItemStack hand = player.getInventory().getItemInMainHand();
            if(hand == null || hand.getType().equals(Material.AIR)) {
                player.sendMessage(ChatUtils.color("&cJe kan geen lucht toevoegen, hou het item in je main hand!"));
                return false;
            }
            int fuelCost = Integer.parseInt(args[1]);
            int productionTime = Integer.parseInt(args[2]);

            manager.addCraftableItem(machine, hand, fuelCost, productionTime);
            player.sendMessage(ChatUtils.color("&aJe hebt een item toegevoegd aan deze machine!"));
            return false;
        }

        if(args[0].equalsIgnoreCase("removeitem")) {
            if (!player.hasPermission("machine.removeitem")) {
                player.sendMessage(ChatUtils.color("&cJij mist de permissie machine.removeitem"));
                return false;
            }
            new MachineDeleteMenu(player, machine);
            return false;
        }

        sendHelp(sender, label);
        return false;
    }

    public void sendHelp(CommandSender sender, String command) {
        PluginDescriptionFile description = MachinePlugin.getInstance().getDescription();
        sender.sendMessage(ChatUtils.color("&cNo subcommand specified"));
        sender.sendMessage(ChatUtils.color("&6/"+command+" <subcommand> <arg>.."));
        sender.sendMessage(ChatUtils.color("&a/"+command+" &f- &aLaat dit menu zien."));
        sender.sendMessage(ChatUtils.color("&a/"+command+" &2create &f- &aCreeer een machine op je target block."));
        sender.sendMessage(ChatUtils.color("&a/"+command+" &2delete &f- &aVerwijder een machine op je target block."));
        sender.sendMessage(ChatUtils.color("&a/"+command+" &2addmember <player> &f- &aVoeg een speler toe aan een machine."));
        sender.sendMessage(ChatUtils.color("&a/"+command+" &2removemember <player> &f- &aVerwijder een speler van een machine."));
        sender.sendMessage(ChatUtils.color("&a/"+command+" &2list &f- &aLaat een lijst van de machine members zien."));
        sender.sendMessage(ChatUtils.color("&a/"+command+" &2brandstof &f- &aKrijg een brandstof item."));
        sender.sendMessage(ChatUtils.color("&a/"+command+" &2additem <fuel-amount> <produce-time> &f- &aVoeg een item aan een machine toe."));
        sender.sendMessage(ChatUtils.color("&a/"+command+" &2removeitem &f- &aOpen het menu waar je items kan verwijderen."));
        sender.sendMessage(ChatUtils.color(""));
        sender.sendMessage(ChatUtils.color("&6" + description.getName() + " &cversie &6" + description.getVersion()));
        sender.sendMessage(ChatUtils.color("&6Author: &c" + StringUtils.join(description.getAuthors(), ", ")));
        sender.sendMessage(ChatUtils.color("&6&nMinecraft-Development&6: &cBeter slecht gejat dan goed bedacht!"));
    }

    private boolean isInteger(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
