package habitTracker;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;
import lifemanagmentsystem.MongodbConnection;
import org.bson.Document;

import java.util.ArrayList;

public class HabitInformationTransfer {

    private final MongoCollection<Document> collection;

    public HabitInformationTransfer() {
        MongoDatabase db = MongodbConnection.getDatabase();
        collection = db.getCollection("habitTracker");
    }

    // ➕ Dodavanje zapisa
    public void addHabitRecord(HabitRecord record) {
        Document doc = new Document("userEmail", record.getUserEmail())
                .append("habit", record.getHabit())
                .append("completed", record.isCompleted())
                .append("date", record.getDate());
        collection.insertOne(doc);
    }

    // 📄 Svi zapisi korisnika
    public ArrayList<HabitRecord> getAllRecords(String userEmail) {
        ArrayList<HabitRecord> records = new ArrayList<>();
        FindIterable<Document> docs = collection.find(new Document("userEmail", userEmail));

        for (Document doc : docs) {
            records.add(new HabitRecord(
                    doc.getString("userEmail"),
                    doc.getString("habit"),
                    doc.getBoolean("completed"),
                    doc.getString("date")
            ));
        }
        return records;
    }

    // 📊 Procenat izvršenja
    public double getCompletionPercentage(String userEmail, String habit) {
        ArrayList<HabitRecord> records = getAllRecords(userEmail);
        int total = 0;
        int done = 0;

        for (HabitRecord r : records) {
            if (r.getHabit().equals(habit)) {
                total++;
                if (r.isCompleted()) done++;
            }
        }
        return total == 0 ? 0 : (done * 100.0 / total);
    }

    // 📌 Sve unikatne navike korisnika
    public ArrayList<String> getUniqueHabits(String userEmail) {
        ArrayList<String> habits = new ArrayList<>();
        for (HabitRecord r : getAllRecords(userEmail)) {
            if (!habits.contains(r.getHabit())) {
                habits.add(r.getHabit());
            }
        }
        return habits;
    }

    // 🔥 Da li je navika završena DANAS
    public boolean isHabitCompletedToday(String userEmail, String habit) {
        String today = java.time.LocalDate.now().toString();

        Document query = new Document("userEmail", userEmail)
                .append("habit", habit)
                .append("date", today)
                .append("completed", true);

        return collection.find(query).first() != null;
    }
}
