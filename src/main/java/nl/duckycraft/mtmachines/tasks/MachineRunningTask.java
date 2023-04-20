package nl.duckycraft.mtmachines.tasks;

import nl.duckycraft.mtmachines.MachinePlugin;
import nl.duckycraft.mtmachines.objects.CraftableItem;
import nl.duckycraft.mtmachines.objects.Machine;
import nl.duckycraft.mtmachines.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class MachineRunningTask extends BukkitRunnable {

    private final Player player;
    private final Machine machine;
    private final CraftableItem craftableItem;

    private int seconds;

    public MachineRunningTask(Player player, Machine machine, CraftableItem item) {
        this.player = player;
        this.machine = machine;
        this.craftableItem = item;
        this.seconds = item.getProductionTime();

        loadHolograms();
    }

    public void loadHolograms() {
        Location location = machine.getLocation();

        Location primaryLocation = location.clone().add(0.5, 1.25, 0.5);
        Location secondaryLocation = location.clone().add(0.5, 1, 0.5);

        ArmorStand primaryStand = location.getWorld().spawn(primaryLocation, ArmorStand.class);
        primaryStand.setCustomName(ChatUtils.color("&7Produceren.."));

        machine.getHoloStands().put(HologramType.PRIMARY, primaryStand);

        ArmorStand secondaryStand = location.getWorld().spawn(secondaryLocation, ArmorStand.class);
        secondaryStand.setCustomName(ChatUtils.color("&7Klaar Over: &c" + ChatUtils.formatTime(seconds)));
        machine.getHoloStands().put(HologramType.SECONDARY, secondaryStand);

        machine.getHoloStands().values().forEach(stand -> {
            stand.setVisible(false);
            stand.setCustomNameVisible(true);
            stand.setInvulnerable(true);
            stand.setMarker(true);
            stand.setGravity(false);
        });
    }

    @Override
    public void run() {
        seconds--;

        machine.getHoloStands().get(HologramType.SECONDARY).setCustomName(ChatUtils.color("&7Klaar Over: &c" + ChatUtils.formatTime(seconds)));


        if (seconds == 0) {
            machine.getInventoryItems().add(craftableItem.getItem());
            machine.setMachineTask(null);
            MachinePlugin.getInstance().getManager().saveAsync(machine);
            cancel();
        }
    }

    @Override
    public void cancel() {
        Bukkit.getScheduler().cancelTask(this.getTaskId());
        machine.getHoloStands().values().forEach(Entity::remove);
    }


    public enum HologramType {
        PRIMARY, SECONDARY;
    }
}
