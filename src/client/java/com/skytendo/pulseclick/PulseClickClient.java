package com.skytendo.pulseclick;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

public class PulseClickClient implements ClientModInitializer {

	public static final String MOD_ID = "pulseclick";

	private static boolean isClicking = false;
	private static KeyBinding activateKeyBinding;
	private static KeyBinding configKeyBinding;
	private static int tickCounter;

	@Override
	public void onInitializeClient() {
		MidnightConfig.init(MOD_ID, PulseClickConfig.class); // Init Config

		activateKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding( // Init activation key bind
				"key.pulseclick.activate",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_F6,
				"category.pulseclick"
		));

		configKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding( // Init config key bind
				"key.pulseclick.config",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_J,
				"category.pulseclick"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return; // If we do not have a client, we are not in-game and we want to return.

			if (configKeyBinding.wasPressed()) {
				client.setScreen(MidnightConfig.getScreen(client.currentScreen, MOD_ID));
			}

			if (activateKeyBinding.wasPressed()) {
				isClicking = !isClicking;

				if (isClicking)
					client.player.sendMessage(Text.translatable("pulseclick.message.activated"), true);
				else
					client.player.sendMessage(Text.translatable("pulseclick.message.deactivated"), true);
			}

			if (isClicking) {
				switch (PulseClickConfig.timingMode) {
					case TICKS -> {
						tickMode(client);
					}
					case ATTACK_COOLDOWN -> {
						attackCooldownMode(client);
					}
				}

			}
		});
	}

	private void attackCooldownMode(MinecraftClient client) {
		if (client.player.getAttackCooldownProgress(0) == 1) {
			attemptMobAttack(client);
		}
	}

	private void tickMode(MinecraftClient client) {
		tickCounter++;
		if (tickCounter >= PulseClickConfig.ticksBetweenClicks) {
			tickCounter = 0;
			attemptMobAttack(client);
		}
	}

	private void attemptMobAttack(MinecraftClient client) {
		if (client.interactionManager == null)
			return;

		HitResult rayTrace = client.crosshairTarget;
		if (rayTrace instanceof EntityHitResult) {
			client.interactionManager.attackEntity(client.player, ((EntityHitResult) rayTrace).getEntity());
			client.player.swingHand(Hand.MAIN_HAND);
		}
	}
}