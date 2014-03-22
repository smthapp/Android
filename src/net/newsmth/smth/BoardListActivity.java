package net.newsmth.smth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

public class BoardListActivity extends AppActivity {
	private ScrollListView m_tableView;
	private int m_mode = 0; //1:新分类讨论区 2:收藏夹 3:分区十大 4:搜索版面 5:驻版列表
	private boolean mode_subdir = false;
	private List<Integer> m_mtarraySectionId;
	private List<Integer> m_mtarrayGroupId;
	private List<JSONObject> m_mtarrayInfo;
	
	private NavigationItem navi;
	
	private String as_bname;
	private int as_mode;
	private int join_result = 0;
	
	//android only
	private MyAdapter adapter;
	private MyOnItemClickListener listener;
	private MyOnItemLongClickListener long_listener;
	private PopupWindow pw;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_board_list);
		
		Bundle bundle = getIntent().getExtras();
		if(null != bundle){
			m_mode = bundle.getInt("mode");
		}
		
		m_mtarrayGroupId = new ArrayList<Integer>();
		m_mtarraySectionId = new ArrayList<Integer>();
		m_mtarrayInfo = new ArrayList<JSONObject>();
		
		mode_subdir = true;
		
		m_tableView = (ScrollListView) findViewById(R.id.boardlistView);
		adapter = new MyAdapter(this);
		m_tableView.setAdapter(adapter);
		listener = new MyOnItemClickListener();
		m_tableView.setOnItemClickListener(listener);
		long_listener = new MyOnItemLongClickListener();
		m_tableView.setOnItemLongClickListener(long_listener);
		
		scroll_enable_refresh(m_tableView);

		navi = (NavigationItem) findViewById(R.id.boardlistNavi);
		navi_enable_left(navi);
		navi.title.setText("版面列表");
		
		loadContent();
	}

	private void back_noreload(){
		if(m_mtarrayGroupId.size() > 0){
			m_mtarrayGroupId.remove(m_mtarrayGroupId.size() - 1);
			m_mtarraySectionId.remove(m_mtarraySectionId.size() - 1);
		}else{
			finish();
		}
	}
	
	@Override
	public void pressBtnBack(){
		if(m_mtarrayGroupId.size() > 0){
			m_mtarrayGroupId.remove(m_mtarrayGroupId.size() - 1);
			m_mtarraySectionId.remove(m_mtarraySectionId.size() - 1);
			
			loadContent();
		}else{
			finish();
		}
	}
	
	private void sortArray(List<JSONObject> array){
		m_mtarrayInfo.clear();
		
		if(array == null || array.size() <= 0){
			return;
		}
		
		if(m_mode == 2){
			m_mtarrayInfo.addAll(array);
			return;
		}
		
		if(m_mode == 5){
			for(JSONObject e : array){
				JSONObject dict_board;
				try{
					dict_board = e.getJSONObject("board");
				}catch(JSONException je){
					continue;
				}
				if(dict_board != null){
					m_mtarrayInfo.add(dict_board);
				}
			}
			userdata.apiSetUserData_MBList(new JSONArray(m_mtarrayInfo));
			return;
		}
		
		Collections.sort(array, new Comparator<JSONObject>(){
			@Override
			public int compare(JSONObject dict_a, JSONObject dict_b) {
				int online_a = dict_a.optInt("current_users", 0);
				int online_b = dict_b.optInt("current_users", 0);
				int board_flag_a = dict_a.optInt("flag", 0);
				int board_flag_b = dict_a.optInt("flag", 0);
				
				if(board_flag_a == -1 || ((board_flag_a & 0x400) != 0)){
					board_flag_a = 1;
				}else{
					board_flag_a = 0;
				}
				
				if(board_flag_b == -1 || ((board_flag_b & 0x400) != 0)){
					board_flag_b = 1;
				}else{
					board_flag_b = 0;
				}
				
				if(board_flag_a != 0 && board_flag_b != 0){
					return 0;
				}
				if(board_flag_a != 0 && board_flag_b == 0){
					return -1;
				}
				if(board_flag_a == 0 && board_flag_b != 0){
					return 1;
				}
				
				if(online_a == online_b){
					return 0;
				}
				if(online_a > online_b){
					return -1;
				}
				return 1;
			}
		});
		m_mtarrayInfo.addAll(array);
	}
	
	private void as_setfav()
	{
		if(m_mode == 2){
			net_smth.net_DelFav(as_bname);
		}else{
			net_smth.net_AddFav(as_bname);
		}
	}
	
	private void as_setmember(){
		if(m_mode == 5){
			net_smth.net_QuitMember(as_bname);
		}else{
			join_result = net_smth.net_JoinMember(as_bname);
		}
	}
	
	@Override
	public void parseContent()
	{
		if(as_mode > 0){
			if(as_mode == 1){
				as_setfav();
			}
			if(as_mode == 2){
				as_setmember();
			}
			m_bLoadRes = 1;
			return;
		}
		
		List<JSONObject> arrayInfo;
		
		if(m_mode == 1 || m_mode == 2 || m_mode == 4 || m_mode == 5){
			int groupId, sectionId;
			if(m_mtarrayGroupId.size() > 0){
				sectionId = m_mtarraySectionId.get(m_mtarraySectionId.size() - 1);
				groupId = m_mtarrayGroupId.get(m_mtarrayGroupId.size() - 1);
			}else{
				sectionId = -1;
				groupId = 0;
			}
			
			if(sectionId >= 0){
				//it's dir board
				arrayInfo = jsonarray_to_list(net_smth.net_ReadSection(sectionId, groupId));
			}else{
				//it's fav dir
				if(m_mode == 1){
					arrayInfo = jsonarray_to_list(net_smth.net_LoadBoards(groupId));
				}else if(m_mode == 4){
					//TODO: query
					arrayInfo = null;
				}else if(m_mode == 5){
					arrayInfo = jsonarray_to_list(net_smth.net_LoadMember(appSetting.getLoginInfoUsr(), 0, 100));
				}else{
					arrayInfo = jsonarray_to_list(net_smth.net_LoadFavorites(groupId));
				}
			}
			if(net_smth.net_error == 0){
				sortArray(arrayInfo);
			}else if(!net_usercancel){
				sortArray(null);
			}
		}else if(m_mode == 3){
			arrayInfo = jsonarray_to_list(net_smth.net_LoadSection());
			
			if(net_smth.net_error == 0){
				m_mtarrayInfo.clear();
				m_mtarrayInfo.addAll(arrayInfo);
			}else if(!net_usercancel){
				m_mtarrayInfo.clear();
			}
		}
		
		if(net_usercancel){
			if(mode_subdir){
				back_noreload();
			}
		}
		
		mode_subdir = false;
		
		m_bLoadRes = 1;
		
		return;
	}
	
	
	private class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		
		public MyAdapter(Context ctx){
			mInflater = LayoutInflater.from(ctx);
		}
		
		@Override
		public int getCount(){
			return m_mtarrayInfo.size();
		}
		
		public Object getItem(int position){
			return null;
		}
		
		public long getItemId(int position){
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			JSONObject dict = m_mtarrayInfo.get(position);
			
			boolean use_boardinfocell = false;
			if(m_mode == 1 || m_mode == 2 || m_mode == 4 || m_mode == 5){
				int board_flag = dict.optInt("flag", 0);
				if(board_flag != -1){
					if((board_flag & 0x400) != 0){
						
					}else{
						use_boardinfocell = true;
					}
				}else{
				}
			}else if(m_mode == 3){
			}
			
			if(use_boardinfocell){
				convertView = mInflater.inflate(R.layout.cell_board_info, null);
				
				TextView label_name = (TextView) convertView.findViewById(R.id.boardinfocell_label_name);
				TextView label_desc = (TextView) convertView.findViewById(R.id.boardinfocell_label_desc);
				TextView label_online = (TextView) convertView.findViewById(R.id.boardinfocell_label_online);
				TextView label_article = (TextView) convertView.findViewById(R.id.boardinfocell_label_article);

				label_name.setText(dict.optString("id"));
				label_desc.setVisibility(View.VISIBLE);
				label_article.setVisibility(View.VISIBLE);
				label_online.setVisibility(View.VISIBLE);
				
				label_desc.setText(dict.optString("name"));
				label_article.setText(String.valueOf(dict.optInt("total", 0)));
				label_online.setText(String.valueOf(dict.optInt("current_users", 0)));
				if(dict.optInt("unread", 0) != 0){
					label_article.setTextColor(Color.RED);
				}else{
					label_article.setTextColor(getResources().getColor(R.color.darkgray));
				}
				//TODO: CLUB board flag
			}else{
				convertView = mInflater.inflate(R.layout.cell_board_dir, null);
				
				TextView label_name = (TextView) convertView.findViewById(R.id.boarddircell_label_name);
				TextView label_desc = (TextView) convertView.findViewById(R.id.boarddircell_label_desc);
				
				String ename = dict.optString("name").replaceAll("　", " ");
				int r = ename.indexOf(" ");
				if(r < 0){
					label_name.setText(ename);
					label_desc.setVisibility(View.INVISIBLE);
				}else{
					String info = ename.substring(0, r);
					int i = r + 1;
					for(; i < ename.length() && ename.charAt(i) == ' '; i++);
					String detail;
					if(i >= ename.length()){
						detail = "";
					}else{
						detail = ename.substring(i);
					}
					label_name.setText(info);
					label_desc.setText(detail);
					label_desc.setVisibility(View.VISIBLE);
				}
			}

			return convertView;
		}
	}
	
	private class MyOnItemClickListener implements OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position,
                long arg3){
			
			JSONObject dict = m_mtarrayInfo.get(position);
			
			if(m_mode == 1 || m_mode == 2 || m_mode == 4 || m_mode == 5){
				int board_flag = dict.optInt("flag", 0);
				if(board_flag == -1){
					m_mtarraySectionId.add(-1);
					m_mtarrayGroupId.add(dict.optInt("bid", 0));
					mode_subdir = true;
					loadContent();
				}else if((board_flag & 0x400) != 0){
					m_mtarraySectionId.add(dict.optInt("section", 0));
					m_mtarrayGroupId.add(dict.optInt("bid", 0));
					mode_subdir = true;
					loadContent();
				}else{
					showArticleList(0, 0, dict.optString("id"), dict.optString("name"));
				}
			}else if(m_mode == 3){
				showArticleList(1, dict.optInt("id"), null, null);
			}
		}
	}
	
	private class MyOnItemLongClickListener implements OnItemLongClickListener{
		@SuppressLint("InlinedApi")
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int position, long arg3) {
			JSONObject dict = m_mtarrayInfo.get(position);
			
			as_bname = dict.optString("id");
			
			if(m_mode == 1 || m_mode == 2 || m_mode == 4 || m_mode == 5){
				int bid = dict.optInt("bid");
				if(bid > 0){
					int board_flag = dict.optInt("flag", 0);
					if(board_flag == -1){
						return false;
					}else if((board_flag & 0x400) != 0){
						return false;
					}else{
					}
				}else{
					return false;
				}
			}else{
				return false;
			}
			
			//action sheet
			View view = View.inflate(getApplicationContext(), R.layout.as_board_info, null);
			Button bt_fav = (Button)view.findViewById(R.id.boardinfoas_button_fav);
			Button bt_member = (Button)view.findViewById(R.id.boardinfoas_button_member);
			Button bt_cancel = (Button)view.findViewById(R.id.boardinfoas_button_cancel);
			
			if(m_mode == 2){
				bt_fav.setText("取消收藏");
			}else{
				bt_fav.setText("收藏");
			}
			
			if(NSAppSetting.USE_MEMBER){
				if(m_mode == 5){	
					bt_member.setText("取消驻版");
				}else{
					bt_member.setText("驻版");
				}
				bt_member.setVisibility(View.VISIBLE);
			}else{
				bt_member.setVisibility(View.INVISIBLE);
			}
			
			bt_fav.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					pw.dismiss();
					as_mode = 1;
					loadContent();
				}
			});
			bt_member.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					pw.dismiss();
					as_mode = 2;
	                loadContent();
				}
			});
			bt_cancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					pw.dismiss();
				}
			});
			
			if(pw == null){
				pw = new PopupWindow(BoardListActivity.this);
				if(android.os.Build.VERSION.SDK_INT >= 11){
					pw.setWidth(LayoutParams.MATCH_PARENT);
					pw.setHeight(LayoutParams.WRAP_CONTENT);
				}else{
					pw.setWidth(300);
					pw.setHeight(170);
				}
				pw.setBackgroundDrawable(new BitmapDrawable(getResources()));
				pw.setFocusable(true);
				pw.setOutsideTouchable(true);
			}
			
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					pw.dismiss();
				}
			});
			
			pw.setContentView(view);
			pw.showAtLocation(arg1, Gravity.BOTTOM, 0, 0);
			pw.update();
			
			return false;
		}
	}
	
	private void showArticleList(int hotmode, int hotmode_section, String boardid, String boardname){
		Intent intent = new Intent(this, ArticleListActivity.class);
		Bundle bundle = new Bundle();
		
		if(hotmode != 0){
			bundle.putInt("mode", 1);
			bundle.putInt("hotmode_section", hotmode_section);
		}else{
			bundle.putInt("mode", 0);
			if(boardid != null){
				bundle.putString("boardid", boardid);
			}
			if(boardname != null){
				bundle.putString("boardname", boardname);
			}
		}
		
		intent.putExtras(bundle);
	    startActivity(intent);
	}
	
	@Override
	public void updateContent(){
		if(as_mode > 0){
			if(as_mode == 1){
				String strAct;
				if(m_mode == 2){
					strAct = "取消收藏";
				}else{
					strAct = "收藏";
				}
			
				new AlertDialog.Builder(this).setTitle(strAct + "成功")
	    		.setPositiveButton("确定",null).setCancelable(false).create()  
	            .show();
			}
			if(as_mode == 2){
				String strAct;
				if(m_mode == 5){
					strAct = "取消驻版";
				}else{
					if(join_result == 0){
						strAct = "驻版成功，您已是正是驻版用户";
					}else{
						strAct = "驻版成功，尚需管理员审核成为正是驻版用户";
					}
				}
				
				new AlertDialog.Builder(this).setTitle(strAct)
		    		.setPositiveButton("确定",null).setCancelable(false).create()  
		            .show();
			}
			as_mode = 0;
			return;
		}
		adapter.notifyDataSetChanged();
	}
	
}
