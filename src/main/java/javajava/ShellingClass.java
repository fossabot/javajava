package javajava;
/* I/O classes */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
/* Time classes */
import java.time.LocalDateTime;
import java.util.Arrays;
/* Logging classes */
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Shell execution methods
 */
public final class ShellingClass {
    /**
     * pointer for all logs
     */
    private static final Logger LOGGER = LogManager.getLogger(ShellingClass.class);
    /**
     * holding the Use account currently logged on
     */
    public static String LOGGED_ACCOUNT = null; // NOPMD by Daniel Popiniuc on 17.04.2025, 16:26

    /**
     * Building Process for shell execution
     * 
     * @param strCommand command to execute
     * @param strParameters command parameters
     */
    private static ProcessBuilder buildProcessForExecution(final String strCommand, final String strParameters) {
        final ProcessBuilder builder = new ProcessBuilder();
        if (strParameters.isEmpty()) {
            builder.command(strCommand);
        } else {
            builder.command(strCommand, strParameters);
        }
        final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nProcessExecutionCommandIntention"), builder.command().toString());
        LOGGER.debug(strFeedback);
        builder.directory(FileHandlingClass.getCurrentUserFolder());
        return builder;
    }

    /**
     * capture Process output
     * 
     * @param process process in scope
     * @param strOutLineSep line separator for the output
     * @return String
     * @throws IOException capturing any error on reading the input stream
     */
    private static String captureProcessOutput(final Process process, final String strOutLineSep) throws IOException {
        String strReturn = null;
        final StringBuilder processOutput = new StringBuilder();
        try (BufferedReader processOutReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String readLine;
            while ((readLine = processOutReader.readLine()) != null) {
                processOutput.append(readLine).append(strOutLineSep);
            }
            strReturn = processOutput.toString();
        } catch (IOException ex) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nProcessExecutionCaptureFailure"), Arrays.toString(ex.getStackTrace()));
            LOGGER.error(strFeedback);
        }
        return strReturn;
    }

    /**
     * Executes a shells command without any output captured
     * 
     * @param strCommand command to execute
     * @param strParameters command parameters
     */
    @SuppressWarnings("unused")
    public static void executeShellUtility(final String strCommand, final String strParameters) {
        final LocalDateTime startTimeStamp = LocalDateTime.now();
        final ProcessBuilder builder = buildProcessForExecution(strCommand, strParameters);
        try {
            final Process process = builder.start();
            final int exitCode = process.waitFor();
            process.destroy();
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nProcessExecutionFinished"), exitCode);
            LOGGER.debug(strFeedback);
        } catch (IOException | InterruptedException e) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nProcessExecutionFailed"), Arrays.toString(e.getStackTrace()));
            LOGGER.error(strFeedback);
        }
        TimingClass.logDuration(startTimeStamp, JavaJavaLocalization.getMessage("i18nProcessExecutionWithoutCaptureCompleted"), "debug");
    }

    /**
     * Executes a shells command with capturing the output to a String
     * 
     * @param strCommand command to execute
     * @param strParameters command parameters
     * @param strOutLineSep line separator for the output
     * @return String
     */
    public static String executeShellUtility(final String strCommand, final String strParameters, final String strOutLineSep) {
        final LocalDateTime startTimeStamp = LocalDateTime.now();
        String strReturn = "";
        final ProcessBuilder builder = buildProcessForExecution(strCommand, strParameters);
        try {
            builder.redirectErrorStream(true);
            final Process process = builder.start();
            strReturn = captureProcessOutput(process, strOutLineSep);
            final int exitCode = process.waitFor();
            process.destroy();
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nProcessExecutionFinished"), exitCode);
            LOGGER.debug(strFeedback);
        } catch (IOException | InterruptedException e) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nProcessExecutionFailed"), Arrays.toString(e.getStackTrace()));
            LOGGER.error(strFeedback);
        }
        TimingClass.logDuration(startTimeStamp, JavaJavaLocalization.getMessage("i18nProcessExecutionWithCaptureCompleted"), "debug");
        return strReturn;
    }

    /**
     * Getting current logged account name
     * @return String
     */
    public static String getCurrentUserAccount() {
        if (LOGGED_ACCOUNT == null) {
            loadCurrentUserAccount();
        }
        return LOGGED_ACCOUNT;
    }

    /**
     * load current logged account name
     */
    private static void loadCurrentUserAccount() {
        String strUser = executeShellUtility("WHOAMI", "/UPN", "");
        if (strUser.startsWith("ERROR:")) {
            final String strFeedback = JavaJavaLocalization.getMessage("i18nUserPrincipalNameError");
            LOGGER.warn(strFeedback);
            strUser = executeShellUtility("WHOAMI", "", "");
        }
        LOGGED_ACCOUNT = strUser;
    }

    /**
     * Constructor
     */
    private ShellingClass() {
        throw new UnsupportedOperationException(Common.strAppClsWrng);
    }
}
