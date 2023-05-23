package baritone.plus.main;

import baritone.plus.api.butler.WhisperChecker;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.*;
import baritone.plus.api.util.helpers.WorldHelper;
import baritone.plus.main.tasks.CraftGenericManuallyTask;
import baritone.plus.main.tasks.construction.PlaceBlockNearbyTask;
import baritone.plus.main.tasks.construction.PlaceSignTask;
import baritone.plus.main.tasks.construction.PlaceStructureBlockTask;
import baritone.plus.main.tasks.construction.compound.ConstructIronGolemTask;
import baritone.plus.main.tasks.construction.compound.ConstructNetherPortalObsidianTask;
import baritone.plus.main.tasks.container.SmeltInFurnaceTask;
import baritone.plus.main.tasks.container.StoreInAnyContainerTask;
import baritone.plus.main.tasks.entity.KillEntityTask;
import baritone.plus.main.tasks.examples.ExampleTask2;
import baritone.plus.main.tasks.misc.*;
import baritone.plus.main.tasks.movement.*;
import baritone.plus.main.tasks.resources.CollectBlazeRodsTask;
import baritone.plus.main.tasks.resources.CollectFlintTask;
import baritone.plus.main.tasks.resources.CollectFoodTask;
import baritone.plus.main.tasks.resources.TradeWithPiglinsTask;
import baritone.plus.main.tasks.speedrun.KillEnderDragonTask;
import baritone.plus.main.tasks.speedrun.KillEnderDragonWithBedsTask;
import baritone.plus.main.tasks.speedrun.WaitForDragonAndPearlTask;
import baritone.plus.main.tasks.stupid.BeeMovieTask;
import baritone.plus.main.tasks.stupid.ReplaceBlocksTask;
import baritone.plus.main.tasks.stupid.SCP173Task;
import baritone.plus.main.tasks.stupid.TerminatorTask;
import com.google.gson.Gson;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.chunk.EmptyChunk;
import org.reflections.Reflections;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * For testing.
 * <p>
 * As solonovamax suggested, this stuff should REALLY be moved to unit tests
 * https://github.com/adrisj7-AltoClef/altoclef/pull/7#discussion_r641792377
 * but getting timed tests and testing worlds set up in Minecraft might be
 * challenging, so this is the temporary resting place for garbage test code for now.
 */
@SuppressWarnings("EnhancedSwitchMigration")
public class Playground {

    public static void printAllTaskClasses() {
        Reflections reflections = new Reflections("baritone.plus.main.tasks");
        Set<Class<? extends Task>> allClasses =
                reflections.getSubTypesOf(Task.class);

        List<String> taskClasses = new ArrayList<>();
        for (Class<? extends Task> taskClass : allClasses) {
            Constructor<?>[] constructors = taskClass.getConstructors();
            List<String> constructorSignatures = new ArrayList<>();
            for (Constructor<?> constructor : constructors) {
                List<String> paramTypes = Arrays.stream(constructor.getParameterTypes())
                        .map(Class::getName)
                        .collect(Collectors.toList());
                constructorSignatures.add(String.join(",", paramTypes));
            }

            String constructorsString = String.join("|", constructorSignatures);
            taskClasses.add(taskClass.getName());
        }

        String jsonString = new Gson().toJson(taskClasses);
        System.out.println(jsonString);
    }

