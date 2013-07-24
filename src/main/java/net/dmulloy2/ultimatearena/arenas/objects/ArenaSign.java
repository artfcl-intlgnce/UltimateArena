package net.dmulloy2.ultimatearena.arenas.objects;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import net.dmulloy2.ultimatearena.UltimateArena;
import net.dmulloy2.ultimatearena.arenas.Arena;

/**
 * Represents an ArenaSign, whether it be join or not
 * @author dmulloy2
 */

public class ArenaSign 
{
	private UltimateArena plugin;
	private Location loc;
	private ArenaZone zone;
	private boolean join = false;
	private int id;
	private Sign sign;
	
	/**
	 * Creates a new ArenaSign
	 * @param plugin - {@link UltimateArena} plugin instance
	 * @param loc - {@link Location} of the spawn
	 * @param zone - {@link ArenaZone} that the sign is for
	 * @param join - Whether or not it is a join sign
	 * @param id - The sign's ID
	 */
	public ArenaSign(UltimateArena plugin, Location loc, ArenaZone zone, boolean join, int id)
	{
		this.plugin = plugin;
		this.loc = loc;
		this.zone = zone;
		this.join = join;
		this.id = id;
		this.sign = getSign();
	}
	
	/**
	 * Gets the {@link Sign} instance
	 * @return {@link Sign} instance
	 */
	public Sign getSign()
	{
		Block block = loc.getWorld().getBlockAt(loc);
		if (block.getState() instanceof Sign)
		{
			return (Sign)block.getState();
		}
		
		return null;
	}
	
	/**
	 * Updates the Sign
	 */
	public void update()
	{
		if (getSign() == null)
		{
			plugin.deleteSign(this);
			return;
		}
		
		if (join)
		{
			sign.setLine(0, "[UltimateArena]");
			sign.setLine(1, "Click to join");
			sign.setLine(2, zone.getArenaName());
			sign.setLine(3, "");
		}
		else
		{
			sign.setLine(0, "[Arena Stats]");
			sign.setLine(1, zone.getArenaName());
			sign.setLine(2, "Type: " + zone.getType().getName());
			sign.setLine(3, getStatus());
		}
		
		sign.update();
	}
	
	/**
	 * Gets the status of the Arena
	 * @return Status of the Arena
	 */
	public String getStatus()
	{
		StringBuilder line = new StringBuilder();
		if (plugin.getArena(zone.getArenaName()) != null)
		{
			Arena a = plugin.getArena(zone.getArenaName());
			line.append(a.getGameMode().toString() + " (");
			line.append(a.getActivePlayers() + "/" + zone.getMaxPlayers() + ")");
		}
		else
		{
			if (zone.isDisabled())
			{
				line.append("DISABLED (0/0)");
			}
			else
			{
				line.append("IDLE (0/");
				line.append(zone.getMaxPlayers());
				line.append(")");
			}
		}
		
		return line.toString();
	}
	
	/**
	 * Saves the sign
	 */
	public void save()
	{
		plugin.getFileHelper().saveSign(this);
	}
	
	// TODO: Explanations for these little methods
	public Location getLocation()
	{
		return loc;
	}

	public String getArena()
	{
		return zone.getArenaName();
	}
	
	public FieldType getArenaType()
	{
		return zone.getType();
	}
	
	public boolean isJoinSign()
	{
		return join;
	}
	
	public boolean isStatusSign()
	{
		return ! join;
	}
	
	public int getId()
	{
		return id;
	}
}