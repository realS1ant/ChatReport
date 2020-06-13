package me.fatpigsarefat.chatreport.commands;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import me.fatpigsarefat.chatreport.utils.ArchiveManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import me.fatpigsarefat.chatreport.ChatReport;
import me.fatpigsarefat.chatreport.utils.Report;
import net.md_5.bungee.api.ChatColor;

public class ChatreportCommand implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("chatreport")) {
			
			if (args.length == 0) {
				sender.sendMessage(ChatColor.AQUA + "/chatreport <player>");
				if (sender.hasPermission("chatreport.view")) {
					sender.sendMessage(ChatColor.AQUA.toString() + ChatColor.BOLD + "ADMIN " + ChatColor.AQUA + "/chatreport view [id]");
					sender.sendMessage(ChatColor.AQUA.toString() + ChatColor.BOLD + "ADMIN " + ChatColor.AQUA + "/chatreport archive [id]");
				}
			}
			
			if (args.length >= 1) {
				if (!(sender instanceof Player)) {
					sender.sendMessage("You need to be a player to create chat reports.");
					return true;
				}
				Player player = (Player) sender;
				if (player.hasPermission("chatreport.view")) {
					if (args[0].equals("view")) {
						if (ChatReport.isUsingDatabase()) {
							new BukkitRunnable() {

								@Override
								public void run() {
									player.sendMessage(ChatColor.GREEN + "Synchronising reports...");
									ChatReport.getReportManager().pullMainThread();
									commandCont(args, player);
								}
							
							}.runTaskAsynchronously(ChatReport.getInstance());
						} else {
							commandCont(args, player);
						}
						return true;
					}
					if (args[0].equals("archive")) {
						if (args.length == 1) {
							Bukkit.getScheduler().runTaskAsynchronously(ChatReport.getInstance(), () -> {
								player.sendMessage(ChatColor.GREEN + "Synchronising archived reports...");
								ArchiveManager.pullMainThread();
								openArchive(player);
							});
						} else {
							if (ChatReport.getReportManager().getReport(args[1]) == null) {
								player.sendMessage(ChatColor.RED + "No report was found by the id #" + args[1]);
							} else {
								Report r = ChatReport.getReportManager().getReport(args[1]);
								for (Player online : Bukkit.getOnlinePlayers()) {
									if (online.hasPermission("chatreport.view")) {
										online.sendMessage(ChatColor.AQUA + player.getName() + " archived chat report #" + args[1].toUpperCase() + " (" + r.getPlayerName() + ")");
									}
								}
								ChatReport.getReportManager().deleteReport(args[1]);
							}
						}
						return true;
					}
				}
 				if (ChatReport.getReportManager().getChatHistory(args[0]) == null) {
					sender.sendMessage(ChatColor.RED + "That player has no chat history (capitalization matters)");
					return true;
				}
				Inventory inv = Bukkit.createInventory(null, ChatReport.getInstance().getConfig().getInt("inventory.size"), ChatColor.RESET.toString() + "Chat Report: " + args[0]);
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
					inv.addItem(is);
				}
				player.openInventory(inv);
				ChatReport.getReportManager().setReporting(player.getName(), args[0]);
			}
			
			return true;
		}
		return false;
	}
	
	private void commandCont(String[] args, Player player) {
		if (args.length == 1) {
			Inventory inv = Bukkit.createInventory(null, 54, ChatColor.RESET.toString() + "Chat Reports");
			ArrayList<Report> reports = ChatReport.getReportManager().getReports();
			if(reports.size()>53){
				ItemStack is = new ItemStack(Material.valueOf("RED_WOOL"));
				ItemMeta ism = is.getItemMeta();
				ism.setDisplayName(ChatColor.DARK_RED + "OVERFLOW");
				ArrayList<String> lore = new ArrayList<>();
				lore.add(ChatColor.RED + "" + (reports.size()-53) + ChatColor.GRAY + " reports in queue");
				ism.setLore(lore);
				is.setItemMeta(ism);
				inv.addItem(is);
			}
			for (Report r : reports.subList(Math.max(reports.size() - 53, 0), reports.size())) {
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
				inv.addItem(is);
			}
			player.openInventory(inv);
		} else {
			if (ChatReport.getReportManager().getReport(args[1]) == null) {
				player.sendMessage(ChatColor.RED + "No report was found by the id #" + args[1]);
			} else {
				openChatReport(player, args[1]);
			}
		}
	}

	private void openArchive(Player player){
		Inventory inv = Bukkit.createInventory(null, 54, ChatColor.RESET.toString() + "Archived Chat Reports");
		for (Report r : ArchiveManager.getArchivedReports()) {
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
			inv.addItem(is);
		}
		player.openInventory(inv);
	}

	public static void openChatReport(Player player, String id) {
		Report r = ChatReport.getReportManager().getReport(id);
		Inventory inv = Bukkit.createInventory(null, 9, ChatColor.RESET.toString() + "Chat Report: #" + r.getId());
		String configKey = "";
		for (String s : ChatReport.getInstance().getConfig().getConfigurationSection("report-reasons").getKeys(false)) {
			if (ChatReport.getInstance().getConfig().getString("report-reasons." + s + ".name").equals(r.getReason())) {
				configKey = s;
			}
		}
		ItemStack is = new ItemStack(Material.getMaterial(configKey == "" ? "STONE" : ChatReport.getInstance().getConfig().getString("report-reasons." + configKey + ".itemstack.type")));
		ItemMeta ism = is.getItemMeta();
		ism.setDisplayName(ChatColor.RED + "Details:");
		List<String> isl = new ArrayList<String>();
		isl.add(ChatColor.GRAY + " * Reason: " + ChatColor.RED + r.getReason());
		SimpleDateFormat sdf = new SimpleDateFormat("d/M/y");
        Date now = new Date();
		isl.add(ChatColor.GRAY + " * Created on: " + ChatColor.RED + sdf.format(r.getCreationDate()));
		isl.add(ChatColor.GRAY + " * Time ago: " + ChatColor.RED  + TimeUnit.MILLISECONDS.toHours(now.getTime() - r.getCreationDate().getTime()) + "h");
		isl.add(ChatColor.GRAY + " * Username: " + ChatColor.RED + r.getPlayerName());
		isl.add(" ");
		ism.setLore(isl);
		is.setItemMeta(ism);
		inv.setItem(0, is);
		
		ItemStack delete = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 14);
		ItemMeta deletem = delete.getItemMeta();
		deletem.setDisplayName(ChatColor.RED + "Archive Report");
		delete.setItemMeta(deletem);
		inv.setItem(8, delete);
		
		ItemStack view = new ItemStack(Material.PAPER);
		ItemMeta viewm = view.getItemMeta();
		viewm.setDisplayName(ChatColor.RED + "View Report");
		List<String> viewl = new ArrayList<String>();
		viewl.add(ChatColor.GRAY + "Click to view the player's chat logs.");
		viewm.setLore(viewl);
		view.setItemMeta(viewm);
		inv.setItem(1, view);
		
		player.openInventory(inv);
	}

	public static void openArchivedChatReport(Player player, String id) {
		System.out.println("opening here");
		Report r = ArchiveManager.getReport(id);
		Inventory inv = Bukkit.createInventory(null, 9, ChatColor.RESET.toString() + "Archived Chat Report: #" + r.getId());
		String configKey = "";
		for (String s : ChatReport.getInstance().getConfig().getConfigurationSection("report-reasons").getKeys(false)) {
			if (ChatReport.getInstance().getConfig().getString("report-reasons." + s + ".name").equals(r.getReason())) {
				configKey = s;
			}
		}
		ItemStack is = new ItemStack(Material.getMaterial(configKey == "" ? "STONE" : ChatReport.getInstance().getConfig().getString("report-reasons." + configKey + ".itemstack.type")));
		ItemMeta ism = is.getItemMeta();
		ism.setDisplayName(ChatColor.RED + "Details:");
		List<String> isl = new ArrayList<String>();
		isl.add(ChatColor.GRAY + " * Reason: " + ChatColor.RED + r.getReason());
		SimpleDateFormat sdf = new SimpleDateFormat("d/M/y");
		Date now = new Date();
		isl.add(ChatColor.GRAY + " * Created on: " + ChatColor.RED + sdf.format(r.getCreationDate()));
		isl.add(ChatColor.GRAY + " * Time ago: " + ChatColor.RED  + TimeUnit.MILLISECONDS.toHours(now.getTime() - r.getCreationDate().getTime()) + "h");
		isl.add(ChatColor.GRAY + " * Username: " + ChatColor.RED + r.getPlayerName());
		isl.add(" ");
		ism.setLore(isl);
		is.setItemMeta(ism);
		inv.setItem(0, is);

		ItemStack delete = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 14);
		ItemMeta deletem = delete.getItemMeta();
		deletem.setDisplayName(ChatColor.RED + "Delete Report");
		delete.setItemMeta(deletem);
		inv.setItem(8, delete);

		ItemStack view = new ItemStack(Material.PAPER);
		ItemMeta viewm = view.getItemMeta();
		viewm.setDisplayName(ChatColor.RED + "View Report");
		List<String> viewl = new ArrayList<String>();
		viewl.add(ChatColor.GRAY + "Click to view the player's chat logs.");
		viewm.setLore(viewl);
		view.setItemMeta(viewm);
		inv.setItem(1, view);

		player.openInventory(inv);
	}
}