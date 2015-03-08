package us.frin.buxfriends.commands;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import us.frin.buxfriends.BuxFriends;
import us.frin.buxfriends.FriendRecord;
import us.frin.buxfriends.PlayerRecord;
//import us.frin.buxfriends.FriendRecord;

public class FriendCommands {
	BuxFriends plugin;
	
	public FriendCommands(BuxFriends plugin) {
		this.plugin = plugin;
	}
	
	// Get list of friends from database
	public String getFriends(Player player) {
		PreparedStatement stmt = null;
		ResultSet res = null;
		StringBuilder sb = new StringBuilder(50);
		try {
			stmt = this.plugin.con.prepareStatement("SELECT * FROM `friends` WHERE owner_uuid = ?");
			stmt.setString(1, player.getUniqueId().toString());
			res = stmt.executeQuery();
			
			int count = 0;
			
			while (res.next()) {
				if (count > 0) sb.append(", ");
				String uuidTmp = res.getString("friend_uuid");
				int confirmed = res.getInt("confirmed");
				
				Player friend = this.plugin.getServer().getPlayer(UUID.fromString(uuidTmp));
				
				String color = ChatColor.RED.toString();
				if (confirmed == 0) {
					color = ChatColor.GRAY.toString();
				}
				if (friend == null) {
					// Either offline player, or never seen before player
					OfflinePlayer offlinePlayer = this.plugin.getServer().getOfflinePlayer(UUID.fromString(uuidTmp));
					if (offlinePlayer != null && offlinePlayer.getName() != null) {
						// Player was on this server before, name is cached
						sb.append(color + offlinePlayer.getName());
					}
					else {
						// Player hasn't ever been on this server before
						String friendTmp = res.getString("friend_name");
						sb.append(color + friendTmp);
					}
				}
				else {
					if (friend.isOnline() && confirmed == 1) {
						color = ChatColor.GREEN.toString();
					}
					sb.append(color + friend.getName());
				}
				count++;
			}
			if (stmt != null) stmt.close();
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
		}
		return sb.toString();
	}
	
	public PlayerRecord findPlayerByName(String name) {
		PreparedStatement stmt = null;
		ResultSet res = null;
		
		PlayerRecord result = null;

		try {
			stmt = this.plugin.con.prepareStatement("SELECT uuid, name FROM `buxnewname` WHERE name = ?");
			stmt.setString(1, name);
			res = stmt.executeQuery();
			
			if (res.first()) {
				result = new PlayerRecord(res.getString("uuid"), res.getString("name"));
				
			}
			if (stmt != null) stmt.close();
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
		}
		return result;
	}

	// Check added status for player
	public int addedStatus(Player player, String friend) {
		Player friendPlayer = this.plugin.getServer().getPlayer(friend);
		
		String uuid = null;
		
		if (friendPlayer == null) {
			// Either offline player, or never seen before player
			PlayerRecord pr = this.findPlayerByName(friend);
			if (pr != null && pr.uuid != null) {
				uuid = pr.uuid;
			}
			if (uuid != null) {
				// Player was on this server before, name is cached
			}
			else {
				// Player cannot be found/never seen before
				return -1;
			}
		}
		else {
			uuid = friendPlayer.getUniqueId().toString();
		}

		PreparedStatement stmt = null;
		ResultSet res = null;

		try {
			stmt = this.plugin.con.prepareStatement("SELECT * FROM `friends` WHERE owner_uuid = ? AND friend_uuid = ?");
			stmt.setString(1, player.getUniqueId().toString());
			stmt.setString(2, uuid);
			res = stmt.executeQuery();
			
			int status = -1;
			
			if (res.first()) {
				int confirmed = res.getInt("confirmed");
				if (confirmed == 0) status = 2;
				else status = 1;
			}
			else {
				status = 0;
			}
			if (stmt != null) stmt.close();
			return status;
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
		}
		return -1;
	}

