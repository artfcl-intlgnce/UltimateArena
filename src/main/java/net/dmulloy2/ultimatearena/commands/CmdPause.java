package net.dmulloy2.ultimatearena.commands;

import net.dmulloy2.ultimatearena.UltimateArena;
import net.dmulloy2.ultimatearena.arenas.Arena;
import net.dmulloy2.ultimatearena.types.Permission;

/**
 * @author dmulloy2
 */

public class CmdPause extends UltimateArenaCommand
{
	public CmdPause(UltimateArena plugin)
	{
		super(plugin);
		this.name = "pause";
		this.requiredArgs.add("arena");
		this.description = "pause the start timer on an arena";
		this.permission = Permission.PAUSE;

		this.mustBePlayer = false;
	}

	@Override
	public void perform()
	{
		Arena arena = plugin.getArena(args[0]);
		if (arena == null)
		{
			err("Could not find an Arena by the name of \"&c{0}&4\"!");
			return;
		}

		arena.setPauseStartTimer(! arena.isPauseStartTimer());
		sendpMessage("&3Start timer for arena &e{0} &3is now &e{1}&3!", arena.getName(), arena.isPauseStartTimer() ? "paused" : "unpaused");
	}
}