package baritone.plus.main.tasks.squashed;

import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.MiningRequirement;
import baritone.plus.main.tasks.ResourceTask;
import baritone.plus.main.tasks.resources.MineAndCollectTask;

import java.util.*;

public class MineCollectSquasher extends TypeSquasher<MineAndCollectTask> {
    @Override
    protected List<ResourceTask> getSquashed(List<MineAndCollectTask> tasks) {
        // A list to store item targets from all tasks
        List<ItemTarget> targetItems = new ArrayList<>();

        // Collect all mining requirements from tasks into a list
        List<MiningRequirement> requirements = new ArrayList<>();

        // Loop through all tasks and collect their item targets
        for (MineAndCollectTask task : tasks) {
            targetItems.addAll(Arrays.asList(task.getItemTargets()));
            requirements.add(task.getRequirement());
        }

        // Get the highest mining requirement
        MiningRequirement maxRequirement = Collections.max(requirements, Comparator.comparing(MiningRequirement::ordinal));

        // Create a new task with all the item targets combined
        MineAndCollectTask combinedTask = new MineAndCollectTask(targetItems.toArray(ItemTarget[]::new), maxRequirement);

        // Return the combined task wrapped in a list
        return Collections.singletonList(combinedTask);
    }
}
