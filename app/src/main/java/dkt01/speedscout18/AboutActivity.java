package dkt01.speedscout18;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView tv1 = findViewById(R.id.about_1_textview);
        tv1.setMovementMethod(LinkMovementMethod.getInstance());
        TextView tv2 = findViewById(R.id.about_2_textview);
        tv2.setMovementMethod(LinkMovementMethod.getInstance());
        TextView tv3 = findViewById(R.id.about_3_textview);
        tv3.setMovementMethod(LinkMovementMethod.getInstance());
        TextView tv4 = findViewById(R.id.about_4_textview);
        tv4.setMovementMethod(LinkMovementMethod.getInstance());
        TextView tv5 = findViewById(R.id.about_5_textview);
        tv5.setMovementMethod(LinkMovementMethod.getInstance());

        // Display version info on about screen
        TextView versionField = findViewById(R.id.appVersion);
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = "Not Available";
        if(pInfo != null)
        {
            version = pInfo.versionName;
        }
        versionField.setText(version);
    }
}
