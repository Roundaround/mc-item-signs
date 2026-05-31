package me.roundaround.itemsigns.forge;

import me.roundaround.itemsigns.ItemSigns;
import me.roundaround.itemsigns.event.ItemSignsEvents;
import me.roundaround.trove.forge.TroveForge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("itemsigns")
public final class ItemSignsForgeMod {
  public ItemSignsForgeMod(FMLJavaModLoadingContext context) {
    TroveForge.bootstrap(context);

    // Loader-agnostic bootstrap: registers the LoadFromNbtEvents callback.
    ItemSigns.init();

    // This Forge build is eventbus-7-typed: register game events with
    // <Event>.BUS.addListener(...). The legacy @SubscribeEvent /
    // MinecraftForge.EVENT_BUS style would not compile against this build.

    // Drop orphaned attachments when a chunk loads with no matching sign.
    // ChunkEvent.Load fires both server- and client-side; guard on ServerLevel.
    ChunkEvent.Load.BUS.addListener(event -> {
      if (event.getLevel() instanceof ServerLevel level && event.getChunk() instanceof LevelChunk chunk) {
        ItemSignsEvents.onChunkLoad(level, chunk);
      }
    });

    // Better Hanging Signs datapack-migration cleanup. The handler logic is
    // loader-agnostic on ItemSigns.BHS_REMOVER; only the event wiring is Forge.
    ServerStartingEvent.BUS.addListener(event -> ItemSigns.BHS_REMOVER.onServerStarting());
    OnDatapackSyncEvent.BUS.addListener(event -> ItemSigns.BHS_REMOVER.onDataPackReload());
    ServerStoppingEvent.BUS.addListener(event -> ItemSigns.BHS_REMOVER.onServerStopping());
    EntityJoinLevelEvent.BUS.addListener(event -> {
      if (event.getLevel() instanceof ServerLevel level) {
        ItemSigns.BHS_REMOVER.onEntityLoad(event.getEntity(), level);
      }
    });
    TickEvent.LevelTickEvent.Post.BUS.addListener(event -> {
      if (event.level() instanceof ServerLevel level) {
        ItemSigns.BHS_REMOVER.onLevelTick(level);
      }
    });
  }
}
