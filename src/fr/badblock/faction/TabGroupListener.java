package fr.badblock.faction;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import fr.badblock.api.common.utils.permissions.Permissible;
import fr.badblock.api.common.utils.permissions.PermissionsManager;
import fr.badblock.gameapi.BadListener;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.utils.BukkitUtils;

public class TabGroupListener extends BadListener
{

	private Map<Permissible, String>	maps					= new HashMap<>();

	public TabGroupListener(Plugin plugin)
	{
		Collection<Permissible> groups = PermissionsManager.getManager().getGroups();
		List<Permissible> linkedList = new LinkedList<>(groups);

		Collections.sort(linkedList, new Comparator<Permissible>()
		{

			@Override
			public int compare(Permissible perm1, Permissible perm2)
			{
				return Integer.compare(perm2.getPower(), perm1.getPower());
			}

		});

		for (int i = 0; i < linkedList.size(); i++)
		{
			Permissible permissible = linkedList.get(i);
			String encrypted = encrypt(i);

			maps.put(permissible, encrypted);
		}

		Bukkit.getScheduler().runTaskTimer(plugin, run(), 20, 20 * 10);
	}

	private Runnable run()
	{
		return new Runnable()
		{
			@Override
			public void run()
			{
				BukkitUtils.getAllPlayers().forEach(player -> update(player));
			}
		};
	}

	@SuppressWarnings("deprecation")
	public void update(BadblockPlayer player)
	{
		Scoreboard scoreboard = player.getScoreboard();

		if (scoreboard == null)
		{
			scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
			return;
		}

		for (BadblockPlayer otherPlayer : BukkitUtils.getAllPlayers())
		{
			for (Entry<Permissible, String> entry : maps.entrySet())
			{
				Team team = scoreboard.getTeam(entry.getValue());
				String key = "permissions.tab." + entry.getKey().getName();
				String stp = GameAPI.getAPI().getI18n().get(key)[0];

				if (team == null)
				{
					team = scoreboard.registerNewTeam(entry.getValue());
					team.setPrefix(stp);
					String suffixKey = "permissions.chat_suffix." + entry.getKey().getName();
					String suffix = GameAPI.getAPI().getI18n().get(suffixKey)[0];
					team.setSuffix(suffix);
				}

				boolean inIt = entry.getKey().getName().equals(otherPlayer.getMainGroup());

				if (!team.hasPlayer(otherPlayer))
				{
					if (inIt)
					{
						team.addPlayer(otherPlayer);
					}
				}
				else
				{
					if (!inIt)
					{
						team.removePlayer(otherPlayer);
					}
				}
			}
		}

	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
	}

	private static String encrypt(int id)
	{
		int firstId = Math.floorDiv(id, 26);
		char firstLetter = generateForId(firstId);
		int leftovers = id % 26;
		char leftover = generateForId(leftovers);
		return firstLetter + "" + leftover;
	}

	private static char generateForId(int id)
	{
		int A = 'A';
		if (id > 26)
		{
			A   = 'a';
			id -= 26;
			return (char) (A + id);
		}

		return (char) (A + id);
	}

}