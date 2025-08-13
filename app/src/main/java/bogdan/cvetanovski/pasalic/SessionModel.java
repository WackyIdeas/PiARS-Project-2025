package bogdan.cvetanovski.pasalic;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.LinkedHashMap;

public class SessionModel {
    public class SessionInfo {
        private int sessionNumber;
        private String sessionDate;

        public SessionInfo(int sn, String date) {
            sessionNumber = sn;
            sessionDate = date;
        }

        public int getSessionNumber() {
            return sessionNumber;
        }

        public void setSessionNumber(int sessionNumber) {
            this.sessionNumber = sessionNumber;
        }

        public String getSessionDate() {
            return sessionDate;
        }

        public void setSessionDate(String sessionDate) {
            this.sessionDate = sessionDate;
        }
    }
    public SessionModel() {
        data = new LinkedHashMap<Integer, SessionInfo>();
        DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        addItem(1, LocalDate.of(2025, 8, 13).format(dtf));
        addItem(2, LocalDate.of(2025, 8, 14).format(dtf));
        addItem(3, LocalDate.of(2025, 8, 12).format(dtf));
    }

    public SessionInfo addItem(int sn, String date) {
        SessionInfo si = new SessionInfo(sn, date);
        return data.put(si.getSessionNumber(), si);
    }

    public SessionInfo removeItem(int index) {
        return data.remove(index);
    }

    public SessionInfo getItem(int index, boolean isKey) {
        if(isKey) return data.get(index);
        if(index < 0 || index >= data.size()) return null;
        return (SessionInfo) data.values().toArray()[index];
    }
    public int size() {
        return data.size();
    }

    private LinkedHashMap<Integer, SessionInfo> data;
}
