package me.fatpigsarefat.chatreport.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.fatpigsarefat.chatreport.ChatReport;

public class EventPlayerChat implements Listener {

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (!event.isCancelled()) {
			ChatReport.getReportManager().addChatHistory(event.getPlayer().getName(), event.getMessage());
		}
	}
}
