package us.frin.buxfriends;

public class FriendRecord {
	// Class for holding data retrieved from MySQL table
	public String owner_uuid;
	public String owner_name;
	public String friend_uuid;
	public String friend_name;
	public int friendid;
	
	public FriendRecord(String owner_uuid, String owner_name, String friend_uuid, String friend_name) {
		this.owner_uuid = owner_uuid;
		this.owner_name = owner_name;
		this.friend_uuid = friend_uuid;
		this.friend_name = friend_name;
	}
}
