package com.aiwatch.postprocess;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import com.aiwatch.Logger;

public class TTSManager {

    private static final Logger LOGGER = new Logger();

    private TextToSpeech textToSpeechSystem = null;

    public void speakMessage(Context context, String message){
        textToSpeechSystem = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                /*int result = textToSpeechSystem.setLanguage(Locale.ENGLISH);
                if(result==TextToSpeech.LANG_NOT_SUPPORTED||result==TextToSpeech.LANG_MISSING_DATA) {
                    LOGGER.d("TTS language is not available ");
                }
                Bundle bundle = new Bundle();
                bundle.putString(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));*/
                int returnVal = textToSpeechSystem.speak(message, TextToSpeech.QUEUE_ADD, null, null);
                LOGGER.d("TTS notify completed with status "+ returnVal);
            }
        });

    }

    public void shutDown(){
        try{
            if(textToSpeechSystem != null){
                textToSpeechSystem.stop();
                textToSpeechSystem.shutdown();
            }
        }catch (Exception e){
            LOGGER.e(e, "Error shutting down tts");
        }
    }
}
