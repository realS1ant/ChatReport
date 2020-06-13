package me.fatpigsarefat.chatreport.utils;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Pattern;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import me.fatpigsarefat.chatreport.ChatReport;

public class ReportManager {

	private ArrayList<Report> reports = new ArrayList<Report>();
	private ArrayList<ChatHistory> chatHistory = new ArrayList<ChatHistory>();
	private HashMap<String, String> reporting = new HashMap<String, String>();

	public ChatHistory getChatHistory(String player) {
		for (ChatHistory ch : chatHistory) {
			if (ch.getPlayerName().equals(player)) {
				return ch;
			}
		}
		return null;
	}

	public void addChatHistory(String player, String message) {
		boolean hasChatHistory = false;
		for (ChatHistory ch : chatHistory) {
			if (ch.getPlayerName().equals(player)) {
				ch.addChatMessage(message);
				hasChatHistory = true;
			}
		}
		if (!hasChatHistory) {
			ChatHistory chatHistory = new ChatHistory(player);
			chatHistory.addChatMessage(message);
			this.chatHistory.add(chatHistory);
		}
	}

	public boolean isReporting(String player) {
		if (reporting.containsKey(player)) {
			return true;
		}
		return false;
	}

	public String getReporting(String player) {
		return reporting.get(player);
	}

	public void setReporting(String player, String reported) {
		reporting.put(player, reported);
	}

