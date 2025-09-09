package bogdan.cvetanovski.pasalic;

public class VoteSafety extends ObjectRow {
    private String hash;
    private int voteID;

    public void setHash(String h) {
        hash = h;
    }
    public void setVoteID(int i) {
        voteID = i;
    }
    public String getHash() {
        return hash;
    }
    public int getVoteID() {
        return voteID;
    }
}
