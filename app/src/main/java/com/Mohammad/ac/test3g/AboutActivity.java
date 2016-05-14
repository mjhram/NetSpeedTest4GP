package com.Mohammad.ac.test3g;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class AboutActivity
  extends Activity
{
  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.activity_about);
  }
  
  public boolean onCreateOptionsMenu(Menu paramMenu)
  {
    getMenuInflater().inflate(R.menu.menu_about, paramMenu);
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