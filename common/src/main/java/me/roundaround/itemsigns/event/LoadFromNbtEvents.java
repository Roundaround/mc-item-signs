package me.roundaround.itemsigns.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Loader-agnostic re-implementation of the previously Fabric-backed
 * {@code Event<BlockEntityLoad>}. The mixins that splice item NBT into freshly
 * loaded block entities ({@code SerializableChunkDataMixin},
 * {@code WorldGenRegionMixin}, {@code LevelChunkMixin}) call
 * {@code LoadFromNbtEvents.BLOCK_ENTITY.invoker().beforeBlockEntityLoaded(...)}
 * and the mod registers a callback via
 * {@code LoadFromNbtEvents.BLOCK_ENTITY.register(...)} — both call shapes are
 * preserved byte-for-byte by exposing a small array-backed dispatcher object in
 * place of Fabric's {@code Event}. The fold semantics match the old
 * {@code EventFactory.createArrayBacked} backing: each callback receives (and
 * may rewrite) the {@code BlockState} produced by the previous callback.
 */
public final class LoadFromNbtEvents {
  public static final Dispatcher BLOCK_ENTITY = new Dispatcher();

  private LoadFromNbtEvents() {
  }

  public static final class Dispatcher {
    private final List<BlockEntityLoad> callbacks = new CopyOnWriteArrayList<>();
    private final BlockEntityLoad invoker = (nbt, level, pos, state, registries) -> {
      BlockState workingState = state;
      for (BlockEntityLoad callback : this.callbacks) {
        workingState = callback.beforeBlockEntityLoaded(nbt, level, pos, workingState, registries);
      }
      return workingState;
    };

    private Dispatcher() {
    }

    public void register(BlockEntityLoad callback) {
      this.callbacks.add(callback);
    }

    public BlockEntityLoad invoker() {
      return this.invoker;
    }
  }

  @FunctionalInterface
  public interface BlockEntityLoad {
    BlockState beforeBlockEntityLoaded(
        CompoundTag nbt,
        ServerLevel level,
        BlockPos pos,
        BlockState state,
        HolderLookup.Provider registries
    );
  }
}
