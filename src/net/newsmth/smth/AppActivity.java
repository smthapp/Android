package net.newsmth.smth;

import java.util.ArrayList;
import java.util.List;

import net.newsmth.libSMTH.*;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


/*  NOTE: need client_signature.java which is not in svn
 * the format is:
 
package net.newsmth.signature;

public class client_signature {
	private static final String CLIENT_USERID = "userid";

	private static final String CLIENT_SECRET = "111111111111";

	private static final String CLIENT_SIGNATURE = "2222222222222";
	
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
*/

public class AppActivity extends Activity {
	protected int m_bLoadRes;
	protected int net_ops;
    protected int net_ops_done;
    protected int net_ops_percent;
    protected boolean net_usercancel;
    
    private ProgressDialog m_progressBar;
    protected SMTHURLConnection net_smth;

    //global
  	protected NSAppSetting appSetting;
  	protected UserData userdata;
  	
  	//Handler
  	public static final int MESSAGE_UI_UPDATE = 1001;

  	@SuppressLint("HandlerLeak")
	private Handler m_handler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                 case MESSAGE_UI_UPDATE:
                 {
                	 hudWasHidden();
                 }
                 break;
                 case SMTHURLConnection.MESSAGE_UI_PROGRESS:
                 {
                	 if(net_ops == 0){
               			net_ops = 1;
             		}
                	net_ops_percent = (net_ops_done * 100 + net_smth.net_progress) / net_ops;
                			
             		if(m_progressBar != null){
             			m_progressBar.setMessage(String.format("%02d%%......", net_ops_percent));
             		}
                 }
                 break;
                 default:
                 break;
            }
            super.handleMessage(msg);
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		net_smth = new SMTHURLConnection();
		net_smth.init_smth(m_handler);

        appSetting = NSAppSetting.getInstance(this);
        userdata = UserData.getInstance(this, appSetting);
        
		//TODO: swipe
	}
	
	public void pressBtnBack(){
		this.finish();
	}
	
	public void moreContent(){
		loadContent();
	}
	
	public void initContent(){
		loadContent();
	}
	
	public void parseContent(){
	}
	
	public boolean scroll_enabled(){
		return true;
	}
	
	public void navi_enable_left(NavigationItem navi){
		navi.setOnLeftClickListener(new NavigationItem.onLeftCLickListener() {
			@Override
			public void onClick() {
				pressBtnBack();
			}
		});
	}
	
	public void scroll_enable_refresh(ScrollListView v){
		v.setOnRefreshListener(new ScrollListView.OnRefreshListener() {
			@Override
			public void onRefresh() {
				if(scroll_enabled()){
					initContent();
				}
			}
		});
	}
	
	public void scroll_enable_loadmore(ScrollListView v){
		v.setOnLoadMoreListener(new ScrollListView.OnLoadMoreListener() {
			@Override
			public void onLoadMore() {
				if(scroll_enabled()){
					moreContent();
				}
			}
		});
	}

	public void loadContent(){
		net_smth.reset_status();
		
		m_bLoadRes = 0;
	    net_ops = 1;
	    net_ops_done = 0;
	    net_ops_percent = 0;
	    net_usercancel = false;
	    
	    m_progressBar = ProgressDialog.show(this, "努力加载中", "05%......");
	    m_progressBar.setCanceledOnTouchOutside(false);
	    m_progressBar.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
				if(NSAppSetting.DEBUG){
					Log.d("SMTH", "usercancel");
				}
				HubTapped();
			}
		});
	    
	    Thread content_thread = new Thread(new Runnable() {
	    	@Override
			public void run() {
	    	    parseContent();

	    	    Message message = new Message();
                message.what = MESSAGE_UI_UPDATE;
                m_handler.sendMessage(message);
	    	}
	    });
	    
	    content_thread.start();
	}
	
	public void updateContent(){
		
	}
	
	private String get_error_msg(){
		switch(net_smth.net_error){
		case 10319:
			return "添加失败,该版面可能已在收藏夹";
		default:
			return null;
		}
	}
	
	private void do_updatecontent(){
		if(m_bLoadRes == 1){
			updateContent();
		}
	}
	//called when parseContent done
	private void hudWasHidden(){
		if(m_progressBar != null){
			m_progressBar.dismiss();
			m_progressBar = null;
		}
		
		if (net_smth.net_error != 0 && !net_usercancel) {
			Log.d("SMTH", "parseContent error:" + net_smth.net_error);
			String errmsg;
            if((errmsg = get_error_msg()) != null){
            }else if(net_smth.net_error < 0) {
                errmsg = "网络或者服务器错误";
            }else if(net_smth.net_error < 11000){
                //11000一下不显示错误原因
                errmsg = "系统错误";
            }else if(net_smth.net_error_desc != null && net_smth.net_error_desc.length() != 0){
                errmsg = net_smth.net_error_desc;
            }else{
                errmsg = "未知错误";
            }

			new AlertDialog.Builder(this).setTitle(errmsg + "(" + net_smth.net_error + ")")
    		.setPositiveButton("确定",new DialogInterface.OnClickListener() {  
                @Override  
                public void onClick(DialogInterface dialog,  
                        int which) {  
                    do_updatecontent();
                }
            }).setCancelable(false).create()  
            .show();
		}else{
			do_updatecontent();
		}
		
	}

	private void HubTapped()
	{
		net_usercancel = true;
		net_smth.cancel();
	}
	
	public void init_without_UI()
	{
		m_progressBar = null;
		
		net_smth = new SMTHURLConnection();
		net_smth.init_smth(null);
	}
	
	protected List<JSONObject> jsonarray_to_list(JSONArray array){
		if(array == null){
			return null;
		}
		List<JSONObject> ret = new ArrayList<JSONObject>();
		for(int i=0; i<array.length(); i++){
			JSONObject ob = array.optJSONObject(i);
			if(ob != null){
				ret.add(ob);
			}
		}
		return ret;
	}
	
	protected List<JSONObject> jsonarray_reverse_to_list(JSONArray array){
		if(array == null){
			return null;
		}
		List<JSONObject> ret = new ArrayList<JSONObject>();
		int len = array.length();
		for(int i=len - 1; i>=0; i--){
			JSONObject ob = array.optJSONObject(i);
			if(ob != null){
				ret.add(ob);
			}
		}
		return ret;
	}
	
}
