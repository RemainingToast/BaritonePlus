package baritone.plus.main.tasks.squashed;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.TaskCatalogue;
import baritone.plus.main.tasks.ResourceTask;
import baritone.plus.main.tasks.container.CraftInTableTask;
import baritone.plus.main.tasks.container.UpgradeInSmithingTableTask;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.helpers.StorageHelper;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

public class CataloguedResourceTask extends ResourceTask {


    private final TaskSquasher _squasher;
    private final ItemTarget[] _targets;
    private final List<ResourceTask> _tasksToComplete;

    public CataloguedResourceTask(boolean squash, ItemTarget... targets) {
        super(targets);
        _squasher = new TaskSquasher();
        _targets = targets;
        _tasksToComplete = new ArrayList<>(targets.length);

        for (ItemTarget target : targets) {
            if (target != null) {
                _tasksToComplete.add(TaskCatalogue.getItemTask(target));
            }
        }

        if (squash) {
            squashTasks(_tasksToComplete);
        }
    }

    public CataloguedResourceTask(ItemTarget... targets) {
        this(true, targets);
    }

    @Override
    protected void onResourceStart(BaritonePlus mod) {

    }

    @Override
    protected Task onResourceTick(BaritonePlus mod) {
        for (ResourceTask task : _tasksToComplete) {
            for (ItemTarget target : task.getItemTargets()) {
                // If we failed to meet this task's targets, do the task.
                if (!StorageHelper.itemTargetsMetInventory(mod, target)) return task;
            }
        }
        return null;
    }

    @Override
    public boolean isFinished(BaritonePlus mod) {
        for (ResourceTask task : _tasksToComplete) {
            for (ItemTarget target : task.getItemTargets()) {
                if (!StorageHelper.itemTargetsMetInventory(mod, target)) return false;
            }
        }
        // All targets are met.
        return true;
    }

    @Override
    protected boolean shouldAvoidPickingUp(BaritonePlus mod) {
        // Useless
        return false;
    }

    @Override
    protected void onResourceStop(BaritonePlus mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof CataloguedResourceTask task) {
            return Arrays.equals(task._targets, _targets);
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return "Collecting Items: " + ArrayUtils.toString(_targets);
    }

    private void squashTasks(List<ResourceTask> tasks) {
        _squasher.addTasks(tasks);
        tasks.clear();
        tasks.addAll(_squasher.getSquashed());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static class TaskSquasher {

        private final Map<Class, baritone.plus.main.tasks.squashed.TypeSquasher> _squashMap = new HashMap<>();

        private final List<ResourceTask> _unSquashableTasks = new ArrayList<>();

        public TaskSquasher() {
            _squashMap.put(CraftInTableTask.class, new CraftSquasher());
            _squashMap.put(UpgradeInSmithingTableTask.class, new SmithingSquasher());
            //_squashMap.put(MineAndCollectTask.class)
        }

        public void addTask(ResourceTask t) {
            Class type = t.getClass();
            if (_squashMap.containsKey(type)) {
                _squashMap.get(type).add(t);
            } else {
                //Debug.logMessage("Unsquashable: " + type + ": " + t);
                _unSquashableTasks.add(t);
            }
        }

        public void addTasks(List<ResourceTask> tasks) {
            for (ResourceTask task : tasks) {
                addTask(task);
            }
        }

        public List<ResourceTask> getSquashed() {
            List<ResourceTask> result = new ArrayList<>();

            for (Class type : _squashMap.keySet()) {
                result.addAll(_squashMap.get(type).getSquashed());
            }
            result.addAll(_unSquashableTasks);

            return result;
        }
    }


}
