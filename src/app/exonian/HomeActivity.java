package app.exonian;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import app.exonian.ArticleActivity.DownloadArticle;
import app.exonian.ArticleActivity.DownloadImage;

public class HomeActivity extends FragmentActivity {

	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	boolean connected = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Check to see if device is connected to internet
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		
		if (networkInfo != null && networkInfo.isConnected()) {
			if (connected == false) {
				// Create the adapter that will return a fragment for each of the three primary sections of the app
				mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

				// Set up the ViewPager with the sections adapter
				mViewPager = (ViewPager) findViewById(R.id.pager);
				mViewPager.setAdapter(mSectionsPagerAdapter);
				connected = true;
			}
		} else {
			connected = false;
			Toast.makeText(this, "No network connection available.", Toast.LENGTH_LONG).show();
		}
	}
	
	public void showArticle(View view) {
		Intent i = new Intent("android.intent.action.ArticleActivity");
		startActivity(i);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu and adds items to the action bar if it is present
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		startActivity(new Intent("android.intent.action.SearchActivity"));
		return true;
	}
	
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// Called to instantiate the fragment for the given page.
			Bundle bundle = new Bundle();
			switch (position) {
				case 0:
					bundle.putString("url", "http://theexonian.com/new/category/news/?json=1&include=title,url,attachments");
					break;
				case 1:
					bundle.putString("url", "http://theexonian.com/new/category/humor/?json=1&include=title,url,attachments");
					break;
				case 2:
					bundle.putString("url", "http://theexonian.com/new/category/exeter-life/?json=1&include=title,url,attachments");
					break;
				case 3:
					bundle.putString("url", "http://theexonian.com/new/category/opinion/?json=1&include=title,url,attachments");
					break;
				case 4:
					bundle.putString("url", "http://theexonian.com/new/category/sports/?json=1&include=title,url,attachments");
					break;
					
			}
			Fragment fragment = new ExonianFragment();
			fragment.setArguments(bundle);
			return fragment;
		}

		@Override
		public int getCount() {
			return 5;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return "News";
			case 1:
				return "Humor";
			case 2:
				return "Life";
			case 3:
				return "Opinion";
			case 4:
				return "Sports";
			}
			return null;
		}
	}
	
	public static class ExonianFragment extends Fragment {
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			super.onCreateView(inflater, container, savedInstanceState);
			
			// Allow network access in the main thread
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			
			String newsFeed = getPage(getArguments().getString("url"));
			String[] articleTitles = {};
			String[] articleLinks = {};
			String[] imageLinks = {};
			
			try {
				// Construct a JSON object and get the content of the post
				JSONArray posts = (JSONArray) ((new JSONObject(newsFeed)).get("posts"));
				articleTitles = new String[posts.length()];
				articleLinks = new String[posts.length()];
				imageLinks = new String[posts.length()];
				for (int i = 0; i < posts.length(); i++) {
					articleTitles[i] = (posts.getJSONObject(i).getString("title")).replaceAll("&#[0-9]+;", "'");
					articleLinks[i] = posts.getJSONObject(i).getString("url");
					JSONArray attachments = posts.getJSONObject(i).getJSONArray("attachments");
					if (attachments.length() > 0) {
						imageLinks[i] = attachments.getJSONObject(0).getString("url");
					} else {
						if (posts.getJSONObject(i).has("thumbnail_images")) {
							JSONArray images = posts.getJSONObject(i).getJSONArray("thumbnail_images");
							if (images.length() > 0) {
								imageLinks[i] = ((JSONObject) (images.get(0))).getString("url");
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// Must be final to access in onClickListener
			final String[] finalArticleLinks = articleLinks;
			final String[] finalImageLinks = imageLinks;
			
			// Set the custom ListView
	        View view = inflater.inflate(R.layout.news_fragment, container, false);
	        ListView listView = (ListView) view.findViewById(R.id.list_news);
	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.article_list, R.id.list_article_title, articleTitles);
	        listView.setAdapter(adapter);
	        
	        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent i = new Intent("android.intent.action.ArticleActivity");
					i.putExtra("article_url", finalArticleLinks[position]);
					i.putExtra("image_url", finalImageLinks[position]);
					startActivity(i);
				}
	        	
			});
	        return view;
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
		
		private String getPage(String URL) {
			InputStream is = null;
			try {
				// The URL is the first in the array
				URI url = URI.create(URL);
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
		
	}

	
}
