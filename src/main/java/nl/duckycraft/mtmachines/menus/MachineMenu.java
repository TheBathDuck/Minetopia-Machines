package nl.duckycraft.mtmachines.menus;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import nl.duckycraft.mtmachines.objects.CraftableItem;
import nl.duckycraft.mtmachines.objects.Machine;
import nl.duckycraft.mtmachines.utils.ChatUtils;
import nl.duckycraft.mtmachines.utils.GUIHolder;
import nl.duckycraft.mtmachines.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class MachineMenu extends GUIHolder {

    private Player player;
    private Machine machine;

    public MachineMenu(Player player, Machine machine) {
        this.player = player;
        this.machine = machine;
        this.inventory = Bukkit.createInventory(this, 6 * 9, ChatUtils.color("&cMachine"));

        machine.getCraftableItems().forEach(craftableItem -> {
            ItemBuilder builder = new ItemBuilder(craftableItem.getItem().clone()); // Prevents lore looping.
            builder.setItemFlags();
            builder.addLoreLine("");
            builder.addLoreLine("&7Brandstoffen: &c" + craftableItem.getFuelCost());
            builder.addLoreLine("&7Productie Tijd: &c" + ChatUtils.formatTime(craftableItem.getProductionTime()));
            builder.addLoreLine("");
            builder.addLoreLine("&7Klik hier om te produceren.");
            builder.setNBT("uuid", craftableItem.getItemUuid().toString());
            inventory.addItem(builder.build());
        });

        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability(0).setName(" ").setItemFlags().build());
        }

        inventory.setItem(47, new ItemBuilder(Material.CHEST)
                .setColoredName("&cMachine Inventory")
                .addLoreLine("")
                .addLoreLine("&7Alles wat je machine produceert")
                .addLoreLine("&7kan je hier claimen!")
                .addLoreLine("")
                .addLoreLine("")
                .addLoreLine("&7Er zitten momenteel &c" + machine.getInventoryItems().size() + " &7items")
                .addLoreLine("&7in deze machine.")
                .setNBT("action", "inventory")
                .build()
        );

        updateFuel();

        open(player);
    }

    private void updateFuel() {
        inventory.setItem(51, new ItemBuilder(Material.COAL)
                .setDurability(1)
                .setColoredName("&cVoeg brandstof toe.")
                .addLoreLine("")
                .addLoreLine("&7Klik om brandstof toe te voegen.")
                .addLoreLine("")
                .addLoreLine("&7Er zit momenteel &c" + machine.getFuel() + " &7brandstof")
                .addLoreLine("&7in deze machine.")
                .setNBT("action", "fillfuel")
                .build()
        );
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) return;
        event.setCancelled(true);

        if (NBTEditor.contains(event.getCurrentItem(), "action")) {
            String action = NBTEditor.getString(event.getCurrentItem(), "action");

            switch (action) {
                case "inventory":
                    new MachineInventoryMenu(player, machine);
                    break;
                case "fillfuel":
                    int total = 0;
                    Inventory playerInventory = player.getInventory();
                    for(ItemStack item : playerInventory.getContents()) {
                        if(item == null) continue;
                        if(!item.getType().equals(Material.COAL)) continue;
                        if(item.getDurability() != 1) continue;
                        total = total + item.getAmount();
                        item.setAmount(0);
                    }
                    if(total <= 0) {
                        player.sendMessage(ChatUtils.color("&cJe hebt geen brandstof in je inventory."));
                        return;
                    }
                    player.sendMessage(ChatUtils.color("&aJe hebt &e" + total + " &abrandstoffen toegevoegd."));
                    machine.setFuel(machine.getFuel() + total);
                    updateFuel();
                    break;
            }

            return;
        }

        if (!(NBTEditor.contains(event.getCurrentItem(), "uuid"))) return;

        UUID uuid = UUID.fromString(NBTEditor.getString(event.getCurrentItem(), "uuid"));
        CraftableItem craftableItem = machine.getCraftableItem(uuid);
        new MachineConfirmMenu(player, machine, craftableItem);
    }

}
