package me.fatpigsarefat.chatreport.nms;

import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R1.IChatBaseComponent;
import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.PacketPlayOutChat;

public class Chat_v1_8_R1 implements Chat {

	@Override
	public void sendUrl(Player player, String prebutton, String url, String cover, String postbutton) {
		PacketPlayOutChat s = new PacketPlayOutChat(
				ChatSerializer.a(prebutton + "{text:\"" + cover + "\",clickEvent:{action:open_url,value:\"" + url + "\"}}" + postbutton));
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(s);
	}
	
	@Override
	public void sendChatCommand(Player player, String prebutton, String command, String cover, String postbutton) {
		IChatBaseComponent c = ChatSerializer.a(prebutton + "{\"text\":\"" + "" + "\",\"extra\":[{\"text\":\"" + cover
				+ "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + "/" + command + "\"}}]}" + postbutton);
		PacketPlayOutChat s = new PacketPlayOutChat(c);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(s);
	}
}