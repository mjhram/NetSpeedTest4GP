package com.Mohammad.ac.SpeedTest;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
