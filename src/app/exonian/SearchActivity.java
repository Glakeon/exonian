/**
 * 
 */
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
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;

/**
 * @author Weihang Fan
 * 
 */
public class SearchActivity extends ListActivity implements
		OnQueryTextListener, OnCloseListener {

	// The SearchView for doing filtering.
	SearchView mSearchView;

	// If non-null, this is the current filter the user has provided.
	String mCurFilter;

	private ArrayAdapter<String> mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);

		// Get the intent, verify the action and get the query
		Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);

			// Create an empty adapter we will use to display the loaded data.
			mAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1);
			setListAdapter(mAdapter);
			(new SearchArticle(this)).execute("http://theexonian.com/new/?s=" + query + "&json=1&include=title,url");
		}
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Place an action bar item for searching.
		MenuItem item = menu.add("Search");
		item.setIcon(android.R.drawable.ic_menu_search);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		mSearchView = new SearchView(this);
		mSearchView.setOnQueryTextListener(this);
		mSearchView.setOnCloseListener(this);
		mSearchView.setIconifiedByDefault(false);
		item.setActionView(mSearchView);
	}

	public boolean onQueryTextChange(String newText) {
		// Called when the action bar search text has changed. Update
		// the search filter, and restart the loader to do a new query
		// with this filter.
		String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
		// Don't do anything if the filter hasn't actually changed.
		// Prevents restarting the loader when restoring state.
		if (mCurFilter == null && newFilter == null) {
			return true;
		}
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		// Don't care about this.
		return true;
	}

	@Override
	public boolean onClose() {
		if (!TextUtils.isEmpty(mSearchView.getQuery())) {
			mSearchView.setQuery(null, true);
		}
		return true;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent i = new Intent("android.intent.action.ArticleActivity");
		i.putExtra("url", mAdapter.getItem(position));
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
				return sb.toString();
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
				// Construct a JSON object and get the content of the post
				JSONObject jsonObject = new JSONObject(result);
				JSONArray jsonArray = jsonObject.getJSONArray("posts");
				ArrayList<String> list = new ArrayList<String>();
				if (jsonArray != null) { 
				   int len = jsonArray.length();
				   for (int i=0;i<len;i++){ 
				    list.add(jsonArray.get(i).toString());
				   } 
				}
				final ArrayList<String> copy = list;
				
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
