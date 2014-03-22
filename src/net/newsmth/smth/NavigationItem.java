package net.newsmth.smth;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NavigationItem extends LinearLayout {
	
	public Button rightBarButtonItem;
	public Button leftBarButtonItem;
	public TextView title;
	
	private onLeftCLickListener left_lis;
	private onRightCLickListener right_lis;

	public NavigationItem(Context context) {
		super(context);
		layout_init(context);
	}
	
	public NavigationItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		layout_init(context);
	}
	
	private void layout_init(Context context){
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.navigation_item, this);
		
		title = (TextView) findViewById(R.id.NagivationItem_title);
		rightBarButtonItem = (Button) findViewById(R.id.NagivationItem_rightBarButtonItem);
		leftBarButtonItem = (Button) findViewById(R.id.NagivationItem_leftBarButtonItem);
		
		leftBarButtonItem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				pressBtnBack(arg0);	
			}
		});
		rightBarButtonItem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				pressBtnNew(arg0);
			}
		});
		
		rightBarButtonItem.setVisibility(INVISIBLE);
	}
	
	public interface onLeftCLickListener{
		public void onClick();
	}
	
	public interface onRightCLickListener{
		public void onClick();
	}
	
	public void setOnLeftClickListener(onLeftCLickListener l){
		left_lis = l;
	}
	
	public void setOnRightCLickListener(onRightCLickListener l){
		right_lis = l;
		if(right_lis != null){
			rightBarButtonItem.setVisibility(VISIBLE);
		}
	}
	
	public void pressBtnBack(View v){
		if(left_lis != null){
			left_lis.onClick();
		}
	}
	
	public void pressBtnNew(View v){
		if(right_lis != null){
			right_lis.onClick();
		}
	}

}
