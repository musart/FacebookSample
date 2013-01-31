package com.kihwan.park.facebook.sample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String URL_PREFIX_FRIENDS = "https://graph.facebook.com/me/friends?access_token=";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button connectButton = (Button) findViewById(R.id.connectButton);
		connectButton.setText(R.string.connecting);
		connectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleLogInOut();
			}
		});

		Button postButton = (Button) findViewById(R.id.postButton);
		postButton.setEnabled(false);
		postButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				updatePostStatus();
			}
		});
		
		Button friendButton = (Button) findViewById(R.id.friendButton);
		friendButton.setEnabled(false);
		friendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getFriendList();
			}
		});

		Session.openActiveSession(this, true, new Session.StatusCallback() {

			@Override
			public void call(Session session, SessionState state, Exception exception) {
				Log.i("", "call()");
				if (session.isOpened()) {
					Log.i("", "session.isOpened()");
					Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

						@Override
						public void onCompleted(GraphUser user, Response response) {
							Log.i("", "onCompleted()");
							if (user != null) {
								Log.i("", "user name : " + user.getName());

								updateStatus(user);
							}
						}
					});
				} else {
					Log.i("", "session.isOpened() false");
					updateStatus(null);
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

	private void updateStatus(GraphUser user) {
		if (user != null) {
			TextView accountTextView = (TextView) findViewById(R.id.account);
			accountTextView.setText(user.getName());

			ProfilePictureView profilePictureView = (ProfilePictureView) findViewById(R.id.photo);
			profilePictureView.setProfileId(user.getId());

			Button connectButton = (Button) findViewById(R.id.connectButton);
			connectButton.setText(R.string.connected);

			Button postButton = (Button) findViewById(R.id.postButton);
			postButton.setEnabled(true);
			
			Button friendButton = (Button) findViewById(R.id.friendButton);
			friendButton.setEnabled(true);
		} else {

		}
	}
	
	private void handleLogInOut() {
		
	}

	private void updatePostStatus() {
		Session session = Session.getActiveSession();
		if (session != null && session.getPermissions().contains("publish_actions")) {
			Log.i("", "I have a permission!");
			//handlePendingAction();
		} else {
			Log.i("", "I have a no permission!");
			session.requestNewPublishPermissions(new Session.NewPermissionsRequest(
					this, Arrays.asList("publish_actions")));
		}
	}
	
	private void getFriendList() {
		Session session = Session.getActiveSession();
		if (session != null) {
			String url = URL_PREFIX_FRIENDS + session.getAccessToken();
			Log.i("", "url:" + url);
			
			JSONTask jsonTask = new JSONTask();
			jsonTask.execute(url);
		}
	}

	public class JSONTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... arg) {
			String lineData = "";
			String result = "";
			String url = arg[0];

			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(url);

			try {
				HttpResponse response = client.execute(get);

				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();

				if (statusCode == 200) {
					BufferedReader rd = new BufferedReader(
							new InputStreamReader(response.getEntity().getContent()));

					while ((lineData = rd.readLine()) != null) {
						result += lineData;
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			Log.i("", "result:" + result);
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				JSONObject friendList = (JSONObject) new JSONTokener(result).nextValue();
				Log.i("", "" + friendList.length());
				Log.i("", "" + friendList.getString("data").length());
				Log.i("", "" + friendList.getString("paging").length());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
