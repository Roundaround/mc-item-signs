package me.roundaround.itemsigns.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
  // wrap the whole method, not the inner Item.useOn call — NeoForge's server path never makes that call from here
  @WrapMethod(method = "useOn")
  private InteractionResult wrapUseOn(UseOnContext context, Operation<InteractionResult> original) {
    InteractionResult vanillaResult = original.call(context);

    Level world = context.getLevel();
    BlockEntity blockEntity = world.getBlockEntity(context.getClickedPos());
    Player player = context.getPlayer();

    if (!(blockEntity instanceof SignBlockEntity signBlockEntity) || player == null) {
      return vanillaResult;
    }

    // If we ever get here, it means the player is holding an item and sneaking.

    // Vanilla gets to handle the item use first. Only if it returns back PASS (as in it did nothing) can we try to
    // place/remove the sign's item.
    if (!(vanillaResult instanceof InteractionResult.Pass)) {
      return vanillaResult;
    }

    if (signBlockEntity.getText(signBlockEntity.getSlotPlayerIsFacing(player)).hasMessage(player.isTextFilteringEnabled())) {
      // If the sign currently has text, keep the vanilla behavior.
      return vanillaResult;
    }

    boolean canModify = player.mayBuild();
    boolean waxed = signBlockEntity.isWaxed();

    if (world.isClientSide()) {
      return canModify || waxed ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    if (!canModify || waxed) {
      return vanillaResult;
    }

    if (signBlockEntity.itemsigns$hasItemFacingPlayer(player)) {
      signBlockEntity.itemsigns$dropItemFacingPlayer(world, player);
      return InteractionResult.SUCCESS;
    }

    if (signBlockEntity.itemsigns$placeItemFacingPlayer(world, player, context.getItemInHand())) {
      return InteractionResult.SUCCESS;
    }

    return vanillaResult;
  }
}
