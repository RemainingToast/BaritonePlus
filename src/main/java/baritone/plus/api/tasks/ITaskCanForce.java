package baritone.plus.api.tasks;

import baritone.plus.main.BaritonePlus;

/**
 * Lets a task declare that it's parent can NOT interrupt itself, and that this task MUST keep executing.
 */
public interface ITaskCanForce {

    /**
     * @param interruptingCandidate This task will try to interrupt our current task.
     * @return Whether the task should forcefully keep going, even when the parent decides it shouldn't
     */
    boolean shouldForce(BaritonePlus mod, Task interruptingCandidate);
}
