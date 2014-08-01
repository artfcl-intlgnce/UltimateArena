package net.dmulloy2.ultimatearena.types;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dmulloy2.types.Reloadable;
import net.dmulloy2.ultimatearena.UltimateArena;
import net.dmulloy2.util.FormatUtil;
import net.dmulloy2.util.ItemUtil;
import net.dmulloy2.util.NumberUtil;
import net.dmulloy2.util.Util;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

/**
 * @author dmulloy2
 */

@Getter @Setter
public class ArenaConfig implements ConfigurationSerializable, Reloadable
{
	// Generic
	protected int gameTime, lobbyTime, maxDeaths;
	protected double cashReward;

	protected boolean allowTeamKilling, countMobKills, canModifyWorld = false;
	protected boolean unlimitedAmmo, rewardBasedOnXp, giveRewards = true;

	protected List<String> blacklistedClasses, whitelistedClasses;

	protected transient List<ItemStack> rewards;
	protected transient Map<Integer, List<KillStreak>> killStreaks;

	// ---- Transient
	protected transient File file;
	protected transient String type;
	protected transient boolean loaded;

	protected transient final UltimateArena plugin;

	public ArenaConfig(@NonNull UltimateArena plugin, @NonNull String type, @NonNull File file)
	{
		this.type = type;
		this.file = file;
		this.plugin = plugin;

		// Initialize some variables
		this.initializeVariables();

		// Load
		this.loaded = load();
	}

	public ArenaConfig(@NonNull ArenaZone az)
	{
		this.type = az.getName() + " - Config";
		this.plugin = az.getPlugin();
		this.file = az.getFile();
	}

	protected void initializeVariables()
	{
		this.rewards = new ArrayList<>();
		this.blacklistedClasses = new ArrayList<>();
		this.whitelistedClasses = new ArrayList<>();
		this.rewardBasedOnXp = xpBasedTypes.contains(type.toUpperCase());
		this.killStreaks = getDefaultKillStreak();
	}

	public final boolean load()
	{
		return load(file, this);
	}

	public final boolean load(@NonNull File file, @NonNull ArenaConfig def)
	{
		Validate.isTrue(! loaded, "Config has already been loaded!");

		try
		{
			YamlConfiguration fc = YamlConfiguration.loadConfiguration(file);
			this.gameTime = fc.getInt("gameTime", def.getGameTime());
			this.lobbyTime = fc.getInt("lobbyTime", def.getLobbyTime());
			this.maxDeaths = Math.max(1, fc.getInt("maxDeaths", def.getMaxDeaths()));
			this.allowTeamKilling = fc.getBoolean("allowTeamKilling", def.isAllowTeamKilling());
			this.cashReward = fc.getDouble("cashReward", def.getCashReward());
			this.countMobKills = fc.getBoolean("countMobKills", def.isCountMobKills());
			this.canModifyWorld = fc.getBoolean("canModifyWorld", def.isCanModifyWorld());
			this.unlimitedAmmo = fc.getBoolean("unlimitedAmmo", def.isUnlimitedAmmo());

			this.rewards = new ArrayList<>();
			if (fc.isSet("rewards"))
			{
				for (String reward : fc.getStringList("rewards"))
				{
					try
					{
						ItemStack stack = ItemUtil.readItem(reward);
						if (stack != null)
							rewards.add(stack);
					}
					catch (Throwable ex)
					{
						plugin.getLogHandler().log(Level.WARNING, Util.getUsefulStack(ex, "parsing item \"" + reward + "\""));
					}
				}
			}
			else
			{
				this.rewards = def.getRewards();
			}

			this.giveRewards = fc.getBoolean("giveRewards", def.isGiveRewards());
			this.rewardBasedOnXp = fc.getBoolean("rewardBasedOnXp", def.isRewardBasedOnXp());

			this.blacklistedClasses = def.getBlacklistedClasses();
			if (fc.isSet("blacklistedClasses"))
			{
				blacklistedClasses.addAll(fc.getStringList("blacklistedClasses"));
			}

			this.whitelistedClasses = def.getWhitelistedClasses();
			if (fc.isSet("whitelistedClasses"))
			{
				whitelistedClasses.addAll(fc.getStringList("whitelistedClasses"));
			}

			this.killStreaks = def.getKillStreaks();
			if (fc.isSet("killStreaks"))
			{
				this.killStreaks = new LinkedHashMap<>();

				// TODO: Use memory sections for this, much easier
				Map<String, Object> values = fc.getConfigurationSection("killStreaks").getValues(true);
				for (Entry<String, Object> entry : values.entrySet())
				{
					int kills = NumberUtil.toInt(entry.getKey());
					if (kills < 0)
						continue;

					List<KillStreak> streaks = new ArrayList<>();

					@SuppressWarnings("unchecked")
					List<String> list = (List<String>) entry.getValue();
					for (String string : list)
					{
						string = string.replaceAll("  ", " ");
						String[] split = string.split(",");

						// Determine type
						KillStreak.Type type = split[0].equalsIgnoreCase("mob") ? KillStreak.Type.MOB : KillStreak.Type.ITEM;

						// Load settings
						String message = split[1].trim();

						// Load specific settings
						if (type == KillStreak.Type.MOB)
						{
							EntityType entityType = EntityType.valueOf(split[2].toUpperCase());
							int amount = NumberUtil.toInt(split[3]);

							streaks.add(new KillStreak(kills, message, entityType, amount));
						}
						else
						{
							String item = FormatUtil.join(",", Arrays.copyOfRange(split, 2, split.length));

							try
							{
								ItemStack stack = ItemUtil.readItem(item);
								if (stack != null)
									streaks.add(new KillStreak(kills, message, stack));
							}
							catch (Throwable ex)
							{
								plugin.getLogHandler().log(Level.WARNING, Util.getUsefulStack(ex, "parsing item \"" + item + "\""));
							}
						}
					}

					killStreaks.put(kills, streaks);
				}
			}

			// Load custom options
			loadCustomOptions(fc, def);
		}
		catch (Throwable ex)
		{
			plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(ex, "loading config for \"" + type + "\""));
			return false;
		}

