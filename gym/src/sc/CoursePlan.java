package sc;

public class CoursePlan {
    private String name;
    private double price;

    public CoursePlan(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
}