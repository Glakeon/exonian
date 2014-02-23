package app.exonian;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class SearchActivity extends ListActivity {

	private ArrayAdapter<Article> mAdapter;
	private EditText filterText;
	private Activity ctx;
	
	private TextWatcher filterTextWatcher = new TextWatcher() {

		public void afterTextChanged(Editable s) {
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			(new SearchArticle(ctx)).execute("http://theexonian.com/new/?s=" + s + "&json=1&include=title,url");
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
		ctx = this;

		// verify the action and get the query
		// Create an empty adapter we will use to display the loaded data.
		mAdapter = new ArrayAdapter<Article>(this,
				android.R.layout.simple_list_item_1);
		setListAdapter(mAdapter);

		filterText = (EditText) findViewById(R.id.search_box);
		filterText.addTextChangedListener(filterTextWatcher);
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent i = new Intent("android.intent.action.ArticleActivity");
		i.putExtra("article_url", mAdapter.getItem(position).getUrl());
		startActivity(i);
	}
	
	// Task to download the content from the Article
	class SearchArticle extends AsyncTask<String, Void, String> {
		
		private Activity activity;
		
		public SearchArticle(Activity activity) {
			this.activity = activity;
		}
		
		// Reads an InputStream and converts it to a string
		public String readIt(InputStream stream) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "utf-8"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				stream.close();
				return sb.toString().replace("&#8217;", "'");
			} catch (IOException e) {
				return "Error loading the article.";
			}
		}

		@Override
		protected String doInBackground(String... urls) {
			InputStream is = null;
			try {
				// The URL is the first in the array
				URI url = URI.create(urls[0]);
				HttpClient httpclient = new DefaultHttpClient();
				HttpGet httpget = new HttpGet(url);
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();
				
				// Starts the query
				is = entity.getContent();
				if (response.getStatusLine().getStatusCode() == 200) {
					
					// Convert the InputStream into a string
					String contentAsString = readIt(is);
					return contentAsString;
				} else {
					return "Error loading the article.";
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				// Close the InputStream
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return "Error loading the article.";
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				// Construct a JSON object and set the adapter to the search result
				JSONObject jsonObject = new JSONObject(result);
				JSONArray jsonArray = jsonObject.getJSONArray("posts");
				ArrayList<Article> list = new ArrayList<Article>();
				if (jsonArray != null) { 
				   int len = jsonArray.length();
				   for (int i = 0; i < len; i++){ 
					   Article toAdd = new Article();
					   JSONObject post = jsonArray.getJSONObject(i);
					   toAdd.setTitle(post.getString("title"));
					   toAdd.setUrl(post.getString("url"));
					   list.add(toAdd);
				   } 
				}
				final ArrayList<Article> copy = list;
				
				activity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mAdapter.clear();
						mAdapter.addAll(copy);
					}

				});
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}
}
