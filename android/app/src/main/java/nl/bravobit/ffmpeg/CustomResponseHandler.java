package nl.bravobit.ffmpeg;
import com.aiwatch.Logger;

public class CustomResponseHandler implements FFcommandExecuteResponseHandler {
    private static final Logger LOGGER = new Logger();

    @Override
    public void onStart() {
        LOGGER.d("ffmpeg merging started. Thread is "+ Thread.currentThread().getName());
    }

    @Override
    public void onFinish() {
        LOGGER.d("ffmpeg merging completed");
    }

    @Override
    public void onSuccess(String message) {
        LOGGER.d("ffmpeg merging success");
    }

    @Override
    public void onProgress(String message) {
        LOGGER.v("ffmpeg merging in progress. Thread is "+ Thread.currentThread().getName());
    }

    @Override
    public void onFailure(String message) {
        LOGGER.e("ffmpeg merging failed " + message);
    }
}
