package com.finch_commander;

import android.speech.RecognitionListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.SpeechKit;

public class MainActivity extends AppCompatActivity {

    Recognizer recognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Handler handler = new Handler();

        SpeechKit sk = SpeechKit.initialize(getApplication().getApplicationContext(),
                                            AppInfo.speechKitAppId,
                                            AppInfo.speechKitServer,
                                            AppInfo.speechKitPort,
                                            AppInfo.speechKitSsl,
                                            AppInfo.speechKitCertSummary,
                                            AppInfo.speechKitCertData,
                                            AppInfo.speechKitApplicationKey);
        Recognizer.Listener recListener = new CustomRecognitionListener((TextView) findViewById(R.id.textView));
        recognizer = sk.createRecognizer(Recognizer.RecognizerType.Dictation,
                                                    Recognizer.EndOfSpeechDetection.Short,
                                                    "en_US", recListener, handler);
        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                recognizer.start();
            }
        });
    }
}
