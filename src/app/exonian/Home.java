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
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class Home extends FragmentActivity {

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
			
	        View view = inflater.inflate(R.layout.news_fragment, container, false);
	        ListView listView = (ListView) view.findViewById(R.id.list_news);
	        String list[] = {"dumb1", "dumb2"};
	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list);
	        listView.setAdapter(adapter);

	        return view;
			/*Button button = new Button(getActivity());
		    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		    button.setLayoutParams(params);

			ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.news_fragment, container, false);
			rootView.addView(button);
			return rootView;*/
		}
		
		public class DownloadNews extends AsyncTask<String, Void, String> {
			
			private Activity activity;
			
			public DownloadNews(Activity activity) {
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
					String contentText = (String) ((JSONObject) jsonObject.get("post")).get("content");
					final String titleText = (String) ((JSONObject) jsonObject.get("post")).get("title");
					final String authorText = (String) ((JSONObject) ((JSONObject) jsonObject.get("post")).get("author")).get("name");
					final String dateText = (String) ((JSONObject) jsonObject.get("post")).get("date");
					
					// Set the font to Ebrima
					final Typeface tf = Typeface.createFromAsset(activity.getAssets(), "fonts/ebrima.ttf");
					
					activity.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							((TextView) activity.findViewById(R.id.article_title)).setText(titleText);
							((TextView) activity.findViewById(R.id.article_author)).setText(authorText);
							((TextView) activity.findViewById(R.id.article_date)).setText(dateText);

							((TextView) activity.findViewById(R.id.article_content)).setTypeface(tf);
							((TextView) activity.findViewById(R.id.article_title)).setTypeface(tf, Typeface.BOLD);
							((TextView) activity.findViewById(R.id.article_author)).setTypeface(tf, Typeface.ITALIC);
						}

					});

					// Do not get the CSS after the </p> tag for the content text
					contentText = contentText.substring(0, contentText.indexOf("<style type='text/css'>"));
					contentText = contentText.substring(0, contentText.lastIndexOf("</p>"));

					final String finalContent = contentText;

					// Run the UI changes in the UI thread per thread policy.
					activity.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							((TextView) activity.findViewById(R.id.article_content)).setText(Html.fromHtml(finalContent));
						}

					});
					// articles.insertArticle(titleText, authorText, finalContent, dateText);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		}
	}
}
