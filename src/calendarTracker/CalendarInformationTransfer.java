package calendarTracker;

import com.mongodb.client.MongoCollection;
import lifemanagmentsystem.MongodbConnection;
import org.bson.Document;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CalendarInformationTransfer {
    private final MongoCollection<Document> collection;

    public CalendarInformationTransfer() {
        this.collection = MongodbConnection.getDatabase().getCollection("calendarTracker");
    }

    public void addPlan(CalendarRecord record) {
        Document doc = new Document("userEmail", record.getUserEmail())
                .append("date", record.getDate())
                .append("planDescription", record.getPlanDescription());
        collection.insertOne(doc);
    }

    public ArrayList<CalendarRecord> getPlans(String userEmail) {
        ArrayList<CalendarRecord> list = new ArrayList<>();
        for (Document doc : collection.find(new Document("userEmail", userEmail))) {
            list.add(new CalendarRecord(
                    doc.getString("userEmail"),
                    doc.getString("date"),
                    doc.getString("planDescription")
            ));
        }
        return list;
    }

    public void deletePlan(String userEmail, String date, String desc) {
        collection.deleteOne(new org.bson.Document("userEmail", userEmail)
                .append("date", date)
                .append("planDescription", desc));
    }

    //public void deletePlan(String userEmail, String date, String desc) {
        //collection.deleteOne(new org.bson.Document("userEmail", userEmail)
             //   .append("date", date)
               // .append("planDescription", desc));
    //}


    //public void deletePlan(String userEmail, String date, String desc) {
        //collection.deleteOne(new org.bson.Document("userEmail", userEmail)
            //
    //}

    public Map<String, String> getPlansGroupedByDate(String userEmail) {
        Map<String, String> grouped = new HashMap<>();
        for (CalendarRecord recordItem : getPlans(userEmail)) {
            grouped.merge(recordItem.getDate(), recordItem.getPlanDescription(), (oldV, newV) -> oldV + "\n• " + newV);
        }
        return grouped;
    }
}