		plugin.getLogHandler().debug("Successfully loaded config for {0}!", type);
		return true;
	}

	protected void loadCustomOptions(YamlConfiguration fc, ArenaConfig def) { }

	// TODO
	private static final List<String> xpBasedTypes = Arrays.asList(new String[]
	{
			"KOTH", "FFA", "CQ", "MOB", "CTF", "PVP", "BOMB"
	});

	public Map<Integer, List<KillStreak>> getDefaultKillStreak()
	{
		Map<Integer, List<KillStreak>> ret = new LinkedHashMap<>();

		ret.put(2, Arrays.asList(new KillStreak[] {
				new KillStreak(2, "&e2 &3kills! Unlocked strength potion!", ItemUtil.readPotion("strength, 1, 1, false"))
		}));

		ret.put(4, Arrays.asList(new KillStreak[] {
				new KillStreak(4, "&e4 &3kills! Unlocked health potion!", ItemUtil.readPotion("heal, 1, 1, false")),
				new KillStreak(4, "&e4 &3kills! Unlocked food!", new ItemStack(Material.GRILLED_PORK, 2))
		}));

		ret.put(5, Arrays.asList(new KillStreak[] {
				new KillStreak(5, "&e5 &3kills! Unlocked Zombies!", EntityType.ZOMBIE, 4)
		}));

		ret.put(8, Arrays.asList(new KillStreak[] {
				new KillStreak(8, "&e8 &3kills! Unlocked attack dogs!", EntityType.WOLF, 2)
		}));

		ret.put(12, Arrays.asList(new KillStreak[] {
				new KillStreak(12, "&e12 &3kills! Unlocked regen potion!", ItemUtil.readPotion("regen, 1, 1, false")),
				new KillStreak(12, "&e12 &3kills! Unlocked food!", new ItemStack(Material.GRILLED_PORK, 2))
		}));

		return ret;
	}

	public final void save()
	{
		save(file);
	}

	public final void save(@NonNull File file)
	{
		try
		{
			if (! file.exists())
				file.createNewFile();

			YamlConfiguration fc = YamlConfiguration.loadConfiguration(file);
			for (Entry<String, Object> entry : serialize().entrySet())
			{
				fc.set(entry.getKey(), entry.getValue());
			}

			fc.save(file);
		} catch (Throwable ex) { }
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> data = new LinkedHashMap<>();

		try
		{
			for (Field field : getClass().getDeclaredFields())
			{
				if (Modifier.isTransient(field.getModifiers()))
					continue;

				boolean accessible = field.isAccessible();

				field.setAccessible(true);

				if (field.getType().equals(Integer.TYPE))
				{
					if (field.getInt(this) != 0)
						data.put(field.getName(), field.getInt(this));
				}
				else if (field.getType().equals(Long.TYPE))
				{
					if (field.getLong(this) != 0)
						data.put(field.getName(), field.getLong(this));
				}
				else if (field.getType().equals(Boolean.TYPE))
				{
					if (field.getBoolean(this))
						data.put(field.getName(), field.getBoolean(this));
				}
				else if (field.getType().isAssignableFrom(Collection.class))
				{
					if (! ((Collection<?>) field.get(this)).isEmpty())
						data.put(field.getName(), field.get(this));
				}
				else if (field.getType().isAssignableFrom(String.class))
				{
					if ((String) field.get(this) != null)
						data.put(field.getName(), field.get(this));
				}
				else if (field.getType().isAssignableFrom(Map.class))
				{
					if (! ((Map<?, ?>) field.get(this)).isEmpty())
						data.put(field.getName(), field.get(this));
				}
				else
				{
					if (field.get(this) != null)
						data.put(field.getName(), field.get(this));
				}

				field.setAccessible(accessible);
			}
		} catch (Throwable ex) { }
		return data;
	}

	@Override
	public void reload()
	{
		// Make sure this class still exists
		if (! file.exists())
		{
			plugin.getClasses().remove(this);
			return;
		}

		// Re-initialize variables
		this.initializeVariables();

		// Re-load
		this.loaded = false;
		this.loaded = load();
	}
}