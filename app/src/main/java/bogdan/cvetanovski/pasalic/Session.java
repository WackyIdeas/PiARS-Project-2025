package bogdan.cvetanovski.pasalic;

public class Session extends ObjectRow {
    private String date;
    private String endDate;
    private String name;
    private String description;

    public String getDate() {
        return date;
    }
    public String getEndDate() {
        return endDate;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public void setDate(String s) {
        date = s;
    }
    public void setEndDate(String s) {
        endDate = s;
    }
    public void setName(String s) {
        name = s;
    }
    public void setDescription(String s) {
        description = s;
    }
}
