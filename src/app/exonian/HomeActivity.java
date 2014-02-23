package app.exonian;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
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

public class HomeActivity extends FragmentActivity {

	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		// Create the adapter that will return a fragment for each of the three primary sections of the app
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
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
			Fragment fragment = new NewsFragment();
			Bundle args = new Bundle();
			args.putInt(NewsFragment.SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return "News";
			case 1:
				return "Humor";
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}
	
	public static class NewsFragment extends Fragment {
		public static final String SECTION_NUMBER = "section_number";

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			super.onCreateView(inflater, container, savedInstanceState);
			
			// Allow network access in the main thread
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			
			String newsFeed = getPage("http://theexonian.com/new/category/news/?json=1");
			String[] articleTitles = {};
			String[] articleLinks = {};
			String[] imageLinks = {};
			
			try {
				// Construct a JSON object and get the content of the post
				JSONArray posts = (JSONArray) (new JSONObject(newsFeed)).get("posts");
				articleTitles = new String[posts.length()];
				articleLinks = new String[posts.length()];
				imageLinks = new String[posts.length()];
				for (int i = 0; i < posts.length(); i++) {
					articleTitles[i] = posts.getJSONObject(i).getString("title");
					articleLinks[i] = posts.getJSONObject(i).getString("url");
					JSONArray attachments = posts.getJSONObject(i).getJSONArray("attachments");
					if (attachments.length() > 0) {
						imageLinks[i] = attachments.getJSONObject(0).getString("url");
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
