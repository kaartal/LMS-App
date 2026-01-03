package financeTracker;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;

public class TransactionManager {

    private final MongoCollection<Document> collection;

    public TransactionManager() {
        MongoDatabase db = lifemanagmentsystem.MongodbConnection.getDatabase();
        collection = db.getCollection("transactionsTracker");
    }


    public void addNewTransaction(Transaction t) {
        collection.insertOne(t.toDocument().append("userEmail", t.getUserEmail()));
    }


    public ArrayList<Transaction> getUserTransactions(String email) {
        ArrayList<Transaction> list = new ArrayList<>();
        MongoCursor<Document> cursor = collection.find(new Document("userEmail", email)).iterator();

        while (cursor.hasNext()) {
            Document doc = cursor.next();
            list.add(new Transaction(
                    doc.getObjectId("_id").toHexString(),
                    doc.getString("userEmail"),
                    doc.getString("Vrsta"),
                    doc.getDouble("Iznos"),
                    doc.getString("Opis"),
                    doc.getString("Kategorija")
            ));
        }
        return list;
    }

    public double getTotalIncome(String email) {
        double totalAmount = 0;
        for (Transaction t : getUserTransactions(email)) {
            if (t.getType().equalsIgnoreCase("Prihod")) {
                totalAmount += t.getAmount();
            }
        }
        return totalAmount;
    }

    public double getTotalExpense(String email) {
        double total = 0;
        for (Transaction t : getUserTransactions(email)) {
            if (t.getType().equalsIgnoreCase("Rashod")) {
                total += t.getAmount();
            }
        }
        return total;
    }


    public Map<String, Double> getIncomesByCategory(String email) {
        Map<String, Double> result = new LinkedHashMap<>();
        String[] categories = {"Plata", "Hrana", "Racuni", "Zabava", "Prijevoz", "Ostalo"};
        for (String cat : categories) result.put(cat, 0.0);

        for (Transaction t : getUserTransactions(email)) {
            if ("Prihod".equalsIgnoreCase(t.getType())) {
                String cat = t.getCategory();
                if (cat == null || !result.containsKey(cat)) cat = "Ostalo";
                result.put(cat, result.get(cat) + t.getAmount());
            }
        }
        return result;
    }


    public Map<String, Double> getExpenseByCategory(String email) {
        Map<String, Double> result = new LinkedHashMap<>();
        String[] categories = {"Plata", "Hrana", "Racuni", "Zabava", "Prijevoz", "Ostalo"};
        for (String cat : categories) result.put(cat, 0.0);

        for (Transaction t : getUserTransactions(email)) {
            if ("Rashod".equalsIgnoreCase(t.getType())) {
                String cat = t.getCategory();
                if (cat == null || !result.containsKey(cat)) cat = "Ostalo";
                result.put(cat, result.get(cat) + t.getAmount());
            }
        }
        return result;
    }


    public void updateSelectedTransaction(Transaction t) {
        Document filter = new Document("_id", new ObjectId(t.getId()))
                .append("userEmail", t.getUserEmail());

        Document updated = new Document("$set", new Document()
                .append("Vrsta", t.getType())
                .append("Iznos", t.getAmount())
                .append("Opis", t.getDescription())
                .append("Kategorija", t.getCategory())
        );

        collection.updateOne(filter, updated);
    }


    public void deleteMarkedTransaction(String id, String email) {
        collection.deleteOne(new Document("_id", new ObjectId(id))
                .append("userEmail", email));
    }


    public void deleteAllTransactions(String email) {
        collection.deleteMany(new Document("userEmail", email));
    }
}
