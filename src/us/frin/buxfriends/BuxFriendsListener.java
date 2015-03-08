package us.frin.buxfriends;

//import org.bukkit.Material;
//import org.bukkit.entity.Player;
//import org.bukkit.Bukkit;
//import org.bukkit.BanList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import us.frin.buxfriends.commands.FriendCommands;

//import com.mysql.jdbc.PreparedStatement;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.PlayerInventory;

public class BuxFriendsListener implements Listener {
	BuxFriends plugin;
	
	public BuxFriendsListener(BuxFriends plugin) {
		this.plugin = plugin;
	}
	
	public String getKickReason(Player player) {
		Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet res = null;
		String result = "";
		try {
//			stmt = this.plugin.con.createStatement();
			pstmt = this.plugin.con.prepareStatement("SELECT reason, bannednickid FROM `bannednicks` WHERE ? RLIKE expr");
			pstmt.setString(1, player.getName());
			res = pstmt.executeQuery();
			
			if (res.first()) {
				result = res.getString("reason");
				int bannednickid = res.getInt("bannednickid");
				if (pstmt != null) pstmt.close();
				pstmt = this.plugin.con.prepareStatement("UPDATE `bannednicks` SET `times_enforced` = `times_enforced` + 1 WHERE bannednickid = ?");
				pstmt.setInt(1, bannednickid);
				pstmt.executeUpdate();
			}
		}
		catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		}
		finally {
			if (res != null) {
				try {
					res.close();
				}
				catch (SQLException e) {
					// Nothing
				}
				res = null;
			}
			
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e) {
					// Nothing
				}
				stmt = null;
			}

			if (pstmt != null) {
				try {
					pstmt.close();
				}
				catch (SQLException e) {
					// Nothing
				}
				pstmt = null;
			}
}
		return result;
	}
	
	@EventHandler // EventPriority.NORMAL by default
	public void onPlayerJoin(PlayerJoinEvent evt) {
		Player player = evt.getPlayer(); // The player who joined
		FriendCommands cmds = new FriendCommands(this.plugin);
		
		int hasUnconfirmedRequests = cmds.hasUnconfirmedRequests(player);
		if (hasUnconfirmedRequests > 0) {
			player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.WHITE+" You have "+hasUnconfirmedRequests+" friend request(s) pending. See them with /friends requests");
		}
		
		cmds.notifyUsersAbout(player, true);
	}
	
	@EventHandler // EventPriority.NORMAL by default
	public void onPlayerQuit(PlayerQuitEvent evt) {
		Player player = evt.getPlayer(); // The player who quit
		FriendCommands cmds = new FriendCommands(this.plugin);
		
		cmds.notifyUsersAbout(player, false);
	}
}
