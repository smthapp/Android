package net.newsmth.smth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;

public class UserData {
	private static UserData global_v;
	private static Activity old_v;
	private static NSAppSetting appSetting;

	public void load_setting(Activity v){
        old_v = v;
	}
	
	public static UserData getInstance(Activity v, NSAppSetting a){
		if(global_v == null){
			global_v = new UserData();
			global_v.load_setting(v);
		}
		appSetting = a;
		return global_v;
	}

	private static JSONObject userdata_brc_dict = null;
	private static String userdata_brc_userid;

	private static String userdata_get_path(String userid){
	    String localUserPath = old_v.getFilesDir().toString() + "/UserData/" + userid;
	    File paths = new File(localUserPath);
	    if(!paths.isDirectory()){
	    	paths.mkdirs();
	    }
	    return localUserPath;
	}
	
	private static String file_to_string(String fname){
		BufferedReader reader = null;
		String str = "";
	    try {
	    	reader = new BufferedReader(new FileReader(fname));
			String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                str = str + tempString;
            }
            reader.close();
        } catch (Exception e) {
        	Log.d("SMTH", "file to string error");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                }
            }
        }
	    return str;
	}
	
	private static JSONObject file_to_jsonobject(String fname){
		String str = file_to_string(fname);
	    if(str == null || str.length() == 0){
	    	return null;
	    }
		try {
			return new JSONObject(str);
		} catch (JSONException e) {
			return null;
		}
	}
	
	private static JSONArray file_to_jsonarray(String fname){
		String str = file_to_string(fname);
	    if(str == null || str.length() == 0){
	    	return null;
	    }
	    try {
			return new JSONArray(str);
		} catch (JSONException e) {
			return null;
		}
	}
	
	private static void string_to_file(String str, String fname){
		PrintStream out = null;
        try {
            out = new PrintStream(new FileOutputStream(fname));
            out.print(str);
        } catch (Exception e) {
        	Log.d("SMTH", "string to file error");
        } finally {  
            if (out != null) {
                out.close();
            }
        }
	}
	
	private static void jsonarray_to_file(JSONArray j, String fname){
		string_to_file(j.toString(), fname);
	}
	
	private static void jsonobject_to_file(JSONObject j, String fname){
		string_to_file(j.toString(), fname);
	}

	private static String userdata_brc_get_fname(String userid){
	    String localUserPath = userdata_get_path(userid);
	    return localUserPath + "/brc";
	}

	private static void userdata_load_brc(String userid)
	{
	    if(userdata_brc_dict != null && userid.equalsIgnoreCase(userdata_brc_userid)){
	        return;
	    }
	    
	    userdata_brc_dict = null;
	    
	    String fname = userdata_brc_get_fname(userid);
	    
	    userdata_brc_dict = file_to_jsonobject(fname);
	    userdata_brc_userid = userid;
	}

	public long apiGetUserData_BRC(String board_id)
	{
	    userdata_load_brc(appSetting.getLoginInfoUsr());
	    
	    if(userdata_brc_dict == null){
	        return 0;
	    }
	    
	    long ts = Integer.valueOf(userdata_brc_dict.optString(board_id, "0"));
	    return ts;
	}

	public int apiSetUserData_BRC(String board_id, long last_art_id)
	{
		userdata_load_brc(appSetting.getLoginInfoUsr());
	    
	    if(userdata_brc_dict == null) {
	        userdata_brc_dict = new JSONObject();
	    }
	    
	    try {
			userdata_brc_dict.put(board_id, String.valueOf(last_art_id));
		} catch (JSONException e) {
			
		}
	    
	    String fname = userdata_brc_get_fname(appSetting.getLoginInfoUsr());
	    jsonobject_to_file(userdata_brc_dict, fname);
	    
	    return 0;
	}

	private static JSONArray userdata_mblist = null;
	private static String userdata_mblist_userid;

	private static String userdata_mblist_get_fname(String userid){
		String localUserPath = userdata_get_path(userid);
	    return localUserPath + "/mblist";
	}

	private static void userdata_load_mblist(String userid)
	{
	    if(userdata_mblist != null && userid.equals(userdata_mblist_userid)){
	        return;
	    }
	    
	    userdata_mblist = null;
	    
	    String fname = userdata_mblist_get_fname(userid);
	    
	    userdata_mblist = file_to_jsonarray(fname);
	    userdata_mblist_userid = userid;
	}

	public JSONArray apiGetUserData_MBList()
	{
	    userdata_load_mblist(appSetting.getLoginInfoUsr());
	    
	    return userdata_mblist;
	}

	public int apiSetUserData_MBList(JSONArray mblist)
	{
	    String fname = userdata_mblist_get_fname(appSetting.getLoginInfoUsr());
	    jsonarray_to_file(mblist, fname);

	    userdata_mblist = null;
	    
	    userdata_load_mblist(appSetting.getLoginInfoUsr());

	    return 0;
	}


	public static String apiGetUserdata_attpost_path(String fname){
		String localUserPath = old_v.getFilesDir().toString() + "/PostAtt";
	    File paths = new File(localUserPath);
	    if(!paths.isDirectory()){
	    	paths.mkdirs();
	    }
	    return localUserPath + "/" + fname;
	}

	/******* friends *******/
	private static JSONObject userdata_friends = null;
	private static String userdata_friends_userid;

	private static String userdata_friends_get_fname(String userid){
		String localUserPath = userdata_get_path(userid);
	    return localUserPath + "/friends";
	}

	private static void userdata_load_friends(String userid)
	{
	    if(userdata_friends != null && userid.equals(userdata_friends_userid)){
	        return;
	    }
	    
	    userdata_friends = null;
	    
	    String fname = userdata_friends_get_fname(userid);
	    
	    userdata_friends = file_to_jsonobject(fname);
	    userdata_friends_userid = userid;
	}

	public JSONObject apiGetUserData_friends()
	{
	    userdata_load_friends(appSetting.getLoginInfoUsr());
	    
	    return userdata_friends;
	}

	public int apiSetUserData_friends(JSONObject friends)
	{
	    String fname = userdata_friends_get_fname(appSetting.getLoginInfoUsr());
	    jsonobject_to_file(friends, fname);
	    
	    userdata_friends = null;
	    
	    userdata_load_friends(appSetting.getLoginInfoUsr());
	    
	    return 0;
	}

	public int apiSetUserData_add_friend(String userid)
	{
	    userdata_load_friends(appSetting.getLoginInfoUsr());
	    
	    try {
			userdata_friends.put(userid, userid);
		} catch (JSONException e) {
		}
	    
	    apiSetUserData_friends(userdata_friends);
	    
	    return 0;
	}

	public int apiSetUserData_del_friend(String userid)
	{
		userdata_load_friends(appSetting.getLoginInfoUsr());
	    
	    userdata_friends.remove(userid);
	                                        
	    apiSetUserData_friends(userdata_friends);
	    
	    return 0;
	}

	public int apiGetUserData_is_friends(String userid)
	{
	    userdata_load_friends(appSetting.getLoginInfoUsr());

	    if(userdata_friends == null){
	        return 0;
	    }
	    
	    String friend = userdata_friends.optString(userid);
	    if(friend != null && friend.length() > 0){
	        return 1;
	    }
	    return 0;
	}

	//config
	private static JSONObject userdata_config = null;
	private static String userdata_config_userid;

	private static String userdata_config_get_fname(String userid){
		String localUserPath = userdata_get_path(userid);
	    return localUserPath + "/config";
	}

	private static void userdata_load_config(String userid)
	{
	    if(userdata_config != null && userid.equals(userdata_config_userid)){
	        return;
	    }
	    
	    userdata_config = null;
	    
	    String fname = userdata_config_get_fname(userid);
	    
	    userdata_config = file_to_jsonobject(fname);
	    userdata_config_userid = userid;
	}

	public String apiGetUserData_config(String key)
	{
	    userdata_load_config(appSetting.getLoginInfoUsr());
	    
	    if(userdata_config == null){
	        return null;
	    }
	    return userdata_config.optString(key);
	}

	public int apiSetUserData_config(String key, String value)
	{
	    userdata_load_config(appSetting.getLoginInfoUsr());
	    if(userdata_config == null){
	        userdata_config = new JSONObject();
	    }
	    try {
			userdata_config.put(key, value);
		} catch (JSONException e) {
		}
	    
	    String fname = userdata_config_get_fname(appSetting.getLoginInfoUsr());
	    jsonobject_to_file(userdata_config, fname);

	    return 0;
	}
}
