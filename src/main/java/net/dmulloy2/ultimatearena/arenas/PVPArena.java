package net.dmulloy2.ultimatearena.arenas;

import net.dmulloy2.ultimatearena.arenas.objects.ArenaZone;
import net.dmulloy2.ultimatearena.arenas.objects.FieldType;

public class PVPArena extends Arena 
{
	public PVPArena(ArenaZone az) 
	{
		super(az);
		
		this.type = FieldType.PVP;
		this.startTimer = 120;
		this.maxGameTime = 60 * 10;
		this.maxDeaths = 3;
	}
	
	@Override
	public int getTeam() 
	{
		return getBalancedTeam();
	}

	@Override
	public void check() 
	{
		if (isInGame())
		{
			if (! simpleTeamCheck(false)) 
			{
				setWinningTeam(-1);
				
				stop();
				
				rewardTeam(-1, false);
			}
			else
			{
				if (getStartingAmount() <= 1) 
				{
					tellPlayers("&9Not enough people to play!");
					
					stop();
				}
			}
		}
	}
	
	@Override
	public void announceWinner()
	{
		tellPlayers("&9You won!");
	}
}