package net.newsmth.smth;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ArticleEditActivity extends AppActivity {

	private String m_lBoardId;
	private String m_lBoardName;
	private JSONObject reply;
	private boolean mailmode;
	private boolean mail_from_article;
	
	//UI
	private TextView m_labelBoard;
	private EditText m_textTitle;
	private EditText m_textCont;
	private EditText text_mailrecv;
	private TextView label_mailrecv;
	private NavigationItem navi;
	//TODO: private boolean waiting_for_mbselect;
	//TODO: private String att0_fname;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_article_edit);
		
		Bundle bundle = getIntent().getExtras();
		if(null != bundle){
			boolean to_mail = bundle.getBoolean("to_mail");
			if(to_mail){
				mailmode = true;
				mail_from_article = bundle.getBoolean("mail_from_article");
			}else{
				mailmode = false;
				m_lBoardId = bundle.getString("boardid");
				if(m_lBoardId != null && m_lBoardId.length() == 0){
					m_lBoardId = null;
				}
				m_lBoardName = bundle.getString("boardname");
				if(m_lBoardName != null && m_lBoardName.length() == 0){
					m_lBoardName = null;
				}
			}
			String reply_str = bundle.getString("reply_dict");
			if(reply_str != null && reply_str.length() > 0){
				try {
					reply = new JSONObject(reply_str);
				} catch (JSONException e) {
					reply = null;
				}
			}else{
				reply = null;
			}
		}
		
		m_labelBoard = (TextView) findViewById(R.id.articleedit_m_labelBoard);
		m_textTitle = (EditText) findViewById(R.id.articleedit_m_textTitle);
		m_textCont = (EditText) findViewById(R.id.articleedit_m_textCont);
		text_mailrecv = (EditText) findViewById(R.id.articleedit_text_mailrecv);
		label_mailrecv = (TextView) findViewById(R.id.articleedit_label_mailrecv);
		
		navi = (NavigationItem) findViewById(R.id.articleeditNavi);
		navi_enable_left(navi);
		navi.setOnRightCLickListener(new NavigationItem.onRightCLickListener() {
			@Override
			public void onClick() {
				loadContent();
			}
		});
		navi.rightBarButtonItem.setText("发表");
		
		if(!mailmode){
			if(m_lBoardId == null){
				label_mailrecv.setVisibility(View.INVISIBLE);
				m_labelBoard.setVisibility(View.INVISIBLE);
				text_mailrecv.setText("");
			}else{
				String strBoard;
				label_mailrecv.setVisibility(View.INVISIBLE);
				text_mailrecv.setVisibility(View.INVISIBLE);
				if(m_lBoardName == null){
					strBoard = "版面:" + m_lBoardId;
				}else{
					strBoard = "版面:" + m_lBoardName;
				}
				m_labelBoard.setText(strBoard);
				//TODO: btn_mbselect INVISIBLE
			}
			navi.title.setText("发表文章");
		}else{
			if(reply != null){
				label_mailrecv.setVisibility(View.INVISIBLE);
				text_mailrecv.setVisibility(View.INVISIBLE);
				m_labelBoard.setText("收件人:" + reply.optString("author_id"));
			}else{
				m_labelBoard.setVisibility(View.INVISIBLE);
			}
			//TODO: btn_mbselect INVISIBLE
			navi.title.setText("发送邮件");
		}
		
		if(reply != null){
			String title = reply.optString("subject");
			String author = reply.optString("author_id");
			String body = reply.optString("body");
			
			m_textCont.requestFocus();
			
			if(body == null || body.length() == 0){
				m_textTitle.setText(title);
			}else{
				if(title.length() < 4){
					m_textTitle.setText("Re: " + title);
				}else{
					String tre = title.substring(0, 4);
					if(tre.equalsIgnoreCase("Re: ")){
						m_textTitle.setText(title);
					}else{
						m_textTitle.setText("Re: " + title);
					}
				}
				
				String bodyRef = "\n【 在 " + author + " 的" + (mailmode?"来信":"大作") + "中提到: 】\n";
				int quot_line = 3;
				int range_location = 0;
				for(;quot_line > 0; quot_line --){
					int t_range = body.substring(range_location).indexOf("\n");
					if(t_range >= 0){
						bodyRef += ": " + body.substring(range_location, t_range + range_location + 1);
						range_location += (t_range + 1);
					}else{
						break;
					}
				}
				int t_range = body.substring(range_location).indexOf("\n");
				if(t_range >= 0){
					bodyRef += ": ....................\n";
				}
				m_textCont.setText(bodyRef);
			}
		}
		
		//TODO: mbselect, att0, ...
	}

	
	@Override
	public boolean scroll_enabled(){
		return false;
	}
	
	@Override
	public void parseContent()
	{
		String strTitle = m_textTitle.getText().toString();
		if(strTitle.length() == 0){
			strTitle = "无标题";
		}
		String strCont = m_textCont.getText().toString();
		
		@SuppressWarnings("unused")
		long article_id;
		
		if(mailmode){
			if(reply != null && !mail_from_article){
				//TODO: upload Att
				article_id = net_smth.net_ReplyMail(reply.optInt("position"), strTitle, strCont);
			}else{
				String receiver;
				if(reply != null){
					receiver = reply.optString("author_id");
				}else{
					receiver = text_mailrecv.getText().toString();
				}
				if(receiver == null || receiver.length() == 0){
					return;
				}else{
					//TODO: uploadAtt
					article_id = net_smth.net_PostMail(receiver, strTitle, strCont);
				}
			}
		}else{
			if(reply != null){
				//TODO: upload ATT
				article_id = net_smth.net_ReplyArticle(m_lBoardId, reply.optInt("id"), strTitle, strCont);
			}else{
				if(m_lBoardId == null){
					//TODO: alert dialog
					return;
				}else{
					//TODO: upload att
					article_id = net_smth.net_PostArticle(m_lBoardId, strTitle, strCont);
				}
			}
		}
		
		if(net_smth.net_error == 0){
			m_bLoadRes = 1;
		}
	}

	@Override
	public void updateContent(){
		//TODO: remove Att
		this.finish();
	}
}