    public static void IDLE_TEST_INIT_FUNCTION(BaritonePlus mod) {
        printAllTaskClasses();
        // Test code here

        // Print all uncatalogued resources as well as resources that don't have a corresponding item
        /*
        Set<String> collectable = new HashSet<>(TaskCatalogue.resourceNames());
        Set<String> allItems = new HashSet<>();

        List<String> notCollected = new ArrayList<>();

        for (Identifier id : Registry.ITEM.getIds()) {
            Item item = Registry.ITEM.get(id);
            String name = ItemUtil.trimItemName(item.getTranslationKey());
            allItems.add(name);
            if (!collectable.contains(name)) {
                notCollected.add(name);
            }
        }

        List<String> notAnItem = new ArrayList<>();
        for (String cataloguedName : collectable) {
            if (!allItems.contains(cataloguedName)) {
                notAnItem.add(cataloguedName);
            }
        }

        notCollected.sort(String::compareTo);
        notAnItem.sort(String::compareTo);

        Function<List<String>, String> temp = (list) -> {
            StringBuilder result = new StringBuilder("");
            for (String name : list) {
                result.append(name).append("\n");
            }
            return result.toString();
        };

        Debug.logInternal("NOT COLLECTED YET:\n" + temp.apply(notCollected));
        Debug.logInternal("\n\n\n");
        Debug.logInternal("NOT ITEMS:\n" + temp.apply(notAnItem));
        */

        /* Print all catalogued resources

        List<String> resources = new ArrayList<>(TaskCatalogue.resourceNames());
        resources.sort(String::compareTo);
        StringBuilder result = new StringBuilder("ALL RESOURCES:\n");
        for (String name : resources) {
            result.append(name).append("\n");
        }
        Debug.logInternal("We got em:\n" + result.toString());

         */
    }

    public static void IDLE_TEST_TICK_FUNCTION(BaritonePlus mod) {
        // Test code here
    }