	// Return true if player has unconfirmed requests
	public int hasUnconfirmedRequests(Player player) {
		PreparedStatement stmt = null;
		ResultSet res = null;
		int status = 0;

		try {
			
			if (player.hasPermission("buxfriends.admin")) {
				stmt = this.plugin.con.prepareStatement("DELETE FROM `friends` WHERE friend_uuid = ? AND confirmed = 0");
				stmt.setString(1, player.getUniqueId().toString());
				stmt.executeUpdate();
				if (stmt != null) stmt.close();
				status = 0;
			}
			else {
				stmt = this.plugin.con.prepareStatement("SELECT COUNT(*) AS n FROM `friends` WHERE friend_uuid = ? AND confirmed = 0");
				stmt.setString(1, player.getUniqueId().toString());
				res = stmt.executeQuery();
				
				if (res.first()) {
					int num = res.getInt("n");
					if (num > 0) status = num;
				}
				else {
					status = 0;
				}
				if (stmt != null) stmt.close();
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
		}
		return status;
	}


	// Return list of friend requests
	public ArrayList<FriendRecord> getFriendRequests(Player player) {
		PreparedStatement stmt = null;
		ResultSet res = null;
		
		ArrayList<FriendRecord> list = new ArrayList<FriendRecord>(5);

		try {
			stmt = this.plugin.con.prepareStatement("SELECT * FROM `friends` WHERE friend_uuid = ? AND confirmed = 0");
			stmt.setString(1, player.getUniqueId().toString());
			res = stmt.executeQuery();
			
			while (res.next()) {
				FriendRecord r = new FriendRecord(res.getString("owner_uuid"), res.getString("owner_name"), res.getString("friend_uuid"), res.getString("friend_name"));
				r.friendid = res.getInt("friendid");
				list.add(r);
			}
			if (stmt != null) stmt.close();
			return list;
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
		}
		return list;
	}

	// Send login/logout notifications
	public void notifyUsersAbout(Player player, boolean login) {
		PreparedStatement stmt = null;
		ResultSet res = null;
		
		ArrayList<FriendRecord> list = new ArrayList<FriendRecord>(5);

		try {
			stmt = this.plugin.con.prepareStatement("SELECT * FROM `friends` WHERE friend_uuid = ? AND confirmed = 1");
			stmt.setString(1, player.getUniqueId().toString());
			res = stmt.executeQuery();
			
			while (res.next()) {
				FriendRecord r = new FriendRecord(res.getString("owner_uuid"), res.getString("owner_name"), res.getString("friend_uuid"), res.getString("friend_name"));
				r.friendid = res.getInt("friendid");
				list.add(r);
			}
			if (stmt != null) stmt.close();
			
			for (Iterator<FriendRecord> iterator = list.iterator(); iterator.hasNext();) {
				FriendRecord friendRecord = (FriendRecord) iterator.next();
				
				Player p = this.plugin.getServer().getPlayer(UUID.fromString(friendRecord.owner_uuid));
				if (p != null && p.isOnline() && p.hasPermission("buxfriends.list")) {
					if (login) {
						p.sendMessage(ChatColor.GREEN+player.getName()+" is now online");
					}
					else {
						p.sendMessage(ChatColor.GRAY+player.getName()+" is now offline");
					}
				}
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
		}
		return;
	}

	// Add friend request to database
	public boolean addFriendRequest(Player player, String friend) {
		Player friendPlayer = this.plugin.getServer().getPlayer(friend);
		
		String uuid = null;
		String modName = friend;
		
		if (friendPlayer == null) {
			// Either offline player, or never seen before player
			PlayerRecord pr = this.findPlayerByName(friend);
			if (pr != null && pr.uuid != null) {
				uuid = pr.uuid;
			}
			if (uuid != null) {
				// Player was on this server before, name is cached
				modName = pr.name;
			}
			else {
				// Player cannot be found/never seen before
				// Won't happen as it is filtered before
				return false;
			}
		}
		else {
			uuid = friendPlayer.getUniqueId().toString();
			modName = friendPlayer.getName();
		}
		
		Player ptmp = this.plugin.getServer().getPlayer(UUID.fromString(uuid));
		if (ptmp != null && ptmp.hasPermission("buxfriends.admin") && !player.hasPermission("buxfriends.admin")) {
			player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.RED+"No permission to add admins to your friends list.");
			return false;
		}
		
		String n1 = player.getUniqueId().toString();
		if (n1.equalsIgnoreCase(uuid)) {
			player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.RED+"You cannot add yourself to the friend list.");
			return false;
		}

		PreparedStatement stmt = null;
		ResultSet res = null;

		try {
			stmt = this.plugin.con.prepareStatement("INSERT INTO `friends` (`owner_uuid`, `owner_name`, `friend_uuid`, `friend_name`, `confirmed`, `created_at`, `updated_at`) VALUES (?, ?, ?, ?, 0, NOW(), NOW())");
			stmt.setString(1, player.getUniqueId().toString());
			stmt.setString(2, player.getName());
			stmt.setString(3, uuid);
			stmt.setString(4, modName);
			stmt.executeUpdate();
			if (stmt != null) stmt.close();
			
			Player pp = this.plugin.getServer().getPlayer(UUID.fromString(uuid));
			if (pp != null && pp.isOnline()) {
				stmt = this.plugin.con.prepareStatement("SELECT * FROM `friends` WHERE friend_uuid = ? AND confirmed = 0 AND owner_uuid = ?");
				stmt.setString(1, uuid);
				stmt.setString(2, player.getUniqueId().toString());
				res = stmt.executeQuery();
				
				if (res.first()) {
					pp.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.WHITE+player.getName()+" just sent you a friend request. Confirm it with /friends confirm "+res.getInt("friendid"));
				}
				if (stmt != null) stmt.close();
			}
			
			return true;
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
		}
		return false;
	}

	// Confirm request
	public boolean confirmRequest(Player player, String friendid) {
		PreparedStatement stmt = null;
		ResultSet res = null;

		try {
			stmt = this.plugin.con.prepareStatement("SELECT * FROM `friends` WHERE friend_uuid = ? AND confirmed = 0 AND friendid = ?");
			stmt.setString(1, player.getUniqueId().toString());
			int i = Integer.parseInt(friendid);
			stmt.setInt(2, i);
			res = stmt.executeQuery();
			
			if (res.first()) {
				String uuid = res.getString("owner_uuid");
				String fu = res.getString("friend_uuid");
				String fn = res.getString("friend_name");
				String on = res.getString("owner_name");
				if (stmt != null) stmt.close();
				
				stmt = this.plugin.con.prepareStatement("UPDATE `friends` SET confirmed = 1 WHERE friendid = ? AND friend_uuid = ? AND confirmed = 0");
				stmt.setInt(1, i);
				stmt.setString(2, player.getUniqueId().toString());
				stmt.executeUpdate();
				if (stmt != null) stmt.close();

				stmt = this.plugin.con.prepareStatement("INSERT INTO `friends` (`owner_uuid`, `owner_name`, `friend_uuid`, `friend_name`, `confirmed`, `created_at`, `updated_at`) VALUES (?, ?, ?, ?, 1, NOW(), NOW())");
				stmt.setString(1, fu);
				stmt.setString(2, fn);
				stmt.setString(3, uuid);
				stmt.setString(4, on);
				stmt.executeUpdate();
				if (stmt != null) stmt.close();

				Player p = this.plugin.getServer().getPlayer(UUID.fromString(uuid));
				if (p != null && p.isOnline()) {
					p.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.WHITE+player.getName()+" just confirmed your friend request.");
				}
				return true;
			}
			else {
				if (stmt != null) stmt.close();
				return false;
			}
		}
		catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		}
		catch (NumberFormatException e) {
			return false;
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
		}
		return false;
	}

	// Deny request
	public boolean denyRequest(Player player, String friendid) {
		PreparedStatement stmt = null;
		ResultSet res = null;

		try {
			stmt = this.plugin.con.prepareStatement("SELECT * FROM `friends` WHERE friend_uuid = ? AND confirmed = 0 AND friendid = ?");
			stmt.setString(1, player.getUniqueId().toString());
			int i = Integer.parseInt(friendid);
			stmt.setInt(2, i);
			res = stmt.executeQuery();
			
			if (res.first()) {
				String uuid = res.getString("owner_uuid");
				if (stmt != null) stmt.close();
				
				stmt = this.plugin.con.prepareStatement("DELETE FROM `friends` WHERE friendid = ? AND friend_uuid = ? AND confirmed = 0");
				stmt.setInt(1, i);
				stmt.setString(2, player.getUniqueId().toString());
				stmt.executeUpdate();
				if (stmt != null) stmt.close();

				Player p = this.plugin.getServer().getPlayer(UUID.fromString(uuid));
				if (p != null && p.isOnline()) {
					p.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.WHITE+player.getName()+" just denied your friend request.");
				}
				return true;
			}
			else {
				if (stmt != null) stmt.close();
				return false;
			}
		}
		catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		}
		catch (NumberFormatException e) {
			return false;
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
		}
		return false;
	}

	// Remove friend
	public boolean removeFriend(Player player, String friend) {
		PreparedStatement stmt = null;
		ResultSet res = null;

		try {
			stmt = this.plugin.con.prepareStatement("SELECT * FROM `friends` WHERE owner_uuid = ? AND friend_name = ?");
			stmt.setString(1, player.getUniqueId().toString());
			stmt.setString(2, friend);
			res = stmt.executeQuery();
			
			if (res.first()) {
				int friendid = res.getInt("friendid");
				if (stmt != null) stmt.close();
				
				stmt = this.plugin.con.prepareStatement("DELETE FROM `friends` WHERE owner_uuid = ? AND friendid = ?");
				stmt.setString(1, player.getUniqueId().toString());
				stmt.setInt(2, friendid);
				stmt.executeUpdate();
				if (stmt != null) stmt.close();

				return true;
			}
			else {
				if (stmt != null) stmt.close();
				return false;
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
		}
		return false;
	}
}
