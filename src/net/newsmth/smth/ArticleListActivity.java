package net.newsmth.smth;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ArticleListActivity extends AppActivity {

	private enum ArticleListMode {
		ArticleListModeNormal,
		ArticleListModeHot,
		ArticleListModeRefer,
		ArticleListModeMail,
		ArticleListModeMailSent,
	}
	
	private String m_lBoardId;
	private String m_lBoardName;
	private long articles_cnt;
	private long from;
	@SuppressWarnings("unused")
	private long size;
	private long load_size;
	private int load_init_mode;
    
    private ArticleListMode mode;
    
    //search submode
    private boolean submode_search;
    private String query_title;
    private String query_user;
    
    //hotmode
    private int hotmode_section;
    
    //refermode
    private int refermode;
    
    //current date
    private long cur_time;
    private long last_read_artid;

	private List<JSONObject> m_mtarrayInfo;
    private NavigationItem navi;
    
    //android only
    private ScrollListView m_tableView;
    private MyAdapter adapter;
	private MyOnItemClickListener listener;
	
	private static final int ARTICLE_PAGE_SIZE = 20;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_article_list);
		
		m_mtarrayInfo = new ArrayList<JSONObject>();
		articles_cnt = 0;
		submode_search = false;

		Bundle bundle = getIntent().getExtras();
		if(null != bundle){
			int nmode = bundle.getInt("mode");
			if(nmode == 1){
				mode = ArticleListMode.ArticleListModeHot;
				hotmode_section = bundle.getInt("hotmode_section");
			}else if(nmode == 2){
				mode = ArticleListMode.ArticleListModeRefer;
				refermode = bundle.getInt("refermode");
			}else if(nmode == 3){
				mode = ArticleListMode.ArticleListModeMail;
			}else if(nmode == 4){
				mode = ArticleListMode.ArticleListModeMailSent;
			}else{
				mode = ArticleListMode.ArticleListModeNormal;
				m_lBoardId = bundle.getString("boardid");
				if(m_lBoardId == null || m_lBoardId.length() == 0){
					m_lBoardName = null;
					m_lBoardId = null;
				}else{
					m_lBoardName = bundle.getString("boardname");
				}
			}
		}
		
		if(mode == ArticleListMode.ArticleListModeMail || mode == ArticleListMode.ArticleListModeMailSent){
			//TODO: setup the new mail button
		}else if(mode == ArticleListMode.ArticleListModeNormal){
			//TODO: setup the new article button
		}
		
		m_tableView = (ScrollListView) findViewById(R.id.articlelistView);
		adapter = new MyAdapter(this);
		m_tableView.setAdapter(adapter);
		listener = new MyOnItemClickListener();
		m_tableView.setOnItemClickListener(listener);
		
		scroll_enable_loadmore(m_tableView);
		scroll_enable_refresh(m_tableView);
		
		navi = (NavigationItem) findViewById(R.id.articlelistNavi);
		navi_enable_left(navi);
		navi.setOnRightCLickListener(new NavigationItem.onRightCLickListener() {
			@Override
			public void onClick() {
				if(mode == ArticleListMode.ArticleListModeNormal || mode == ArticleListMode.ArticleListModeMail || mode == ArticleListMode.ArticleListModeMailSent){
					Intent intent = new Intent(ArticleListActivity.this, ArticleEditActivity.class);
					Bundle bundle = new Bundle();
					
					if(mode == ArticleListMode.ArticleListModeMail || mode == ArticleListMode.ArticleListModeMailSent){
						bundle.putBoolean("to_mail", true);
						bundle.putBoolean("mail_from_article", false);
					}else{
						bundle.putBoolean("to_mail", false);
						bundle.putString("boardid", m_lBoardId);
						bundle.putString("boardname", m_lBoardName);
					}
					intent.putExtras(bundle);
				    startActivity(intent);
				}else{
					return;
				}
			}
		});
		navi.rightBarButtonItem.setText("新文章");
		
		initContent();
	}
	
	private void initCurDate(){
		cur_time = System.currentTimeMillis() / 1000;
	}
	
	@Override
	public void initContent(){
		load_init_mode = 1;
		loadContent();
	}
	
	@Override
	public void moreContent(){
		load_init_mode = 0;
		loadContent();
	}
	
	@Override
	public void pressBtnBack(){
		if(submode_search){
			submode_search = false;
			
			//TODO: scroll to 0
			
			initContent();
		}else{
			this.finish();
		}
	}
	
	private void settitle(){
		if(submode_search){
			navi.title.setText("搜索结果");
		}else{
			if(mode == ArticleListMode.ArticleListModeHot){
				navi.title.setText("热门话题");
			}else if(mode == ArticleListMode.ArticleListModeNormal){
				if(m_lBoardName != null){
					navi.title.setText(m_lBoardName);
				}else{
					navi.title.setText(m_lBoardId);
				}
			}else if(mode == ArticleListMode.ArticleListModeMail){
				navi.title.setText("收件箱");
			}else{
				if(refermode == 1){
					navi.title.setText("@我的文章");
				}else{
					navi.title.setText("回复我的文章");
				}
			}
		}
	}
	
	@Override
	public void parseContent(){
		net_ops = 2;
		
		if(load_init_mode != 0){
			long new_articles_cnt;
			
			if(submode_search){
				articles_cnt = 0;
			}else{
				if(mode == ArticleListMode.ArticleListModeHot){
					new_articles_cnt = 0;
				}else if(mode == ArticleListMode.ArticleListModeNormal){
					new_articles_cnt = net_smth.net_GetThreadCnt(m_lBoardId);
					
					last_read_artid = userdata.apiGetUserData_BRC(m_lBoardId);
				}else if(mode == ArticleListMode.ArticleListModeMail){
					navi.title.setText("收件箱");
					JSONObject dict = net_smth.net_GetMailCount();
					if(dict != null){
						new_articles_cnt = dict.optLong("total_count");
					}else{
						new_articles_cnt = 0;
					}
				}else if(mode == ArticleListMode.ArticleListModeMailSent) {
					new_articles_cnt = net_smth.net_GetMailCountSent();
				}else{
					JSONObject dict = net_smth.net_GetReferCount(refermode);
					if(dict != null){
						new_articles_cnt = dict.optLong("total_count");
					}else{
						new_articles_cnt = 0;
					}
				}
				
				if(new_articles_cnt == articles_cnt){
					//no update
					//return;
				}
				articles_cnt = new_articles_cnt;
			}
			
			m_mtarrayInfo.clear();
			
			if(submode_search){
				from = 0;
				load_size = ARTICLE_PAGE_SIZE;
			}else{
				if(mode == ArticleListMode.ArticleListModeMail || mode == ArticleListMode.ArticleListModeMailSent || mode == ArticleListMode.ArticleListModeRefer){
					from = articles_cnt;
				}else{
					from = 0;
				}
				size = 0;
				
				if(articles_cnt > ARTICLE_PAGE_SIZE){
					load_size = ARTICLE_PAGE_SIZE;
				}else{
					load_size = articles_cnt;
				}
			}
		}else{
			if(submode_search == false){
				if(mode == ArticleListMode.ArticleListModeMail || mode == ArticleListMode.ArticleListModeMailSent || mode == ArticleListMode.ArticleListModeRefer){
					if(from > ARTICLE_PAGE_SIZE){
						load_size = ARTICLE_PAGE_SIZE;
					}else{
						load_size = from;
					}
				}else{
					load_size = articles_cnt - from;
					if(load_size > ARTICLE_PAGE_SIZE){
						load_size = ARTICLE_PAGE_SIZE;
					}
				}
			}
		}
		
		JSONArray arrayInfo;
		
		if(submode_search){
			arrayInfo = net_smth.net_SearchArticle(m_lBoardId, query_title, query_user, from, load_size);
		}else{
			if(mode == ArticleListMode.ArticleListModeHot){
	            arrayInfo = net_smth.net_LoadSectionHot(hotmode_section);
	        }else if(mode == ArticleListMode.ArticleListModeNormal){
	            arrayInfo = net_smth.net_LoadThreadList(m_lBoardId, from, load_size, NSAppSetting.brcmode);
	        }else if(mode == ArticleListMode.ArticleListModeMail){
	            arrayInfo = net_smth.net_LoadMailList(from-load_size, load_size);
	        }else if(mode == ArticleListMode.ArticleListModeMailSent){
	            arrayInfo = net_smth.net_LoadMailSentList(from-load_size, load_size);
	        }else{
	            arrayInfo = net_smth.net_LoadRefer(refermode, from-load_size, load_size);
	        }
		}
		
		if(net_smth.net_error == 0 && arrayInfo.length() > 0){
			m_bLoadRes = 1;
			
			initCurDate();
			
			if(submode_search){
				m_mtarrayInfo.addAll(jsonarray_to_list(arrayInfo));
				
				size += arrayInfo.length();
				from += arrayInfo.length();
			}else if(mode == ArticleListMode.ArticleListModeMail
					|| mode == ArticleListMode.ArticleListModeMailSent
					|| mode == ArticleListMode.ArticleListModeRefer){
				m_mtarrayInfo.addAll(jsonarray_reverse_to_list(arrayInfo));
				
				size += load_size;
				from -= load_size;
			}else{
				m_mtarrayInfo.addAll(jsonarray_to_list(arrayInfo));
				if(m_lBoardName == null){
					for(int i=0; i<arrayInfo.length(); i++){
						JSONObject ob = arrayInfo.optJSONObject(i);
						m_lBoardName = ob.optString("board_name");
						if(m_lBoardName != null){
							break;
						}
					}
				}
				
				if(mode != ArticleListMode.ArticleListModeHot){
					if(from == 0){
						//calc last_read_id
						long _latest_read = 0;
						for(JSONObject dict : m_mtarrayInfo){
							String flags = dict.optString("flags");
							String flag0 = flags.substring(0, 1);
							if(flag0.equalsIgnoreCase("d")){
								continue;
							}
							_latest_read = dict.optLong("last_reply_id");
							break;
						}
						if(_latest_read > 0){
							userdata.apiSetUserData_BRC(m_lBoardId, _latest_read);
						}
					}
					
					size += load_size;
					from += load_size;
				}
			}
		}
	}
	
	@Override
	public boolean scroll_enabled()
	{
		if(mode == ArticleListMode.ArticleListModeHot){
			return false;
		}else{
			return true;
		}
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
				convertView = mInflater.inflate(R.layout.cell_article_info, null);
			}
			
			JSONObject dict = m_mtarrayInfo.get(position);
			
			//标题左下方
            String strTime;
            if(mode == ArticleListMode.ArticleListModeHot){
                //十大，显示版面
                strTime = "版面:" + dict.optString("board");
            }else{
                //普通文章，提醒，邮件，显示发表时间
                strTime = NSAppSetting.appGetDateString(dict.optLong("time"), cur_time);
            }

            //标题右下方
            String strReplyTime = "";
            int reply_unread = 0;
            int reply_count = 0;
            if(mode == ArticleListMode.ArticleListModeMail || mode == ArticleListMode.ArticleListModeMailSent){
                //邮件，不显示
            }else if(mode != ArticleListMode.ArticleListModeRefer){
                //十大或者普通文章，显示回复数
                reply_count = dict.optInt("count");
                if(reply_count > 0){
                    reply_count --;
                }
                String strLastTime;
                if(reply_count > 0){
                    strLastTime = NSAppSetting.appGetDateString(dict.optLong("last_time"), cur_time);
                }else{
                    strLastTime = "";
                }
                strReplyTime = "[" + reply_count + "]" + strLastTime;
                
                if(reply_count > 0 && mode == ArticleListMode.ArticleListModeNormal && last_read_artid != 0){
                    if(dict.optLong("last_reply_id") > last_read_artid){
                        reply_unread = 1;
                    }
                }
            }else{
                //提醒，显示版面
                strReplyTime = "版面:" + dict.optString("board_id");
            }
            
            int unread = 0;
            int att = 0;
            int ding = 0;
            if(mode == ArticleListMode.ArticleListModeNormal){
                String flags = dict.optString("flags");
                String flag0 = flags.substring(0, 1);
                if(flag0.equals("*")){
                    unread = 1;
                }
                if(flag0.equalsIgnoreCase("d")){
                    ding = 1;
                }
                if(flags.substring(3, 4).equals("@")){
                    att = 1;
                }
            }else if(mode == ArticleListMode.ArticleListModeRefer){
                int flag = dict.optInt("flag");
                if(flag == 0){
                    unread = 1;
                }
            }else if(mode == ArticleListMode.ArticleListModeMail){
            	String flags = dict.optString("flags");
                String flag0 = flags.substring(0, 1);
                
                if(flag0.equalsIgnoreCase("N")){
                    unread = 1;
                }
            }
            
            //发信人
            String author;
            if(mode == ArticleListMode.ArticleListModeRefer) {
                author = dict.optString("user_id");
            }else{
                author = dict.optString("author_id");
            }

			TextView label_title = (TextView) convertView.findViewById(R.id.articleinfocell_label_title);
			TextView label_posttime = (TextView) convertView.findViewById(R.id.articleinfocell_label_posttime);
			TextView label_replytime = (TextView) convertView.findViewById(R.id.articleinfocell_label_replytime);
			TextView label_author = (TextView) convertView.findViewById(R.id.articleinfocell_label_userid);
			ImageView img_reply = (ImageView) convertView.findViewById(R.id.articleinfocell_img_reply);
			ImageView image_att = (ImageView) convertView.findViewById(R.id.articleinfocell_image_att);

			if(reply_count < 10){
				img_reply.setImageResource(R.drawable.icon_article_normal);
			}else if(reply_count < 100){
				img_reply.setImageResource(R.drawable.icon_article_light);
			}else if(reply_count < 1000){
				img_reply.setImageResource(R.drawable.icon_article_fire);
			}else{
				img_reply.setImageResource(R.drawable.icon_article_huo);
			}
			
			if(reply_unread != 0){
				label_replytime.setTextColor(Color.RED);
			}else{
				label_replytime.setTextColor(getResources().getColor(R.color.darkgray));
			}
			
			if(att != 0){
				image_att.setVisibility(View.VISIBLE);
			}else{
				image_att.setVisibility(View.INVISIBLE);
			}
			
			if(ding != 0){
				label_title.setTextColor(Color.RED);
			}else{
				label_title.setTextColor(Color.BLACK);
			}
			
			if(unread != 0){
				label_posttime.setTextColor(Color.RED);
			}else{
				label_posttime.setTextColor(getResources().getColor(R.color.darkgray));
			}
			
			label_title.setText(dict.optString("subject"));
			label_posttime.setText(strTime);
			label_replytime.setText(strReplyTime);
			label_author.setText(author);
			
			//TODO: image_face

			return convertView;
		}
	}
	
	private void showArticleContent(String boardid, String boardname, long art_id, int position){
		Intent intent = new Intent(this, ArticleContentActivity.class);
		Bundle bundle = new Bundle();
		
		if(mode == ArticleListMode.ArticleListModeRefer){
			bundle.putInt("art_mode", refermode);
			bundle.putInt("art_position", position);
			if(boardid != null){
				bundle.putString("boardid", boardid);
			}
			if(boardname != null){
				bundle.putString("boardname", boardname);
			}
			bundle.putLong("art_id", art_id);
			bundle.putLong("cnt", 0);
		}else if(mode == ArticleListMode.ArticleListModeMail){
			bundle.putInt("art_mode", 10);
			bundle.putInt("art_position", position);
			if(boardid != null){
				bundle.putString("boardid", boardid);
			}
			if(boardname != null){
				bundle.putString("boardname", boardname);
			}
			bundle.putLong("art_id", art_id);
			bundle.putLong("cnt", 0);
		}else if(mode == ArticleListMode.ArticleListModeMailSent){
			bundle.putInt("art_mode", 11);
			bundle.putInt("art_position", position);
			if(boardid != null){
				bundle.putString("boardid", boardid);
			}
			if(boardname != null){
				bundle.putString("boardname", boardname);
			}
			bundle.putLong("art_id", art_id);
			bundle.putLong("cnt", 0);
		}else{
			bundle.putInt("art_mode", 0);
			bundle.putInt("art_position", 0);
			if(boardid != null){
				bundle.putString("boardid", boardid);
			}
			if(boardname != null){
				bundle.putString("boardname", boardname);
			}
			bundle.putLong("art_id", art_id);
			bundle.putLong("cnt", position);
		}
		
		intent.putExtras(bundle);
	    startActivity(intent);
	}
	
	private class MyOnItemClickListener implements OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position,
                long arg3){
			
			JSONObject dict = m_mtarrayInfo.get(position);
			
			long article_id = dict.optLong("id");
			if(mode == ArticleListMode.ArticleListModeHot){
				showArticleContent(dict.optString("board"), null, article_id, dict.optInt("count"));
			}else if(mode == ArticleListMode.ArticleListModeNormal){
				showArticleContent(m_lBoardId, m_lBoardName, article_id, dict.optInt("count"));
			}else if(mode == ArticleListMode.ArticleListModeMail){
				showArticleContent(null, null, 0, dict.optInt("position"));
			}else if(mode == ArticleListMode.ArticleListModeMailSent){
				showArticleContent(null, null, 0, dict.optInt("position"));
			}else{
				showArticleContent(dict.optString("board_id"), null, dict.optLong("id"), dict.optInt("position"));
			}
		}
	}
	
	@Override
	public void updateContent(){
		if(load_init_mode != 0){
			settitle();
		}
		adapter.notifyDataSetChanged();
	}
	
}
