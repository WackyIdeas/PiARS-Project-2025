package bogdan.cvetanovski.pasalic;

/*
 *  Base abstract class for SQL rows.
 *  Use getters and setters as row information will be pulled
 *  Using Cursors.
 */
public abstract class ObjectRow {
    private int id;

    public int getId() {
        return id;
    }
    public void setId(int i) {
        id = i;
    }
}

