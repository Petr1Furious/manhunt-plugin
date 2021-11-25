package me._furiouspotato_.manhunt.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me._furiouspotato_.manhunt.Main;
import me._furiouspotato_.manhunt.ManhuntPlayer;

public class ManhuntCommand implements CommandExecutor {
	private Main plugin;

	public ManhuntCommand(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;

			if (args.length == 0) {
				return false;
			}

			if (args[0].equalsIgnoreCase("setup")) {
				if (player.hasPermission("manhunt.operator")) {
					if (plugin.gameStatus != -1) {
						sender.sendMessage(ChatColor.RED + "The game is already running!");

						return false;
					} else {
						int gameType = 0;
						if (args.length >= 2) {
							gameType = Integer.valueOf(args[1]) - 1;
						}

						int borderSize = 60000000;
						boolean glowing = false;
						int duration = -1, duration1 = -1, duration2 = -1;
						if (gameType == 1) {
							borderSize = 1000;
							glowing = true;
							duration = 3600;
						}
						if (gameType == 2) {
							borderSize = 1000;
							glowing = true;
							duration = 3600;
							duration1 = 720;
							duration2 = 720;
						}
						if (gameType == 3) {
							borderSize = 100;
							glowing = true;
							duration = 600;
						}

						if (args.length >= 3) {
							duration = Integer.valueOf(args[2]);
						}
						if (args.length >= 4) {
							if (args[3].equalsIgnoreCase("true")) {
								glowing = true;
							}
						}
						if (args.length >= 5) {
							borderSize = Integer.valueOf(args[4]);
						}
						if (args.length >= 6) {
							duration1 = Integer.valueOf(args[5]);
						}
						if (args.length >= 7) {
							duration2 = Integer.valueOf(args[6]);
						}

						plugin.setupGame(gameType, player.getWorld(), borderSize, glowing, duration, duration1,
								duration2);

						return false;
					}
				} else {
					sender.sendMessage(ChatColor.DARK_RED + "You don't have permission!");
					return true;
				}
			}

			if (args[0].equalsIgnoreCase("join")) {
				if (args.length >= 2) {
					if (args[1].equalsIgnoreCase("runner") || args[1].equalsIgnoreCase("hunter")) {
						if (args.length == 2) {
							if (plugin.players.containsKey(player.getName())) {
								plugin.removePlayer(player.getName());
							}
							plugin.addPlayer(player.getName(), args[1].equalsIgnoreCase("hunter"));

							return false;
						}
						if (args.length == 3) {
							if (player.hasPermission("manhunt.operator")) {
								boolean found = false;

								for (Player player2 : Bukkit.getOnlinePlayers()) {
									if (player2.getName().equalsIgnoreCase(args[2]) || args[2].equalsIgnoreCase("*")) {
										if (!args[2].equalsIgnoreCase("*")
												&& plugin.players.containsKey(player2.getName())) {
											plugin.removePlayer(player2.getName());
										}

										if (!plugin.players.containsKey(player2.getName())) {
											plugin.addPlayer(player2.getName(), args[1].equalsIgnoreCase("hunter"));
											found = true;
										}
									}
								}

								if (!found) {
									if (!args[2].equalsIgnoreCase("*")) {
										sender.sendMessage(ChatColor.RED + "The player " + args[2]
												+ " is not found or he is already in game!");
									} else {
										sender.sendMessage(ChatColor.RED + "There are no players not in game!");
									}
								}

								return false;
							} else {
								sender.sendMessage(ChatColor.DARK_RED + "You don't have permission!");
								return true;
							}
						}
					}
				}
			}

			if (args[0].equalsIgnoreCase("join1")) {
				if (player.hasPermission("manhunt.operator")) {
					Player target = player;
					if (args.length == 2) {
						target = Bukkit.getPlayerExact(args[1]);
						if (target == null) {
							sender.sendMessage(ChatColor.RED + "The player " + args[1] + " is not found!");

							return false;
						}
					}

					List<String> names = new ArrayList<String>();
					for (Map.Entry<String, ManhuntPlayer> entry : plugin.players.entrySet()) {
						ManhuntPlayer mplayer = entry.getValue();
						names.add(mplayer.player.getName());
					}

					for (String name : names) {
						plugin.removePlayer(name);
					}

					for (Player player2 : Bukkit.getOnlinePlayers()) {
						if (player2.getName() != target.getName()) {
							plugin.addPlayer(player2.getName(), true);
						} else {
							plugin.addPlayer(player2.getName(), false);
						}
					}

					return false;
				} else {
					sender.sendMessage(ChatColor.DARK_RED + "You don't have permission!");
					return true;
				}
			}

			if (args[0].equalsIgnoreCase("leave")) {
				if (args.length == 1) {
					plugin.removePlayer(player.getName());

					return false;
				}
				if (args.length == 2) {
					if (player.hasPermission("manhunt.operator")) {
						boolean found = false;

						for (Player player2 : Bukkit.getOnlinePlayers()) {
							if (player2.getName().equalsIgnoreCase(args[1]) || args[1].equalsIgnoreCase("*")) {
								if (plugin.players.containsKey(player2.getName())) {
									plugin.removePlayer(player2.getName());
									found = true;
								}
							}
						}

						if (!found) {
							if (!args[1].equalsIgnoreCase("*")) {
								sender.sendMessage(ChatColor.RED + "The player " + args[1] + " is not found!");
							} else {
								sender.sendMessage(ChatColor.RED + "There are no players in game!");
							}
						}

						return false;
					} else {
						sender.sendMessage(ChatColor.DARK_RED + "You don't have permission!");
						return true;
					}
				}
			}

			if (args[0].equalsIgnoreCase("start")) {
				if (args.length == 1) {
					if (player.hasPermission("manhunt.operator")) {
						plugin.startGame();

						return false;
					} else {
						sender.sendMessage(ChatColor.DARK_RED + "You don't have permission!");
						return true;
					}
				}
			}

			if (args[0].equalsIgnoreCase("end")) {
				if (args.length == 1) {
					if (player.hasPermission("manhunt.operator")) {
						if (plugin.gameStatus == -1) {
							sender.sendMessage(ChatColor.RED + "The game is not running.");
						} else {
							plugin.endGame(-1);
						}

						return false;
					} else {
						sender.sendMessage(ChatColor.DARK_RED + "You don't have permission!");
						return true;
					}
				}
			}

			if (args[0].equalsIgnoreCase("list")) {
				if (args.length == 1) {
					if (plugin.players.isEmpty()) {
						sender.sendMessage(ChatColor.GOLD + "There are no players in game.");
					} else {
						sender.sendMessage(ChatColor.GOLD + "Players in game:");
						String res = new String();
						for (Map.Entry<String, ManhuntPlayer> entry : plugin.players.entrySet()) {
							res += plugin.getColoredName(entry.getKey()) + ChatColor.GOLD + ", ";
						}
						res = res.substring(0, (int) res.length() - 2);
						res += ChatColor.GOLD + ".";
						sender.sendMessage(ChatColor.GOLD + res);
					}

					return false;
				}
			}

			sender.sendMessage(ChatColor.DARK_RED + "Wrong command syntax!");

			return true;
		}

		return false;
	}
}
