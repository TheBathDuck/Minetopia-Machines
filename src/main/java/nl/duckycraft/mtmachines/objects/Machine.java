package nl.duckycraft.mtmachines.objects;

import lombok.Getter;
import lombok.Setter;
import nl.duckycraft.mtmachines.MachinePlugin;
import nl.duckycraft.mtmachines.tasks.MachineRunningTask;
import nl.duckycraft.mtmachines.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Machine {

    private final @Getter Location location;
    private final @Getter List<UUID> members;
    private @Getter @Setter int fuel;

    private final @Getter List<CraftableItem> craftableItems;
    private final @Getter List<ItemStack> inventoryItems;

    private @Getter @Setter BukkitTask machineTask;
    private @Getter @Setter HashMap<MachineRunningTask.HologramType, ArmorStand> holoStands;

    public Machine(Location location, List<UUID> members, List<CraftableItem> craftableItems, List<ItemStack> inventoryItems, int fuel) {
        this.location = location;
        this.members = members;
        this.craftableItems = craftableItems;
        this.inventoryItems = inventoryItems;
        this.fuel = fuel;
        this.holoStands = new HashMap<>();
    }


    public void addMember(UUID uuid) {
        this.members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        this.members.remove(uuid);
    }

    public void addInventoryItem(ItemStack itemStack) {
        this.inventoryItems.add(itemStack);
    }

    public void removeInventoryItem(ItemStack itemStack) {
        this.inventoryItems.remove(itemStack);
    }

    public void addCraftableItem(CraftableItem factoryItem) {
        this.craftableItems.add(factoryItem);
    }

    public void removeCraftableItem(CraftableItem factoryItem) {
        this.craftableItems.remove(factoryItem);
    }

    public CraftableItem getCraftableItem(UUID uuid) {
        Optional<CraftableItem> itemOptional = craftableItems.stream().filter(item -> item.getItemUuid().equals(uuid)).findFirst();
        return itemOptional.orElse(null);
    }

    public String getSerializedLocation() {
        return LocationUtils.toString(location);
    }

    public boolean isRunning() {
        return (getMachineTask() != null);
    }

    public void removeHologram() {
        holoStands.values().forEach(Entity::remove);
    }

    public void craftItem(Player executor, CraftableItem item) {
        setMachineTask(new MachineRunningTask(executor, this, item).runTaskTimerAsynchronously(MachinePlugin.getInstance(), 0L, 20L));
    }

}
