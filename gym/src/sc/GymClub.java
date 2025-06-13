package sc;

public class GymClub {	
    private String clubId, name;
    private Region region;

    public GymClub(String clubId, String name, Region region) {
        this.clubId = clubId;
        this.name   = name;
        this.region = region;
    }

    public String getClubId() { return clubId; }
    public String getName()   { return name; }
    public Region getRegion() { return region; }

    @Override
    public String toString() {
        return clubId + "：" + name;
    }
    
}
