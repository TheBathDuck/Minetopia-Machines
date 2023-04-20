package nl.duckycraft.mtmachines.manager;

import lombok.Getter;
import nl.duckycraft.mtmachines.MachinePlugin;
import nl.duckycraft.mtmachines.menus.MachineDeleteMenu;
import nl.duckycraft.mtmachines.objects.CraftableItem;
import nl.duckycraft.mtmachines.objects.Machine;
import nl.duckycraft.mtmachines.utils.ConfigurationFile;
import nl.duckycraft.mtmachines.utils.LocationUtils;
import nl.duckycraft.mtmachines.utils.SerializeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class MachineManager {

    private final @Getter HashMap<Location, Machine> loadedMachines;
    private @Getter HashMap<UUID, OfflinePlayer> offlineCache;

    public MachineManager() {
        loadedMachines = new HashMap<>();
        offlineCache = new HashMap<>();
    }

    public void init() {
        AtomicInteger amount = new AtomicInteger(0);
        long start = System.currentTimeMillis();

        ConfigurationFile dataFile = MachinePlugin.getInstance().getDataFile();
        FileConfiguration dataConfig = dataFile.getConfig();

        ConfigurationSection machineSection = dataConfig.getConfigurationSection("machines");
        if (machineSection == null) {
            dataConfig.createSection("machines");
            dataFile.saveConfig();
            return;
        }

        machineSection.getKeys(false).forEach(serializedLocation -> {
            ConfigurationSection currentMachine = machineSection.getConfigurationSection(serializedLocation);

            Location location = LocationUtils.fromString(serializedLocation);

            int fuel = currentMachine.getInt("fuel");

            List<UUID> members = new ArrayList<>();
            currentMachine.getStringList("users").forEach(stringUuid -> members.add(UUID.fromString(stringUuid)));


            List<ItemStack> inventory = new ArrayList<>();
            currentMachine.getStringList("inventory").forEach(itemString -> inventory.add(SerializeUtils.fromBase64(itemString)));


            List<CraftableItem> craftableItems = new ArrayList<>();
            ConfigurationSection itemSection = currentMachine.getConfigurationSection("items");
            if (itemSection != null) {
                itemSection.getKeys(false).forEach(uuidString -> {
                    UUID uuid = UUID.fromString(uuidString);
                    String serializedItem = itemSection.getString(uuidString + ".serializedItem");
                    int fuelCost = itemSection.getInt(uuidString + ".fuelCost");
                    int productionTime = itemSection.getInt(uuidString + ".productionTime");
                    craftableItems.add(new CraftableItem(uuid, serializedItem, fuelCost, productionTime));
                });
            }

            Machine machine = new Machine(location, members, craftableItems, inventory, fuel);
            addMachine(machine);
            amount.getAndIncrement();
        });

        Bukkit.getLogger().info("[MT-Machines] Loaded a total of " + amount.get() + " machines in " + (start - System.currentTimeMillis()) + "ms.");
    }

    public void cache(OfflinePlayer offlinePlayer) {
        offlineCache.put(offlinePlayer.getUniqueId(), offlinePlayer);
    }

    public void clearCache() {
        offlineCache = new HashMap<>();
    }

    public Machine getMachine(Location location) {
        return loadedMachines.get(location);
    }

    public void addMachine(Machine machine) {
        loadedMachines.put(machine.getLocation(), machine);
    }

    public void unloadMachine(Machine machine) {
        loadedMachines.remove(machine);
    }

    public void createMachine(Location location) {
        Machine createdMachine = new Machine(location, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 0);

        // cache machine
        addMachine(createdMachine);
        save(createdMachine);
    }

    public void save(Machine machine) {
        ConfigurationFile dataFile = MachinePlugin.getInstance().getDataFile();
        FileConfiguration dataConfig = dataFile.getConfig();

        if (dataConfig.getConfigurationSection("machines." + machine.getSerializedLocation()) == null) {
            dataConfig.createSection("machines." + machine.getSerializedLocation());
        }
        ConfigurationSection machineSection = dataConfig.getConfigurationSection("machines." + machine.getSerializedLocation());

        machineSection.set("fuel", machine.getFuel());

        List<String> savingUsers = new ArrayList<>();
        machine.getMembers().forEach(uuid -> {
            savingUsers.add(uuid.toString());
        });
        machineSection.set("users", savingUsers);

        List<String> serializedInventory = new ArrayList<>();
        machine.getInventoryItems().forEach(item -> serializedInventory.add(SerializeUtils.toBase64(item)));
        machineSection.set("inventory", serializedInventory);

        dataFile.saveConfig();
    }

    public void saveAsync(Machine machine) {
        Bukkit.getScheduler().runTaskAsynchronously(MachinePlugin.getInstance(), () -> this.save(machine));
    }

    public void addCraftableItem(Machine machine, ItemStack item, int fuelAmount, int productionTime) {
        ConfigurationFile dataFile = MachinePlugin.getInstance().getDataFile();
        FileConfiguration dataConfig = dataFile.getConfig();

        UUID uuid = UUID.randomUUID();
        String serializedItem = SerializeUtils.toBase64(item);

        machine.addCraftableItem(new CraftableItem(uuid, serializedItem, fuelAmount, productionTime));

        if (dataConfig.getConfigurationSection("machines." + machine.getSerializedLocation() + ".items") == null) {
            dataConfig.createSection("machines." + machine.getSerializedLocation() + ".items");
        }

        ConfigurationSection itemSection = dataConfig.getConfigurationSection("machines." + machine.getSerializedLocation() + ".items");

        itemSection.set(uuid + ".serializedItem", serializedItem);
        itemSection.set(uuid + ".fuelCost", fuelAmount);
        itemSection.set(uuid + ".productionTime", productionTime);

        dataFile.saveConfig();
    }

    public void removeCraftableItem(Machine machine, CraftableItem item) {
        ConfigurationFile dataFile = MachinePlugin.getInstance().getDataFile();
        FileConfiguration dataConfig = dataFile.getConfig();
        String id = machine.getSerializedLocation();

        dataConfig.set("machines." + id + ".items." + item.getItemUuid().toString(), null);
        dataFile.saveConfig();

        machine.removeCraftableItem(item);
    }

    public void delete(Machine machine) {
        ConfigurationFile dataFile = MachinePlugin.getInstance().getDataFile();
        FileConfiguration dataConfig = dataFile.getConfig();

        dataConfig.set("machines." + machine.getSerializedLocation(), null);
        loadedMachines.remove(machine.getLocation());
        dataFile.saveConfig();
    }

}
