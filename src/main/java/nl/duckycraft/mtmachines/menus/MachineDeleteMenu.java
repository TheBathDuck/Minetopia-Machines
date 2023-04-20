package nl.duckycraft.mtmachines.menus;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import nl.duckycraft.mtmachines.MachinePlugin;
import nl.duckycraft.mtmachines.objects.CraftableItem;
import nl.duckycraft.mtmachines.objects.Machine;
import nl.duckycraft.mtmachines.utils.ChatUtils;
import nl.duckycraft.mtmachines.utils.GUIHolder;
import nl.duckycraft.mtmachines.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class MachineDeleteMenu extends GUIHolder {

    private Player player;
    private Machine machine;
    private CraftableItem item;

    public MachineDeleteMenu(Player player, Machine machine) {
        this.player = player;
        this.machine = machine;
        this.inventory = Bukkit.createInventory(this, 6 * 9, ChatUtils.color("&cVerwijder een item."));

        for(int i = 0; i < machine.getCraftableItems().size(); i++) {
            CraftableItem craftableItem = machine.getCraftableItems().get(i);

            ItemBuilder builder = new ItemBuilder(craftableItem.getItem().clone());
            builder.setNBT("uuid", craftableItem.getItemUuid().toString());

            inventory.setItem(i, builder.build());
        }

        open(player);

    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        event.setCancelled(true);

        String uuid = NBTEditor.getString(event.getCurrentItem(), "uuid");
        if(uuid == null) return;

        CraftableItem item = machine.getCraftableItem(UUID.fromString(uuid));
        MachinePlugin.getInstance().getManager().removeCraftableItem(machine, item);
        new MachineDeleteMenu(player, machine);
    }
}
