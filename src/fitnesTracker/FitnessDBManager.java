package fitnesTracker;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;
import lifemanagmentsystem.MongodbConnection;
import org.bson.Document;

import java.util.ArrayList;

public class FitnessDBManager {

    private final MongoCollection<Document> collection;

    public FitnessDBManager() {
        MongoDatabase db = MongodbConnection.getDatabase();
        collection = db.getCollection("fitnessTracker");
    }


    public void addFitnessRecord(FitnessRecord record) {
        Document doc = new Document("userEmail", record.getUserEmail())
                .append("calories", record.getCalories())
                .append("date", record.getDate());
        collection.insertOne(doc);
    }


    public ArrayList<FitnessRecord> getAllRecords(String userEmail) {
        ArrayList<FitnessRecord> records = new ArrayList<>();
        FindIterable<Document> docs = collection.find(new Document("userEmail", userEmail));

        for (Document doc : docs) {
            FitnessRecord r = new FitnessRecord(
                    doc.getString("userEmail"),
                    doc.getDouble("calories"),
                    doc.getString("date")
            );
            records.add(r);
        }
        return records;
    }

    public double getAverageCalories(String userEmail) {
        ArrayList<FitnessRecord> records = getAllRecords(userEmail);
        if (records.isEmpty()) return 0;
        double total = 0;
        for (FitnessRecord r : records) total += r.getCalories();
        return total / records.size();
    }
}
