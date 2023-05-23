package baritone.plus.launch.mixins;

// ActionResult ClientPlayerInteractionManager.interactBlock(ClientPlayerEntity player, WorldClient world, Hand hand, BlockHitResult hitResult);

/*@Mixin(ClientPlayerInteractionManager.class)
public final class ClientInteractWithBlockMixin {
    @Inject(
            method = "interactBlock",
            at = @At("HEAD")
    )
    private void onClientBlockInteract(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> ci) {
        //Debug.logMessage("(client) INTERACTED WITH: " + (hitResult != null? hitResult.getPosition() : "(nothing)"));
        if (hitResult != null) {
            EventBus.publish(new BlockInteractEvent(hitResult));
        }

    }
}*/
