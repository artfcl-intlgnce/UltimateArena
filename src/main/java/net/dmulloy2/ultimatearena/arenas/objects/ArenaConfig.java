package net.dmulloy2.ultimatearena.arenas.objects;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.dmulloy2.ultimatearena.UltimateArena;
import net.dmulloy2.ultimatearena.util.InventoryHelper;
import net.dmulloy2.ultimatearena.util.ItemHelper;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ArenaConfig
{
	private int gameTime, lobbyTime, maxDeaths, maxWave, cashReward;
	
	private boolean allowTeamKilling;
	
	private List<ItemStack> rewards = new ArrayList<ItemStack>();
	
	private boolean loaded = false;
	
	private String arenaName;
	private File file;
	private final UltimateArena plugin;
	
	public ArenaConfig(final UltimateArena plugin, String str, File file)
	{
		this.arenaName = str;
		this.file = file;
		this.plugin = plugin;
		
		this.loaded = load();
		if (!loaded)
		{
			plugin.outConsole(Level.SEVERE, "Could not load config for " + arenaName + "!");
		}
	}
	
	public boolean load()
	{
		try
		{
			YamlConfiguration fc = YamlConfiguration.loadConfiguration(file);
			if (arenaName.equals("mob"))
			{
				this.maxWave = fc.getInt("maxWave");
			}
			
			this.gameTime = fc.getInt("gameTime");
			this.lobbyTime = fc.getInt("lobbyTime");
			this.maxDeaths = fc.getInt("maxDeaths");
			this.allowTeamKilling = fc.getBoolean("allowTeamKilling");
			this.cashReward = fc.getInt("cashReward");
					
			List<String> words = fc.getStringList("rewards");
			for (String word : words)
			{
				ItemStack stack = ItemHelper.readItem(word);
				rewards.add(stack);
			}
		}
		catch (Exception e)
		{
			plugin.outConsole(Level.SEVERE, "Failed to load config for \"{0}\": {1}", arenaName, e.getMessage());
			return false;
		}
		
		plugin.debug("Loaded ArenaConfig for type: {0}!", arenaName);
		return true;
	}
	
	public void giveRewards(Player player, boolean half) 
	{
		for (ItemStack stack : rewards)
		{
			if (stack == null)
				continue;
			
			if (half) stack.setAmount((int) Math.floor(stack.getAmount() / 2));
			
			InventoryHelper.addItem(player, stack);
		}
		
		// dmulloy2 new method
		if (plugin.getConfig().getBoolean("moneyrewards"))
		{
			if (plugin.getEconomy() != null)
			{
				if (cashReward > 0)
				{
					plugin.getEconomy().depositPlayer(player.getName(), cashReward);
					String format = plugin.getEconomy().format(cashReward);
					player.sendMessage(ChatColor.GREEN + format + " has been added to your balance!");
				}
			}
		}
	}

	public int getGameTime() 
	{
		return gameTime;
	}

	public int getLobbyTime() 
	{
		return lobbyTime;
	}

	public int getMaxDeaths() 
	{
		return maxDeaths;
	}

	public int getMaxWave()
	{
		return maxWave;
	}

	public int getCashReward()
	{
		return cashReward;
	}

	public boolean isAllowTeamKilling() 
	{
		return allowTeamKilling;
	}
	
	public String getArenaName()
	{
		return arenaName;
	}
	
	public boolean isLoaded()
	{
		return loaded;
	}
}