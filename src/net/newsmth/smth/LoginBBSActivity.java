package net.newsmth.smth;

import org.json.JSONObject;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

public class LoginBBSActivity extends AppActivity {
	
	//private
	private String last_changelog;
	private int notify_number;
	private String notify_msg;
	
	private String newversion;
	
	private int ret_newversion;
	private int ret_pwd;
	
	private String m_strUser;
	private String m_strPwd;
	
	//android only
	private EditText m_txtFldUser;
	private EditText m_txtFldPwd;
	private Activity this_activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_bbs);
        
        //android only
        m_txtFldUser = (EditText) findViewById(R.id.m_txtFldUser);
        m_txtFldPwd = (EditText) findViewById(R.id.m_txtFldPwd);
        
        String username = appSetting.getLoginInfoUsr();
        String password = appSetting.getLoginInfoPwd();
        
        if(username!=null && password!=null && !username.equals("") && !password.equals("")){
        	m_txtFldUser.setText(username);
        	m_txtFldPwd.setText(password);
        }
        
        this_activity = this;
        LinearLayout screen = (LinearLayout) findViewById(R.id.loginbbsView);
        screen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (v.getId() == R.id.loginbbsView) {
                            InputMethodManager imm = (InputMethodManager)this_activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
            }
		});
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login_bb, menu);
        return true;
    }
    
    public void pressBtnClear(View v){
    	m_txtFldUser.setText("");
    	m_txtFldPwd.setText("");
    	
    	appSetting.setLoginInfo("", "");
    }
    
    public void pressBtnLogin(View v){
    	loadContent();
    }
    
    public int checkVersion()
    {
        JSONObject dict = net_smth.net_GetVersion();
        if(net_smth.net_error != 0 || dict==null){
            return -1;
        }
        
        if(NSAppSetting.DEBUG){
        	NSAppSetting.USE_MEMBER = true;
        }else if(dict.optInt("use_member") > 0){
        	NSAppSetting.USE_MEMBER = true;
        }else{
        	NSAppSetting.USE_MEMBER = false;
        }
       
        NSAppSetting.help_board = dict.optString("help_board");
        if(NSAppSetting.help_board != null && NSAppSetting.help_board.length() == 0){
        	NSAppSetting.help_board = null;
        }
        
        int latest_major = dict.optInt("latest_major");
        int latest_minor = dict.optInt("latest_minor");
        int latest_rc    = dict.optInt("latest_rc");
        int min_major = dict.optInt("min_major");
        int min_minor = dict.optInt("min_minor");
        int min_rc    = dict.optInt("min_rc");
        last_changelog = dict.optString("latest_changelog");
        
        notify_number = dict.optInt("notify_number");
        notify_msg = dict.optString("notify_msg");

        //app version
        String appVer = NSAppSetting.appVer;
        
        String cur_ver[] = appVer.split("\\.", 3);
        
        int cur_major = Integer.parseInt(cur_ver[0]), cur_minor = Integer.parseInt(cur_ver[1]), cur_rc = Integer.parseInt(cur_ver[2]);
        Log.d("SMTH", "current app version " + appVer + ":" + cur_major + cur_minor + cur_rc);
        
        if((cur_major < min_major) || (cur_major == min_major && cur_minor < min_minor) || (cur_major == min_major && cur_minor == min_minor && cur_rc < min_rc)){
            return -2;
        }
        
        if((cur_major < latest_major) || (cur_major == latest_major && cur_minor < latest_minor) || (cur_major == latest_major && cur_minor == latest_minor && cur_rc < latest_rc)){
            
            newversion = "" + latest_major + "." + latest_minor + "." + latest_rc;
            return 2;
        }
        return 1;

    }

    
    private boolean checkInputText()
    {
    	if(m_strUser.length() == 0){
    		ret_newversion = -3;
    		return false;
    	}
    	if(m_strPwd.length() == 0){
    		ret_newversion = -4;
    		return false;
    	}
    	return true;
    }
    
    @Override
    public void parseContent(){
    	ret_newversion = 0;
    	ret_pwd = 0;
    	
    	m_strUser = m_txtFldUser.getText().toString();
    	m_strPwd = m_txtFldPwd.getText().toString();
    	
    	if(!checkInputText()){
    		//different with iOS, don't do dialog in this thread.
    		m_bLoadRes = 1;
    		return;
    	}
    	
    	notify_number = 0;
    	net_ops = 2;
    	
    	ret_newversion = checkVersion();
    	if(ret_newversion > 0){
    		ret_pwd = net_smth.net_LoginBBS(m_strUser, m_strPwd);
    	}
    	
    	Log.d("SMTH", "login ret:" + ret_newversion + ret_pwd);
    	m_bLoadRes = 1;
    }
    
    private void goContentView(){
    	Intent intent = new Intent(this, MainMenuActivity.class); 
        startActivity(intent);
    }
    
    private void show_notification(){
    	if(notify_number > NSAppSetting.my_notify_number){
			new AlertDialog.Builder(this).setTitle("通知").setMessage(notify_msg)
    		.setPositiveButton("继续提示此条通知", new DialogInterface.OnClickListener() {  
                @Override  
                public void onClick(DialogInterface dialog,  
                        int which) {  
                    goContentView();
                }
            }).setNegativeButton("不再提示此条通知", new DialogInterface.OnClickListener() {  
                @Override  
                public void onClick(DialogInterface dialog,  
                        int which) {  
                    NSAppSetting.my_notify_number = notify_number;
                    appSetting.appSettingChange("my_notify_number", String.valueOf(notify_number));
                    
                    goContentView();
                }
            }).setCancelable(false).create()  
            .show();
		}else{
    		goContentView();
		}
    }
    
    private void show_newversion(){
    	if(ret_newversion == 2){
			new AlertDialog.Builder(this).setTitle("应用有更新版本，建议升级").setMessage(last_changelog)
    		.setPositiveButton("继续提示此版本", new DialogInterface.OnClickListener() {  
                @Override  
                public void onClick(DialogInterface dialog,  
                        int which) {  
                    show_notification();
                }
            }).setNegativeButton("不再提示此版本", new DialogInterface.OnClickListener() {  
                @Override  
                public void onClick(DialogInterface dialog,  
                        int which) {  
                    NSAppSetting.my_dismiss_version = newversion;
                    appSetting.appSettingChange("dismiss_version", newversion);
                    
                    show_notification();
                }
            }).setCancelable(false).create()  
            .show();
		}else{
			show_notification();
		}
    }
    
    @Override
    public void updateContent(){
    	if(ret_newversion == -2){
    		new AlertDialog.Builder(this).setTitle("应用版本太低，请升级后重试")
    		.setPositiveButton("确定",null).setCancelable(false).create()  
            .show();
    	}else if(ret_newversion == -3){
    		new AlertDialog.Builder(this).setTitle("账号为空，请输入.")
    		.setPositiveButton("确定",null).setCancelable(false).create()  
            .show();
    	}else if(ret_newversion == -4){
    		new AlertDialog.Builder(this).setTitle("密码为空，请输入.")
    		.setPositiveButton("确定",null).setCancelable(false).create()  
            .show();
    	}else if(ret_newversion > 0 && ret_pwd > 0){
    		appSetting.setLoginInfo(m_strUser, m_strPwd);
    		
    		//TODO: load friends list background
    		
    		//show new version alert
    		show_newversion();
    	}
    }
}
