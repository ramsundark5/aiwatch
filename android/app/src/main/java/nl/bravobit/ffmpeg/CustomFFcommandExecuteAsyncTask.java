package nl.bravobit.ffmpeg;

import android.os.AsyncTask;

import com.aiwatch.Logger;
import com.google.common.util.concurrent.SimpleTimeLimiter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class CustomFFcommandExecuteAsyncTask extends AsyncTask<Void, String, CommandResult> implements FFtask {
    private static final Logger LOGGER = new Logger();
    private final String[] cmd;
    private Map<String, String> environment;
    private final FFcommandExecuteResponseHandler ffmpegExecuteResponseHandler;
    private final ShellCommand shellCommand;
    private final long timeout;
    private long startTime;
    private Process process;
    private String output = "";
    private boolean quitPending;

    CustomFFcommandExecuteAsyncTask(String[] cmd, Map<String, String> environment, long timeout, FFcommandExecuteResponseHandler ffmpegExecuteResponseHandler) {
        this.cmd = cmd;
        this.timeout = timeout;
        this.environment = environment;
        this.ffmpegExecuteResponseHandler = ffmpegExecuteResponseHandler;
        this.shellCommand = new ShellCommand();
    }

    @Override
    protected void onPreExecute() {
        startTime = System.currentTimeMillis();
        if (ffmpegExecuteResponseHandler != null) {
            ffmpegExecuteResponseHandler.onStart();
        }
    }

    @Override
    protected CommandResult doInBackground(Void... params) {
        try {
            process = shellCommand.run(cmd, environment);
            if (process == null) {
                return CommandResult.getDummyFailureResponse();
            }
            Log.d("Running publishing updates method");
            checkAndUpdateProcess();
            return CommandResult.getOutputFromProcess(process);
        } catch (TimeoutException e) {
            Log.e("FFmpeg binary timed out", e);
            return new CommandResult(false, e.getMessage());
        } catch (Exception e) {
            Log.e("Error running FFmpeg binary", e);
        } finally {
            Util.destroyProcess(process);
        }
        return CommandResult.getDummyFailureResponse();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if (values != null && values[0] != null && ffmpegExecuteResponseHandler != null) {
            ffmpegExecuteResponseHandler.onProgress(values[0]);
        }
    }

    @Override
    protected void onPostExecute(CommandResult commandResult) {
        if (ffmpegExecuteResponseHandler != null) {
            output += commandResult.output;
            if (commandResult.success) {
                ffmpegExecuteResponseHandler.onSuccess(output);
            } else {
                ffmpegExecuteResponseHandler.onFailure(output);
            }
            ffmpegExecuteResponseHandler.onFinish();
        }
    }

    private void checkAndUpdateProcess() throws TimeoutException, InterruptedException {
        while (!Util.isProcessCompleted(process)) {

            // checking if process is completed
            if (Util.isProcessCompleted(process)) {
                return;
            }

            // Handling timeout
            if (timeout != Long.MAX_VALUE && System.currentTimeMillis() > startTime + timeout) {
                throw new TimeoutException("FFmpeg binary timed out");
            }

            try {
                SimpleTimeLimiter timeLimiter = SimpleTimeLimiter.create(Executors.newSingleThreadExecutor());
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line = timeLimiter.callWithTimeout(reader::readLine, 10, TimeUnit.SECONDS);
                if (line != null) {
                    if (isCancelled()) {
                        process.destroy();
                        process.waitFor();
                        return;
                    }

                    if (quitPending) {
                        sendQ();
                        process = null;
                        return;
                    }

                    output += line + "\n";
                    publishProgress(line);
                    checkAndUpdateProcess();
                }
                LOGGER.i("finished ffmpeg processing");
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isProcessCompleted() {
        return Util.isProcessCompleted(process);
    }

    @Override
    public boolean killRunningProcess() {
        return Util.killAsync(this);
    }

    @Override
    public void sendQuitSignal() {
        quitPending = true;
    }

    private void sendQ() {
        OutputStream outputStream = process.getOutputStream();
        try {
            outputStream.write("q\n".getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
