package me.fatpigsarefat.chatreport.nms;

import org.bukkit.entity.Player;

public interface Chat {

	public void sendUrl(Player player, String prebutton, String url, String cover, String postbutton);
	public void sendChatCommand(Player player, String prebutton, String command, String cover, String postbutton);

}