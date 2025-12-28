package com.airtribe.learntrack.constants;

/**
 * Contains all menu option constants used in the application
 */
public class MenuOptions {
    
    // Main Menu Options
    public static final int MAIN_STUDENT_MANAGEMENT = 1;
    public static final int MAIN_COURSE_MANAGEMENT = 2;
    public static final int MAIN_ENROLLMENT_MANAGEMENT = 3;
    public static final int MAIN_EXIT = 0;
    
    // Student Management Options
    public static final int STUDENT_ADD = 1;
    public static final int STUDENT_VIEW_ALL = 2;
    public static final int STUDENT_SEARCH = 3;
    public static final int STUDENT_UPDATE = 4;
    public static final int STUDENT_DEACTIVATE = 5;
    public static final int STUDENT_BACK = 0;
    
    // Course Management Options
    public static final int COURSE_ADD = 1;
    public static final int COURSE_VIEW_ALL = 2;
    public static final int COURSE_SEARCH = 3;
    public static final int COURSE_UPDATE = 4;
    public static final int COURSE_TOGGLE_STATUS = 5;
    public static final int COURSE_BACK = 0;
    
    // Enrollment Management Options
    public static final int ENROLLMENT_ENROLL = 1;
    public static final int ENROLLMENT_VIEW_BY_STUDENT = 2;
    public static final int ENROLLMENT_VIEW_BY_COURSE = 3;
    public static final int ENROLLMENT_UPDATE_STATUS = 4;
    public static final int ENROLLMENT_VIEW_ALL = 5;
    public static final int ENROLLMENT_BACK = 0;
    
    // Private constructor to prevent instantiation
    private MenuOptions() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

