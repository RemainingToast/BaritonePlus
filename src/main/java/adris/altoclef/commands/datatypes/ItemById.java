package adris.altoclef.commands.datatypes;

import baritone.api.command.datatypes.IDatatypeContext;
import baritone.api.command.datatypes.IDatatypeFor;
import baritone.api.command.exception.CommandException;
import baritone.api.command.helpers.TabCompleteHelper;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.stream.Stream;

public enum ItemById implements IDatatypeFor<Item> {
    INSTANCE;

    private ItemById() {
    }

    public Item get(IDatatypeContext ctx) throws CommandException {
        Identifier id = new Identifier(ctx.getConsumer().getString());
        Item item;
        if ((item = (Item) Registries.ITEM.getOrEmpty(id).orElse((Item) null)) == null) {
            throw new IllegalArgumentException("no block found by that id");
        } else {
            return item;
        }
    }

    public Stream<String> tabComplete(IDatatypeContext ctx) throws CommandException {
        return (new TabCompleteHelper()).append(Registries.ITEM.getIds().stream().map(Object::toString)).filterPrefixNamespaced(ctx.getConsumer().getString()).sortAlphabetically().stream();
    }
}