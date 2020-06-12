package me.fatpigsarefat.chatreport.utils;

import java.util.ArrayList;

public class ChatHistory {

	private String playerName;
	private ArrayList<String> chatMessages;
	
	public ChatHistory(String playerName) {
		this.playerName = playerName;
		chatMessages = new ArrayList<String>();
	}
	
	public String getPlayerName() {
		return playerName;
	}
	
	public ArrayList<String> getChatMessages() {
		return chatMessages;
	}
	
	public void addChatMessage(String s) {
		chatMessages.add(s);
	}
	
}
