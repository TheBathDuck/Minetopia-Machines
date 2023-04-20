package nl.duckycraft.mtmachines.menus;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import nl.duckycraft.mtmachines.MachinePlugin;
import nl.duckycraft.mtmachines.objects.Machine;
import nl.duckycraft.mtmachines.utils.ChatUtils;
import nl.duckycraft.mtmachines.utils.GUIHolder;
import nl.duckycraft.mtmachines.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class MachineInventoryMenu extends GUIHolder {

    private Player player;
    private Machine machine;

    public MachineInventoryMenu(Player player, Machine machine) {
        this.player = player;
        this.machine = machine;
        this.inventory = Bukkit.createInventory(this, 6 * 9, ChatUtils.color("&cMachine Inventory"));

//        machine.getInventoryItems().forEach(item -> {
//            inventory.addItem(item);
//        });
        for(int i = 0; i < machine.getInventoryItems().size(); i++) {
            ItemStack item = machine.getInventoryItems().get(i);
            inventory.setItem(i, item);
        }

        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability(0).setName(" ").setItemFlags().build());
        }
        this.inventory.setItem(49, new ItemBuilder(Material.SPECTRAL_ARROW)
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

        if(NBTEditor.contains(event.getCurrentItem(), "action")) {
            String action = NBTEditor.getString(event.getCurrentItem(), "action");

            switch (action) {
                case "return":
                    new MachineMenu(player, machine);
                    break;
            }

            return;
        }

        ItemStack item = event.getCurrentItem();
        if(!machine.getInventoryItems().contains(item)) return;
        machine.getInventoryItems().remove(item);
        player.getInventory().addItem(item);
        MachinePlugin.getInstance().getManager().saveAsync(machine);
        new MachineInventoryMenu(player, machine);
    }
}
