package me._furiouspotato_.manhunt;

import org.bukkit.entity.Player;

public class ManhuntPlayer {
	Main plugin;

	public Player player;
	public boolean isHunter;
	public boolean wasCaught;
	public String targetNickname;
	public boolean justJoined;

	ManhuntPlayer(Player player, Main plugin) {
		this.plugin = plugin;
		this.player = player;
		this.isHunter = false;
		this.wasCaught = false;
		this.targetNickname = "";
		this.justJoined = false;
	}
}
