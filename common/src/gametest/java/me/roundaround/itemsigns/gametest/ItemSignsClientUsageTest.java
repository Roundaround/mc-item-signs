package me.roundaround.itemsigns.gametest;

import me.roundaround.allay.api.gametest.ClientGameTest;
import me.roundaround.itemsigns.interfaces.SignBlockEntityExtensions;
import me.roundaround.trove.gametest.ClientTest;
import me.roundaround.trove.gametest.ClientTestContext;
import me.roundaround.trove.gametest.ClientWorld;
import me.roundaround.trove.gametest.GameTestAssertionException;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;

/**
 * Drives the mod's feature through the real interaction path: place an empty
 * standing sign, stand in front of it, and right-click it with an item — the
 * {@code SignBlockMixin} mounts the held stack onto the sign. We then wait a few
 * ticks so the {@code AbstractSignRendererMixin} actually renders the mounted
 * item (a render-thread crash there would kill the client), and assert the stack
 * mounted on the server-authoritative block entity. Single-player, creative.
 */
@ClientGameTest
public class ItemSignsClientUsageTest implements ClientTest {
  private static final BlockPos SIGN = new BlockPos(0, 65, 2);

  @Override
  public void runTest(ClientTestContext context) {
    try (ClientWorld world = context.worldBuilder().creative().stopTime(true).create()) {
      world.fill(new BlockPos(-1, 64, -1), new BlockPos(1, 64, 3), "minecraft:smooth_stone");
      world.setBlock(SIGN, "minecraft:oak_sign");
      world.teleport(0.5, 65.0, 0.5);
      context.waitTicks(2);

      // Right-click the empty sign with an item: the mod mounts it on the side
      // the player faces. An empty hand here would just edit the sign, so hold one.
      world.setMainHandItem("minecraft:diamond");
      world.useItemOn(SIGN);
      world.settle();

      assertItemMounted(world);

      // Let the sign-render mixin run with the item present; a crash there fails us.
      context.waitTicks(10);
      assertItemMounted(world);
    }
  }

  private static void assertItemMounted(ClientWorld world) {
    boolean mounted = world.computeOnServerPlayer((player) -> {
      BlockEntity blockEntity = player.level().getBlockEntity(SIGN);
      if (!(blockEntity instanceof SignBlockEntity sign)) {
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
    if (!mounted) {
      throw new GameTestAssertionException("right-clicking the sign with an item should have mounted it");
    }
  }
}
