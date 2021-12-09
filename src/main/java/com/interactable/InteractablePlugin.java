package com.interactable;

import com.google.common.base.Strings;
import com.google.inject.Provides;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Player;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.WallObject;
import net.runelite.api.events.ClientTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

@Slf4j
@PluginDescriptor(
	name = "Interactable Hints"
)
public class InteractablePlugin extends Plugin
{
	private int MAX_DISTANCE = 2350;
	private int remainingClientTicks = -1;

	@Getter(AccessLevel.PACKAGE)
	private boolean shown = false;
	@Getter(AccessLevel.PACKAGE)
	private Color currentObjectColor;
	@Getter(AccessLevel.PACKAGE)
	private Color currentNpcAttackColor;
	@Getter(AccessLevel.PACKAGE)
	private Color currentNpcInteractColor;
	@Getter(AccessLevel.PACKAGE)
	private List<TileObject> objects = new ArrayList<TileObject>();
	@Getter(AccessLevel.PACKAGE)
	private List<NPC> attackNpcs = new ArrayList<NPC>();
	@Getter(AccessLevel.PACKAGE)
	private List<NPC> interactNpcs = new ArrayList<NPC>();

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private InteractableOverlay interactableOverlay;

	@Inject
	private InteractableConfig config;

	@Inject
	private KeyManager keyManager;

	@Override
	protected void startUp() throws Exception
	{
		shown = false;
		keyManager.registerKeyListener(hotkeyListener);
		overlayManager.add(interactableOverlay);
		objects = new ArrayList<TileObject>();
		attackNpcs = new ArrayList<NPC>();
		interactNpcs = new ArrayList<NPC>();
	}

	@Override
	protected void shutDown() throws Exception
	{
		shown = false;
		keyManager.unregisterKeyListener(hotkeyListener);
		overlayManager.remove(interactableOverlay);
		objects = null;
		attackNpcs = null;
		interactNpcs = null;
	}

	@Subscribe
	public void onClientTick(ClientTick clientTick)
	{
		if (config.autoHideTimeout() > 0 && shown)
		{
			if (remainingClientTicks > 50)
			{
				remainingClientTicks--;
			}
			else if (remainingClientTicks > 0)
			{
				fadeOut();
				remainingClientTicks--;
			}
			else
			{
				shown = false;
				log.debug("Interactable Hints auto-hide triggered");
				currentObjectColor = config.objectHighlightColor();
				currentNpcAttackColor = config.attackNpcColor();
				currentNpcInteractColor = config.interactableNpcColor();
			}
		}
	}

	private void fadeOut()
	{
		int newObjectAlpha = config.objectHighlightColor().getAlpha() * remainingClientTicks / 50;
		currentObjectColor = new Color(config.objectHighlightColor().getRed(), config.objectHighlightColor().getGreen(), config.objectHighlightColor().getBlue(), newObjectAlpha);

		int newAttackAlpha = config.attackNpcColor().getAlpha() * remainingClientTicks / 50;
		currentNpcAttackColor = new Color(config.attackNpcColor().getRed(), config.attackNpcColor().getGreen(), config.attackNpcColor().getBlue(), newAttackAlpha);

		int newInteractAlpha = config.interactableNpcColor().getAlpha() * remainingClientTicks / 50;
		currentNpcInteractColor = new Color(config.interactableNpcColor().getRed(), config.interactableNpcColor().getGreen(), config.interactableNpcColor().getBlue(), newInteractAlpha);
	}

