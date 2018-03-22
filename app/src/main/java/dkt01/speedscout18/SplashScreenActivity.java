package dkt01.speedscout18;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Just shows an image on app launch and forwards to Main Activity
 */
public class SplashScreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
