package com.yash.log.constants;

public final class LogConstant {

    //() define capturing groups
    //Each group captures a part of the log line that matches the pattern inside that parentheses
    //Groups are numbered from left to right starting at 1
    //\S+ matches one or more non-whitespace characters (timestamp)
    //\s+ matches one or more whitespace characters
    //(INFO|WARN|ERROR|DEBUG) matches and captures one of the log levels
    //\d+ matches one or more digits (process id)
    //--- matches the literal string "---"
    //\[.*?\] matches and captures the thread name enclosed in square brackets
    //\[.*?\] matches and captures the logger name enclosed in square brackets
    //\S+ matches one or more non-whitespace characters (source)
    //:\s+ matches a colon followed by one or more whitespace characters
    //.* matches and captures the rest of the line (log message)
    public final static String LOG_PATTERN= "^(\\S+)\\s+(INFO|WARN|ERROR|DEBUG)\\s+\\d+\\s+---\\s+\\[.*?\\]\\s+\\[.*?\\]\\s+(\\S+)\\s+:\\s+(.*)$";

    public final static String EXCEPTION_PATTERN= "(\\b[a-zA-Z0-9]+Exception\\b)";

    public final static String UNKNOWN_ERROR= "Unknown Error";

    public final static String INVALID_FILE_TYPE= "Invalid file type. Only .log files are accepted.";
    public final static String FILE_TOO_LARGE= "File too large. Max size is 10MB";
    public final static String UPLOAD_SUCCESSFULLY= "Logs uploaded and saved successfully!";



}
