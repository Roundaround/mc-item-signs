package me.roundaround.itemsigns.neoforge;

import me.roundaround.itemsigns.ItemSigns;
import me.roundaround.itemsigns.event.ItemSignsEvents;
import me.roundaround.trove.neoforge.TroveNeoForge;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@Mod("itemsigns")
public final class ItemSignsNeoForgeMod {
  public ItemSignsNeoForgeMod(IEventBus modBus, ModContainer container) {
    TroveNeoForge.bootstrap(modBus, container);

    // Loader-agnostic bootstrap: registers the LoadFromNbtEvents callback.
    ItemSigns.init();

    // Drop orphaned attachments when a chunk loads with no matching sign.
    // GAME bus (NeoForge.EVENT_BUS); ChunkEvent.Load fires both server- and
    // client-side, so guard on ServerLevel.
    NeoForge.EVENT_BUS.addListener(ChunkEvent.Load.class, event -> {
      if (event.getLevel() instanceof ServerLevel level) {
        ItemSignsEvents.onChunkLoad(level, event.getChunk());
      }
    });

    // Better Hanging Signs datapack-migration cleanup. The handler logic is
    // loader-agnostic on ItemSigns.BHS_REMOVER; only the event wiring is NeoForge.
    NeoForge.EVENT_BUS.addListener(ServerStartingEvent.class,
        event -> ItemSigns.BHS_REMOVER.onServerStarting());
    NeoForge.EVENT_BUS.addListener(OnDatapackSyncEvent.class,
        event -> ItemSigns.BHS_REMOVER.onDataPackReload());
    NeoForge.EVENT_BUS.addListener(ServerStoppingEvent.class,
        event -> ItemSigns.BHS_REMOVER.onServerStopping());
    NeoForge.EVENT_BUS.addListener(EntityJoinLevelEvent.class, event -> {
      if (event.getLevel() instanceof ServerLevel level) {
        ItemSigns.BHS_REMOVER.onEntityLoad(event.getEntity(), level);
      }
    });
    NeoForge.EVENT_BUS.addListener(LevelTickEvent.Post.class, event -> {
      if (event.getLevel() instanceof ServerLevel level) {
        ItemSigns.BHS_REMOVER.onLevelTick(level);
      }
    });
  }
}
