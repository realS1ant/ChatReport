package me.fatpigsarefat.chatreport.events;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.fatpigsarefat.chatreport.ChatReport;
import me.fatpigsarefat.chatreport.commands.ChatreportCommand;
import me.fatpigsarefat.chatreport.utils.Report;
import net.md_5.bungee.api.ChatColor;

public class EventInventoryClick implements Listener {

	@EventHandler
	public void onPlayerChat(InventoryClickEvent event) {
		if (event.getInventory().getName().contains(ChatColor.RESET.toString() + "Chat Report: #")) {
			String id = ChatColor.stripColor(event.getInventory().getName().replace("Chat Report: #", ""));
			Report r = ChatReport.getReportManager().getReport(id);
			event.setCancelled(true);
			if (event.getSlot() == 1) {
				event.getWhoClicked().sendMessage(ChatColor.AQUA + "== Viewing chat history: " + r.getPlayerName());
				for (String s : r.getChatHistory().getChatMessages()) {
					event.getWhoClicked().sendMessage(ChatColor.RED + r.getPlayerName() + ": " + ChatColor.WHITE + s);
				}
				event.getWhoClicked().sendMessage(ChatColor.AQUA + "== End of chat history: " + r.getPlayerName());
				event.getWhoClicked().closeInventory();
			}
			if (event.getSlot() == 8) {
				Player player = (Player) event.getWhoClicked();
				for (Player online : Bukkit.getOnlinePlayers()) {
					if (online.hasPermission("chatreport.view")) {
						online.sendMessage(ChatColor.AQUA + event.getWhoClicked().getName() + " deleted chat report #" + id + " (" + r.getPlayerName() + ")");
					}
				}
				ChatReport.getReportManager().deleteReport(id);
				if (ChatReport.isUsingDatabase()) {
					ChatReport.getReportManager().push();
				}
				player.closeInventory();
			}
		} else if (event.getInventory().getName().contains(ChatColor.RESET.toString() + "Chat Report: ") && ChatReport.getReportManager().isReporting(event.getWhoClicked().getName())) {
			event.setCancelled(true);
			if (event.getCurrentItem() == null) {
				return;
			}
			for (String s : ChatReport.getInstance().getConfig().getConfigurationSection("report-reasons").getKeys(false)) {
				ItemStack is = new ItemStack(Material.getMaterial(ChatReport.getInstance().getConfig().getString("report-reasons." + s + ".itemstack.type")));
				ItemMeta ism = is.getItemMeta();
				ism.setDisplayName(ChatColor.translateAlternateColorCodes('&', ChatColor.RED + ChatReport.getInstance().getConfig().getString("report-reasons." + s + ".name")));
				List<String> isl = new ArrayList<String>();
				for (String lore : ChatReport.getInstance().getConfig().getStringList("report-reasons." + s + ".itemstack.lore")) {
					isl.add(ChatColor.translateAlternateColorCodes('&', lore));
				}
				ism.setLore(isl);
				is.setItemMeta(ism);
				if (event.getCurrentItem().equals(is)) {
					Player player = (Player) event.getWhoClicked();
					Report r = ChatReport.getReportManager().createReport(ChatReport.getReportManager().getReporting(event.getWhoClicked().getName()), ChatReport.getInstance().getConfig().getString("report-reasons." + s + ".name"));
					if (ChatReport.isUsingDatabase()) {
						ChatReport.getReportManager().push();
					}
					String messageToModerator = ChatColor.translateAlternateColorCodes('&', ChatReport.getInstance().getConfig().getString("messages.report-created-moderator"));
					messageToModerator = messageToModerator.replace("%reporter%", event.getWhoClicked().getName());
					messageToModerator = messageToModerator.replace("%id%", r.getId());
					messageToModerator = messageToModerator.replace("%player%", ChatReport.getReportManager().getReporting(event.getWhoClicked().getName()));
					messageToModerator = messageToModerator.replace("%reason%", ChatReport.getInstance().getConfig().getString("report-reasons." + s + ".name"));
					messageToModerator = messageToModerator.replace("%command%", "/chatreport view " + r.getId());
					String[] messageToModeratorSplit = messageToModerator.split("\n");
					for (Player online : Bukkit.getOnlinePlayers()) {
						if (online.hasPermission("chatreport.view")) {
							for (String toMod : messageToModeratorSplit) {
								if (toMod.contains("%button%") && ChatReport.isChatSupported()) {
									String[] nmsSplit = toMod.split("%button%");
									ChatReport.getChat().sendChatCommand(online, "", "chatreport view " + r.getId(), nmsSplit[0] + "[View Report]" + nmsSplit[1], "");
								} else {
									online.sendMessage(ChatColor.translateAlternateColorCodes('&', toMod));
								}
							}
						}
					}
					String messageToPlayer = ChatColor.translateAlternateColorCodes('&', ChatReport.getInstance().getConfig().getString("messages.report-created"));
					messageToPlayer = messageToPlayer.replace("%player%", ChatReport.getReportManager().getReporting(event.getWhoClicked().getName()));
					messageToPlayer = messageToPlayer.replace("%id%", r.getId());
					player.sendMessage(messageToPlayer);
					ChatReport.getReportManager().setReporting(event.getWhoClicked().getName(), null);
					player.closeInventory();
					break;
				}
			}
		} else if (event.getInventory().getName().contains(ChatColor.RESET.toString() + "Chat Reports")) {
			event.setCancelled(true);
			if (event.getCurrentItem() == null) {
				return;
			}
			for (Report r : ChatReport.getReportManager().getReports()) {
				String configKey = "";
				for (String s : ChatReport.getInstance().getConfig().getConfigurationSection("report-reasons").getKeys(false)) {
					if (ChatReport.getInstance().getConfig().getString("report-reasons." + s + ".name").equals(r.getReason())) {
						configKey = s;
					}
				}
				ItemStack is = new ItemStack(Material.getMaterial(configKey == "" ? "STONE" : ChatReport.getInstance().getConfig().getString("report-reasons." + configKey + ".itemstack.type")));
				ItemMeta ism = is.getItemMeta();
				ism.setDisplayName(ChatColor.RED + "#" + r.getId());
				List<String> isl = new ArrayList<String>();
				isl.add(ChatColor.GRAY + " * Reason: " + ChatColor.RED + r.getReason());
				SimpleDateFormat sdf = new SimpleDateFormat("d/M/y");
		        Date now = new Date();
				isl.add(ChatColor.GRAY + " * Created on: " + ChatColor.RED + sdf.format(r.getCreationDate()));
				isl.add(ChatColor.GRAY + " * Time ago: " + ChatColor.RED  + TimeUnit.MILLISECONDS.toHours(now.getTime() - r.getCreationDate().getTime()) + "h");
				isl.add(ChatColor.GRAY + " * Username: " + ChatColor.RED + r.getPlayerName());
				isl.add(" ");
				isl.add(ChatColor.GRAY + "Left-click to view");
				ism.setLore(isl);
				is.setItemMeta(ism);
				if (is.equals(event.getCurrentItem())) {
					Player player = (Player) event.getWhoClicked();
					player.closeInventory();
					ChatreportCommand.openChatReport(player, r.getId());
				}
 			}
		}
	}
}
