package com.finch_commander;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.nuance.nmdp.speechkit.SpeechKit;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SpeechKit sk = SpeechKit.initialize(getApplication().getApplicationContext(),
                                            AppInfo.speechKitAppId,
                                            AppInfo.speechKitServer,
                                            AppInfo.speechKitPort,
                                            AppInfo.speechKitSsl,
                                            AppInfo.speechKitCertSummary,
                                            AppInfo.speechKitCertData,
                                            AppInfo.speechKitApplicationKey);
    }
}
