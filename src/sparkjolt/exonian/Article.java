package sparkjolt.exonian;

import java.io.*;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import android.os.*;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.widget.Toast;

public class Article extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.article);
	    
		// Allow network access in the main thread
	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    StrictMode.setThreadPolicy(policy); 
	    
	    String feed = readExonianFeed();
	    try {
	    	JSONArray jsonArray = new JSONArray(feed);
	    	for (int i = 0; i < jsonArray.length(); i++) {
	    		JSONObject object = jsonArray.getJSONObject(i);
	    		Toast.makeText(this, object.getString("text"), Toast.LENGTH_SHORT).show();
	    	}
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
