package com.kihwan.park.facebook.sample;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FriendListActivity extends ListActivity {

	List<String> mFriendList = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
		String jsonData = extras.getString("jsonFriendData");
		
		mFriendList = new ArrayList<String>();
		
		try {
			JSONObject json = (JSONObject) new JSONTokener(jsonData).nextValue();
			JSONArray friendData = json.getJSONArray("data");
			JSONObject paging = json.getJSONObject("paging");
			for(int i=0; i<friendData.length(); i++) {
				JSONObject friend = (JSONObject) friendData.get(i);
				mFriendList.add( friend.getString("name") + friend.getString("id") );
			}
			
			setListAdapter(new ArrayAdapter<String>(this, R.layout.list_friend, mFriendList));
			Log.i("", "" + paging.length());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		ListView listView = getListView();
		listView.setTextFilterEnabled(true);
	}
}
