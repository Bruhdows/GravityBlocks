package com.bruhdows.gravityblocks.object;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

@Getter
public class GravityBlock {

    private final BlockDisplay display;
    private Vector velocity;
    private final double size;
    private boolean isHeld;
    private Player holder;

    private static final double GRAVITY = 0.05;
    private static final double DRAG = 0.985;
    private static final double GROUND_FRICTION = 0.88;
    private static final double BOUNCE_DAMPING = 0.65;
    private static final double MAX_VELOCITY = 4.0;
    private static final double MIN_BOUNCE_VELOCITY = 0.08;

    private static final double HOLD_DISTANCE = 3.5;
    private static final double HOLD_SPRING_STRENGTH = 0.25;
    private static final double HOLD_DAMPING = 0.75;
    private static final double MAX_HOLD_VELOCITY = 2.0;
    private static final int VELOCITY_HISTORY_SIZE = 5;

    private Location lastHeldPosition;
    private final Vector[] velocityHistory;
    private int velocityHistoryIndex;
    private int ticksSinceGrabbed;
    private boolean onGround;

    public GravityBlock(Location location, Material material, double size) {
        this.display = location.getWorld().spawn(
                new Location(location.getWorld(), location.getX(), location.getY() + 1, location.getZ()),
                BlockDisplay.class,
                display -> {
                    BlockData blockData = material.createBlockData();
                    display.setBlock(blockData);
                    display.setBrightness(new Display.Brightness(15, 15));

                    Transformation transformation = display.getTransformation();
                    Vector3f scale = new Vector3f((float) size, (float) size, (float) size);
                    transformation.getScale().set(scale);
                    display.setTransformation(transformation);

                    display.setInterpolationDuration(2);
                    display.setInterpolationDelay(-1);
                }
        );

        this.velocity = new Vector(0, 0, 0);
        this.size = size;
        this.isHeld = false;
        this.holder = null;
        this.velocityHistory = new Vector[VELOCITY_HISTORY_SIZE];
        for (int i = 0; i < VELOCITY_HISTORY_SIZE; i++) {
            velocityHistory[i] = new Vector(0, 0, 0);
        }
        this.velocityHistoryIndex = 0;
        this.ticksSinceGrabbed = 0;
        this.onGround = false;
        this.lastHeldPosition = null;
    }

    public void tick() {
        if (isHeld && holder != null) {
            updateHeldPosition();
            ticksSinceGrabbed++;
            return;
        }

        velocity.setY(velocity.getY() - GRAVITY);
        velocity.multiply(DRAG);

        if (onGround) {
            velocity.setX(velocity.getX() * GROUND_FRICTION);
            velocity.setZ(velocity.getZ() * GROUND_FRICTION);
        }

        if (velocity.length() > MAX_VELOCITY) {
            velocity.normalize().multiply(MAX_VELOCITY);
        }

        Location currentLoc = display.getLocation();
        Location nextLoc = currentLoc.clone().add(velocity);

        if (checkWorldCollision(nextLoc)) {
            handleWorldCollision(currentLoc);
        } else {
            display.teleport(nextLoc);
            onGround = false;
        }
    }

    private void updateHeldPosition() {
        if (holder == null || !holder.isOnline()) {
            release();
            return;
        }

        Location eyeLoc = holder.getEyeLocation();
        Vector direction = eyeLoc.getDirection();
        Location targetLoc = eyeLoc.add(direction.multiply(HOLD_DISTANCE));
        Location currentLoc = display.getLocation();

        Vector toTarget = targetLoc.toVector().subtract(currentLoc.toVector());
        Vector springForce = toTarget.clone().multiply(HOLD_SPRING_STRENGTH);

        velocity.add(springForce);
        velocity.multiply(HOLD_DAMPING);

        if (velocity.length() > MAX_HOLD_VELOCITY) {
            velocity.normalize().multiply(MAX_HOLD_VELOCITY);
        }

        Location newLoc = currentLoc.add(velocity);
        display.teleport(newLoc);

        if (lastHeldPosition != null) {
            Vector currentVelocity = newLoc.toVector().subtract(lastHeldPosition.toVector());

            if (ticksSinceGrabbed > 0) {
                Vector lastVel = velocityHistory[(velocityHistoryIndex - 1 + VELOCITY_HISTORY_SIZE) % VELOCITY_HISTORY_SIZE];
                currentVelocity = lastVel.clone().multiply(0.3).add(currentVelocity.multiply(0.7));
            }

            velocityHistory[velocityHistoryIndex] = currentVelocity.clone();
            velocityHistoryIndex = (velocityHistoryIndex + 1) % VELOCITY_HISTORY_SIZE;
        }

        lastHeldPosition = newLoc.clone();
    }

