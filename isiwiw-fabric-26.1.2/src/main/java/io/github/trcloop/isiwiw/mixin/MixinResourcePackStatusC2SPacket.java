package io.github.trcloop.isiwiw.mixin;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;

/**
 * Common-sided Mixin.
 * This Mixin works alongside {@link MixinResourcePackSendS2CPacket}, and prevents
 * the client from telling the server that it ever rejected the resource pack.
 *
 * TLDR;
 * - In vanilla, if you reject the pack, the client will tell the server you rejected it.
 * - In here, if you reject the pack, the client will lie to the server that you accepted it and successfully loaded it.
 */
@Mixin(value = ServerboundResourcePackPacket.class, priority = 0)
public abstract class MixinResourcePackStatusC2SPacket
{
	// ==================================================
	private @Shadow @Mutable ServerboundResourcePackPacket.Action action;
	// ==================================================
	@Inject(
			method = "<init>(Ljava/util/UUID;Lnet/minecraft/network/protocol/common/ServerboundResourcePackPacket$Action;)V",
			at = @At("RETURN"),
			require = 0)
	private void onConstructor(UUID uuid, ServerboundResourcePackPacket.Action action, CallbackInfo ci)
	{
		if (this.action == ServerboundResourcePackPacket.Action.DECLINED)
			this.action = ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED;
	}
	// ==================================================
}
