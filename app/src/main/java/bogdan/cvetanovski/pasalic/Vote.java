package bogdan.cvetanovski.pasalic;

public class Vote extends ObjectRow {
    private int votesYes;
    private int votesNo;
    private int votesAbstain;
    private int sessionId;

    public int getYesVotes() {
        return votesYes;
    }
    public int getNoVotes() {
        return votesNo;
    }
    public int getAbstainVotes() {
        return votesAbstain;
    }
    public int getSessionId() {
        return sessionId;
    }
    public void setVotesYes(int i) {
        if(i < 0) i = 0;
        votesYes = i;
    }
    public void setVotesNo(int i) {
        if(i < 0) i = 0;
        votesNo = i;
    }
    public void setVotesAbstain(int i) {
        if(i < 0) i = 0;
        votesAbstain = i;
    }
    public void setSessionId(int i) {
        sessionId = i;
    }
}