	@Provides
	InteractableConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(InteractableConfig.class);
	}

	private final HotkeyListener hotkeyListener = new HotkeyListener(() -> config.toggleKeybind())
	{
		@Override
		public void hotkeyPressed()
		{
			currentObjectColor = config.objectHighlightColor();
			currentNpcAttackColor = config.attackNpcColor();
			currentNpcInteractColor = config.interactableNpcColor();

			shown = !shown;
			log.debug("Interactable Hints overlay toggled to " + shown);
			if (shown)
			{
				remainingClientTicks = config.autoHideTimeout() * 50;
			}
		}
	};

	void generateObjectLists()
	{
		objects = new ArrayList<TileObject>();
		attackNpcs = new ArrayList<NPC>();
		interactNpcs = new ArrayList<NPC>();

		for (NPC npc : client.getNpcs())
		{
			NPCComposition npcComp = replaceNpcImposters(npc.getComposition());

			if (npcCompCheck(npcComp) && npcCheck(npc))
			{
				if (npcAttackable(npcComp))
				{
					attackNpcs.add(npc);
				}
				else if (npcInteractable(npcComp))
				{
					interactNpcs.add(npc);
				}
			}
		}

		Scene scene = client.getScene();
		Tile[][] tiles = scene.getTiles()[client.getPlane()];

		for (int x = 0; x < Constants.SCENE_SIZE; ++x)
		{
			for (int y = 0; y < Constants.SCENE_SIZE; ++y)
			{
				Tile tile = tiles[x][y];
				if (tile == null)
				{
					continue;
				}

				for (GameObject gameObject : tile.getGameObjects())
				{
					if (gameObjectCheck(gameObject) && !(gameObject.getRenderable() instanceof Player) && !(gameObject.getRenderable() instanceof NPC))
					{
						int objectId = gameObject.getId();
						ObjectComposition comp = replaceObjectImposters(client.getObjectDefinition(objectId));

						if (objectCompCheck(comp))
						{
							if (!objects.contains(gameObject))
							{
								objects.add(gameObject);
							}
							break;
						}
					}
				}

				WallObject wallObject = tile.getWallObject();

				if (gameObjectCheck(wallObject))
				{
					int objectId = wallObject.getId();
					ObjectComposition comp = replaceObjectImposters(client.getObjectDefinition(objectId));

					if (objectCompCheck(comp))
					{
						if (!objects.contains(wallObject))
						{
							objects.add(wallObject);
						}
					}
				}

				GroundObject groundObject = tile.getGroundObject();

				if (gameObjectCheck(groundObject))
				{
					int objectId = groundObject.getId();
					ObjectComposition comp = replaceObjectImposters(client.getObjectDefinition(objectId));

					if (objectCompCheck(comp))
					{
						if (!objects.contains(groundObject))
						{
							objects.add(groundObject);
						}
					}
				}
			}
		}
	}

	private boolean objectCompCheck(ObjectComposition comp)
	{
		return (comp != null && !Strings.isNullOrEmpty(comp.getName()) && !comp.getName().equals("null") && objectActionCheck(comp));
	}

	private boolean npcCompCheck(NPCComposition comp)
	{
		return (comp != null && !Strings.isNullOrEmpty(comp.getName()) && !comp.getName().equals("null"));
	}

	private boolean gameObjectCheck(TileObject object)
	{
		return object != null && object.getLocalLocation().distanceTo(client.getLocalPlayer().getLocalLocation()) <= MAX_DISTANCE;
	}

	private boolean npcCheck(NPC npc)
	{
		return npc != null && npc.getLocalLocation().distanceTo(client.getLocalPlayer().getLocalLocation()) <= MAX_DISTANCE;
	}

	private ObjectComposition replaceObjectImposters(ObjectComposition comp)
	{
		return comp.getImpostorIds() != null ? comp.getImpostor() : comp;
	}

	private NPCComposition replaceNpcImposters(NPCComposition comp)
	{
		return comp.getConfigs() != null && comp.transform() != null ? comp.transform() : comp;
	}

	private boolean objectActionCheck(ObjectComposition comp)
	{
		for (String action : comp.getActions())
		{
			if (!Strings.isNullOrEmpty(action) && !action.equals("null"))
			{
				return true;
			}
		}
		return false;
	}

	private boolean npcAttackable(NPCComposition comp)
	{
		for (String action : comp.getActions())
		{
			if (!Strings.isNullOrEmpty(action) && action.equals("Attack") && client.getVarpValue(1306) !=3)
			{
				return true;
			}
		}
		return false;
	}

	private boolean npcInteractable(NPCComposition comp)
	{
		for (String action : comp.getActions())
		{
			if (!Strings.isNullOrEmpty(action) && !action.equals("null") && !action.equals("Attack"))
			{
				return true;
			}
		}
		return false;
	}
}