	public Report createReport(String player, String reason) {
		ChatHistory ch = null;
		for (ChatHistory ch1 : chatHistory) {
			if (ch1.getPlayerName().equals(player)) {
				ch = ch1;
			}
		}
		if (ch == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		Date now = cal.getTime();
		Report r = new Report(now, player, ch, reason, getId());
		reports.add(r);
		push();
		return r;
	}

	public void pullMainThread() {
		reports.clear();
		if (ChatReport.isUsingDatabase()) {
			DatabaseConnection db = ChatReport.getInstance().getDatabaseConnection();
			ResultSet rs = db.queryWithResult("SELECT * FROM chatreports;");
			try {
				while (rs.next()) {
					String chatHistoryR = rs.getString("chatHistory");
					String[] chatHistoryP = chatHistoryR.split(Pattern.quote("&nl/"));
					ArrayList<String> chatHistory = new ArrayList<String>();
					for (String s : chatHistoryP) {
						chatHistory.add(s);
					}
					SavedChatHistory ch = new SavedChatHistory();
					ch.setChatMessages(chatHistory);
					String id = rs.getString("id");
					String player = rs.getString("playerName");
					String reason = rs.getString("reason");
					long dateL = rs.getLong("date");
					Date date = new Date(dateL);
					Report r = new Report(date, player, ch, reason, id);
					reports.add(r);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			File d = new File(ChatReport.getInstance().getDataFolder() + File.separator + "reports.yml");
			YamlConfiguration data = YamlConfiguration.loadConfiguration(d);
			if (data.contains("reports")) {
				for (String s : data.getConfigurationSection("reports").getKeys(false)) {
					SavedChatHistory ch = new SavedChatHistory();
					ch.setChatMessages(data.getStringList("reports." + s + ".chat"));
					String player = data.getString("reports." + s + ".player");
					String reason = data.getString("reports." + s + ".reason");
					Date date = new Date(data.getLong("reports." + s + ".date"));
					Report r = new Report(date, player, ch, reason, s);
					reports.add(r);
				}
			}
		}
	}

	public void pull() {
		reports.clear();
		if (ChatReport.isUsingDatabase()) {
			new BukkitRunnable() {

				@Override
				public void run() {
					DatabaseConnection db = ChatReport.getInstance().getDatabaseConnection();
					ResultSet rs = db.queryWithResult("SELECT * FROM chatreports;");
					try {
						while (rs.next()) {
							String chatHistoryR = rs.getString("chatHistory");
							String[] chatHistoryP = chatHistoryR.split(Pattern.quote("&nl/"));
							ArrayList<String> chatHistory = new ArrayList<String>();
							for (String s : chatHistoryP) {
								chatHistory.add(s);
							}
							SavedChatHistory ch = new SavedChatHistory();
							ch.setChatMessages(chatHistory);
							String id = rs.getString("id");
							String player = rs.getString("playerName");
							String reason = rs.getString("reason");
							long dateL = rs.getLong("date");
							Date date = new Date(dateL);
							Report r = new Report(date, player, ch, reason, id);
							reports.add(r);
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}.runTaskAsynchronously(ChatReport.getInstance());

		} else

		{
			File d = new File(ChatReport.getInstance().getDataFolder() + File.separator + "reports.yml");
			YamlConfiguration data = YamlConfiguration.loadConfiguration(d);
			if (data.contains("reports")) {
				for (String s : data.getConfigurationSection("reports").getKeys(false)) {
					SavedChatHistory ch = new SavedChatHistory();
					ch.setChatMessages(data.getStringList("reports." + s + ".chat"));
					String player = data.getString("reports." + s + ".player");
					String reason = data.getString("reports." + s + ".reason");
					Date date = new Date(data.getLong("reports." + s + ".date"));
					Report r = new Report(date, player, ch, reason, s);
					reports.add(r);
				}
			}
		}
	}

	public void pushMainThread() {
		if (ChatReport.isUsingDatabase()) {
			DatabaseConnection db = ChatReport.getInstance().getDatabaseConnection();
			if (db.truncateTable()) {
				for (Report r : reports) {
					StringBuilder chatHistory = new StringBuilder();
					int pos = 0;
					for (String s : r.getChatHistory().getChatMessages()) {
						pos++;
						if (pos == r.getChatHistory().getChatMessages().size()) {
							chatHistory.append(s);
						} else {
							chatHistory.append(s).append("&nl/");
						}
					}
					db.query("INSERT INTO chatreports VALUES (\"" + r.getId() + "\", \"" + r.getPlayerName() + "\", \""
							+ r.getReason() + "\", \"" + chatHistory.toString() + "\", " + r.getCreationDate().getTime() + ")");
				}
			}
		} else {
			File d = new File(ChatReport.getInstance().getDataFolder() + File.separator + "reports.yml");
			YamlConfiguration data = YamlConfiguration.loadConfiguration(d);
			data.set("reports", null);
			for (Report r : reports) {
				data.set("reports." + r.getId() + ".chat", r.getChatHistory().getChatMessages());
				data.set("reports." + r.getId() + ".player", r.getPlayerName());
				data.set("reports." + r.getId() + ".reason", r.getReason());
				data.set("reports." + r.getId() + ".date", r.getCreationDate().getTime());
			}
			try {
				data.save(d);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void push() {
		if (ChatReport.isUsingDatabase()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					DatabaseConnection db = ChatReport.getInstance().getDatabaseConnection();
					if (db.truncateTable()) {
						for (Report r : reports) {
							StringBuilder chatHistory = new StringBuilder();
							int pos = 0;
							for (String s : r.getChatHistory().getChatMessages()) {
								pos++;
								if (pos == r.getChatHistory().getChatMessages().size()) {
									chatHistory.append(s);
								} else {
									chatHistory.append(s).append("&nl/");
								}
							}
							db.query("INSERT INTO chatreports VALUES (\"" + r.getId() + "\", \"" + r.getPlayerName() + "\", \""
									+ r.getReason() + "\", \"" + chatHistory.toString() + "\", " + r.getCreationDate().getTime() + ")");
						}
					}
				}
			}.runTaskAsynchronously(ChatReport.getInstance());
		} else {
			File d = new File(ChatReport.getInstance().getDataFolder() + File.separator + "reports.yml");
			YamlConfiguration data = YamlConfiguration.loadConfiguration(d);
			data.set("reports", null);
			for (Report r : reports) {
				data.set("reports." + r.getId() + ".chat", r.getChatHistory().getChatMessages());
				data.set("reports." + r.getId() + ".player", r.getPlayerName());
				data.set("reports." + r.getId() + ".reason", r.getReason());
				data.set("reports." + r.getId() + ".date", r.getCreationDate().getTime());
			}
			try {
				data.save(d);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean deleteReport(String id) {
		ArrayList<Report> toRemove = new ArrayList<Report>();
		for (Report r : reports) {
			if (r.getId().equals(id)) {
				toRemove.add(r);
			}
		}
		for (Report r : toRemove) {
			reports.remove(r);
			ArchiveManager.addReport(r);
		}
		push();
		return false;
	}

	public Report getReport(String id) {
		for (Report r : reports) {
			if (r.getId().equals(id)) {
				return r;
			}
		}
		return null;
	}

	public ArrayList<Report> getReports(String playerName) {
		ArrayList<Report> toReturn = new ArrayList<Report>();
		for (Report r : reports) {
			if (r.getPlayerName().equals(playerName)) {
				toReturn.add(r);
			}
		}
		return toReturn;
	}

	public ArrayList<Report> getReports() {
		return reports;
	}

	public String getId() {
		String id = "";
		while (checkIfIdExists(id) || id.equals("")) {
			String s = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
			StringBuilder sb = new StringBuilder();
			Random ran = new Random();
			while (sb.length() < 6) {
				int index = ran.nextInt(36) + 0;
				sb.append(s.charAt(index));
			}
			id = sb.toString();
		}
		return id;
	}

	public boolean checkIfIdExists(String id) {
		for (Report r : reports) {
			if (r.getId().equals(id)) {
				return true;
			}
		}
		return false;
	}

}
