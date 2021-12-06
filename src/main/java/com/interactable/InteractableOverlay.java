package com.interactable;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.TileObject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

class InteractableOverlay extends Overlay
{
	private final Client client;
	private final InteractablePlugin plugin;
	private final InteractableConfig config;
	private final ModelOutlineRenderer modelOutlineRenderer;

	@Inject
	private InteractableOverlay(Client client, InteractablePlugin plugin, InteractableConfig config, ModelOutlineRenderer modelOutlineRenderer)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.modelOutlineRenderer = modelOutlineRenderer;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(OverlayPriority.HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.isShown())
		{
			renderObjects();
		}

		return null;

	}

	private void renderObjects()
	{
		plugin.generateObjectLists();

		if (config.showObject())
		{
			for (TileObject object : plugin.getObjects())
			{
				if (object != null)
				{
					modelOutlineRenderer.drawOutline(object, config.borderWidth(), plugin.getCurrentObjectColor(), config.outlineFeather());
				}
			}
		}

		if (config.showAttackNpc())
		{
			for (NPC npc : plugin.getAttackNpcs())
			{
				if (npc != null)
				{
					modelOutlineRenderer.drawOutline(npc, config.borderWidth(), plugin.getCurrentNpcAttackColor(), config.outlineFeather());
				}
			}
		}

		if (config.showInteractableNpc())
		{
			for (NPC npc : plugin.getInteractNpcs())
			{
				if (npc != null)
				{
					modelOutlineRenderer.drawOutline(npc, config.borderWidth(), plugin.getCurrentNpcInteractColor(), config.outlineFeather());
				}
			}
		}


	}
}