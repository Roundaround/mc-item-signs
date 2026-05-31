package me.roundaround.itemsigns;

import me.roundaround.allay.api.Entrypoint;
import me.roundaround.itemsigns.event.ItemSignsEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

@Entrypoint(Entrypoint.MAIN)
public final class ItemSignsMod implements ModInitializer {
  @Override
  public void onInitialize() {
    // Loader-agnostic bootstrap: registers the LoadFromNbtEvents callback that
    // splices stored items into freshly loaded sign block entities.
    ItemSigns.init();

    // Drop orphaned attachments when a chunk loads with no matching sign.
    ServerChunkEvents.CHUNK_LOAD.register(
        (level, chunk, _) -> ItemSignsEvents.onChunkLoad(level, chunk));

    // Better Hanging Signs datapack-migration cleanup. The handler logic is
    // loader-agnostic on ItemSigns.BHS_REMOVER; only the event wiring is Fabric.
    ServerLifecycleEvents.SERVER_STARTING.register(
        (server) -> ItemSigns.BHS_REMOVER.onServerStarting());
    ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(
        (server, resourceManager, success) -> ItemSigns.BHS_REMOVER.onDataPackReload());
    ServerLifecycleEvents.SERVER_STOPPING.register(
        (server) -> ItemSigns.BHS_REMOVER.onServerStopping());
    ServerEntityEvents.ENTITY_LOAD.register(
        (entity, world) -> ItemSigns.BHS_REMOVER.onEntityLoad(entity, world));
    ServerTickEvents.END_LEVEL_TICK.register(
        (level) -> ItemSigns.BHS_REMOVER.onLevelTick(level));
  }
}
