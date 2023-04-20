package nl.duckycraft.mtmachines.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class SerializeUtils {

    public static ItemStack fromBase64(String string) {
        ItemStack finalStack = null;
        try {
            byte[] serializedItem = Base64.getDecoder().decode(string);
            ByteArrayInputStream in = new ByteArrayInputStream(serializedItem);
            BukkitObjectInputStream is = new BukkitObjectInputStream(in);

            finalStack = (ItemStack) is.readObject();
        } catch (Exception e) {
            return null;
        }
        return finalStack;
    }

    public static String toBase64(ItemStack item) {
        String finalString = null;
        try {
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);
            os.writeObject(item);
            os.flush();
            byte[] serializedObject = io.toByteArray();
            finalString = new String(Base64.getEncoder().encode(serializedObject));
        } catch (Exception e) {
            return null;
        }
        return finalString;
    }

}
