package fitnesTracker;

public class FitnessRecord {

    private String userEmail;
    private String date;
    private double calories;
    private int durationMinutes;
    private double distanceKilometers;
    private String intensityLevel;

    public FitnessRecord(String userEmail, String date, double calories, int durationMinutes, double distanceKilometers, String intensityLevel) {
        this.userEmail = userEmail;
        this.date = date;
        this.calories = calories;
        this.durationMinutes = durationMinutes;
        this.distanceKilometers = distanceKilometers;
        this.intensityLevel = intensityLevel;
    }

    public String getUserEmail() { return userEmail; }
    public String getDate() { return date; }
    public double getCalories() { return calories; }
    public int getDurationMinutes() { return durationMinutes; }
    public double getDistanceKilometers() { return distanceKilometers; }
    public String getIntensityLevel() { return intensityLevel; }
}
