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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class Download {
	private String DEBUG_TAG = "error";
	
	public class Page extends AsyncTask<String, Void, String> {
		
		private Activity activity;
		
		public Page(Activity activity) {
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

				// Do not get the CSS after the </p> tag for the content text
				contentText = contentText.substring(0, contentText.indexOf("<style type='text/css'>"));
				contentText = contentText.substring(0, contentText.lastIndexOf("</p>"));

				final String finalContent = contentText;

				// Set the font to Ebrima
				final Typeface tf = Typeface.createFromAsset(activity.getAssets(), "fonts/ebrima.ttf");

				// Run the UI changes in the UI thread per thread policy.
				activity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						((TextView) activity.findViewById(R.id.article_content)).setText(Html.fromHtml(finalContent));
						((TextView) activity.findViewById(R.id.article_title)).setText(titleText);
						((TextView) activity.findViewById(R.id.article_author)).setText(authorText);

						((TextView) activity.findViewById(R.id.article_content)).setTypeface(tf);
						((TextView) activity.findViewById(R.id.article_title)).setTypeface(tf, Typeface.BOLD);
						((TextView) activity.findViewById(R.id.article_author)).setTypeface(tf, Typeface.ITALIC);
					}

				});
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}
	
	// Task to download an image from a URL
	public class Image extends AsyncTask<String, Void, Bitmap> {
		
		private Activity activity;
		
		public Image(Activity activity) {
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
					// Convert the InputStream into a bitmap
					return BitmapFactory.decodeStream(is);
				} else {
					Log.e(DEBUG_TAG, "Error loading the image.");
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
			Log.e(DEBUG_TAG, "Error loading the image.");
			return null;
		}

		@Override
		protected void onPostExecute(final Bitmap result) {
			// Run the UI changes in the UI thread
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					((ImageView) activity.findViewById(R.id.article_image))
							.setImageBitmap(result);
				}
			});
		}
	}
}
