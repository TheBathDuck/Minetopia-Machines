package nl.duckycraft.mtmachines.menus;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import nl.duckycraft.mtmachines.manager.MachineManager;
import nl.duckycraft.mtmachines.objects.CraftableItem;
import nl.duckycraft.mtmachines.objects.Machine;
import nl.duckycraft.mtmachines.utils.ChatUtils;
import nl.duckycraft.mtmachines.utils.GUIHolder;
import nl.duckycraft.mtmachines.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MachineConfirmMenu extends GUIHolder {

    private Player player;
    private Machine machine;
    private CraftableItem item;

    public MachineConfirmMenu(Player player, Machine machine, CraftableItem item) {
        this.player = player;
        this.machine = machine;
        this.item = item;
        this.inventory = Bukkit.createInventory(this, 4 * 9, ChatUtils.color("&cMachine Starten?"));

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability(0).setName(" ").setItemFlags().build());
        }

        this.inventory.setItem(13, item.getItem());

        this.inventory.setItem(11, new ItemBuilder(Material.CONCRETE)
                .setDurability(14)
                .setColoredName("&cAnnuleer.")
                .setNBT("action", "close")
                .build());

        this.inventory.setItem(15, new ItemBuilder(Material.CONCRETE)
                .setDurability(5)
                .setColoredName("&aStart Machine")
                .addLoreLine("")
                .addLoreLine("&7Kosten: &c" + item.getFuelCost() + " Brandstoffen")
                .addLoreLine("&7Produceer tijd: &c" + ChatUtils.formatTime(item.getProductionTime()))
                .addLoreLine("")
                .addLoreLine("&7Jij hebt: &c" + machine.getFuel() + " Brandstoffen")
                .setNBT("action", "produce")
                .setItemFlags()
                .build()
        );

        this.inventory.setItem(31, new ItemBuilder(Material.BARRIER)
                .setColoredName("&cSluit")
                .setNBT("action", "close")
                .setItemFlags()
                .build()
        );

        this.inventory.setItem(32, new ItemBuilder(Material.SPECTRAL_ARROW)
                .setColoredName("&6Vorige Pagina")
                .setNBT("action", "return")
                .setItemFlags()
                .build()
        );

        open(player);
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) return;
        event.setCancelled(true);
        if(!(NBTEditor.contains(event.getCurrentItem(), "action"))) return;
        String action = NBTEditor.getString(event.getCurrentItem(), "action");

        switch (action) {
            case "close":
                player.closeInventory();
                break;
            case "return":
                new MachineMenu(player, machine);
                break;
            case "produce":
                if(machine.getFuel() < item.getFuelCost()) {
                    player.sendMessage(ChatUtils.color("&cDeze machine heeft niet genoeg brandstoffen!"));
                    return;
                }
                machine.setFuel(machine.getFuel() - item.getFuelCost());
                player.closeInventory();
                machine.craftItem(player, item);
                break;
        }

    }


}
