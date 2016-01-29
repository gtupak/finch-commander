package com.finch_commander;

import android.os.Bundle;
import android.widget.TextView;

import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.SpeechError;


/**
 * Created by gabriel on 28/01/16.
 */
public class CustomRecognitionListener implements Recognizer.Listener {
    private TextView resultView;

    public CustomRecognitionListener(TextView textView){
        this.resultView = textView;
    }


    @Override
    public void onRecordingBegin(Recognizer recognizer) {

    }

    @Override
    public void onRecordingDone(Recognizer recognizer) {

    }

    @Override
    public void onResults(Recognizer recognizer, Recognition results) {
        String topResult;
        if(results.getResultCount() > 0){
            topResult = results.getResult(0).getText();
            resultView.setText(topResult);
        }
    }

    @Override
    public void onError(Recognizer recognizer, SpeechError speechError) {

    }
}
