package serversql;

import java.util.ArrayList;
import java.util.logging.Logger;

public class Alunni {
    public ArrayList<Alunno> array;

    public ArrayList<Alunno> getAlunni() {
        return array;
    }

    public void setAlunni(ArrayList<Alunno> array) {
        this.array = array;
    }

    public Alunni() {
    }
    
    public Alunni(ArrayList<Alunno> array) {
        this.array = array;
    }
    
    public void add(Alunno a){
        array.add(a);
    }
}
