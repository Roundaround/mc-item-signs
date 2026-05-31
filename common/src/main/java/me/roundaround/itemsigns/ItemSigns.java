package me.roundaround.itemsigns;

import me.roundaround.itemsigns.attachment.SignItemsAttachment;
import me.roundaround.itemsigns.compat.BetterHangingSignsRemover;
import me.roundaround.itemsigns.event.LoadFromNbtEvents;
import me.roundaround.itemsigns.generated.Constants;
import me.roundaround.itemsigns.server.SignItemStorage;
import net.minecraft.nbt.NbtOps;
import net.minecraft.tags.BlockTags;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Loader-agnostic common bootstrap. Holds the shared {@link Logger} and the
 * {@link BetterHangingSignsRemover} singleton (both previously lived on the
 * Fabric-only {@code ItemSignsMod} and were referenced from common code), plus
 * the one-time registration of the {@code LoadFromNbtEvents} callback that
 * splices stored items into freshly loaded sign block entities.
 *
 * <p>Each loader's entrypoint calls {@link #init()} exactly once during mod
 * construction so the NBT-splice mixins have their callback registered on every
 * loader. The per-loader server-event subscriptions (chunk-load cleanup, the
 * Better Hanging Signs hooks) live in the loader source sets and delegate to the
 * vanilla-typed handler methods in {@link me.roundaround.itemsigns.event.ItemSignsEvents}
 * and on {@link #BHS_REMOVER}.
 */
public final class ItemSigns {
  public static final Logger LOGGER = LogManager.getLogger(Constants.MOD_ID);
  public static final BetterHangingSignsRemover BHS_REMOVER = new BetterHangingSignsRemover();

  private ItemSigns() {
  }

  public static void init() {
    LoadFromNbtEvents.BLOCK_ENTITY.register((nbt, level, pos, state, registries) -> {
      if (!state.is(BlockTags.ALL_SIGNS)) {
        return state;
      }

      SignItemsAttachment attachment = SignItemStorage.getInstance(level).get(pos);
      if (attachment != null) {
        nbt.store(
            SignItemsAttachment.NBT_KEY,
            SignItemsAttachment.CODEC,
            registries.createSerializationContext(NbtOps.INSTANCE),
            attachment
        );
      }
      return state;
    });
  }
}
