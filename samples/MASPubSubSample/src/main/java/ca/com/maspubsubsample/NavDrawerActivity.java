package ca.com.maspubsubsample;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;


public abstract class NavDrawerActivity extends AppCompatActivity {

    abstract int getDrawerLayoutViewId();

    public void closeDrawer(){
        DrawerLayout drawer = (DrawerLayout) findViewById(getDrawerLayoutViewId());
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
