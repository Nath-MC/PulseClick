package com.skytendo.pulseclick;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class PulseClickClient implements ClientModInitializer {

	public static final String MOD_ID = "pulseclick";

	private static boolean isClicking = false;
	private static KeyBinding activateKeyBinding;
	private static KeyBinding configKeyBinding;
	private static int tickCounter;

	@Override
	public void onInitializeClient() {
		// Init Config
		MidnightConfig.init(MOD_ID, PulseClickConfig.class);

		// Init activation key bind
		activateKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.pulseclick.activate",
				InputUtil.Type.KEYSYM,
				InputUtil.GLFW_KEY_F6,
				"category.pulseclick"
		));

		// Init config key bind
		configKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.pulseclick.config",
				InputUtil.Type.KEYSYM,
				InputUtil.GLFW_KEY_J,
				"category.pulseclick"
		));

		// Deactivate PulseClick if the player disconnects
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(() -> {
			if (isClicking) {
				toggleAutoClicker(client);
			}
		}));

		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
	}

	private void tick(MinecraftClient client) {
		// If we do not have a client, we are not in-game and we want to return.
		if (client.player == null) return;

		// Open config if the key was pressed
		while (configKeyBinding.wasPressed()) {
			client.setScreen(MidnightConfig.getScreen(client.currentScreen, MOD_ID));
		}

		while (activateKeyBinding.wasPressed()) {
			toggleAutoClicker(client);
		}

		if (isClicking) {
			// Release the key that might have been pressed in the previous tick
			releaseKey(client);

			switch (PulseClickConfig.timingMode) {
				case TICKS -> tickMode(client);
				case ATTACK_COOLDOWN -> attackCooldownMode(client);
			}

			// Deactivate PulseClick if the player is starving (if enabled)
			if (PulseClickConfig.preventStarvation) {
				if (client.player.getHungerManager().getFoodLevel() < 4) {
					client.player.sendMessage(Text.translatable("pulseclick.message.starvation"), false);
					toggleAutoClicker(client);
				}
			}

			if (PulseClickConfig.preventToolBreak) {
				if (client.player.getMainHandStack().getComponents().get(DataComponentTypes.DAMAGE) != null) {
					if (client.player.getMainHandStack().getMaxDamage() - client.player.getMainHandStack().getDamage() < 10) {
						client.player.sendMessage(Text.translatable("pulseclick.message.toolbreak"), false);
						toggleAutoClicker(client);
					}
				}
			}

			// Disconnect if the player has low health (if enabled)
			if (PulseClickConfig.disconnectOnLowHealth) {
				if (client.player.getHealth() < 3) {
					disconnect(client);
				}
			}

			// Unpause the game if it was paused and the player unfocused the game window
			if (PulseClickConfig.unpauseGameOnUnfocus) {
				if (!client.isWindowFocused()) {
					if (client.currentScreen instanceof GameMenuScreen) {
						client.options.pauseOnLostFocus = false;
						client.setScreen(null);
					}
				}
			}

			if (PulseClickConfig.raidFarmMode) {
				PlayerEntity player = client.player;
				if (!player.hasStatusEffect(StatusEffects.BAD_OMEN)) {
					client.options.useKey.setPressed(true);
				} else {
					client.options.useKey.setPressed(false);
				}
			}
		}
	}

	/**
	 * Toggles the auto clicker on and off and sends an overlay message
	 * @param client The client
	 */
	private void toggleAutoClicker(MinecraftClient client) {
		isClicking = !isClicking;
		tickCounter = 0;
		if (isClicking) {
			client.player.sendMessage(Text.translatable("pulseclick.message.activated"), true);
			if (PulseClickConfig.timingMode == TimingMode.TICKS) {
				tickCounter = PulseClickConfig.ticksBetweenClicks;
			}
		}
		else {
			client.player.sendMessage(Text.translatable("pulseclick.message.deactivated"), true);
			releaseKey(client);
		}
	}

	/**
	 * Executed if the timing mode is set to ATTACK_COOLDOWN
	 * @param client The client
	 */
	private void attackCooldownMode(MinecraftClient client) {
		if (client.player.getAttackCooldownProgress(0) == 1) {
			pressKey(client);
			attemptMobAttack(client);
		}
	}

	/**
	 * Executed if the timing mode is set to TICKS
	 * @param client The client
	 */
	private void tickMode(MinecraftClient client) {
		tickCounter++;
		if (tickCounter >= PulseClickConfig.ticksBetweenClicks) {
			tickCounter = 0;
			pressKey(client);
			attemptMobAttack(client);
		}
	}

	/**
	 * Attempts to attack a mob
	 * @param client The client
	 */
	private void attemptMobAttack(MinecraftClient client) {
		if (client.interactionManager == null)
			return;
		if (PulseClickConfig.key != Key.MOUSE_LEFT)
			return;

		HitResult rayTrace = client.crosshairTarget;
		if (rayTrace instanceof EntityHitResult) {
			client.interactionManager.attackEntity(client.player, ((EntityHitResult) rayTrace).getEntity());
			client.player.swingHand(Hand.MAIN_HAND);
		}
	}

	/**
	 * Presses the key that is currently configured to be held down
	 * @param client The client
	 */
	private void pressKey(MinecraftClient client) {
		switch (PulseClickConfig.key) {
			case MOUSE_LEFT -> client.options.attackKey.setPressed(true);
			case MOUSE_RIGHT -> client.options.useKey.setPressed(true);
		}
	}

	/**
	 * Release the key that is currently configured to be held down
	 * @param client The client
	 */
	private void releaseKey(MinecraftClient client) {
		switch (PulseClickConfig.key) {
			case MOUSE_LEFT -> client.options.attackKey.setPressed(false);
			case MOUSE_RIGHT -> client.options.useKey.setPressed(false);
		}
	}

	/**
	 * Disconnect the player from the (integrated or external) server
	 * @param client The client
	 */
	private void disconnect(MinecraftClient client) {
		boolean singleplayer = client.isInSingleplayer();
		ServerInfo serverInfo = client.getCurrentServerEntry();
		client.world.disconnect();
		if (singleplayer) {
			client.disconnect(new MessageScreen(Text.translatable("menu.savingLevel")));
		} else {
			client.disconnect();
		}

		TitleScreen titleScreen = new TitleScreen();
		if (singleplayer) {
			client.setScreen(titleScreen);
		} else if (serverInfo != null && serverInfo.isRealm()) {
			client.setScreen(new RealmsMainScreen(titleScreen));
		} else {
			client.setScreen(new MultiplayerScreen(titleScreen));
		}
	}
}