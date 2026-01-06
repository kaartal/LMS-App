package habitTracker;

public class HabitRecord {
    private String userEmail;
    private String habit;
    private boolean completed;
    private String date;

    public HabitRecord(String userEmail, String habit, boolean completed, String date) {
        this.userEmail = userEmail;
        this.habit = habit;
        this.completed = completed;
        this.date = date;
    }

    public String getUserEmail() { return userEmail; }
    public String getHabit() { return habit; }
    public boolean isCompleted() { return completed; }
    public String getDate() { return date; }
}
