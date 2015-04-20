/**
 * UltimateArena - fully customizable PvP arenas
 * Copyright (C) 2012 - 2015 MineSworn
 * Copyright (C) 2013 - 2015 dmulloy2
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.dmulloy2.ultimatearena.types;

import lombok.Getter;
import lombok.Setter;
import net.dmulloy2.ultimatearena.UltimateArena;
import net.dmulloy2.ultimatearena.arenas.Arena;
import net.dmulloy2.util.Util;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.material.Wool;

/**
 * @author dmulloy2
 */

@Getter @Setter
public abstract class ArenaFlag extends FlagBase
{
	protected Team owningTeam;
	protected Team cappingTeam;
	protected int power;

	protected boolean capped;

	public ArenaFlag(Arena arena, ArenaLocation location, UltimateArena plugin)
	{
		super(arena, location, plugin);

		Wool wool = new Wool();
		wool.setColor(DyeColor.SILVER);
		Util.setData(notify, wool);
	}

	@Override
	protected void setup()
	{
		super.setup();
		location.getBlock().setType(Material.WOOL);
	}

	@Override
	public abstract void checkNear(ArenaPlayer[] arenaPlayers);

	protected final void setOwningTeam(Team team)
	{
		this.owningTeam = team;

		Wool wool = new Wool();
		wool.setColor(team == Team.RED ? DyeColor.RED : DyeColor.BLUE);
		Util.setData(notify, wool);
	}
}
