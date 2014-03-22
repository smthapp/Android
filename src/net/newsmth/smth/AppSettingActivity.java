package net.newsmth.smth;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class AppSettingActivity extends AppActivity {

	private ListView m_tableView;
	private int action;
	
	private NavigationItem navi;
	
	//android only
	private MyAdapter adapter;
	private MyOnItemClickListener listener;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_setting);
		
		m_tableView = (ListView) findViewById(R.id.appsettingView);
		adapter = new MyAdapter(this);
		m_tableView.setAdapter(adapter);
		listener = new MyOnItemClickListener();
		m_tableView.setOnItemClickListener(listener);
		
		navi = (NavigationItem) findViewById(R.id.appsettingNavi);
		navi_enable_left(navi);
		navi.title.setText("系统设置");
	}
	
	@Override
	public boolean scroll_enabled()
	{
		return false;
	}

	private class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		
		public MyAdapter(Context ctx){
			mInflater = LayoutInflater.from(ctx);
		}
		
		@Override
		public int getCount(){
			if(NSAppSetting.help_board != null){
				return 11;
			}else{
				return 10;
			}
		}
		
		public Object getItem(int position){
			return null;
		}
		
		public long getItemId(int position){
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.cell_app_setting, null);
			}
			
			TextView textLabel = (TextView) convertView.findViewById(R.id.appsettingcell_textLabel);
			TextView detailTextLabel = (TextView) convertView.findViewById(R.id.appsettingcell_detailTextLabel);
			
			switch(position){
			case 0:
				textLabel.setText("账户");
				detailTextLabel.setText(appSetting.getLoginInfoUsr() + " >");
				break;
			case 1:
				textLabel.setText("注销");
				detailTextLabel.setText("");
				break;
			case 2:
				textLabel.setText("显示图片");
				if(NSAppSetting.attachment_images_size == 0){
					detailTextLabel.setText("中 >");
				}else if(NSAppSetting.attachment_images_size == 1){
					detailTextLabel.setText("大 >");
				}else if(NSAppSetting.attachment_images_size == 2){
					detailTextLabel.setText("无 >");
				}else{
					detailTextLabel.setText("小 >");
				}
				break;
			case 3:
				textLabel.setText("清除图片缓存");
				detailTextLabel.setText("");
				break;
			case 4:
				textLabel.setText("上传图片尺寸");
				if(NSAppSetting.upphoto_size == 0){
					detailTextLabel.setText("中 >");
				}else if(NSAppSetting.upphoto_size == 1){
					detailTextLabel.setText("大 >");
				}else{
					detailTextLabel.setText("小 >");
				}
				break;
			case 5:
				textLabel.setText("进版面");
				if(NSAppSetting.brcmode == 0){
					detailTextLabel.setText("清除所有文章未读 >");
				}else if(NSAppSetting.brcmode == 1){
					detailTextLabel.setText("清最后一文未读 >");
				}else{
					detailTextLabel.setText("不清未读 >");
				}
				break;
			case 6:
				break;
			case 7:
				textLabel.setText("文章回复显示次序");
				if(NSAppSetting.article_sort == 0){
					detailTextLabel.setText("逆序 >");
				}else{
					detailTextLabel.setText("正序 >");
				}
				break;
			case 8:
				textLabel.setText("关于");
				detailTextLabel.setText(">");
				break;
			case 9:
				textLabel.setText("应用反馈");
				detailTextLabel.setText(">");
				break;
			case 10:
				textLabel.setText("帮助");
				detailTextLabel.setText(">");
				break;
			default:
				break;
			}
			
			return convertView;
		}
	}
	
	private class MyOnItemClickListener implements OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position,
                long arg3){
			
			switch(position){
			case 0:
				//TODO: show user info
				break;
			case 1:
				action = 1;
				loadContent();
				break;
			case 2:
				NSAppSetting.attachment_images_size ++;
				if(NSAppSetting.attachment_images_size > 3){
					NSAppSetting.attachment_images_size = 0;
				}
				appSetting.appSettingChange("attachment_images_size", String.valueOf(NSAppSetting.attachment_images_size));
				adapter.notifyDataSetChanged();
				break;
			case 3:
				//TODO: clear image cache
				break;
			case 4:
				NSAppSetting.upphoto_size ++;
				if(NSAppSetting.upphoto_size > 2){
					NSAppSetting.upphoto_size = 0;
				}
				appSetting.appSettingChange("upphoto_size", String.valueOf(NSAppSetting.upphoto_size));
				adapter.notifyDataSetChanged();
				break;
			case 5:
				NSAppSetting.brcmode ++;
				if(NSAppSetting.brcmode > 2){
					NSAppSetting.brcmode = 0;
				}
				appSetting.appSettingChange("brcmode", String.valueOf(NSAppSetting.brcmode));
				adapter.notifyDataSetChanged();
				break;
			case 6:
				break;
			case 7:
				NSAppSetting.article_sort ++;
				if(NSAppSetting.article_sort > 1){
					NSAppSetting.article_sort = 0;
				}
				appSetting.appSettingChange("article_sort", String.valueOf(NSAppSetting.article_sort));
				adapter.notifyDataSetChanged();
				break;
			case 8:
				showAbout();
				break;
			case 9:
				sendFeedback();
				break;
			case 10:
				showHelp();
				break;
			default:
				break;
			}
		}
	}
	
	private void logoutAccount()
	{
		net_smth.net_LogoutBBS();
	}
	
	@Override
	public void parseContent()
	{
		if(action == 1){
			m_bLoadRes = 1;
			logoutAccount();
		}
	}
	
	@Override
	public void updateContent()
	{
		if(action == 1){
			Intent intent = new Intent(this, LoginBBSActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
			
			startActivity(intent);
		}
	}
	
	private void showHelp()
	{
	    if(NSAppSetting.help_board != null && NSAppSetting.help_board.length() > 0){
	    	Intent intent = new Intent(this, ArticleListActivity.class);
			Bundle bundle = new Bundle();
			
			bundle.putInt("mode", 0);
			bundle.putString("boardid", NSAppSetting.help_board);
			
			intent.putExtras(bundle);
		    startActivity(intent);
	    }
	}
	
	private void showAbout()
	{
		new AlertDialog.Builder(this).setTitle("水木社区欢迎您")
		.setMessage("水木社区官方APP " + NSAppSetting.appVer + "\n\n网址: http://www.newsmth.net\nBugReport:站内发信给SYSOP")
		.setPositiveButton("确定",null).setCancelable(false).create()  
        .show();
	}
	
	private void sendFeedback(){
		JSONObject reply = new JSONObject();
		try {
			reply.put("subject", "Android应用反馈");
			reply.put("author_id", "SYSOP");
		} catch (JSONException e) {
			return;
		}
		
		Intent intent = new Intent(this, ArticleEditActivity.class);
		Bundle bundle = new Bundle();
		
		bundle.putBoolean("to_mail", true);
		bundle.putBoolean("mail_from_article", true);
		bundle.putString("reply_dict", reply.toString());
		
		intent.putExtras(bundle);
	    startActivity(intent);
	}

}
