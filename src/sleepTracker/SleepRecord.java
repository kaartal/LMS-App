package sleepTracker;

public class SleepRecord {
    private String userEmail;
    private double hours;
    private String date;
    //private string details;

    public SleepRecord(String userEmail, double hours, String date) {
        this.userEmail = userEmail;
        this.hours = hours;
        this.date = date;
        //this.details = details;
    }

    public String getUserEmail() { return userEmail; }
    public double getHours() { return hours; }
    public String getDate() { return date; }
    //public String getDetails() { return details; }
    //public String getDetailsAboutSleep() { return retils; }
}
