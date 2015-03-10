package us.frin.buxfriends;

//import org.bukkit.Bukkit;
//import net.milkbowl.vault.chat.Chat;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.ChatColor;
//import org.bukkit.Location;
//import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.frin.buxfriends.commands.FriendCommands;


public class BuxFriendsCommandExecutor implements CommandExecutor {
	BuxFriends plugin;
	
	public BuxFriendsCommandExecutor(BuxFriends plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("f")
				|| cmd.getName().equalsIgnoreCase("friend")
				|| cmd.getName().equalsIgnoreCase("friends")) {
			if (args.length == 0) {
				//////////////////////////////////
				// Friend list command
				//////////////////////////////////
				if (!(sender instanceof Player)) {
					sender.sendMessage("This command can only be run by a player.");
				}
				else {
					Player player = (Player) sender;
					if (!player.hasPermission("buxfriends.list")) {
						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.RED+"No permission to use this command.");
						return true;
					}
					
					FriendCommands cmds = new FriendCommands(this.plugin);
					String friends = cmds.getFriends(player);
					
					if (friends.length() == 0) {
						friends = ChatColor.WHITE+"Your friends list is empty. See /friends help for more info.";
					}
					
					player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+friends);
//					this.plugin.getLogger().info("[BuxFriends] "+player.getName()+" ran /friends command");
				}
				
