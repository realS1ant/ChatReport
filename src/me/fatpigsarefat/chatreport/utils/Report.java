package me.fatpigsarefat.chatreport.utils;

import java.util.ArrayList;
import java.util.Date;

public class Report {
	
	private Date creationDate;
	private String playerName;
	private SavedChatHistory chatHistory;
	private String reason;
	private String id;
	
	public Report(Date creationDate, String playerName, ChatHistory chatHistory, String reason, String id) {
		this.creationDate = creationDate;
		this.playerName = playerName;
		this.chatHistory = new SavedChatHistory();
		ArrayList<String> chatMessages = new ArrayList<String>();
		for (String s : chatHistory.getChatMessages()) {
			chatMessages.add(s);
		}
		this.chatHistory.setChatMessages(chatMessages);
		this.reason = reason;
		this.id = id;
	}
	
	public Report(Date creationDate, String playerName, SavedChatHistory chatHistory, String reason, String id) {
		this.creationDate = creationDate;
		this.playerName = playerName;
		this.chatHistory = chatHistory;
		this.reason = reason;
		this.id = id;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public String getPlayerName() {
		return playerName;
	}

	public SavedChatHistory getChatHistory() {
		return chatHistory;
	}

	public String getReason() {
		return reason;
	}

	public String getId() {
		return id;
	}
	
}