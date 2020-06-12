package me.fatpigsarefat.chatreport.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import me.fatpigsarefat.chatreport.ChatReport;

public class EventInventoryClose implements Listener {

	@EventHandler
	public void onPlayerChat(InventoryCloseEvent event) {
		if (ChatReport.getReportManager().isReporting(event.getPlayer().getName())) {
			ChatReport.getReportManager().setReporting(event.getPlayer().getName(), null);
		}
	}
}
