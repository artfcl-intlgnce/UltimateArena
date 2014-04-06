package net.dmulloy2.ultimatearena.commands;

import net.dmulloy2.ultimatearena.UltimateArena;
import net.dmulloy2.ultimatearena.arenas.Arena;
import net.dmulloy2.ultimatearena.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdInfo extends UltimateArenaCommand
{
	public CmdInfo(UltimateArena plugin)
	{
		super(plugin);
		this.name = "info";
		this.aliases.add("lb");
		this.optionalArgs.add("arena");
		this.description = "view info on the arena you are in";
		this.permission = Permission.INFO;

		this.mustBePlayer = true;
	}

	@Override
	public void perform()
	{
		Arena arena = null;

		if (args.length == 0)
		{
			if (! plugin.isInArena(player))
			{
				err("You are not in an arena!");
				return;
			}

			arena = plugin.getArena(player);
		}
		else
		{
			arena = plugin.getArena(args[0]);
		}

		if (arena == null)
		{
			err("Please specify a valid arena!");
			return;
		}

		sendMessage("&3====[ &e{0} &3]====", capitalize(arena.getName()));

		sendMessage(""); // Empty line

		sendMessage("&3Active Players:");
		for (String s : arena.getLeaderboard(player))
		{
			sendMessage(s);
		}
	}
}