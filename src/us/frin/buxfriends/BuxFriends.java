package us.frin.buxfriends;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;

//import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import us.frin.buxfriends.BuxFriendsCommandExecutor;
import us.frin.buxfriends.BuxFriendsListener;

public class BuxFriends extends JavaPlugin {
	
	public Connection con;
	public Permission permission = null;
	
	public void initDatabase() {
		PreparedStatement stmt = null;
		ResultSet res = null;

		try {
			stmt = this.con.prepareStatement("CREATE TABLE IF NOT EXISTS `friends` ("
			  +"`friendid` int(10) unsigned NOT NULL AUTO_INCREMENT,"
			  +"`owner_uuid` varchar(36) COLLATE utf8_unicode_ci NOT NULL,"
			  +"`owner_name` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL,"
			  +"`friend_uuid` varchar(36) COLLATE utf8_unicode_ci NOT NULL,"
			  +"`friend_name` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL,"
			  +"`confirmed` int(1) unsigned NOT NULL DEFAULT '0',"
			  +"`created_at` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',"
			  +"`updated_at` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',"
			  +"PRIMARY KEY (`friendid`)"
			  +") ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1");
			stmt.executeUpdate();
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
	
	@Override
	public void onEnable() {
		// Attempt MySQL connection
		try {
			Class.forName("com.mysql.jdbc.Driver");
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			String user = getConfig().getString("user");
			String pass = getConfig().getString("pass");
			String name = getConfig().getString("name");
			String port = getConfig().getString("port");
			String address = getConfig().getString("address");
			con = DriverManager.getConnection("jdbc:mysql://" + address + ":"
						+ port + "/" + name, user, pass);
//			con = DriverManager.getConnection(url, username, password);
			this.initDatabase();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Setup connections to other plugins
		if (!setupPermissions()) {
			getLogger().severe("Disabling BuxFriends - No Permissions Plugin Found");
			getServer().getPluginManager().disablePlugin(this);
		}
		
		// Initialize listener
		getServer().getPluginManager().registerEvents(new BuxFriendsListener(this), this);
		
		// Initialize command handler
		BuxFriendsCommandExecutor executor = new BuxFriendsCommandExecutor(this);
//		this.getCommand("friends").setExecutor(new BuxFriendsCommandExecutor(this));
		this.getCommand("f").setExecutor(executor);
		this.getCommand("friend").setExecutor(executor);
		this.getCommand("friends").setExecutor(executor);

		getLogger().info("Plugin BuxFriends loaded successfully");
	}
	
	// Vault code
	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
		return (permission != null);
    }
	
	@Override
	public void onDisable() {
		// Clean up MySQL connection
		try {
			if (con != null && !con.isClosed()) {
				con.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
