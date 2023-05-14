package adris.altoclef.tasks.anarchy;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.movement.GetToXZTask;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

// Essentially GetToXZTask but Suicides when no progress can be made
public class GetToXZSuicideTask extends GetToXZTask {

    private final Predicate<ClientPlayerEntity> _condition;
    private final int _maxAttempts;
    private int _currentAttempts;

    public GetToXZSuicideTask(BlockPos pos) {
        super(pos);
        this._maxAttempts = 0;
        this._condition = suicide -> true;
    }

    public GetToXZSuicideTask(BlockPos pos, Predicate<ClientPlayerEntity> condition) {
        super(pos);
        this._maxAttempts = 0;
        this._condition = condition;
    }

    public GetToXZSuicideTask(BlockPos pos, int maxAttempts) {
        super(pos);
        this._maxAttempts = maxAttempts;
        this._condition = suicide -> true;
    }

    public GetToXZSuicideTask(BlockPos pos, int maxAttempts, Predicate<ClientPlayerEntity> condition) {
        super(pos);
        this._maxAttempts = maxAttempts;
        this._condition = condition;
    }

    @Override
    protected void onWander(AltoClef mod) {
        _currentAttempts++;

//        if (_currentAttempts <= _maxAttempts) {
//            return;
//        }

        if (_condition.test(mod.getPlayer())) {
            var _networkHandler = mod.mc().getNetworkHandler();
            if (_networkHandler != null) _networkHandler.sendChatCommand("kill");
        }
    }
}
