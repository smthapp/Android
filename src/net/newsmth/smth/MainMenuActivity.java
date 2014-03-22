package net.newsmth.smth;

import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class MainMenuActivity extends AppActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	private void showBoardList(int m_mode){
		Intent intent = new Intent(this, BoardListActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("mode", m_mode);
	    intent.putExtras(bundle);
	    startActivity(intent);
	}
	
	public void pressBtnListBoard(View v){
		showBoardList(1);
	}
	
	public void pressBtnFav(View v){
		showBoardList(2);
	}
	
	public void pressBtnSectionTop(View v){
		showBoardList(3);
	}
	
	public void pressBtnMemberBoard(View v){
		showBoardList(5);
	}
	
	public void pressBtnTop(View v){
		Intent intent = new Intent(this, ArticleListActivity.class);
		Bundle bundle = new Bundle();
		
	
		bundle.putInt("mode", 1);
		bundle.putInt("hotmode_section", 0);
		
		intent.putExtras(bundle);
	    startActivity(intent);
	}
	
	public void pressBtnMessages(View v){
		Intent intent = new Intent(this, MessagesActivity.class);
	    startActivity(intent);
	}
	
	public void pressBtnAppSetting(View v){
		Intent intent = new Intent(this, AppSettingActivity.class);
	    startActivity(intent);
	}
}