    public static void TEMP_TEST_FUNCTION(BaritonePlus mod, String arg) {
        //mod.runUserTask();
        Debug.logMessage("Running test...");

        switch (arg) {
            case "":
                // None specified
                Debug.logWarning("Please specify a test (ex. stacked, bed, terminate)");
                break;
            case "sign":
                mod.runUserTask(new PlaceSignTask("Hello there!"));
                break;
            case "sign2":
                mod.runUserTask(new PlaceSignTask(new BlockPos(10, 3, 10), "Hello there!"));
                break;
            case "pickup":
                mod.runUserTask(new PickupDroppedItemTask(new ItemTarget(Items.IRON_ORE, 3), true));
                break;
            case "chunk": {
                // We may have missed a chunk that's far away...
                BlockPos p = new BlockPos(100000, 3, 100000);
                Debug.logMessage("LOADED? " + (!(mod.getWorld().getChunk(p) instanceof EmptyChunk)));
                break;
            }
            case "structure":
                mod.runUserTask(new PlaceStructureBlockTask(new BlockPos(10, 6, 10)));
                break;
            case "place": {
                //BlockPos targetPos = new BlockPos(0, 6, 0);
                //mod.runUserTask(new PlaceSignTask(targetPos, "Hello"));
                //Direction direction = Direction.WEST;
                //mod.runUserTask(new InteractItemWithBlockTask(TaskCatalogue.getItemTarget("lava_bucket", 1), direction, targetPos, false));
                mod.runUserTask(new PlaceBlockNearbyTask(Blocks.CRAFTING_TABLE, Blocks.FURNACE));
                //mod.runUserTask(new PlaceStructureBlockTask(new BlockPos(472, 24, -324)));
                break;
            }
            case "deadmeme":
                File file = new File("test.txt");
                try {
                    FileReader reader = new FileReader(file);
                    mod.runUserTask(new BeeMovieTask("bruh", mod.getPlayer().getBlockPos(), reader));
                } catch (FileNotFoundException e) {
                    Debug.logMessage(e.getMessage());
                    e.printStackTrace();
                }
                break;
            case "stacked":
                // It should only need:
                // 24 (armor) + 3*3 (pick) + 2 = 35 diamonds
                // 2*3 (pick) + 1 = 7 sticks
                // 4 planks
                /*
                mod.runUserTask(TaskCatalogue.getSquashedItemTask(
                        new ItemTarget("diamond_chestplate", 1),
                        new ItemTarget("diamond_leggings", 1),
                        new ItemTarget("diamond_helmet", 1),
                        new ItemTarget("diamond_boots", 1),
                        new ItemTarget("diamond_pickaxe", 3),
                        new ItemTarget("diamond_sword", 1),
                        new ItemTarget("crafting_table", 1)
                ));
                 */
                mod.runUserTask(new EquipArmorTask(Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_HELMET, Items.DIAMOND_BOOTS));
                break;
            case "stacked2":
                mod.runUserTask(new EquipArmorTask(Items.DIAMOND_CHESTPLATE));
                break;
            case "ravage":
                mod.runUserTask(new RavageRuinedPortalsTask());
                break;
            case "temples":
                mod.runUserTask(new RavageDesertTemplesTask());
                break;
            case "outer":
                mod.runUserTask(new GetToOuterEndIslandsTask());
                break;
            case "smelt":
                ItemTarget target = new ItemTarget("iron_ingot", 4);
                ItemTarget material = new ItemTarget("iron_ore", 4);
                mod.runUserTask(new SmeltInFurnaceTask(new SmeltTarget(target, material)));
                break;
            case "repair":
//                mod.runUserTask(new RepairToolTask(new ItemTarget("leather_chestplate", 1), new ItemTarget("golden_boots", 1) , new ItemTarget("iron_sword", 1)));
                mod.runUserTask(new RepairToolTask());
            break;
            case "iron":
                mod.runUserTask(new ConstructIronGolemTask());
                break;
            case "avoid":
                // Test block break predicate
                mod.getBehaviour().avoidBlockBreaking((BlockPos b) -> (-1000 < b.getX() && b.getX() < 1000)
                        && (-1000 < b.getY() && b.getY() < 1000)
                        && (-1000 < b.getZ() && b.getZ() < 1000));
                Debug.logMessage("Testing avoid from -1000, -1000, -1000 to 1000, 1000, 1000");
                break;
            case "portal":
                //mod.runUserTask(new EnterNetherPortalTask(new ConstructNetherPortalBucketTask(), Dimension.NETHER));
                mod.runUserTask(new EnterNetherPortalTask(new ConstructNetherPortalObsidianTask(), WorldHelper.getCurrentDimension() == Dimension.OVERWORLD ? Dimension.NETHER : Dimension.OVERWORLD));
                break;
            case "kill":
                List<ZombieEntity> zombs = mod.getEntityTracker().getTrackedEntities(ZombieEntity.class);
                if (zombs.size() == 0) {
                    Debug.logWarning("No zombs found.");
                } else {
                    LivingEntity entity = zombs.get(0);
                    mod.runUserTask(new KillEntityTask(entity));
                }
                break;
            case "craft":
                // Test de-equip
                new Thread(() -> {
                    for (int i = 3; i > 0; --i) {
                        Debug.logMessage(i + "...");
                        sleepSec(1);
                    }

                    Item[] c = new Item[]{Items.COBBLESTONE};
                    Item[] s = new Item[]{Items.STICK};
                    CraftingRecipe recipe = CraftingRecipe.newShapedRecipe("test pickaxe", new Item[][]{c, c, c, null, s, null, null, s, null}, 1);

                    mod.runUserTask(new CraftGenericManuallyTask(new RecipeTarget(Items.STONE_PICKAXE, 1, recipe)));
                    /*
                    Item toEquip = Items.BUCKET;//Items.AIR;
                    Slot target = PlayerInventorySlot.getEquipSlot(EquipmentSlot.MAINHAND);

                    InventoryTracker t = mod.getItemStorage();

                    // Already equipped
                    if (t.getItemStackInSlot(target).getItem() == toEquip) {
                        Debug.logMessage("Already equipped.");
                    } else {
                        List<Integer> itemSlots = t.getInventorySlotsWithItem(toEquip);
                        if (itemSlots.size() != 0) {
                            int slot = itemSlots.get(0);
                            t.swapItems(Slot.getFromInventory(slot), target);
                            Debug.logMessage("Equipped via swap");
                        } else {
                            Debug.logWarning("Failed to equip item " + toEquip.getTranslationKey());
                        }
                    }
                     */
                }).start();
                //mod.getItemStorage().equipItem(Items.AIR);
                break;
            case "food":
                mod.runUserTask(new CollectFoodTask(20));
                break;
            case "temple":
                mod.runUserTask(new LocateDesertTempleTask());
                break;
            case "blaze":
                mod.runUserTask(new CollectBlazeRodsTask(7));
                break;
            case "flint":
                mod.runUserTask(new CollectFlintTask(5));
                break;
            case "sleep":
                mod.runUserTask(new SleepThroughNightTask());
                break;
            case "unobtainable":
                String fname = "unobtainables.txt";
                try {
                    int unobtainable = 0;
                    int total = 0;
                    File f = new File(fname);
                    FileWriter fw = new FileWriter(f);
                    for (Identifier id : Registries.ITEM.getIds()) {
                        Item item = Registries.ITEM.get(id);
                        if (!TaskCatalogue.isObtainable(item)) {
                            ++unobtainable;
                            fw.write(item.getTranslationKey() + "\n");
                        }
                        total++;
                    }
                    fw.flush();
                    fw.close();
                    Debug.logMessage(unobtainable + " / " + total + " unobtainable items. Wrote a list of items to \"" + f.getAbsolutePath() + "\".");
                } catch (IOException e) {
                    Debug.logWarning(e.toString());
                }
                break;
            case "piglin":
                mod.runUserTask(new TradeWithPiglinsTask(32, new ItemTarget(Items.ENDER_PEARL, 12)));
                break;
            case "stronghold":
                mod.runUserTask(new GoToStrongholdPortalTask(12));
                break;
            case "terminate":
                mod.runUserTask(new TerminatorTask(mod.getPlayer().getBlockPos(), 900));
                break;
            case "replace":
                // Creates a mini valley of crafting tables.
                BlockPos from = mod.getPlayer().getBlockPos().add(new Vec3i(-100, -20, -100));
                BlockPos to = mod.getPlayer().getBlockPos().add(new Vec3i(100, 255, 100));
                Block[] toFind = new Block[]{Blocks.GRASS_BLOCK};// Blocks.COBBLESTONE};
                ItemTarget toReplace = new ItemTarget("crafting_table");//"stone");
                mod.runUserTask(new ReplaceBlocksTask(toReplace, from, to, toFind));
                break;
            case "bed":
                mod.runUserTask(new PlaceBedAndSetSpawnTask());
                break;
            case "dragon":
                mod.runUserTask(new KillEnderDragonWithBedsTask(new WaitForDragonAndPearlTask()));
                break;
            case "dragon-pearl":
                mod.runUserTask(new ThrowEnderPearlSimpleProjectileTask(new BlockPos(0, 60, 0)));
                break;
            case "dragon-old":
                mod.runUserTask(new KillEnderDragonTask());
                break;
            case "chest":
                mod.runUserTask(new StoreInAnyContainerTask(true, new ItemTarget(Items.DIAMOND, 3)));
                break;
            case "173":
                mod.runUserTask(new SCP173Task());
                break;
            case "example":
                mod.runUserTask(new ExampleTask2());
                break;
            case "netherite":
                mod.runUserTask(TaskCatalogue.getSquashedItemTask(
                        new ItemTarget("netherite_pickaxe", 1),
                        new ItemTarget("netherite_sword", 1),
                        new ItemTarget("netherite_helmet", 1),
                        new ItemTarget("netherite_chestplate", 1),
                        new ItemTarget("netherite_leggings", 1),
                        new ItemTarget("netherite_boots", 1)));
                break;
            case "whisper": {
                File check = new File("whisper.txt");
                try {
                    FileInputStream fis = new FileInputStream(check);
                    Scanner sc = new Scanner(fis);
                    String me = sc.nextLine(),
                            template = sc.nextLine(),
                            message = sc.nextLine();
                    WhisperChecker.MessageResult result = WhisperChecker.tryParse(me, template, message);
                    Debug.logMessage("Got message: " + result);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            }
            default:
                mod.logWarning("Test not found: \"" + arg + "\".");
                break;
        }
    }

    private static void sleepSec(double seconds) {
        try {
            Thread.sleep((int) (1000 * seconds));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
