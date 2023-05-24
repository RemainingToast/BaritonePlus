package baritone.plus.main.tasks.squashed;


import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.ResourceTask;
import baritone.plus.main.tasks.container.UpgradeInSmithingTableTask;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.helpers.StorageHelper;
import baritone.plus.api.util.slots.Slot;
import baritone.plus.api.util.slots.SmithingTableSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SmithingScreenHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SmithingSquasher extends TypeSquasher<UpgradeInSmithingTableTask> {

    @Override
    protected List<ResourceTask> getSquashed(List<UpgradeInSmithingTableTask> tasks) {
        // Group materials + tools together, then return a list of the same UpgradeInSmithing tasks
        List<ResourceTask> result = new ArrayList<>();
        List<ItemTarget> units = new ArrayList<>();
        for (UpgradeInSmithingTableTask task : tasks) {
            units.add(task.getMaterials());
            units.add(task.getTools());
        }
        result.add(new GetMaterialsTask(units.toArray(ItemTarget[]::new)));
        // Afterwards, perform the smithing.
        result.addAll(tasks);
        return result;
    }

    private static class GetMaterialsTask extends ResourceTask {

        public GetMaterialsTask(ItemTarget[] targets) {
            super(targets);
        }

        @Override
        protected boolean shouldAvoidPickingUp(BaritonePlus mod) {
            return false;
        }

        @Override
        protected void onResourceStart(BaritonePlus mod) {

        }

        private int getItemsInSlot(BaritonePlus mod, Slot slot, ItemTarget match) {
            ItemStack stack = StorageHelper.getItemStackInSlot(slot);
            if (!stack.isEmpty() && match.matches(stack.getItem())) {
                return stack.getCount();
            }
            return 0;
        }

        @Override
        protected Task onResourceTick(BaritonePlus mod) {
            List<ItemTarget> resultingTargets = Arrays.asList(_itemTargets);

            // Subtract required counts if we're in a smithing table, so putting items in the table doesn't remove them.
            boolean inSmithingTable = (mod.getPlayer().currentScreenHandler instanceof SmithingScreenHandler);
            if (inSmithingTable) {
                for (int i = 0; i < resultingTargets.size(); ++i) {
                    ItemTarget target = resultingTargets.get(i);
                    int smithingTableCount = getItemsInSlot(mod, SmithingTableSlot.INPUT_SLOT_MATERIALS, target)
                            + getItemsInSlot(mod, SmithingTableSlot.INPUT_SLOT_TOOL, target)
                            + getItemsInSlot(mod, SmithingTableSlot.OUTPUT_SLOT, target);
                    resultingTargets.set(i, new ItemTarget(target, target.getTargetCount() - smithingTableCount));
                }
            }
            return new CataloguedResourceTask(resultingTargets.toArray(ItemTarget[]::new));
        }

        @Override
        protected void onResourceStop(BaritonePlus mod, Task interruptTask) {

        }

        @Override
        protected boolean isEqualResource(ResourceTask other) {
            return true; // item targets are the only difference
        }

        @Override
        protected String toDebugStringName() {
            return "Collecting Smithing Materials";
        }
    }
}
