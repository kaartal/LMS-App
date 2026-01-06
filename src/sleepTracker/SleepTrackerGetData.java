package sleepTracker;

import java.util.ArrayList;

public class SleepTrackerGetData {

    private final ArrayList<SleepRecord> sleepRecords = new ArrayList<>();


    public void addSleepRecord(SleepRecord record) {
        sleepRecords.add(record);
    }


    public double getAverageSleepHours() {
        if (sleepRecords.isEmpty()) return 0;
        double total = 0;
        for (SleepRecord r : sleepRecords) {
            total += r.getHours();
        }
        return total / sleepRecords.size();
    }


    public ArrayList<SleepRecord> getAllRecords() {
        return sleepRecords;
    }
}
