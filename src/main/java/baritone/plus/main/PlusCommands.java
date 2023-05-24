package baritone.plus.main;

import baritone.plus.api.command.PlusCommand;
import baritone.plus.main.commands.*;

import java.util.HashMap;

public class PlusCommands {
    private final HashMap<String, PlusCommand> _commandSheet = new HashMap<>();

    // TODO - Reflection.
    public PlusCommands() {
        // List commands here
        registerNewCommand(
                new AnarchyCommand(),
                new BuildPlusCommand(),
//                new BrainPlusCommand(),
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

            BaritonePlus.INSTANCE.getClientBaritone().getCommandManager().getRegistry().register(plusCommand);
            _commandSheet.put(plusCommand.getName(), plusCommand);
        }
    }
}
