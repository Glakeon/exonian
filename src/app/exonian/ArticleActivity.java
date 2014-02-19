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
import android.widget.TextView;
import android.widget.Toast;

public class ArticleActivity extends FragmentActivity {

	Adapter db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final String stringUrl = getIntent().getStringExtra("url") + "?json=1";
		db = new Adapter(this);
		db.open();
		
		setContentView(R.layout.article);
		
		// Connect to a specific article and image
		final String imageUrl = "http://theexonian.com/new/wp-content/uploads/2014/02/DSC08103-700x466.jpg";
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		
		// Make sure the device is connected
		if (networkInfo != null && networkInfo.isConnected()) {
			(new DownloadArticle(this)).execute(stringUrl);
			(new DownloadImage(this)).execute(imageUrl);
		} else {
			Toast.makeText(this, "No network connection available.", Toast.LENGTH_LONG).show();
		}

        getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return false;
		
	}
	
	// Task to download the content from the Article
	public class DownloadArticle extends AsyncTask<String, Void, String> {
		
		private Activity activity;
		
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
				contentText = contentText.substring(0, contentText.indexOf("style="));
				contentText = contentText.substring(0, contentText.lastIndexOf("</p>"));

				final String finalContent = contentText;

				// Run the UI changes in the UI thread per thread policy.
				activity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						((TextView) activity.findViewById(R.id.article_content)).setText(Html.fromHtml(finalContent));
					}

				});
				// Log.d("Database", Long.toString(db.insertArticle(titleText, authorText, finalContent, dateText)));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}
	
	// Task to download an image from a URL
	public class DownloadImage extends AsyncTask<String, Void, Bitmap> {
		
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
