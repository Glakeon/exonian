package sparkjolt.exonian;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.Menu;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Article extends FragmentActivity {

    TextView content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.article);
	    
		// Allow network access in the main thread
	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        content = (TextView) findViewById(R.id.content);

        String feed = readExonianFeed();
        try {
            // Construct a JSON object from the feed, and get the content of the post from it.
            JSONObject jsonObject = new JSONObject(feed);
            String contentText = (String) ((JSONObject) jsonObject.get("post")).get("content");
            // Only get the text, not the css, which begins after the end of the p tag.
            contentText = contentText.substring(0, contentText.indexOf("</p>"));
            content.setText(Html.fromHtml(contentText));
        } catch (Exception e) {
            e.printStackTrace();
	    }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}
	
	// Using the StringBuilder class, returns the JSON as a string
	public String readExonianFeed() {
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet("http://theexonian.com/new/2014/02/13/rdfz-students-come-to-campus/?json=1");
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return builder.toString();
	}
}
