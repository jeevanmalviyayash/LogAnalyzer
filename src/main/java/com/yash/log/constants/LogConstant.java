package com.yash.log.constants;

public final class LogConstant {

    public final static String LOG_PATTERN= "^(\\S+)\\s+(INFO|WARN|ERROR|DEBUG)\\s+\\d+\\s+---\\s+\\[.*?\\]\\s+\\[.*?\\]\\s+(\\S+)\\s+:\\s+(.*)$";

    public final static String EXCEPTION_PATTERN= "(\\b[a-zA-Z0-9]+Exception\\b)";

    public final static String UNKNOWN_ERROR= "Unknown Error";

    public final static String INVALID_FILE_TYPE= "Invalid file type. Only .log files are accepted.";
    public final static String FILE_TOO_LARGE= "File too large. Max size is 10MB";
    public final static String UPLOAD_SUCCESSFULLY= "Logs uploaded and saved successfully!";



}
