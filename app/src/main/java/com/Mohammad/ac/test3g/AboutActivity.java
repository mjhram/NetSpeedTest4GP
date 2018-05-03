package com.Mohammad.ac.test3g;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class AboutActivity
  extends AppCompatActivity
{
  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.activity_about);
    TextView tvDocument = findViewById(R.id.editText2);
    String bodyData =getResources().getString(R.string.privacypolicy);
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
      tvDocument.setText(Html.fromHtml(bodyData,Html.FROM_HTML_MODE_LEGACY));
    } else {
      tvDocument.setText(Html.fromHtml(bodyData));
    }
    tvDocument.setMovementMethod(new LinkMovementMethod());

    Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }
  
  public boolean onCreateOptionsMenu(Menu paramMenu)
  {
    //getMenuInflater().inflate(R.menu.menu_about, paramMenu);
    return true;
  }
  
  public boolean onOptionsItemSelected(MenuItem paramMenuItem)
  {
    if (paramMenuItem.getItemId() == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(paramMenuItem);
  }
}


/* Location:              C:\Downloads\Android\11Reverse Eng\My 3G Test\output_jar.jar!\com\Mohammad\ac\test3g\AboutActivity.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */