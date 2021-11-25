package me._furiouspotato_.manhunt.tabcompleters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import me._furiouspotato_.manhunt.Main;

public class ManhuntCompleter implements TabCompleter {
	private Main plugin;

	public ManhuntCompleter(Main plugin) {
		this.plugin = plugin;
	}

	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> res = new ArrayList<>();

		if (sender instanceof Player) {
			List<String> all = new ArrayList<>();
			List<String> operator = new ArrayList<>();

			if (args.length == 1) {
				all = Arrays.asList("join", "leave", "list");
				operator = Arrays.asList("join1", "setup", "start", "end");
			}

			if (args.length >= 2) {
				if (args[0].equalsIgnoreCase("setup")) {
					if (args.length == 2) {
						operator.add("<Game type>");
					}
					if (args.length == 3) {
						operator.add("<Game duration>");
					}
					if (args.length == 4) {
						operator = Arrays.asList("<Enable glowing>", "false", "true");
					}
					if (args.length == 5) {
						operator.add("<Border size>");
					}
					if (args.length == 6) {
						operator.add("<Invulnerability off duration>");
					}
					if (args.length == 7) {
						operator.add("<Invulnerability on duration>");
					}
				}
			}

			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("join")) {
					all.add("hunter");
					all.add("runner");
				}
			}

			if (args.length == 3) {
				if (args[0].equalsIgnoreCase("join")
						&& (args[1].equalsIgnoreCase("hunter") || args[1].equalsIgnoreCase("runner"))) {
					Player[] players = new Player[Bukkit.getServer().getOnlinePlayers().size()];
					Bukkit.getServer().getOnlinePlayers().toArray((Object[]) players);
					for (Player player : players) {
						operator.add(player.getDisplayName());
					}
					operator.add("*");
				}
			}

			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("leave")) {
					Player[] players = new Player[Bukkit.getServer().getOnlinePlayers().size()];
					Bukkit.getServer().getOnlinePlayers().toArray((Object[]) players);
					for (Player player : players) {
						operator.add(player.getDisplayName());
					}
					operator.add("*");
				}
				if (args[0].equalsIgnoreCase("join1")) {
					Player[] players = new Player[Bukkit.getServer().getOnlinePlayers().size()];
					Bukkit.getServer().getOnlinePlayers().toArray((Object[]) players);
					for (Player player : players) {
						operator.add(player.getDisplayName());
					}
				}
			}

			List<String> arguments = new ArrayList<>();
			for (String argument : all) {
				arguments.add(argument);
			}
			if (((Player) sender).hasPermission("manhunt.operator")) {
				for (String argument : operator) {
					arguments.add(argument);
				}
			}

			for (String argument : arguments) {
				if (argument.toLowerCase().indexOf(args[args.length - 1].toLowerCase()) == 0) {
					res.add(argument);
				}
			}
			Collections.sort(res);
			return res;
		}
		return new ArrayList<String>();
	}
}
