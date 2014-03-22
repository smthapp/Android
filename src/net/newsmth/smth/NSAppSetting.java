package net.newsmth.smth;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.app.Activity;

public class NSAppSetting /*extends Activity*/ {
	public static final String appVer = "0.0.1";
	public static boolean DEBUG = true;
	
	//system config
	public static boolean USE_MEMBER = false;
	public static String help_board = null;
	
	//user config
	public static int attachment_images_size; //0:middle, 1:large, 2:none, 3:small
	public static int upphoto_size; //0:middle, 1:large, 2:small
	public static int brcmode;
	public static int my_notify_number;
	public static String my_dismiss_version;
	public static int article_sort;
	
	private static String strUsr;
	private static String strPwd;
	
	
	private static NSAppSetting global_v;
	private static Activity old_v;
	
	//android only
	
	public void load_setting(Activity v){
		SharedPreferences preferences = v.getSharedPreferences("config", Context.MODE_PRIVATE);

        attachment_images_size = Integer.valueOf(preferences.getString("attachment_images_size", "0")).intValue();
        upphoto_size = Integer.valueOf(preferences.getString("upphoto_size", "0")).intValue();
        brcmode = Integer.valueOf(preferences.getString("brcmode", "0")).intValue();
        
        strUsr = preferences.getString("username", "");
        strPwd = preferences.getString("password", "");
        
        my_notify_number = Integer.valueOf(preferences.getString("my_notify_number", "0")).intValue();
        my_dismiss_version = preferences.getString("dismiss_version", "");
        
        article_sort = Integer.valueOf(preferences.getString("article_sort", "0")).intValue();
        
        old_v = v;
	}
	
	public static NSAppSetting getInstance(Activity v){
		if(global_v == null){
			global_v = new NSAppSetting();
			global_v.load_setting(v);
		}
		return global_v;
	}
	
	public void setLoginInfo(String usr, String pwd){
		SharedPreferences preferences = old_v.getSharedPreferences("config", Context.MODE_PRIVATE);
    	
    	Editor editor=preferences.edit();
    	editor.putString("username", usr);
    	editor.putString("password", pwd);
    	editor.commit();
    	
    	this.load_setting(old_v);
	}
	
	public String getLoginInfoUsr(){
		return strUsr;
	}
	
	public String getLoginInfoPwd(){
		return strPwd;
	}
	
	public void appSettingChange(String name, String value){
		this.appSettingChange(name, value, old_v);
	}
	
	public void appSettingChange(String name, String value, Activity v){
		SharedPreferences preferences = v.getSharedPreferences("config", Context.MODE_PRIVATE);
    	
    	Editor editor=preferences.edit();
    	editor.putString(name, value);
    	editor.commit();
    	
    	this.load_setting(v);
	}
	
	private static String appGetDateString_internal(long time, long cur_time, int after){
		if(cur_time == 0){
			cur_time = System.currentTimeMillis() / 1000;
		}
		
		long ts = time;
		long c_ts = cur_time;
		
		if(after > 0){
	        if(ts <= c_ts){
	            return "现在";
	        }
	    }else{
	        if(ts >= c_ts){
	            return "现在";
	        }
	    }
	    if(ts == 0){
	        return "";
	    }
	    long d;
	    String post;
	    if(after > 0){
	        d = ts - c_ts;
	        post = "后";
	    }else{
	        d = c_ts - ts;
	        post = "前";
	    }
	    
	    if(d < 60){
	        return "" + d + "秒" + post;
	    }
	    d /= 60;
	    if(d < 60){
	        return "" + d + "分钟" + post;
	    }
	    d /= 60;
	    if(d < 24){
	        return "" + d + "小时" + post;
	    }
	    d /= 24; //天数
	    if(d < 7){
	        return "" + d + "天" + post;
	    }
	    if(d < 30){
	        return "" + d + "周" + post;
	    }
	    if(d < 365){
	        return "" + d + "月" + post;
	    }
	    d /= 365;
	    return "" + d + "年" + post;

	}
	
	public static String appGetDateString(long time, long cur_time){
		return appGetDateString_internal(time, cur_time, 0);
	}
	
	public static String appGetDateStringAfter(long time, long cur_time){
		return appGetDateString_internal(time, cur_time, 1);
	}
	
}
