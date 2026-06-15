package io.github.trcloop.isiwiw.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.ClientBrandRetriever;

/**
 * Client-sided Mixin.
 * Fabric Loader patches ClientBrandRetriever to return "fabric" instead of "vanilla",
 * which allows servers to detect that the client is running Fabric.
 * This mixin overrides that to return "vanilla" so the server cannot tell.
 */
@Mixin(value = ClientBrandRetriever.class, priority = 0)
public class MixinClientBrandRetriever
{
	// ==================================================
	@Inject(method = "getClientModName", at = @At("HEAD"), cancellable = true, require = 0)
	private static void onGetClientModName(CallbackInfoReturnable<String> ci)
	{
		ci.setReturnValue("vanilla");
	}
	// ==================================================
}
