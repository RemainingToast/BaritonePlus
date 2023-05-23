package baritone.plus.launch.mixins;

/*@Mixin(ScreenHandler.class)
public class SlotClickMixin {

    @Redirect(
            method = "internalOnSlotClick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;internalOnSlotClick(IILnet/minecraft/screen/slot/ClickType;Lnet/minecraft/entity/player/PlayerEntity;)V")
    )
    private void slotClick(ScreenHandler self, int slotIndex, int button, ClickType actionType, PlayerEntity player) {
        // TODO: "self" is misleading, reread Mixin docs to understand the implications here.

        // This calculation is already done, BUT we also want a "before&after" type beat.

        DefaultedList<Slot> afterSlots = self.slots;
        List<ItemStack> beforeStacks = new ArrayList<>(afterSlots.size());
        for (Slot slot : afterSlots) {
            beforeStacks.add(slot.getStack().copy());
        }
        // Perform slot changes potentially
        self.onSlotClick(slotIndex, button, actionType, player);
        // Check for changes and alert
        for (int i = 0; i < beforeStacks.size(); ++i) {
            ItemStack before = beforeStacks.get(i);
            ItemStack after = afterSlots.get(i).getStack();
            if (!ItemStack.areEqual(before, after)) {
                baritone.plus.api.util.slots.Slot slot = baritone.plus.api.util.slots.Slot.getFromCurrentScreen(i);
                EventBus.publish(new SlotClickChangedEvent(slot, before, after));
            }
        }
    }
}*/
