package net.newsmth.smth;

import org.json.JSONObject;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MessagesActivity extends AppActivity {
	//reply
	private int reply_count;
	private int reply_unread;
    
    //@
	private int at_count;
	private int at_unread;
    
    //mail
	private int mail_count;
	private int mail_unread;
	@SuppressWarnings("unused")
	private int mail_isfull;
    
    //mailsend
	private int mail_count_send;
	
	private ScrollListView m_tableView;
	private NavigationItem navi;
	
	//android only
	private MyAdapter adapter;
	private MyOnItemClickListener listener;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_messages);
		
		reply_count = 0;
	    reply_unread = 0;
	    
	    m_tableView = (ScrollListView) findViewById(R.id.messagesView);
		adapter = new MyAdapter(this);
		m_tableView.setAdapter(adapter);
		listener = new MyOnItemClickListener();
		m_tableView.setOnItemClickListener(listener);
		
		scroll_enable_refresh(m_tableView);
		
		navi = (NavigationItem) findViewById(R.id.messagesNavi);
		navi_enable_left(navi);
		navi.title.setText("我的消息");
		
	    loadContent();
	}

	@Override
	public void parseContent()
	{
	    m_bLoadRes = 1;
	    net_ops = 3;
	    
	    JSONObject dict = net_smth.net_GetReferCount(2);
	    if(dict != null) {
	        reply_count = dict.optInt("total_count");
	        reply_unread = dict.optInt("new_count");
	    }
	    dict = net_smth.net_GetReferCount(1);
	    if(dict != null) {
	        at_count = dict.optInt("total_count");
	        at_unread = dict.optInt("new_count");
	    }

	    dict = net_smth.net_GetMailCount();
	    if(dict != null) {
	        mail_count = dict.optInt("total_count");
	        mail_unread = dict.optInt("new_count");
	        mail_isfull = dict.optInt("is_full");
	    }
	    mail_count_send = -1;
	}
	

	private class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		
		public MyAdapter(Context ctx){
			mInflater = LayoutInflater.from(ctx);
		}
		
		@Override
		public int getCount(){
			return 4;
		}
		
		public Object getItem(int position){
			return null;
		}
		
		public long getItemId(int position){
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.cell_message_info, null);
			}
			
			String strName = "";
			int one_count = 0;
			int one_unread = 0;
			
			switch(position){
			case 0:
				strName = "回复我的文章";
				one_count = reply_count;
				one_unread = reply_unread;
				break;
			case 1:
				strName = "@我的文章";
				one_count = at_count;
				one_unread = at_unread;
				break;
			case 2:
				strName = "邮件-收件箱";
				one_count = mail_count;
				one_unread = mail_unread;
				break;
			case 3:
				strName = "邮件-发件箱";
				one_count = mail_count_send;
				break;
			default:
				break;
			}
			
			TextView label_name = (TextView) convertView.findViewById(R.id.messageinfocell_label_name);
			TextView label_count = (TextView) convertView.findViewById(R.id.messageinfocell_label_count);
			
			label_name.setText(strName);
			if(one_count < 0){
				label_count.setText("");
			}else if(one_unread == 0){
				label_count.setText("共" + one_count + "条");
				label_count.setTextColor(Color.BLACK);
			}else{
				label_count.setText("共" + one_count + "条," + one_unread + "条未读");
				label_count.setTextColor(Color.RED);
			}
			
			return convertView;
		}
	}
	
	private void showReferList(int refermode){
		Intent intent = new Intent(this, ArticleListActivity.class);
		Bundle bundle = new Bundle();
		
		if(refermode == 10){
			bundle.putInt("mode", 3);
		}else if(refermode == 11){
			bundle.putInt("mode", 4);
		}else{
			bundle.putInt("mode", 2);
			bundle.putInt("refermode", refermode);
		}
		
		intent.putExtras(bundle);
	    startActivity(intent);
	}
	
	private class MyOnItemClickListener implements OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position,
                long arg3){
			
			switch(position){
			case 0:
				showReferList(2);
				break;
			case 1:
				showReferList(1);
				break;
			case 2:
				showReferList(10);
				break;
			case 3:
				showReferList(11);
				break;
			}
		}
	}
	
	@Override
	public void updateContent(){
		adapter.notifyDataSetChanged();
	}

}
