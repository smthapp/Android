package net.newsmth.smth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ArticleContentActivity extends AppActivity {

	private enum ArticleContentViewMode {
		ArticleContentViewModeNormal,
		ArticleContentViewModeRefer,
		ArticleContentViewModeMail,
		ArticleContentViewModeMailSent,
	}
	
	private String m_lBoardId;
	private String m_lBoardName;
	private long article_id;
	private long size;
	private long article_cnt;
	
	private ArticleContentViewMode mode;
	
	//refermode
	private int refermode;
	private int referposition;
	
	//mail
	private int mailposition;
	
	//as
	private JSONObject reply_dict;
	
	private List<JSONObject> m_mtarrayInfo;
	private NavigationItem navi;
	
	private static final int THREAD_PAGE_SIZE = 10;
	
	//android only
	private ScrollListView m_tableView;
    private MyAdapter adapter;
	private MyOnItemClickListener listener;
	private PopupWindow pw;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_article_content);
		
		m_mtarrayInfo = new ArrayList<JSONObject>();
		
		Bundle bundle = getIntent().getExtras();
		if(null != bundle){
			int art_mode = bundle.getInt("art_mode");
			
			if(art_mode == 0){
				mode = ArticleContentViewMode.ArticleContentViewModeNormal;
			}else if(art_mode == 10){
				mode = ArticleContentViewMode.ArticleContentViewModeMail;
				mailposition = bundle.getInt("art_position");
			}else if(art_mode == 11){
				mode = ArticleContentViewMode.ArticleContentViewModeMailSent;
				mailposition = bundle.getInt("art_position");
			}else{
				mode = ArticleContentViewMode.ArticleContentViewModeRefer;
				refermode = art_mode;
				referposition = bundle.getInt("art_position");
			}
			
			String boardid = bundle.getString("boardid");
			if(boardid != null){
				m_lBoardId = boardid;
			}
			String boardname = bundle.getString("boardname");
			if(boardname != null){
				m_lBoardName = boardname;
			}
			
			article_id = bundle.getLong("art_id");
			article_cnt = bundle.getLong("cnt");
		}
		
		if(mode != ArticleContentViewMode.ArticleContentViewModeNormal
				|| m_lBoardId == null
				|| (m_lBoardName != null && m_lBoardName.length() != 0)){
			//TODO: navi.rightbutton = null
		}
		
		m_tableView = (ScrollListView) findViewById(R.id.articlecontentView);
		adapter = new MyAdapter(this);
		m_tableView.setAdapter(adapter);
		listener = new MyOnItemClickListener();
		m_tableView.setOnItemClickListener(listener);
		
		scroll_enable_loadmore(m_tableView);
		scroll_enable_refresh(m_tableView);
		
		navi = (NavigationItem) findViewById(R.id.articlecontentNavi);
		navi_enable_left(navi);
		navi.title.setText("文章列表");
		
		initContent();
	}

	@Override
	public void initContent()
	{
		m_mtarrayInfo.clear();
		size = 0;
		
		loadContent();
	}
	
	//TODO: pressBtnGoboard
	
	//TODO: parseAtt
	
	private void add_article_with_att(JSONObject art){
		//TODO: parseAtt
		m_mtarrayInfo.add(art);
	}
	
	private void add_articles_with_att(List<JSONObject> arts){
		for(JSONObject e : arts){
			add_article_with_att(e);
		}
	}
	
	@Override
	public void parseContent()
	{
		if(mode == ArticleContentViewMode.ArticleContentViewModeNormal){
			JSONArray arrayInfo = net_smth.net_GetThread(m_lBoardId, article_id, size, THREAD_PAGE_SIZE, NSAppSetting.article_sort);
			if(net_smth.net_error == 0){
				m_bLoadRes = 1;
				
				add_articles_with_att(jsonarray_to_list(arrayInfo));
				
				size = m_mtarrayInfo.size();
			}
		}else if(mode == ArticleContentViewMode.ArticleContentViewModeRefer){
			JSONObject arrayInfo = net_smth.net_GetArticle(m_lBoardId, article_id);
			if(net_smth.net_error == 0){
				m_bLoadRes = 1;
				add_article_with_att(arrayInfo);
			}
			if(net_smth.net_error == 0 || net_smth.net_error == 11011){
				//11011:文章不存在，已经被删除，那么也需要青未读标记
	            int old_net_error = net_smth.net_error;
	            net_smth.net_SetReferRead(refermode, referposition);
	            net_smth.net_error = old_net_error;
			}
		}else{
			//邮件模式
	        JSONObject arrayInfo;
	        if(mode == ArticleContentViewMode.ArticleContentViewModeMail){
	            arrayInfo = net_smth.net_GetMail(mailposition);
	        }else{
	            arrayInfo = net_smth.net_GetMailSent(mailposition);
	        }
	        if (net_smth.net_error == 0)
	        {
	            m_bLoadRes = 1;
	            //add single article
	            add_article_with_att(arrayInfo);
	        }
		}
	}
	
	@Override
	public boolean scroll_enabled()
	{
		if(mode != ArticleContentViewMode.ArticleContentViewModeNormal){
			return false;
		}
		
		if(m_mtarrayInfo.size() >= article_cnt){
			return false;
		}
		
		return true;
	}

	private class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		
		public MyAdapter(Context ctx){
			mInflater = LayoutInflater.from(ctx);
		}
		
		@Override
		public int getCount(){
			//TODO: search cell, more cell
			return m_mtarrayInfo.size();
		}
		
		public Object getItem(int position){
			return null;
		}
		
		public long getItemId(int position){
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.cell_article_content, null);
			}
			
			JSONObject dict = m_mtarrayInfo.get(position);
			
			//TODO: init_fontsize, clear_image
			
			//String title = dict.optString("subject");
	        String author = dict.optString("author_id");
	        String body = dict.optString("body");

	        SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss", Locale.US);
	        Date curDate = new Date(dict.optLong("time") * 1000);
	        String dateStr = formatter.format(curDate);

	        //set cell
	        int lou = 0;
	        if(mode == ArticleContentViewMode.ArticleContentViewModeNormal){
	            if(NSAppSetting.article_sort == 0){
	                if(position == 0){
	                }else{
	                    lou = (int) (article_cnt - position);
	                    if(lou < 1){
	                        lou = 1;
	                    }
	                }
	            }else{
	                lou = position;
	            }
	        }else{
	            lou = -1;
	        }
	       
			TextView m_labelUsr = (TextView) convertView.findViewById(R.id.articlecontentcell_m_labelUsr);
			TextView m_labelDetailInfo = (TextView) convertView.findViewById(R.id.articlecontentcell_m_labelDetailInfo);
			TextView m_txtViewArtContent = (TextView) convertView.findViewById(R.id.articlecontentcell_m_txtViewArtContent);
			TextView label_lou = (TextView) convertView.findViewById(R.id.articlecontentcell_label_lou);

			m_labelUsr.setText(author);
			m_labelDetailInfo.setText(dateStr);
			m_txtViewArtContent.setText(body);
			if(lou == 0){
				label_lou.setText("楼主");
			}else if(lou > 0){
				label_lou.setText("" + lou + "楼");
			}else{
				label_lou.setText("");
			}
			
			return convertView;
		}
	}
	
	private class MyOnItemClickListener implements OnItemClickListener
	{
		@SuppressLint("InlinedApi")
		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position,
                long arg3){
			
			reply_dict = m_mtarrayInfo.get(position);
			
			//action sheet
			View view = View.inflate(getApplicationContext(), R.layout.as_article_content, null);
			Button bt_reply = (Button)view.findViewById(R.id.articlecontentas_button_reply);
			Button bt_emailreply = (Button)view.findViewById(R.id.articlecontentas_button_emailreply);
			Button bt_forward = (Button)view.findViewById(R.id.articlecontentas_button_forward);
			Button bt_cancel = (Button)view.findViewById(R.id.articlecontentas_button_cancel);
			
			if(mode == ArticleContentViewMode.ArticleContentViewModeMailSent){
				return;
			}
			
			if(mode != ArticleContentViewMode.ArticleContentViewModeMail){
				String strFlags = reply_dict.optString("flags").substring(2, 3);
				if(strFlags.equalsIgnoreCase("y")){
					//readonly
					bt_reply.setVisibility(View.INVISIBLE);
				}
			}else{
				bt_emailreply.setVisibility(View.INVISIBLE);
			}
			
			bt_reply.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					pw.dismiss();
					Intent intent = new Intent(ArticleContentActivity.this, ArticleEditActivity.class);
					Bundle bundle = new Bundle();
					
					if(mode == ArticleContentViewMode.ArticleContentViewModeMail){
						bundle.putBoolean("to_mail", true);
						bundle.putString("reply_dict", reply_dict.toString());
						bundle.putBoolean("mail_from_article", false);
					}else if(mode == ArticleContentViewMode.ArticleContentViewModeMailSent){
						return;
					}else{
						bundle.putBoolean("to_mail", false);
						bundle.putString("boardid", m_lBoardId);
						bundle.putString("boardname", m_lBoardName);
						bundle.putString("reply_dict", reply_dict.toString());
						bundle.putBoolean("mail_from_article", false);
					}
					intent.putExtras(bundle);
				    startActivity(intent);
				}
			});
			bt_emailreply.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					pw.dismiss();
					Intent intent = new Intent(ArticleContentActivity.this, ArticleEditActivity.class);
					Bundle bundle = new Bundle();
					
					if(mode == ArticleContentViewMode.ArticleContentViewModeMail){
						bundle.putBoolean("to_mail", true);
						bundle.putString("reply_dict", reply_dict.toString());
						bundle.putBoolean("mail_from_article", false);
					}else if(mode == ArticleContentViewMode.ArticleContentViewModeMailSent){
						return;
					}else{
						bundle.putBoolean("to_mail", true);
						bundle.putString("reply_dict", reply_dict.toString());
						bundle.putBoolean("mail_from_article", true);
					}
					intent.putExtras(bundle);
				    startActivity(intent);
				}
			});
			bt_forward.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					//TODO:
					pw.dismiss();
				}
			});
			bt_cancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					pw.dismiss();
				}
			});
			
			if(pw == null){
				pw = new PopupWindow(ArticleContentActivity.this);
				if(android.os.Build.VERSION.SDK_INT >= 11){
					pw.setWidth(LayoutParams.MATCH_PARENT);
					pw.setHeight(LayoutParams.WRAP_CONTENT);
				}else{
					pw.setWidth(300);
					pw.setHeight(220);
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
			pw.showAtLocation(v, Gravity.BOTTOM, 0, 0);
			pw.update();
			
		}
	}
	
	@Override
	public void updateContent(){
		adapter.notifyDataSetChanged();
	}

}
