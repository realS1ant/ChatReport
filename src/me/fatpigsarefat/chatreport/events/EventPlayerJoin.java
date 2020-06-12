package me.fatpigsarefat.chatreport.events;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.fatpigsarefat.chatreport.ChatReport;

public class EventPlayerJoin implements Listener {

	@EventHandler
	public void onPlayerChat(PlayerJoinEvent event) {
		if (event.getPlayer().hasPermission("chatreport.view")) {
			if (!ChatReport.getReportManager().getReports().isEmpty()) {
				String message = ChatColor.translateAlternateColorCodes('&', ChatReport.getInstance().getConfig().getString("messages.report-unread")).replace("%amount%", String.valueOf(ChatReport.getReportManager().getReports().size()));

				new BukkitRunnable() {
					
					@Override
					public void run() {
						event.getPlayer().sendMessage(message);
					}
					
				}.runTaskLater(ChatReport.getInstance(), 10L);
			}
		}
	}
}
