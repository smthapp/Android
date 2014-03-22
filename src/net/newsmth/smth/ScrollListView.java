package net.newsmth.smth;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;

//TODO:
//add headview and footerview

public class ScrollListView extends ListView implements OnScrollListener {

	private final int HEAD_HEIGHT = 100;
	private final int FOOTER_HEIGHT = 100;
	
	private int first_item_index = 0;
	private int last_item_index = 0;
	private int total_index = 0;
	
	private boolean refreshed;
	private boolean scrolling;
	private boolean refresh_recorded;
	private int refresh_start_y;
	private boolean loadmore_recorded;
	private int loadmore_start_y;
	
	private enum ScrollAction{
		ScrollAction_NONE,
		ScrollAction_REFRESH,
		ScrollAction_LOADMORE,
	};
	private ScrollAction last_action;
	
	private OnLoadMoreListener m_loadmore_listener = null;
	private OnRefreshListener m_refresh_listener = null;
	
	public ScrollListView(Context ctx){
		super(ctx);
		scroll_init(ctx);
	}
	
	public ScrollListView(Context ctx, AttributeSet attrs){
		super(ctx, attrs);
		scroll_init(ctx);
	}
	
	public ScrollListView(Context ctx, AttributeSet attrs, int style){
		super(ctx, attrs, style);
		scroll_init(ctx);
	}
	
	private void scroll_init(Context ctx){
		
		refreshed = false;
		loadmore_recorded = false;
		refresh_recorded = false;
		scrolling = false;
		
		setOnScrollListener(this);
	}
	
	public interface OnLoadMoreListener {
		public void onLoadMore();
	}
	
	public interface OnRefreshListener {
		public void onRefresh();
	}
	
	public void setOnLoadMoreListener(OnLoadMoreListener l){
		m_loadmore_listener = l;
	}
	
	public void setOnRefreshListener(OnRefreshListener l){
		m_refresh_listener = l;
	}

	@Override
	public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
		first_item_index = arg1;
		last_item_index = arg1 + arg2;
		total_index = arg3;
	}
	
	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
		switch(arg1){
		case SCROLL_STATE_IDLE:
			scrolling = false;
			break;
		case SCROLL_STATE_FLING:
			scrolling = true;
			break;
		default:
			break;
		}
	}
	
	public boolean onTouchEvent(MotionEvent event)
	{
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if(scrolling){
				break;
			}
			if(first_item_index == 0){
				if(!refresh_recorded && !refreshed){
					refresh_recorded = true;
					refresh_start_y = (int) event.getY();
				}
			}
			if(last_item_index == total_index){
				if(!loadmore_recorded && !refreshed){
					loadmore_recorded = true;
					loadmore_start_y = (int) event.getY();
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if(refreshed){
				if(last_action == ScrollAction.ScrollAction_REFRESH){
					Log.d("SMTH", "on refresh");
					if(m_refresh_listener != null){
						m_refresh_listener.onRefresh();
					}
				}else if(last_action == ScrollAction.ScrollAction_LOADMORE){
					Log.d("SMTH", "on loadmore");
					if(m_loadmore_listener != null){
						m_loadmore_listener.onLoadMore();
					}
				}
				refreshed = false;
				refresh_recorded = false;
				loadmore_recorded = false;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if(scrolling){
				break;
			}
			int cur_y = (int) event.getY();
			if(refreshed){
				break;
			}
			if(first_item_index == 0){
				if(!refresh_recorded && !refreshed){
					refresh_recorded = true;
					refresh_start_y = (int) event.getY();
				}
				if(cur_y - refresh_start_y > HEAD_HEIGHT){
					last_action = ScrollAction.ScrollAction_REFRESH;
					refreshed = true;
					break;
				}
			}
			if(last_item_index == total_index){
				if(!loadmore_recorded && !refreshed){
					loadmore_recorded = true;
					loadmore_start_y = (int) event.getY();
				}
				if(loadmore_start_y - cur_y > FOOTER_HEIGHT){
					last_action = ScrollAction.ScrollAction_LOADMORE;
					refreshed = true;
					break;
				}
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(event);
	}
}
