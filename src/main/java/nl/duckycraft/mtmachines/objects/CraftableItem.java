package nl.duckycraft.mtmachines.objects;

import lombok.Getter;
import lombok.Setter;
import nl.duckycraft.mtmachines.utils.SerializeUtils;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class CraftableItem {

    private final @Getter UUID itemUuid;
    private @Getter @Setter ItemStack item;
    private @Getter @Setter int fuelCost;
    private @Getter @Setter int productionTime;

    public CraftableItem(UUID itemUuid, String serializedItem, int fuelCost, int productionTime) {
        this.itemUuid = itemUuid;
        this.fuelCost = fuelCost;
        this.productionTime = productionTime;
        this.item = SerializeUtils.fromBase64(serializedItem);
    }


}
