package sleepTracker;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;
import lifemanagmentsystem.MongodbConnection;
import org.bson.Document;

import java.util.ArrayList;

public class SleepInformationTransfer {

    private final MongoCollection<Document> collection;

    public SleepInformationTransfer() {
        MongoDatabase db = MongodbConnection.getDatabase();
        collection = db.getCollection("sleepTracker");
    }


    public void addSleepRecord(SleepRecord record) {
        Document doc = new Document("userEmail", record.getUserEmail())
                .append("hours", record.getHours())
                .append("date", record.getDate());
        collection.insertOne(doc);
    }

    //public void addSleepRecord(SleepRecord record) {
        //Document doc = new Document("userEmail", record.getUserEmail();
    //}


    public ArrayList<SleepRecord> getAllRecords(String userEmail) {
        ArrayList<SleepRecord> records = new ArrayList<>();
        FindIterable<Document> docs = collection.find(new Document("userEmail", userEmail));

        for (Document doc : docs) {
            SleepRecord r = new SleepRecord(
                    doc.getString("userEmail"),
                    doc.getDouble("hours"),
                    doc.getString("date")
            );
            records.add(r);
        }
        return records;
    }
}
