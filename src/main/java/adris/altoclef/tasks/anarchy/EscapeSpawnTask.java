package adris.altoclef.tasks.anarchy;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.anarchy.duping.TameDonkeyTask;
import adris.altoclef.tasks.container.StoreInContainerTask;
import adris.altoclef.tasks.movement.FastTravelTask;
import adris.altoclef.tasks.movement.GetToXZTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.StorageHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.init.Items;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.Random;

// Escape Spawn on Anarchy Servers
// - Move to 1k+
// - Gather Starter Gear (weapon, shield, food)
// - Fast Travel via Highways to 10k
// - Set Respawn Point in Overworld off Axis
// - Gather Diamond Gear

public class EscapeSpawnTask extends Task {

    private static final Random random = new Random();

    private static final Item[] ANARCHY_ITEMS = new Item[]{
            Items.TOTEM_OF_UNDYING,
            Items.ENCHANTED_GOLDEN_APPLE,
            Items.END_CRYSTAL,
            Items.DIAMOND_SWORD
    };

    private BlockPos _outsideSpawnGoal;
    private BlockPos _fastTravelGoal;

    @Override
    protected void onStart(AltoClef mod) {
        mod.getBehaviour().push();

        mod.getBehaviour().setAllowDiagonalAscend(true);
        mod.getBehaviour().addProtectedItems(ANARCHY_ITEMS);

        mod.getClientBaritoneSettings().allowParkour.value = true;
        mod.getClientBaritoneSettings().allowParkourAscend.value = true;
        mod.getClientBaritoneSettings().allowParkourPlace.value = true;

        mod.getClientBaritoneSettings().allowSprint.value = mod.getFoodChain().hasFood()
                && StorageHelper.calculateInventoryFoodScore(mod) > 30;

        _outsideSpawnGoal = generatePosition(mod,
                1000,
                500
        );

        _fastTravelGoal = generatePosition(mod,
                10000,
                90000
        );
    }

    @Override
    protected Task onTick(AltoClef mod) {
        // Escape 0,0
        if (isWithinRange(mod, 1000)) {
            // Store Important Items - If we happened to have any
            if (mod.getItemStorage().hasItem(ANARCHY_ITEMS)) {
                var enderChest = mod.getBlockTracker().getNearestTracking(Blocks.ENDER_CHEST);

                if (enderChest.isPresent()) {
                    setDebugState("Saving Important Items..");
                    return new StoreInContainerTask(
                            enderChest.get(),
                            false,
                            Arrays.stream(ANARCHY_ITEMS).map(ItemTarget::new).toArray(ItemTarget[]::new)
                    );
                }
            }

            setDebugState("Moving out of 1k+ range.");
            return new GetToXZSuicideTask(_outsideSpawnGoal, player -> {
                // TODO Suicide Condition
                var food = StorageHelper.calculateInventoryFoodScore(mod) < 10;
                var items = mod.getItemStorage().hasItem(ANARCHY_ITEMS);
                return !food && !items && !player.isCreative() && !player.isSpectator();
            });
        }

        // Then get food
        if (StorageHelper.calculateInventoryFoodScore(mod) < 30) {
            return new CollectFoodTask(30);
        }

        if (isOutsideRange(mod, 10000)) {


            setDebugState("Find a Donkey; Why not?");
            return new TameDonkeyTask();


        } else {
            setDebugState("Travelling Further...");
            return new FastTravelTask(_fastTravelGoal, false);
        }
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof EscapeSpawnTask;
    }

    @Override
    protected String toDebugString() {
        return "Escaping Spawn";
    }

    private static BlockPos generatePosition(AltoClef mod, int originalRange, int newPositionRange) {
        var player = mod.getPlayer();
        int playerX = player.getBlockX();
        int playerZ = player.getBlockZ();

        // Add or subtract newPositionRange based on the sign of playerX and playerZ
        int newX = playerX < 0 ? playerX - newPositionRange : playerX + newPositionRange;
        int newZ = playerZ < 0 ? playerZ - newPositionRange : playerZ + newPositionRange;

        // Ensure the new position is at least originalRange away from the player
        while (Math.abs(newX - playerX) < originalRange || Math.abs(newZ - playerZ) < originalRange) {
            newX = playerX < 0 ? newX - 1 : newX + 1;
            newZ = playerZ < 0 ? newZ - 1 : newZ + 1;
        }

        return new BlockPos(newX, player.getBlockY(), newZ);
    }

    private boolean isWithinRange(AltoClef mod, int range) {
        var player = mod.getPlayer();
        // Check if the absolute value of player's block positions are within the range
        return Math.abs(player.getBlockX()) <= range &&
                Math.abs(player.getBlockY()) <= range &&
                Math.abs(player.getBlockZ()) <= range;
    }

    private boolean isOutsideRange(AltoClef mod, int range) {
        var player = mod.getPlayer();
        // Check if the absolute value of player's block positions are within the range
        return Math.abs(player.getBlockX()) >= range &&
                Math.abs(player.getBlockY()) >= range &&
                Math.abs(player.getBlockZ()) >= range;
    }


    public static class Axis {
        private final int multiplierX;
        private final int multiplierZ;

        public Axis(int multiplierX, int multiplierZ) {
            this.multiplierX = multiplierX;
            this.multiplierZ = multiplierZ;
        }

        public BlockPos calculateHighwayPosition(int x, int z) {
            int axisX = multiplierX * Math.abs(x);
            int axisZ = multiplierZ * Math.abs(z);
            return new BlockPos(axisX, 120, axisZ);
        }

        public int calculateDistance(int x, int z) {
            return Math.abs(multiplierX * x - multiplierZ * z);
        }
    }

    public BlockPos findClosestNetherHighway(AltoClef mod) {
        var player = mod.getPlayer();
        int x = player.getBlockX();
        int z = player.getBlockZ();

        Axis[] axes = {
                new Axis(1, 0),   // +X
                new Axis(-1, 0),  // -X
                new Axis(0, 1),   // +Z
                new Axis(0, -1),  // -Z
                new Axis(1, -1),  // +X -Z
                new Axis(-1, 1),  // -X +Z
                new Axis(1, 1),   // +Z -X
                new Axis(-1, -1)  // -Z +X
        };

        var closestAxis = axes[0];
        int minDistance = Integer.MAX_VALUE;

        for (Axis axis : axes) {
            int distance = axis.calculateDistance(x, z);
            if (distance < minDistance) {
                minDistance = distance;
                closestAxis = axis;
            }
        }

        return closestAxis.calculateHighwayPosition(x, z);
    }

}
