package me.roundaround.itemsigns.gametest;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.itemsigns.interfaces.SignBlockEntityExtensions;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import me.roundaround.trove.gametest.GameTestAssertionException;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.SignBlockEntity;

/**
 * Sneak + right-click skips the block's own use handler, so the mount/unmount
 * goes through {@code ItemStackMixin} instead of {@code SignBlockMixin}.
 */
@ClientGameTest
public class ItemSignsSneakUsageTest implements ClientTest {
  private static final BlockPos SIGN = new BlockPos(0, 65, 2);

  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      world.fill(new BlockPos(-1, 64, -1), new BlockPos(1, 64, 3), "minecraft:smooth_stone");
      world.setBlock(SIGN, "minecraft:oak_sign");
      world.teleport(0.5, 65.0, 0.5);
      world.setMainHandItem("minecraft:diamond");
      context.waitTicks(2);

      context.runOnClient((mc) -> mc.options.keyShift.setDown(true));
      try {
        context.waitTicks(5);
        if (!world.computeOnServerPlayer((player) -> player.isSecondaryUseActive())) {
          throw new GameTestAssertionException("player should be sneaking on the server");
        }

        world.useItemOn(SIGN);
        world.settle();
        assertMounted(world, true, "sneak + right-clicking an empty sign with an item should have mounted it");

        world.useItemOn(SIGN);
        world.settle();
        assertMounted(world, false, "sneak + right-clicking a sign holding an item should have removed it");
      } finally {
        context.runOnClient((mc) -> mc.options.keyShift.setDown(false));
      }
    }
  }

  private static void assertMounted(ClientWorld world, boolean expected, String message) {
    boolean mounted = world.computeOnServerPlayer((player) -> {
      if (!(player.level().getBlockEntity(SIGN) instanceof SignBlockEntity sign)) {
        return false;
      }
      var items = ((SignBlockEntityExtensions) sign).itemsigns$getItems();
      if (items == null) {
        return false;
      }
      for (ItemStack stack : items) {
        if (!stack.isEmpty()) {
          return true;
        }
      }
      return false;
    });
    if (mounted != expected) {
      throw new GameTestAssertionException(message);
    }
  }
}
