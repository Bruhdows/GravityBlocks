package com.bruhdows.gravityblocks.manager;

import com.bruhdows.gravityblocks.GravityBlocksPlugin;
import com.bruhdows.gravityblocks.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.stream.Stream;

public record GravityStickManager(GravityBlocksPlugin plugin) {

    private NamespacedKey getGravityStickKey() {
        return new NamespacedKey(plugin, "gravity_stick");
    }

    public ItemStack createGravityStick() {
        ItemStack stick = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = stick.getItemMeta();

        if (meta != null) {
            meta.getPersistentDataContainer().set(
                    getGravityStickKey(),
                    PersistentDataType.BOOLEAN,
                    true
            );

            meta.displayName(TextUtil.color("<!i><gold><b>Gravity Stick"));
            meta.lore(Stream.of(
                    "",
                    "<!i><yellow><b>RIGHT CLICK <!b><gray>to grab/release blocks",
                    "<!i><yellow><b>LEFT CLICK <!b><gray>to delete or throw blocks",
                    "",
                    "<!i><gold>Have fun with gravity!"
            ).map(TextUtil::color).toList());
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            stick.setItemMeta(meta);
        }

        return stick;
    }

    public boolean isGravityStick(ItemStack item) {
        if (item == null || item.getType() != Material.BLAZE_ROD) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        return meta.getPersistentDataContainer().has(
                getGravityStickKey(),
                PersistentDataType.BOOLEAN
        );
    }
}