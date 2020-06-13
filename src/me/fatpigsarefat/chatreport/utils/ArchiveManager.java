package me.fatpigsarefat.chatreport.utils;

import me.fatpigsarefat.chatreport.ChatReport;
import org.bukkit.Bukkit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class ArchiveManager {

    private static ArrayList<Report> archivedReports = new ArrayList<>();
    private static ChatReport main = ChatReport.getInstance();
    private static DatabaseConnection dB = main.getDatabaseConnection();

    public static void addReport(Report report) {
        if (!ChatReport.isUsingDatabase()) return;
            archivedReports.add(report);
            push();
    }

    public static void delReport(String id){
        if(!ChatReport.isUsingDatabase()) return;
        ArrayList<Report> toRemove = new ArrayList<Report>();
            for (Report r : archivedReports) {
                if (r.getId().equals(id)) {
                    toRemove.add(r);
                }
            }
            for (Report r : toRemove) {
                archivedReports.remove(r);
            }
            push();
    }

    public static Report getReport(String id){
        for(Report r : archivedReports){
            if(r.getId().equalsIgnoreCase(id)) return r;
        }
        return null;
    }

    public static void pull() {
        archivedReports.clear();
        if(!ChatReport.isUsingDatabase()) return;
         main.getServer().getScheduler().runTaskAsynchronously(main, () -> {
            DatabaseConnection db = ChatReport.getInstance().getDatabaseConnection();
            ResultSet rs = db.queryWithResult("SELECT * FROM archivedreports;");
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
                    archivedReports.add(r);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public static void pullMainThread() {
        archivedReports.clear();
        if(!ChatReport.isUsingDatabase()) return;
        DatabaseConnection db = ChatReport.getInstance().getDatabaseConnection();
        ResultSet rs = db.queryWithResult("SELECT * FROM archivedreports;");
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
                archivedReports.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void push() {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            if (dB.truncateArchive()) {
                for (Report r : archivedReports) {
                    String chatHistory = "";
                    int pos = 0;
                    for (String s : r.getChatHistory().getChatMessages()) {
                        pos++;
                        if (pos == r.getChatHistory().getChatMessages().size()) {
                            chatHistory = chatHistory + s;
                        } else {
                            chatHistory = chatHistory + s + "&nl/";
                        }
                    }
                    dB.query("INSERT INTO archivedreports VALUES (\"" + r.getId() + "\", \"" + r.getPlayerName() + "\", \""
                            + r.getReason() + "\", \"" + chatHistory + "\", " + r.getCreationDate().getTime() + ")");
                }
            }
        });
    }
    public static void pushMainThread() {
        if (dB.truncateArchive()) {
            for (Report r : archivedReports) {
                String chatHistory = "";
                int pos = 0;
                for (String s : r.getChatHistory().getChatMessages()) {
                    pos++;
                    if (pos == r.getChatHistory().getChatMessages().size()) {
                        chatHistory = chatHistory + s;
                    } else {
                        chatHistory = chatHistory + s + "&nl/";
                    }
                }
                dB.query("INSERT INTO archivedreports VALUES (\"" + r.getId() + "\", \"" + r.getPlayerName() + "\", \""
                        + r.getReason() + "\", \"" + chatHistory + "\", " + r.getCreationDate().getTime() + ")");
            }
        }
    }

    public static List<Report> getArchivedReports() {
        return Collections.unmodifiableList(archivedReports);
    }


}