    private boolean checkWorldCollision(Location location) {
        BoundingBox box = getBoundingBox(location);

        int minX = (int) Math.floor(box.getMinX());
        int minY = (int) Math.floor(box.getMinY());
        int minZ = (int) Math.floor(box.getMinZ());
        int maxX = (int) Math.ceil(box.getMaxX());
        int maxY = (int) Math.ceil(box.getMaxY());
        int maxZ = (int) Math.ceil(box.getMaxZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location blockLoc = new Location(location.getWorld(), x, y, z);
                    if (blockLoc.getBlock().getType().isSolid()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void handleWorldCollision(Location currentLoc) {
        boolean collisionOccurred = false;

        Location testLoc = currentLoc.clone().add(velocity.getX(), 0, 0);
        if (checkWorldCollision(testLoc)) {
            velocity.setX(-velocity.getX() * BOUNCE_DAMPING);
            collisionOccurred = true;
        }

        testLoc = currentLoc.clone().add(0, velocity.getY(), 0);
        if (checkWorldCollision(testLoc)) {
            if (velocity.getY() < 0) {
                onGround = true;
            }

            if (Math.abs(velocity.getY()) < MIN_BOUNCE_VELOCITY) {
                velocity.setY(0);
            } else {
                velocity.setY(-velocity.getY() * BOUNCE_DAMPING);
            }
            collisionOccurred = true;
        }

        testLoc = currentLoc.clone().add(0, 0, velocity.getZ());
        if (checkWorldCollision(testLoc)) {
            velocity.setZ(-velocity.getZ() * BOUNCE_DAMPING);
            collisionOccurred = true;
        }

        if (!collisionOccurred) {
            Location safeLoc = currentLoc.clone();
            if (!checkWorldCollision(currentLoc.clone().add(velocity.getX(), 0, 0))) {
                safeLoc.add(velocity.getX(), 0, 0);
            }
            if (!checkWorldCollision(currentLoc.clone().add(0, velocity.getY(), 0))) {
                safeLoc.add(0, velocity.getY(), 0);
            }
            if (!checkWorldCollision(currentLoc.clone().add(0, 0, velocity.getZ()))) {
                safeLoc.add(0, 0, velocity.getZ());
            }
            display.teleport(safeLoc);
        }
    }

    public boolean checkCollisionWith(GravityBlock other) {
        if (this == other) return false;

        BoundingBox thisBox = getBoundingBox(display.getLocation());
        BoundingBox otherBox = other.getBoundingBox(other.display.getLocation());

        return thisBox.overlaps(otherBox);
    }

    public void handleCollisionWith(GravityBlock other) {
        Location thisLoc = display.getLocation();
        Location otherLoc = other.display.getLocation();
        Vector collisionNormal = thisLoc.toVector().subtract(otherLoc.toVector()).normalize();

        Vector relativeVel = velocity.clone().subtract(other.velocity);
        double velAlongNormal = relativeVel.dot(collisionNormal);

        if (velAlongNormal > 0) return;

        double restitution = 0.7;
        double impulse = -(1 + restitution) * velAlongNormal;
        impulse /= 2;

        Vector impulseVec = collisionNormal.clone().multiply(impulse);
        velocity.add(impulseVec);
        other.velocity.subtract(impulseVec);

        Vector separation = collisionNormal.multiply(0.05);
        display.teleport(thisLoc.add(separation));
        other.display.teleport(otherLoc.subtract(separation));
    }

    public BoundingBox getBoundingBox(Location location) {
        return new BoundingBox(
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getX() + size,
                location.getY() + size,
                location.getZ() + size
        );
    }

    public void grab(Player player) {
        this.isHeld = true;
        this.holder = player;
        this.velocity.setX(0).setY(0).setZ(0);
        this.lastHeldPosition = display.getLocation().clone();
        this.ticksSinceGrabbed = 0;
        this.onGround = false;

        for (int i = 0; i < VELOCITY_HISTORY_SIZE; i++) {
            velocityHistory[i] = new Vector(0, 0, 0);
        }
        this.velocityHistoryIndex = 0;
    }

    public void release() {
        if (this.isHeld && ticksSinceGrabbed > 2) {
            Vector avgVelocity = new Vector(0, 0, 0);
            int samplesUsed = Math.min(ticksSinceGrabbed, VELOCITY_HISTORY_SIZE);

            for (int i = 0; i < samplesUsed; i++) {
                avgVelocity.add(velocityHistory[i]);
            }

            avgVelocity.multiply(1.0 / samplesUsed);

            double momentumMultiplier = 1.8;
            this.velocity = avgVelocity.multiply(momentumMultiplier);

            if (this.velocity.length() > MAX_VELOCITY) {
                this.velocity.normalize().multiply(MAX_VELOCITY);
            }
        }

        this.isHeld = false;
        this.holder = null;
        this.lastHeldPosition = null;
    }

    public void throwBlock(Vector direction, double power) {
        release();
        this.velocity = direction.normalize().multiply(power);
    }

    public void remove() {
        display.remove();
    }

    public Location getLocation() {
        return display.getLocation();
    }
}