package theexonian.theexonian;

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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Article extends FragmentActivity {

	private static final String DEBUG_TAG = "exie";

	/**
	 * @author Weihang Fan
	 * 
	 */
	private class DownloadWebpageTask extends AsyncTask<String, Void, String> {

		// Reads an InputStream and converts it to a String.
		public String readIt(InputStream stream) {
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(stream, "utf-8"), 8);
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
		 */
		@Override
		protected String doInBackground(String... urls) {
			// urls comes from the execute() call: urls[0] is the url.
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
					// Convert the InputStream into a string
					String contentAsString = readIt(is);
					return contentAsString;
				} else {
					return "Error loading the article.";
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				// Makes sure that the InputStream is closed after the app is
				// finished using it.
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
				String contentText = (String) ((JSONObject) jsonObject
						.get("post")).get("content");
				final String titleText = (String) ((JSONObject) jsonObject
						.get("post")).get("title");
				final String authorText = (String) ((JSONObject) ((JSONObject) jsonObject
						.get("post")).get("author")).get("name");

				// Do not get the CSS after the </p> tag for the content text
				contentText = contentText.substring(0,
						contentText.indexOf("<style type='text/css'>"));
				contentText = contentText.substring(0,
						contentText.lastIndexOf("</p>"));

				final String finalContent = contentText;

				// Set the font to Ebrima
				final Typeface tf = Typeface.createFromAsset(getAssets(),
						"fonts/ebrima.ttf");

				// Run the UI changes in the UI thread per thread policy.
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						((TextView) findViewById(R.id.article_content))
								.setText(Html.fromHtml(finalContent));
						((TextView) findViewById(R.id.article_title))
								.setText(titleText);
						((TextView) findViewById(R.id.article_author))
								.setText(authorText);

						((TextView) findViewById(R.id.article_content))
								.setTypeface(tf);
						((TextView) findViewById(R.id.article_title))
								.setTypeface(tf, Typeface.BOLD);
						((TextView) findViewById(R.id.article_author))
								.setTypeface(tf, Typeface.ITALIC);
					}

				});
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * @author Weihang Fan
	 * 
	 */
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
		 */
		@Override
		protected Bitmap doInBackground(String... urls) {
			// urls comes from the execute() call: urls[0] is the url.
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
					// Convert the InputStream into a bitmap
					return BitmapFactory.decodeStream(is);
				} else {
					Log.e(DEBUG_TAG, "Error loading the image.");
				}

				// Makes sure that the InputStream is closed after the app is
				// finished using it.
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
			Log.e(DEBUG_TAG, "Error loading the image.");
			return null;
		}

		@Override
		protected void onPostExecute(final Bitmap result) {
			// Run the UI changes in the UI thread per thread policy.
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					((ImageView) findViewById(R.id.article_image))
							.setImageBitmap(result);
				}

			});
		}
	}

	private TextView title;
	private TextView author;
	private TextView date;
	private TextView content;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.article);

		// Access the TextViews
		content = (TextView) findViewById(R.id.article_content);
		title = (TextView) findViewById(R.id.article_title);
		author = (TextView) findViewById(R.id.article_author);

		final String stringUrl = "http://theexonian.com/new/2014/02/13/boys-track-wins-ea-girls-fall-close-behind/?json=1";
		final String imageUrl = "http://theexonian.com/new/wp-content/uploads/2014/02/DSC08103-700x466.jpg";
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			new DownloadWebpageTask().execute(stringUrl);
			new DownloadImageTask().execute(imageUrl);
		} else {
			Toast.makeText(this, "No network connection available.",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}
}
