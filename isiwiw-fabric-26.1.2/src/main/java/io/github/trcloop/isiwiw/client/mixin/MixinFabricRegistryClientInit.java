package io.github.trcloop.isiwiw.client.mixin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.impl.client.registry.sync.FabricRegistryClientInit;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.fabricmc.fabric.impl.registry.sync.SyncCompletePayload;
import net.fabricmc.fabric.impl.registry.sync.RemapException;
import net.fabricmc.fabric.impl.registry.sync.packet.RegistrySyncPayload;
import net.minecraft.client.Minecraft;

/**
 * Client-sided Mixin.
 * Prevents 'Fabric API' from forcing users to install mods present on the server.
 */
@Mixin(value = FabricRegistryClientInit.class, remap = false, priority = 0)
public abstract class MixinFabricRegistryClientInit
{
	// ==================================================
	private static final Logger ISIWIW_LOGGER = LoggerFactory.getLogger("isiwiw");
	// ==================================================
	@Inject(method = "onInitializeClient", at = @At("HEAD"), cancellable = true, require = 0)
	private void onOnInitializeClient(CallbackInfo ci)
	{
		//register our own handler that doesn't kick the player on failure
		ClientConfigurationNetworking.registerGlobalReceiver(RegistrySyncPayload.ID, (payload, context) ->
		{
			final var client = Minecraft.getInstance();

			if(!RegistrySyncManager.DEBUG && client.isLocalServer())
			{
				context.responseSender().sendPacket(SyncCompletePayload.INSTANCE);
				return;
			}

			client.execute(() ->
			{
				try
				{
					net.fabricmc.fabric.impl.client.registry.sync.ClientRegistrySyncHandler.apply(payload);
					context.responseSender().sendPacket(SyncCompletePayload.INSTANCE);
				}
				catch(Throwable e)
				{
					ISIWIW_LOGGER.error("Registry remapping failed!", e);
					//DO NOT kick the player - this is the whole point of ISIWIW
				}
			});
		});

		//DO NOT use Fabric's mechanism.
		ci.cancel();
	}
	// ==================================================
}
