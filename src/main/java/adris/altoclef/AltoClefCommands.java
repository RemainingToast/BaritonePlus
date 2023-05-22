package adris.altoclef;

import adris.altoclef.brainWIP.commands.BrainPlusCommand;
import adris.altoclef.commands.*;
import adris.altoclef.commands.PlusCommand;

import java.util.HashMap;

/**
 * Initializes altoclef's built in commands.
 */
public class AltoClefCommands {
    private final HashMap<String, PlusCommand> _commandSheet = new HashMap<>();
    public AltoClefCommands() {
        // List commands here
        registerNewCommand(
                new AnarchyCommand(),
                new BrainPlusCommand(),
//                new HelpPlusCommand(),
                new GetCommand(),
                new FollowCommand(),
                new GiveCommand(),
                new EquipCommand(),
                new DepositCommand(),
                new StashCommand(),
                new GotoPlusCommand(),
                new GotoWithElytraCommand(),
                new IdleCommand(),
                new CoordsCommand(),
                new StatusCommand(),
                new InventoryCommand(),
                new LocateStructureCommand(),
                new StopPlusCommand(),
                new TestCommand(),
                new FoodCommand(),
                new MeatCommand(),
                new PausePlusCommand(),
                new ReloadSettingsCommand(),
                new GamerCommand(),
                new SpeedrunCommand(),
                new PunkCommand(),
                new HeroCommand(),
                new ListCommand(),
                new CoverWithSandCommand(),
                new CoverWithBlocksCommand()
                //new TestMoveInventoryCommand(),
                //    new TestSwapInventoryCommand()
        );
    }

    public void registerNewCommand(PlusCommand... plusCommands) {
        for (PlusCommand plusCommand : plusCommands) {
            if (_commandSheet.containsKey(plusCommand.getName())) {
                Debug.logInternal("Command with name " + plusCommand.getName() + " already exists! Can't register that name twice.");
                continue;
            }

            AltoClef.INSTANCE.getClientBaritone().getCommandManager().getRegistry().register(plusCommand);
            _commandSheet.put(plusCommand.getName(), plusCommand);
        }
    }
}
