package calendarTracker;

public class CalendarRecord {
    private String userEmail;
    private String date;
    private String planDescription;

    public CalendarRecord(String userEmail, String date, String planDescription) {
        this.userEmail = userEmail;
        this.date = date;
        this.planDescription = planDescription;

        //this.details = details;
        //this.DetailsInfo = planDescription;
    }

    public String getUserEmail() { return userEmail; }
    public String getDate() { return date; }
    public String getPlanDescription() { return planDescription; }
}