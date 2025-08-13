package bogdan.cvetanovski.pasalic;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class StudentModel {
    public class StudentInfo {
        private String firstName;
        private String lastName;
        private String index;
        private int image; // Use Resources for now

        public StudentInfo(String fn, String ln, String i, int img) {
            firstName = fn;
            lastName = ln;
            index = i;
            image = img;
        }
        public String getIndex() {
            return index;
        }
        public void setIndex(String index) {
            this.index = index;
        }
        public int getImage() {
            return image;
        }
        public void setImage(int image) {
            this.image = image;
        }
        public String getLastName() {
            return lastName;
        }
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        public String getFirstName() {
            return firstName;
        }
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
    }
    public StudentModel() {
        data = new LinkedHashMap<String, StudentInfo>();
        addItem("Pera", "Peric", "RA1/2026", -1);
        addItem("Mara", "Maric", "RA2/2026", -1);
        addItem("Marko", "Markovic", "RA3/2026", -1);
        addItem("Milena", "Milic", "RA4/2026", -1);
        addItem("Milos", "Nikolic", "RA5/2026", -1);
        addItem("Radmila", "Radic", "RA6/2026", -1);
        addItem("Nikola", "Milanov", "RA7/2026", -1);
        addItem("Stefan", "Stefanovic", "RA8/2026", -1);
        addItem("Petar", "Petrovic", "RA9/2026", -1);
        addItem("Anastasija", "Jovanovic", "RA10/2026", -1);
        addItem("Bojan", "Petkovic", "RA11/2026", -1);
        addItem("Dragan", "Dejanovic", "RA12/2026", -1);
        addItem("Ljudmila", "Perisic", "RA13/2026", -1);
        addItem("Dragana", "Stojanovic", "RA14/2026", -1);
        addItem("Jelena", "Jankovic", "RA15/2026", -1);
        addItem("Goran", "Stojkovic", "RA16/2026", -1);
    }

    public StudentInfo addItem(String fn, String ln, String i, int img) {
        StudentInfo si = new StudentInfo(fn, ln, i, img);
        return data.put(si.getIndex(), si);
    }

    public StudentInfo removeItem(String index) {
        return data.remove(index);
    }

    public StudentInfo getItem(String index) {
        return data.get(index);
    }
    public StudentInfo getItem(int index) {
        if(index < 0 || index >= data.size()) return null;
        return (StudentInfo)data.values().toArray()[index];
    }

    public int size() {
        return data.size();
    }

    private LinkedHashMap<String, StudentInfo> data;
}
