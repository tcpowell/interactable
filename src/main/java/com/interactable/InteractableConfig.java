package com.interactable;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Range;

@ConfigGroup(InteractableConfig.GROUP)
public interface InteractableConfig extends Config
{
	String GROUP = "interactableHighlight";

	@ConfigItem(
		keyName = "toggleKeybind",
		name = "Toggle Overlay",
		description = "Binds a key (combination) to toggle the overlay.",
		position = 0
	)
	default Keybind toggleKeybind()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		keyName = "autoHideTimeout",
		name = "Auto Hide Timeout",
		description = "Timeout (in seconds) where the overlay is hidden (0 = do not auto hide)",
		position = 1
	)
	default int autoHideTimeout()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "showObject",
		name = "Show Objects",
		description = "Outline interactable Objects",
		position = 2
	)
	default boolean showObject()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "objectHighlightColor",
		name = "Object Color",
		description = "The color of the outline for Objects",
		position = 3
	)
	default Color objectHighlightColor()
	{
		return new Color(0, 255, 255, 160);
	}

	@ConfigItem(
		keyName = "showAttackNpc",
		name = "Show Attackable NPCs",
		description = "Outline attackable NPCs",
		position = 4
	)
	default boolean showAttackNpc()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "attackNpcColor",
		name = "Attackable NPC Color",
		description = "The color of the outline for attackable NPCs",
		position = 5
	)
	default Color attackNpcColor()
	{
		return new Color(255, 10, 10, 160);
	}

	@ConfigItem(
		keyName = "showInteractableNpc",
		name = "Show Interactable NPCs",
		description = "Outline interactable NPCs",
		position = 6
	)
	default boolean showInteractableNpc()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "interactableNpcColor",
		name = "Interactable NPC Color",
		description = "The color of the outline for interactable NPCs",
		position = 7
	)
	default Color interactableNpcColor()
	{
		return new Color(255, 255, 10, 160);
	}

	@ConfigItem(
		keyName = "borderWidth",
		name = "Border Width",
		description = "Width of the outlined border",
		position = 8
	)
	default int borderWidth()
	{
		return 4;
	}

	@ConfigItem(
		keyName = "outlineFeather",
		name = "Outline feather",
		description = "Specify between 0-4 how much of the model outline should be faded",
		position = 9
	)
	@Range(
		max = 4
	)
	default int outlineFeather()
	{
		return 4;
	}

}
