package io.github.trcloop.isiwiw.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client-sided Mixin.
 * Intercepts outbound packets and suppresses Fabric-specific payloads
 * so the server cannot detect that the client is running Fabric mods.
 *
 * Suppressed payloads:
 * - fabric:networking/v1 (CommonVersionPayload) - Fabric networking handshake
 * - fabric:register/v1 (CommonRegisterPayload) - Fabric channel registration
 * - minecraft:register (RegistrationPayload) - Custom channel registration
 * - minecraft:unregister (RegistrationPayload) - Custom channel unregistration
 * - BrandPayload with non-vanilla brand
 */
@Mixin(Connection.class)
public class MixinConnection
{
	// ==================================================
	@Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V",
			at = @At("HEAD"), cancellable = true, require = 0)
	private void onSend(Packet<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo ci)
	{
		if (packet instanceof ServerboundCustomPayloadPacket customPayloadPacket)
		{
			CustomPacketPayload payload = customPayloadPacket.payload();
			String typeName = payload.type().id().toString();

			// Block Fabric-specific payloads
			if (typeName.startsWith("fabric:") ||
				typeName.equals("minecraft:register") ||
				typeName.equals("minecraft:unregister"))
			{
				ci.cancel();
				return;
			}

			// Block brand payloads (we already spoof ClientBrandRetriever, but
			// in case Fabric injects its own brand payload, block it too)
			if (payload instanceof net.minecraft.network.protocol.common.custom.BrandPayload brandPayload)
			{
				if (!"vanilla".equals(brandPayload.brand()))
				{
					ci.cancel();
					return;
				}
			}
		}
	}
	// ==================================================
}
