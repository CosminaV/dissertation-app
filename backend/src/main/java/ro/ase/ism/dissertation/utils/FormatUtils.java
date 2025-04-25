package ro.ase.ism.dissertation.utils;

public class FormatUtils {

    private FormatUtils() {
    }

    public static String formatAcademicYear(int year) {
        return year + "-" + (year + 1);
    }

    public static String formatFullName(String firstName, String lastName) {
        return firstName + " " + lastName;
    }
}
