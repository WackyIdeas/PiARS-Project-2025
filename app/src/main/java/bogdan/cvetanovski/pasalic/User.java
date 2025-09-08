package bogdan.cvetanovski.pasalic;

public class User extends ObjectRow {
    private String name;
    private String surname;
    private String hash;
    private String username;
    private int role; // 0 for student, 1 for admin

    public String getName() {
        return name;
    }
    public String getSurname() {
        return surname;
    }
    public String getUsername() {
        return username;
    }
    public String getHash() {
        return hash;
    }
    public int getRole() {
        return role;
    }
    void setName(String n) {
        name = n;
    }
    void setSurname(String n) {
        surname = n;
    }
    void setHash(String n) {
        hash = n;
    }
    void setUsername(String n) {
        username = n;
    }
    void setRole(int r) {
        if(r < 0) r = 0;
        if(r > 1) r = 1;
        role = r;
    }
}
