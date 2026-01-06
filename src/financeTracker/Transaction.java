package financeTracker;

import org.bson.Document;

public class Transaction {

    private String id;
    private String type;
    private double amount;
    private String description;
    private String category;
    private String userEmail;
    //private String details;
    //private String detailsInfo;

   // public Transaction(String id, String userEmail, String type, double amount, String description, String category) {
       // this.id = id;
        //this.userEmail = userEmail;
        //this.type = type;
       //
    //}

    public Transaction(String id, String userEmail, String type, double amount, String description, String category) {
        this.id = id;
        this.userEmail = userEmail;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.category = category;
        //this.details = details;
        //this.detailsInfo = detailsInfo;
    }

    public Transaction(String userEmail, String type, double amount, String description, String category) {
        this.userEmail = userEmail;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.category = category;
        //this.detailsInfo = detailsInfo;
    }

    public Document toDocument() {
        return new Document("Vrsta", type)
                .append("Iznos", amount)
                .append("Opis", description)
                .append("Kategorija", category);
    }

        public String getId() { return id; }
        public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }

    public String getCategory() { return category; }
    public String getUserEmail() { return userEmail;}


    //public String getdetails() { return details; }
    //public String getdetailsInfo() { return detailsInfo; }
    }
