package app.exonian;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class ArticleActivity extends FragmentActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.article);
		
		// Connect to a specific article and image
		final String stringUrl = "http://theexonian.com/new/2014/02/13/boys-track-wins-ea-girls-fall-close-behind/?json=1&include=content,title,author";
		final String imageUrl = "http://theexonian.com/new/wp-content/uploads/2014/02/DSC08103-700x466.jpg";
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		
		// Make sure the device is connected
		if (networkInfo != null && networkInfo.isConnected()) {
			Download d = new Download();
			Download.Page p = d.new Page(this);
			Download.Image i = d.new Image(this);
			p.execute(stringUrl);
			i.execute(imageUrl);
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
}