				return true;
			}
			else if (args[0].equalsIgnoreCase("add")) {
				//////////////////////////////////
				// Friends add request command
				//////////////////////////////////
				if (!(sender instanceof Player)) {
					sender.sendMessage("This command can only be run by a player.");
				}
				else {
					Player player = (Player) sender;
					if (!player.hasPermission("buxfriends.add")) {
						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.RED+"No permission to use this command.");
						return true;
					}
					
					if (args.length < 2) {
						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] Missing name of player you wish to add to your friend list, see "+ChatColor.RED+"/friends help");
						return true;
					}
					
					FriendCommands cmds = new FriendCommands(this.plugin);
					int addedStatus = cmds.addedStatus(player, args[1]);
					if (addedStatus == 2) {
						// 2 means already added but not confirmed
						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.RED+args[1]+" is already added to your friend list but not confirmed.");
						return true;
					}
					else if (addedStatus == 1) {
						// 1 means already added and confirmed
						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.RED+args[1]+" is already added to your friend list.");
						return true;
					}
					else if (addedStatus == 0) {
						// Not yet added, let's add with confirmation
						// PERMISSIONS!!
						
						boolean res = cmds.addFriendRequest(player, args[1]);
						
						if (res) {
							player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.WHITE+args[1]+" has been sent friend request, which needs to be confirmed first.");
						}
						
						return true;
					}
					else if (addedStatus == -1) {
						// Cannot find user
						
						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.RED+args[1]+" was never seen before.");
						return true;
					}
					else if (addedStatus == 3) {
						// Already confirmed other side
						
//						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.RED+args[1]+" is already on your friend list.");
						return true;
					}
					else if (addedStatus == 4) {
						// Not yet confirmed from other side request
						
						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.RED+"You already have a pending request from "+args[1]+", see "+ChatColor.RED+"/friends requests");
						return true;
					}
				}
				
				return true;
			}
			else if (args[0].equalsIgnoreCase("requests") || args[0].equalsIgnoreCase("req")) {
				//////////////////////////////////
				// Friends requests list command
				//////////////////////////////////
				if (!(sender instanceof Player)) {
					sender.sendMessage("This command can only be run by a player.");
				}
				else {
					Player player = (Player) sender;
//					if (!player.hasPermission("buxfriends.requests")) {
//						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.RED+"No permission to use this command.");
//						return true;
//					}
					
					FriendCommands cmds = new FriendCommands(this.plugin);
					ArrayList<FriendRecord> requests = cmds.getFriendRequests(player);
					if (requests.size() == 0) {
						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.WHITE+"You have no requests pending.");
						return true;
					}
					player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.WHITE+"Friend requests:");
					int n = 0;
					// Only list first ten friend requests
					for (Iterator<FriendRecord> iterator = requests.iterator(); iterator.hasNext();) {
						if (n >= 10) break;
						FriendRecord friendRecord = (FriendRecord) iterator.next();
						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.WHITE+"["+ChatColor.YELLOW+friendRecord.friendid+ChatColor.WHITE+"] "+friendRecord.owner_name);
						n++;
					}
					if (requests.size() > 10) {
						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.WHITE+"Only showing first 10 friend requests.");
					}
					player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.WHITE+"Confirm request with: /friends confirm # (replace # with number inside brackets), deny request with /friends deny #");
				}
				
				return true;
			}
			else if (args[0].equalsIgnoreCase("confirm")) {
				//////////////////////////////////
				// Friends confirm command
				//////////////////////////////////
				if (!(sender instanceof Player)) {
					sender.sendMessage("This command can only be run by a player.");
				}
				else {
					Player player = (Player) sender;
//					if (!player.hasPermission("buxfriends.confirm")) {
//						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.RED+"No permission to use this command.");
//						return true;
//					}
					
					if (args.length < 2) {
						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] Missing number of friend request, see "+ChatColor.RED+"/friends requests");
						return true;
					}
					
					FriendCommands cmds = new FriendCommands(this.plugin);
					boolean result = cmds.confirmRequest(player, args[1]);
					
					if (result) {
						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.WHITE+"Friend request has been successfully confirmed.");
					}
					else {
						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.RED+"Cannot find friend request "+args[1]+".");
					}
				}
				
				return true;
			}
			else if (args[0].equalsIgnoreCase("deny")) {
				//////////////////////////////////
				// Friends deny command
				//////////////////////////////////
				if (!(sender instanceof Player)) {
					sender.sendMessage("This command can only be run by a player.");
				}
				else {
					Player player = (Player) sender;
//					if (!player.hasPermission("buxfriends.confirm")) {
//						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.RED+"No permission to use this command.");
//						return true;
//					}
					
					if (args.length < 2) {
						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] Missing number of friend request, see "+ChatColor.RED+"/friends requests");
						return true;
					}
					
					FriendCommands cmds = new FriendCommands(this.plugin);
					boolean result = cmds.denyRequest(player, args[1]);
					
					if (result) {
						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.WHITE+"Friend request has been denied.");
					}
					else {
						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.RED+"Cannot find friend request "+args[1]+".");
					}
				}
				
				return true;
			}
			else if (args[0].equalsIgnoreCase("remove")) {
				//////////////////////////////////
				// Friends remove command
				//////////////////////////////////
				if (!(sender instanceof Player)) {
					sender.sendMessage("This command can only be run by a player.");
				}
				else {
					Player player = (Player) sender;
//					if (!player.hasPermission("buxfriends.remove")) {
//						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.RED+"No permission to use this command.");
//						return true;
//					}
					
					if (args.length < 2) {
						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] Missing name of player, see "+ChatColor.RED+"/friends help");
						return true;
					}
					
					FriendCommands cmds = new FriendCommands(this.plugin);
					boolean result = cmds.removeFriend(player, args[1]);
					
					if (result) {
						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.WHITE+"Player was successfully removed from your friend list.");
					}
					else {
						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.RED+"Cannot find player "+args[1]+" on your friend list.");
					}
				}
				
				return true;
			}
			else if (args[0].equalsIgnoreCase("help")) {
				//////////////////////////////////
				// Friends help command
				//////////////////////////////////
				if (!(sender instanceof Player)) {
					sender.sendMessage("This command can only be run by a player.");
				}
				else {
					Player player = (Player) sender;
//					if (!player.hasPermission("buxfriends.help")) {
//						player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.RED+"No permission to use this command.");
//						return true;
//					}
					
					player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.WHITE+"The Buxville Friends Plugin");
					player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.WHITE+"/friends add <name>: Add friend request");
					player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.WHITE+"/friends confirm <name>: Confirm friend request");
					player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.WHITE+"/friends deny <name>: Deny friend request");
					player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.WHITE+"/friends requests: List friend requests");
					player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.WHITE+"/friends remove <name>: Remove player from friend list");
					player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.WHITE+"/friends help: this help page");
					player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.WHITE+"/friends: shows list of friends with their online status ("+ChatColor.GREEN+"online, "+ChatColor.RED+"offline, "+ChatColor.GRAY+"unconfirmed"+ChatColor.WHITE+")");
					player.sendMessage(ChatColor.DARK_PURPLE + "[BuxFriends] "+ChatColor.WHITE+"All features can be used with /f, /friend or /friends commands");
//					this.plugin.getLogger().info("[BuxFriends] "+player.getName()+" ran /friends help command");
				}
				
				return true;
			}
			return true;
		}
		return false;
	}
	
}
