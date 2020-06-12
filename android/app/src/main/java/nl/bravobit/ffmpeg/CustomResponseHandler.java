package nl.bravobit.ffmpeg;
import com.aiwatch.Logger;

public class CustomResponseHandler implements FFcommandExecuteResponseHandler {
    private static final Logger LOGGER = new Logger();
    private String commandName;

    public CustomResponseHandler(String commandName){
        this.commandName = commandName;
    }

    @Override
    public void onStart() {
        LOGGER.d("ffmpeg " + commandName + " started. Thread is "+ Thread.currentThread().getName());
    }

    @Override
    public void onFinish() {
        LOGGER.d("ffmpeg " + commandName + " completed");
    }

    @Override
    public void onSuccess(String message) {
        LOGGER.d("ffmpeg  " + commandName + " success");
    }

    @Override
    public void onProgress(String message) {
        LOGGER.v("ffmpeg " + commandName + " in progress. Thread is "+ Thread.currentThread().getName());
    }

    @Override
    public void onFailure(String message) {
        LOGGER.e("ffmpeg " + commandName + " failed " + message);
    }
}