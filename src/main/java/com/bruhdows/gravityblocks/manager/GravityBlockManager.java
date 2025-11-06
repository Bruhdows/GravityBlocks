package com.bruhdows.gravityblocks.manager;

import com.bruhdows.gravityblocks.object.GravityBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class GravityBlockManager {

    private final List<GravityBlock> gravityBlocks = new ArrayList<>();

    public void createGravityBlock(Location location, Material material, double size) {
        GravityBlock block = new GravityBlock(location, material, size);
        gravityBlocks.add(block);
    }

    public void removeGravityBlock(GravityBlock block) {
        block.remove();
        gravityBlocks.remove(block);
    }

    public void tick() {
        for (GravityBlock block : gravityBlocks) {
            block.tick();
        }

        for (int i = 0; i < gravityBlocks.size(); i++) {
            for (int j = i + 1; j < gravityBlocks.size(); j++) {
                GravityBlock block1 = gravityBlocks.get(i);
                GravityBlock block2 = gravityBlocks.get(j);

                if (block1.checkCollisionWith(block2)) {
                    block1.handleCollisionWith(block2);
                }
            }
        }
    }

    public GravityBlock getTargetedBlock(Player player, double maxDistance) {
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection().normalize();
        Vector start = eyeLoc.toVector();

        GravityBlock closest = null;
        double closestDistance = maxDistance;

        for (GravityBlock block : gravityBlocks) {
            BoundingBox boundingBox = block.getBoundingBox(block.getLocation());
            RayTraceResult result = boundingBox.rayTrace(start, direction, maxDistance);

            if (result != null) {
                double distance = result.getHitPosition().distance(start);
                if (distance < closestDistance) {
                    closest = block;
                    closestDistance = distance;
                }
            }
        }

        return closest;
    }

    public GravityBlock getHeldBlock(Player player) {
        for (GravityBlock block : gravityBlocks) {
            if (block.isHeld() && block.getHolder() != null &&
                    block.getHolder().getUniqueId().equals(player.getUniqueId())) {
                return block;
            }
        }
        return null;
    }

    public void cleanupAll() {
        Iterator<GravityBlock> iterator = gravityBlocks.iterator();
        while (iterator.hasNext()) {
            GravityBlock block = iterator.next();
            block.remove();
            iterator.remove();
        }
    }

    public int getBlockCount() {
        return gravityBlocks.size();
    }
}