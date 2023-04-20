package nl.duckycraft.mtmachines;

import lombok.Getter;
import nl.duckycraft.mtmachines.commands.MachineCommand;
import nl.duckycraft.mtmachines.commands.SerializeCommand;
import nl.duckycraft.mtmachines.listeners.MachineBreakListener;
import nl.duckycraft.mtmachines.listeners.MachineInteractionListener;
import nl.duckycraft.mtmachines.manager.MachineManager;
import nl.duckycraft.mtmachines.menus.MachineMenu;
import nl.duckycraft.mtmachines.utils.ConfigurationFile;
import nl.duckycraft.mtmachines.utils.GUIHolder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;

public final class MachinePlugin extends JavaPlugin {

    private static @Getter MachinePlugin instance;
    private @Getter ConfigurationFile dataFile;
    private @Getter MachineManager manager;

    @Override
    public void onEnable() {
        instance = this;

        GUIHolder.init(this);
        saveDefaultConfig();

        dataFile = new ConfigurationFile(this, "data.yml", true);
        dataFile.saveConfig();

        manager = new MachineManager();
        manager.init();

        getCommand("serialize").setExecutor(new SerializeCommand());
        getCommand("machine").setExecutor(new MachineCommand());

        Arrays.asList(
                new MachineInteractionListener(),
                new MachineBreakListener()
        ).forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this));

    }

    @Override
    public void onDisable() {
        manager.getLoadedMachines().values().forEach(machine -> {
            manager.save(machine);
            BukkitTask task = machine.getMachineTask();
            if(task != null && !task.isCancelled()) {
                task.cancel();
            }
            machine.removeHologram();
        });

    }
}
