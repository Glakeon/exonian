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
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

public class ArticleActivity extends FragmentActivity {

	private Adapter db;
	private ShareActionProvider mShareActionProvider;
	private String link;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    
	    link = getIntent().getStringExtra("article_url");
		final String stringUrl = getIntent().getStringExtra("article_url") + "?json=1";
		final String imageUrl = getIntent().getStringExtra("image_url");
		db = new Adapter(this);
		db.open();
		
		setContentView(R.layout.article);
		
		// Check to see if device is connected to internet
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		
		if (networkInfo != null && networkInfo.isConnected()) {
			(new DownloadArticle(this)).execute(stringUrl);
			if (imageUrl != null) {
				(new DownloadImage(this)).execute(imageUrl);
			}
		} else {
			Toast.makeText(this, "No network connection available.", Toast.LENGTH_LONG).show();
		}

        getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu and adds items to the action bar if it is present
		getMenuInflater().inflate(R.menu.home, menu);
		MenuItem item = menu.findItem(R.id.menu_item_share);
		mShareActionProvider = (ShareActionProvider) item.getActionProvider();
		Intent i = new Intent();
		i.setAction(Intent.ACTION_SEND);
		i.putExtra(Intent.EXTRA_TEXT, link);
		i.setType("text/plain");
		mShareActionProvider.setShareIntent(i);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.search_button:
			startActivity(new Intent("android.intent.action.SearchActivity"));
			break;
		default:
			break;
		}
		return false;
		
	}
	
	// Task to download the content from the Article
	class DownloadArticle extends AsyncTask<String, Void, String> {
		
		private Activity activity;
		private Article cached = null;
		
		public DownloadArticle(Activity activity) {
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
			Article ret = db.searchArticle(urls[0]);
			if (ret == null) {
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
			else {
				cached = ret;
				return "";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (cached == null) {
				try {
					// Construct a JSON object and get the content of the post
					JSONObject jsonObject = new JSONObject(result);
					JSONObject post = jsonObject.getJSONObject("post");
					final String urlText = post.getString("url");
					String contentText = post.getString("content");
					final String titleText = post.getString("title").replaceAll("&#[0-9]+;", "'");
					final String authorText = post.getJSONObject("author").getString("name");
					final String dateText = post.getString("date");
					
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
					contentText = contentText.substring(0, contentText.indexOf("style="));
					contentText = contentText.substring(0, contentText.lastIndexOf("</p>"));
					contentText = contentText.replace("&#8217;", "'");
	
					final String finalContent = contentText;
	
					// Run the UI changes in the UI thread per thread policy.
					activity.runOnUiThread(new Runnable() {
	
						@Override
						public void run() {
							((TextView) activity.findViewById(R.id.article_content)).setText(Html.fromHtml(finalContent));
						}
	
					});
	
					try {
						db.insertArticle(titleText, authorText, finalContent, dateText, urlText + "?json=1");
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			else {
				activity.runOnUiThread(new Runnable() {

					// Set the font to Ebrima
					final Typeface tf = Typeface.createFromAsset(activity.getAssets(), "fonts/ebrima.ttf");
					
					@Override
					public void run() {
						((TextView) activity.findViewById(R.id.article_title)).setText(cached.getTitle());
						((TextView) activity.findViewById(R.id.article_author)).setText(cached.getAuthor());
						((TextView) activity.findViewById(R.id.article_date)).setText(cached.getDate());

						((TextView) activity.findViewById(R.id.article_content)).setTypeface(tf);
						((TextView) activity.findViewById(R.id.article_title)).setTypeface(tf, Typeface.BOLD);
						((TextView) activity.findViewById(R.id.article_author)).setTypeface(tf, Typeface.ITALIC);
						
						((TextView) activity.findViewById(R.id.article_content)).setText(Html.fromHtml(cached.getContent()));
					}

				});
			}
		}

	}
	
	// Task to download an image from a URL
	class DownloadImage extends AsyncTask<String, Void, Bitmap> {
		
		private Activity activity;
		
		public DownloadImage(Activity activity) {
			this.activity = activity;
		}
		@Override
		protected Bitmap doInBackground(String... urls) {
			InputStream is = null;
			try {
				URI url = URI.create(urls[0]);
				HttpClient httpclient = new DefaultHttpClient();
				HttpGet httpget = new HttpGet(url);
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();
				
				// Starts the query
				is = entity.getContent();
				if (response.getStatusLine().getStatusCode() == 200) {
					// Convert the InputStream into a Bitmap
					return BitmapFactory.decodeStream(is);
				} else {
					Log.e("Error", "Error loading the image.");
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			Log.e("Error", "Error loading the image.");
			return null;
		}

		@Override
		protected void onPostExecute(final Bitmap result) {
			// Run the UI changes in the UI thread
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					((ImageView) activity.findViewById(R.id.article_image)).setImageBitmap(result);
				}
			});
		}
	}
}
