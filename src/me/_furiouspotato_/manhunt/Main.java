package me._furiouspotato_.manhunt;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import me._furiouspotato_.manhunt.commands.ManhuntCommand;
import me._furiouspotato_.manhunt.tabcompleters.ManhuntCompleter;
import me._furiouspotato_.manhunt.ManhuntPlayer;
import me._furiouspotato_.manhunt.ManhuntPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Main extends JavaPlugin implements Listener {
	boolean showY;

	boolean overworldCompass;
	boolean netherCompass;
	boolean endCompass;

	ItemStack[][] hunterInv;
	ItemStack[][] runnerInv;

	//

	public HashMap<String, ManhuntPlayer> players;
	public int gameStatus;

	int gameType;

	boolean invincibility;

	World world;

	int borderSize;
	boolean enableGlowing;
	int duration, duration1, duration2;

	String[] nicknames;

	@Override
	public void onEnable() {
		showY = false;

		overworldCompass = true;
		netherCompass = true;
		endCompass = false;

		hunterInv = new ItemStack[4][41];
		hunterInv[0][0] = new ItemStack(Material.COMPASS, 1);

		runnerInv = new ItemStack[4][41];

		for (int i = 0; i < 40; i++) {
			runnerInv[3][i] = new ItemStack(Material.STONE, 64);
		}
		runnerInv[3][0] = new ItemStack(Material.IRON_SWORD, 1);
		runnerInv[3][1] = new ItemStack(Material.DIAMOND_PICKAXE, 1);
		runnerInv[3][1].addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);
		runnerInv[3][7] = new ItemStack(Material.COOKED_PORKCHOP, 64);
		runnerInv[3][8] = new ItemStack(Material.WATER_BUCKET, 1);
		runnerInv[3][36] = new ItemStack(Material.IRON_BOOTS, 1);
		runnerInv[3][36].addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 32767);
		runnerInv[3][37] = new ItemStack(Material.IRON_LEGGINGS, 1);
		runnerInv[3][38] = new ItemStack(Material.IRON_CHESTPLATE, 1);
		runnerInv[3][39] = new ItemStack(Material.IRON_HELMET, 1);
		{
			int makeUnbreakable[] = { 0, 1, 36, 37, 38, 39 };
			for (int i : makeUnbreakable) {
				ItemMeta itemMeta = runnerInv[3][i].getItemMeta();
				itemMeta.setUnbreakable(true);
				runnerInv[3][i].setItemMeta(itemMeta);
			}
		}
		hunterInv[3] = runnerInv[3].clone();
		hunterInv[3][6] = new ItemStack(Material.COMPASS, 1);
		hunterInv[3][36] = new ItemStack(Material.DIAMOND_BOOTS, 1);
		hunterInv[3][36].addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 32767);
		hunterInv[3][37] = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
		hunterInv[3][38] = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
		hunterInv[3][39] = new ItemStack(Material.DIAMOND_HELMET, 1);
		{
			int makeUnbreakable[] = { 0, 1, 36, 37, 38, 39 };
			for (int i : makeUnbreakable) {
				ItemMeta itemMeta = runnerInv[3][i].getItemMeta();
				itemMeta.setUnbreakable(true);
				hunterInv[3][i].setItemMeta(itemMeta);
			}
		}
		runnerInv[3][36].removeEnchantment(Enchantment.PROTECTION_FALL);

		//

		getServer().getPluginManager().registerEvents(this, (Plugin) this);
		getCommand("manhunt").setExecutor(new ManhuntCommand(this));
		getCommand("manhunt").setTabCompleter(new ManhuntCompleter(this));

		players = new HashMap<String, ManhuntPlayer>();
		gameStatus = -1;
	}

	public void setupGame(int gameType, World world, int borderSize, boolean enableGlowing, int duration, int duration1,
			int duration2) {
		if (gameStatus != -1) {
			return;
		}
		gameStatus = 2;

		this.world = world;

		this.gameType = gameType;

		this.borderSize = borderSize;
		this.enableGlowing = enableGlowing;

		this.duration = duration;
		this.duration1 = duration1;
		this.duration2 = duration2;

		players.clear();
		for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
			player.sendMessage(ChatColor.GOLD + "New Manhunt game is going to start soon!");
			player.sendMessage(ChatColor.GOLD + "Type " + ChatColor.BLUE + ChatColor.BOLD + "/manhunt join <role>"
					+ ChatColor.RESET + ChatColor.GOLD + " to join.");
		}
	}

	public String getColoredName(String name) {
		if (!players.containsKey(name)) {
			return "";
		}
		if (!players.get(name).isHunter) {
			return ChatColor.AQUA + name;
		} else {
			return ChatColor.RED + name;
		}
	}

	public void addPlayer(String playerName, boolean isHunter) {
		if (gameStatus == -1) {
			return;
		}

		if (!players.containsKey(playerName) && Bukkit.getPlayerExact(playerName) != null) {
			Player player = Bukkit.getPlayerExact(playerName);
			players.put(playerName, new ManhuntPlayer(player, this));
			players.get(playerName).isHunter = isHunter;
			updateNicknames();

			String message;
			if (!isHunter) {
				message = ChatColor.AQUA + "the runner";
			} else {
				message = ChatColor.RED + "the hunter";
			}

			for (Player player2 : (List<Player>) Bukkit.getOnlinePlayers()) {
				player2.sendMessage(ChatColor.GOLD + "Player " + getColoredName(playerName) + ChatColor.GOLD
						+ " has joined the game as " + message + ChatColor.GOLD + ".");
			}

			if (gameStatus == 0) {
				player.setGameMode(GameMode.SURVIVAL);
				players.get(playerName).justJoined = true;
				player.setHealth(0.0);
			}
		}
	}

	public void removePlayer(String playerName) {
		if (gameStatus == -1) {
			return;
		}

		if (players.containsKey(playerName)) {
			for (Player player2 : (List<Player>) Bukkit.getOnlinePlayers()) {
				player2.sendMessage(ChatColor.GOLD + "Player " + getColoredName(playerName) + ChatColor.GOLD
						+ " has left the game.");
			}

			players.remove(playerName);
			updateNicknames();
		}
	}

	public void startGame() {
		if (gameStatus != 2) {
			return;
		}

		gameStatus = 0;

		world.getWorldBorder().setSize(borderSize);
		world.getWorldBorder().setCenter(world.getSpawnLocation());

		for (Map.Entry<String, ManhuntPlayer> entry : players.entrySet()) {
			ManhuntPlayer mplayer = entry.getValue();

			mplayer.wasCaught = false;
			mplayer.player.setGameMode(GameMode.SURVIVAL);
			mplayer.justJoined = true;
			mplayer.player.setHealth(0.0);
			mplayer.player.spigot().respawn();
			/*
			 * mplayer.player.setSaturation(5.0F); mplayer.player.setFoodLevel(20);
			 * mplayer.player.setFallDistance(0); mplayer.player.setVelocity(new Vector(0,
			 * 0, 0)); mplayer.player.setRemainingAir(mplayer.player.getMaximumAir());
			 * mplayer.player.teleport(mplayer.player.getWorld().getHighestBlockAt(mplayer.
			 * player.getLocation()) .getLocation().add(0, 2, 0));
			 */
			if (!mplayer.isHunter) {
				mplayer.player.getInventory().setContents(runnerInv[gameType]);
			} else {
				mplayer.player.getInventory().setContents(hunterInv[gameType]);
			}
			for (PotionEffect effect : mplayer.player.getActivePotionEffects())
				mplayer.player.removePotionEffect(effect.getType());
			if (!mplayer.isHunter && enableGlowing) {
				mplayer.player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 2147483647, 0, false, false));
			}
		}

		for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
			if (!players.containsKey(player.getName())) {
				player.setGameMode(GameMode.SPECTATOR);
			}
		}

		for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
			player.sendMessage(ChatColor.GOLD + "The Manhunt game is starting now!");
		}

		invincibility = false;

		Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
			int phase = 0;
			int gameTimer = duration + 1;
			int timer = duration1 + 1;

			@Override
			public void run() {
				if (duration >= 0) {
					gameTimer--;
					if (gameTimer == 0) {
						endGame(0);
						return;
					}
					if (gameTimer > 0 && (gameTimer == duration || gameTimer % 300 == 0 || gameTimer <= 10
							|| gameTimer <= 300 && gameTimer % 60 == 0 || gameTimer == 30)) {
						String message;
						if (gameTimer % 60 == 0) {
							int minutes = gameTimer / 60;
							if (minutes > 1) {
								message = ChatColor.GOLD + String.valueOf(minutes) + " minutes left.";
							} else {
								message = ChatColor.GOLD + "1 minute left.";
							}
						} else {
							message = ChatColor.GOLD + String.valueOf(gameTimer) + " seconds left.";
						}
						for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
							player.sendMessage(message);
						}
					}
				}

				if (duration1 >= 0 && duration2 >= 0) {
					timer--;
					if (timer == 0) {
						if (phase == 0) {
							phase = 1;
							timer = duration2;
							enableInvincibility();
						} else {
							phase = 0;
							timer = duration1;
							disableInvincibility();
						}
					}
					for (Map.Entry<String, ManhuntPlayer> entry : players.entrySet()) {
						ManhuntPlayer mplayer = entry.getValue();

						mplayer.player.setInvulnerable(invincibility);
					}
				}
			}
		}, 0L, 20L);
	}

	void enableInvincibility() {
		invincibility = true;
		for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
			player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "All players are now invincible!");
		}
	}

	void disableInvincibility() {
		invincibility = false;
		for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
			player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "All players are no longer invincible!");
		}
	}

	public void endGame(int result) {
		if (gameStatus == -1) {
			return;
		}

		if (result == -1) {
			for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
				player.sendMessage(ChatColor.GOLD + "The Manhunt game has been finished.");
			}
		}

		int cntHunters = 0, cntRunners = 0;
		for (Map.Entry<String, ManhuntPlayer> entry : players.entrySet()) {
			ManhuntPlayer mplayer = entry.getValue();
			if (!mplayer.isHunter) {
				cntRunners++;
			} else {
				cntHunters++;
			}
		}

		if (result == 0) {
			String message1, message2;
			if (cntRunners == 1) {
				message1 = "The Speedrunner ";
				message2 = "has won!";
			} else {
				message1 = "The Speedrunners ";
				message2 = "have won!";
			}
			for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
				player.sendMessage(ChatColor.AQUA + message1 + ChatColor.GOLD + message2);
			}
		}
		if (result == 1) {
			String message1, message2;
			if (cntHunters == 1) {
				message1 = "The Hunter ";
				message2 = "has won!";
			} else {
				message1 = "The Hunters ";
				message2 = "have won!";
			}
			for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
				player.sendMessage(ChatColor.RED + message1 + ChatColor.GOLD + message2);
			}
		}

		for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
			if (player.getGameMode() == GameMode.SPECTATOR) {
				player.setGameMode(GameMode.SURVIVAL);
				player.teleport(player.getWorld().getHighestBlockAt(player.getLocation()).getLocation().add(0, 2, 0));
			}
			player.setInvulnerable(false);
			player.removePotionEffect(PotionEffectType.GLOWING);
		}

		if (result == -1) {
			gameStatus = -1;
			players.clear();
		} else {
			gameStatus = 2;
		}

		world.getWorldBorder().setCenter(0, 0);
		world.getWorldBorder().setSize(60000000);

		Bukkit.getScheduler().cancelTasks(this);
	}

	void updateNicknames() {
		int newSize = 0;
		for (Map.Entry<String, ManhuntPlayer> entry : players.entrySet()) {
			ManhuntPlayer mplayer = entry.getValue();
			if (!mplayer.isHunter) {
				newSize++;
			}
		}

		nicknames = new String[newSize];
		int counter = 0;
		for (Map.Entry<String, ManhuntPlayer> entry : players.entrySet()) {
			ManhuntPlayer mplayer = entry.getValue();
			if (!mplayer.isHunter) {
				nicknames[counter++] = mplayer.player.getName();
			}
		}
		Arrays.sort(nicknames);
	}

	void updateCompass(ManhuntPlayer mplayer) {
		if (!overworldCompass && mplayer.player.getWorld().getEnvironment() == Environment.NORMAL) {
			mplayer.player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
					TextComponent.fromLegacyText(ChatColor.GOLD + "Compass does not work in the overworld!"));
			return;
		}
		if (!netherCompass && mplayer.player.getWorld().getEnvironment() == Environment.NETHER) {
			mplayer.player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
					TextComponent.fromLegacyText(ChatColor.GOLD + "Compass does not work in the nether!"));
			return;
		}
		if (!endCompass && mplayer.player.getWorld().getEnvironment() == Environment.THE_END) {
			mplayer.player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
					TextComponent.fromLegacyText(ChatColor.GOLD + "Compass does not work in the end!"));
			return;
		}
		if (Bukkit.getPlayerExact(mplayer.targetNickname) != null
				&& Bukkit.getPlayerExact(mplayer.targetNickname).getWorld() == mplayer.player.getWorld()
				&& players.containsKey(mplayer.targetNickname)) {
			String message = ChatColor.GOLD + "Pointing at " + ChatColor.AQUA + mplayer.targetNickname + ChatColor.GOLD;
			if (showY) {
				message += " (y = " + String.valueOf(players.get(mplayer.targetNickname).player.getLocation().getY())
						+ ")";
			}
			mplayer.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));

			Inventory inv = mplayer.player.getInventory();
			for (int i = 0; i < 41; i++) {
				if (inv.getContents()[i] != null && inv.getContents()[i].getType() == Material.COMPASS) {
					CompassMeta cm = (CompassMeta) inv.getContents()[i].getItemMeta();
					cm.setLodestone(Bukkit.getPlayerExact(mplayer.targetNickname).getLocation());
					cm.setLodestoneTracked(false);
					inv.getContents()[i].setItemMeta(cm);
				}
			}
		} else {
			String message = ChatColor.GOLD + "Pointing at the last location of " + ChatColor.AQUA
					+ mplayer.targetNickname + ChatColor.GOLD;
			if (showY) {
				message += " (y = " + String.valueOf(players.get(mplayer.targetNickname).player.getLocation().getY())
						+ ")";
			}
			mplayer.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
		}
	}

	void updateCompassTarget(ManhuntPlayer mplayer) {
		int id = nicknames.length - 1;
		for (int i = 0; i < nicknames.length; i++) {
			if (nicknames[i] == mplayer.targetNickname) {
				id = i;
				break;
			}
		}

		id = (id + 1) % nicknames.length;
		for (int i = 0; i < nicknames.length; i++) {
			if (Bukkit.getPlayerExact(nicknames[id]) != null
					&& players.get(nicknames[id]).player.getWorld() == mplayer.player.getWorld()) {
				mplayer.targetNickname = nicknames[id];
				updateCompass(mplayer);
				return;
			}
			id = (id + 1) % nicknames.length;
		}

		mplayer.player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
				TextComponent.fromLegacyText(ChatColor.GOLD + "No runners found in the same world."));
	}

	public boolean checkAllCaught() {
		boolean allRunnersCaught = true;
		for (Map.Entry<String, ManhuntPlayer> entry : players.entrySet()) {
			ManhuntPlayer tplayer = entry.getValue();
			if (!tplayer.isHunter) {
				if (tplayer.wasCaught == false) {
					allRunnersCaught = false;
				}
			}
		}

		return allRunnersCaught;
	}

	void killPlayer(ManhuntPlayer mplayer) {
		if (!mplayer.isHunter) {
			mplayer.player.setGameMode(GameMode.SPECTATOR);
			mplayer.wasCaught = true;
		}

		if (checkAllCaught()) {
			endGame(1);
		}
	}

	@EventHandler
	public void onDeath(EntityDeathEvent e) {
		if (gameStatus != 0)
			return;
		if (!(e.getEntity() instanceof Player))
			return;
		Player entity = (Player) e.getEntity();
		String entityName = entity.getName();
		if (!players.containsKey(entityName) || players.get(entityName).wasCaught) {
			entity.setGameMode(GameMode.SPECTATOR);
			entity.teleport(entity.getWorld().getHighestBlockAt(entity.getLocation()).getLocation().add(0, 20, 0));
			return;
		}

		if (!players.get(entityName).justJoined) {
			String curColor = "" + ChatColor.AQUA;
			if (players.get(entityName).isHunter)
				curColor = "" + ChatColor.RED;

			for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
				player.sendMessage(curColor + entityName + ChatColor.GOLD + " has died!");
			}

			killPlayer(players.get(entityName));
		} else {
			players.get(entityName).justJoined = false;
		}
	}

	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent e) {
		if (gameStatus != 0)
			return;
		if (!(e.getEntity() instanceof EnderDragon))
			return;

		LivingEntity entity = e.getEntity();
		String message;
		if (!(entity.getKiller() instanceof Player) || !players.containsKey(entity.getKiller().getName())) {
			message = ChatColor.WHITE + entity.getKiller().getName();
		} else if (!players.get(entity.getKiller().getName()).isHunter) {
			message = ChatColor.AQUA + entity.getKiller().getName();
		} else {
			message = ChatColor.RED + entity.getKiller().getName();
		}
		for (Player player : (List<Player>) Bukkit.getOnlinePlayers()) {
			player.sendMessage(
					ChatColor.DARK_PURPLE + "The Ender Dragon " + ChatColor.GOLD + "was killed by " + message);
		}
		endGame(0);
	}

	@EventHandler
	public void onClick(PlayerInteractEvent e) {
		if (gameStatus != 0)
			return;
		if (!players.containsKey(e.getPlayer().getName()) || !players.get(e.getPlayer().getName()).isHunter)
			return;

		Player p = e.getPlayer();
		if (e.getMaterial() != Material.COMPASS)
			return;
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			updateCompassTarget(players.get(p.getName()));
		} else if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
			updateCompass(players.get(p.getName()));
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		if (gameStatus != 0)
			return;
		if (!players.containsKey(e.getPlayer().getName()) || !players.get(e.getPlayer().getName()).isHunter)
			return;

		ManhuntPlayer mplayer = players.get(e.getPlayer().getName());

		if (!mplayer.isHunter) {
			mplayer.player.getInventory().setContents(runnerInv[gameType]);
			if (enableGlowing) {
				mplayer.player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 2147483647, 0, false, false));
			}
		} else {
			mplayer.player.getInventory().setContents(hunterInv[gameType]);
		}
	}

	@EventHandler
	private void onPlayerLogin(PlayerLoginEvent e) {
		if (gameStatus == -1)
			return;
		if (!players.containsKey(e.getPlayer().getName())) {
			e.getPlayer().setGameMode(GameMode.SPECTATOR);
			e.getPlayer().teleport(world.getHighestBlockAt(e.getPlayer().getLocation()).getLocation().add(0, 20, 0));
		} else {
			players.get(e.getPlayer().getName()).player = e.getPlayer();
		}
	}
}
