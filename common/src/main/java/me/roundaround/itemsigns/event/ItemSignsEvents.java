package me.roundaround.itemsigns.event;

import me.roundaround.itemsigns.ItemSigns;
import me.roundaround.itemsigns.attachment.SignItemsAttachment;
import me.roundaround.itemsigns.server.SignItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.HashMap;

/**
 * Loader-agnostic handlers for the server-side events this mod reacts to. Only
 * vanilla types appear in the signatures, so the per-loader entrypoints can call
 * these directly from their respective event buses (Fabric
 * {@code ServerChunkEvents.CHUNK_LOAD}, NeoForge {@code ChunkEvent.Load},
 * Forge {@code ChunkEvent.Load.BUS}).
 */
public final class ItemSignsEvents {
  private ItemSignsEvents() {
  }

  /**
   * Reconcile {@link SignItemStorage} against a freshly loaded chunk: any stored
   * attachment that no longer has a matching sign block entity is orphaned —
   * drop its items and forget it.
   */
  public static void onChunkLoad(ServerLevel level, LevelChunk chunk) {
    SignItemStorage storage = SignItemStorage.getInstance(level);
    HashMap<BlockPos, SignItemsAttachment> signs = storage.allInChunk(chunk.getPos());
    chunk.getBlockEntities().forEach((pos, blockEntity) -> {
      if (blockEntity instanceof SignBlockEntity) {
        signs.remove(pos);
      }
    });

    signs.forEach((pos, attachment) -> {
      if (attachment != null && !attachment.isEmpty()) {
        ItemSigns.LOGGER.warn(
            "Attached item data found for sign at {}, but no appropriate sign found. Dropping the attached items.",
            pos.toShortString()
        );

        Containers.dropContents(level, pos, attachment.getAll());
      }

      storage.remove(pos);
    });
  }
}
