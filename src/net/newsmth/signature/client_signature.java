package net.newsmth.signature;

public class client_signature {
	private static final String CLIENT_USERID = "testapp";
	
	private static final String CLIENT_SECRET = "0055a40712ee09f74f70d193c5e8dbc3";

	private static final String CLIENT_SIGNATURE = "4da4774bea90f3509293d112eb6a24cd";
	
	public static String get_secret(){
		return CLIENT_SECRET;
	}
	
	public static String get_signature(){
		return CLIENT_SIGNATURE;
	}
	
	public static String get_userid(){
		return CLIENT_USERID;
	}
}
