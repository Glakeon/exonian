/**
 * 
 */
package app.exonian;

import android.app.ListActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * @author Weihang Fan
 *
 */
public class SearchActivity extends ListActivity {

	private TextWatcher filterTextWatcher = new TextWatcher() {

		public void afterTextChanged(Editable s) {
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);

		((EditText) findViewById(R.id.search_box)).addTextChangedListener(filterTextWatcher);
	}
	
}
