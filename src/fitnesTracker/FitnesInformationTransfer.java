package fitnesTracker;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import lifemanagmentsystem.MongodbConnection;

import java.util.ArrayList;

public class FitnesInformationTransfer {

    private final MongoCollection<Document> collection;

    public FitnesInformationTransfer() {
        MongoDatabase db = MongodbConnection.getDatabase();
        collection = db.getCollection("fitnessTracker");
    }

    public void addFitnessRecord(FitnessRecord record) {
        Document doc = new Document("userEmail", record.getUserEmail())
                .append("date", record.getDate())
                .append("calories", record.getCalories())
                .append("duration", record.getDurationMinutes())
                .append("distance", record.getDistanceKilometers())
                .append("intensity", record.getIntensityLevel());


         //.append("duration", record.getDurationMinutes())
                //.append("distance", record.getDistanceKilometers())
                //.append("intensity", record.getIntensityLevel());
        collection.insertOne(doc);
    }

    public void deleteFitnessRecord(String userEmail, String date) {
        collection.deleteOne(new Document("userEmail", userEmail).append("date", date));
    }

    public ArrayList<FitnessRecord> getAllRecords(String userEmail) {
        ArrayList<FitnessRecord> records = new ArrayList<>();
        FindIterable<Document> docs = collection.find(new Document("userEmail", userEmail));

        //public ArrayList<FitnessRecord> getAllRecords(String userEmail)


        for (Document doc : docs) {
            String date = doc.getString("date");
            double calories = doc.getDouble("calories") != null ? doc.getDouble("calories") : 0;
            int duration = doc.getInteger("duration") != null ? doc.getInteger("duration") : 0;
            double distance = doc.getDouble("distance") != null ? doc.getDouble("distance") : 0;
            String intensity = doc.getString("intensity") != null ? doc.getString("intensity") : "Slab";

            records.add(new FitnessRecord(
                    doc.getString("userEmail"),
                    date,
                    calories,
                    duration,
                    distance,
                    intensity
                    //calories,
                    //duration,
                    //distance,
                    //intensity
            ));
        }

        return records;
    }
}
