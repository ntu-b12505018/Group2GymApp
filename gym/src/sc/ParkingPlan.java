package sc;

public class ParkingPlan {
    private String planId, name;
    private double fee;

    public ParkingPlan(String planId, String name, double fee) {
        this.planId = planId;
        this.name   = name;
        this.fee    = fee;
    }

    public String getPlanId() { return planId; }
    public String getName()   { return name; }
    public double getFee()    { return fee; }

    // NEW: 讓其他程式碼可呼叫 getPrice()
    public double getPrice()  { return fee; }

    @Override
    public String toString() {
        return name + " - NT$" + fee;
    }
}