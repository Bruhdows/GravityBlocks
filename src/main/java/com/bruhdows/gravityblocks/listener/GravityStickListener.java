package com.bruhdows.gravityblocks.listener;

import com.bruhdows.gravityblocks.GravityBlocksPlugin;
import com.bruhdows.gravityblocks.manager.GravityBlockManager;
import com.bruhdows.gravityblocks.object.GravityBlock;
import com.bruhdows.gravityblocks.util.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public record GravityStickListener(GravityBlocksPlugin plugin) implements Listener {

    private static final double THROW_POWER = 1.5;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (!plugin.getGravityStickManager().isGravityStick(item)) {
            return;
        }

        event.setCancelled(true);

        if (event.getAction() == Action.RIGHT_CLICK_AIR ||
                event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            handleRightClick(player);
        } else if (event.getAction() == Action.LEFT_CLICK_AIR ||
                event.getAction() == Action.LEFT_CLICK_BLOCK) {
            handleLeftClick(player);
        }
    }

    private void handleRightClick(Player player) {
        GravityBlockManager manager = plugin.getGravityBlockManager();
        GravityBlock heldBlock = manager.getHeldBlock(player);

        if (heldBlock != null) {
            heldBlock.release();
            TextUtil.sendActionBar(player, "<green>Released gravity block!");
        } else {
            GravityBlock targetBlock = manager.getTargetedBlock(player, 10.0);

            if (targetBlock != null) {
                if (targetBlock.isHeld()) {
                    TextUtil.sendActionBar(player, "<red>This block is already being held!");
                } else {
                    targetBlock.grab(player);
                    TextUtil.sendActionBar(player, "<green>Grabbed gravity block!");

                }
            }
        }
    }

    private void handleLeftClick(Player player) {
        GravityBlockManager manager = plugin.getGravityBlockManager();
        GravityBlock heldBlock = manager.getHeldBlock(player);

        if (heldBlock != null) {
            Vector direction = player.getEyeLocation().getDirection();
            heldBlock.throwBlock(direction, THROW_POWER);
            TextUtil.sendActionBar(player, "<green>Threw gravity block!");
        } else {
            GravityBlock targetBlock = manager.getTargetedBlock(player, 10.0);

            if (targetBlock != null) {
                manager.removeGravityBlock(targetBlock);
                TextUtil.sendActionBar(player, "<red>Deleted gravity block!");
            }
        }
    }
}