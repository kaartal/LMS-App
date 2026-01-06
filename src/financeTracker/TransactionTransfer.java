package financeTracker;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;

public class TransactionTransfer {

    private final MongoCollection<Document> collection;

    // CRETADE ONE TABLE IN DT NAME transactionsTracker
    public TransactionTransfer() {
        MongoDatabase db = lifemanagmentsystem.MongodbConnection.getDatabase();
        collection = db.getCollection("transactionsTracker");
    }


    // ADD NEW TRANSACTION
    public void addNewTransaction(Transaction t) {
        //collection.insertOne(t.toDocument().append("email", t.getUserEmail()));
        collection.insertOne(t.toDocument().append("userEmail", t.getUserEmail()));
    }

    // GET ALL TRANSACTION FROM USER

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


    // TAKE ALL TRANSCATION FROM USER SESSION
        //public ArrayList<Transaction> getAllTransactionFromUser(String email) {
            //ArrayList<Transaction> transactions = new ArrayList<>();
//
            //MongoCursor<Document> cursor = collection
                 //   .find(new Document("userEmail", email))
                  //  .sort(new Document("_id", 1))
                  //  .iterator();
//
            //while (cursor.hasNext()) {
               // Document doc = cursor.next();
//
                //Transaction t = new Transaction(
                       // doc.getObjectId("_id").toHexString(),
                       // doc.getString("userEmail"),
                       // doc.getString("Vrsta"),
                       // doc.getDouble("Iznos"),
                       // doc.getString("Opis"),
                       // doc.getString("Kategorija")
                //);
//
                //transactions.add(t);
            //}
//
            //return transactions;
        //}





    // GOING IN ALL TRANSACTION AND COLLECT ALL DATA INCOME, EXPENSE, INCOMES BY CATEGORY, EXPANSE BY CATEGORY
    public FinancialSummary getFinancialSummary(String email) {
        //double Income = 0;
        //double Expense = 0;
        double totalIncome = 0;
        double totalExpense = 0;

        Map<String, Double> incomeByCategory = new LinkedHashMap<>();
        Map<String, Double> expenseByCategory = new LinkedHashMap<>();


        //Map<String, Double> income = new LinkedHashMap<>();
        //Map<String, Double> expense = new LinkedHashMap<>();



        // CATEGORIES TRANSACTIONS
        String[] categories = {"Plata", "Hrana", "Racuni", "Zabava", "Prijevoz", "Ostalo"};
        for (String cat : categories) {
            incomeByCategory.put(cat, 0.0);
            expenseByCategory.put(cat, 0.0);
        }

        // IF NOT INCOME OR EXPENSE THAN OTHERS
        for (Transaction t : getUserTransactions(email)) {
            String cat = t.getCategory() != null ? t.getCategory() : "Ostalo";

            if ("Prihod".equalsIgnoreCase(t.getType())) {
                totalIncome += t.getAmount();
                incomeByCategory.put(cat, incomeByCategory.getOrDefault(cat, 0.0) + t.getAmount());
            } else if ("Rashod".equalsIgnoreCase(t.getType())) {
                totalExpense += t.getAmount();
                expenseByCategory.put(cat, expenseByCategory.getOrDefault(cat, 0.0) + t.getAmount());
            }
        }

        return new FinancialSummary(totalIncome, totalExpense, incomeByCategory, expenseByCategory);
    }


    // UPDATE CRETAD TRANSACTION FROM BEFORE
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


   // public void deleteTransaction(String id, String email) {
      //  collection.deleteOne;
            //    .append("email", email));
    //}


    public void deleteAllTransactions(String email) {
        collection.deleteMany(new Document("userEmail", email));
    }


    public static class FinancialSummary {
        public final double totalIncome;
        public final double totalExpense;
        public final Map<String, Double> incomeByCategory;
        public final Map<String, Double> expenseByCategory;

        public FinancialSummary(double totalIncome, double totalExpense,
                                Map<String, Double> incomeByCategory, Map<String, Double> expenseByCategory) {
            this.totalIncome = totalIncome;
            this.totalExpense = totalExpense;

            this.incomeByCategory = incomeByCategory;
            this.expenseByCategory = expenseByCategory;
        }

        //public double getBalanceFromUser() {
           // return totalIncome - totalExpense;
        //}

        public double getBalance() {
            return totalIncome - totalExpense;
        }
    }
}
