package nl.bravobit.ffmpeg;

import java.util.Map;

interface CustomFFbinaryInterface {

    /**
     * Executes a command
     *
     * @param environmentVars                 Environment variables
     * @param cmd                             command to execute
     * @param ffcommandExecuteResponseHandler {@link FFcommandExecuteResponseHandler}
     * @return the task
     */
    FFtask execute(Map<String, String> environmentVars, String[] cmd, FFcommandExecuteResponseHandler ffcommandExecuteResponseHandler);

    /**
     * Executes a command
     *
     * @param cmd                             command to execute
     * @param ffcommandExecuteResponseHandler {@link FFcommandExecuteResponseHandler}
     * @return the task
     */
    FFtask execute(String[] cmd, FFcommandExecuteResponseHandler ffcommandExecuteResponseHandler);

    /**
     * Executes a command synchronously
     *
     * @param environmentVars                 Environment variables
     * @param cmd                             command to execute
     * @return The output of the command
     */
    String execute(Map<String, String> environmentVars, String[] cmd);

    /**
     * Executes a command synchronously
     *
     * @param cmd                             command to execute
     * @return The output of the command
     */
    String execute(String[] cmd);

    /**
     * Checks if FF binary is supported on this device
     *
     * @return true if FF binary is supported on this device
     */
    boolean isSupported();

    /**
     * Checks if a command with given task is currently running
     *
     * @param task - the task that you want to check
     * @return true if a command is running
     */
    boolean isCommandRunning(FFtask task);

    /**
     * Kill given running process
     *
     * @param task - the task to kill
     * @return true if process is killed successfully
     */
    boolean killRunningProcesses(FFtask task);

    /**
     * Timeout for binary process, should be minimum of 10 seconds
     *
     * @param timeout in milliseconds
     */
    void setTimeout(long timeout);
}