package baritone.plus.main.tasks.resources;

import baritone.plus.api.util.CraftingRecipe;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.helpers.ItemHelper;
import net.minecraft.item.Item;

import java.util.function.Function;

// This is literally identical to its parent, but we give it a name for psychological reasons
public abstract class CraftWithMatchingWoolTask extends CraftWithMatchingMaterialsTask {

    private final Function<ItemHelper.ColorfulItems, Item> _getMajorityMaterial;
    private final Function<ItemHelper.ColorfulItems, Item> _getTargetItem;

    public CraftWithMatchingWoolTask(ItemTarget target, Function<ItemHelper.ColorfulItems, Item> getMajorityMaterial, Function<ItemHelper.ColorfulItems, Item> getTargetItem, CraftingRecipe recipe, boolean[] sameMask) {
        super(target, recipe, sameMask);
        _getMajorityMaterial = getMajorityMaterial;
        _getTargetItem = getTargetItem;
    }


    @Override
    protected Item getSpecificItemCorrespondingToMajorityResource(Item majority) {
        for (ItemHelper.ColorfulItems colorfulItem : ItemHelper.getColorfulItems()) {
            if (_getMajorityMaterial.apply(colorfulItem) == majority) {
                return _getTargetItem.apply(colorfulItem);
            }
        }
        return null;
    }
}
