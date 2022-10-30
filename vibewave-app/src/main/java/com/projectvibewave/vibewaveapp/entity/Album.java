import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import java.time.LocalDate;

@Entity
public class Album {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    private String name;
    private int formatid;
    private int userid;

    private LocalDate publishdate;
    private String coverfilename;

    public Album () { 
        //
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getFormatId() {
        return formatid;
    }

    public int getUserId() {
        return userid;
    }

    public LocalDate getPublishDate() {
        return publishdate;
    }

    public String getCoverFilename() {
        return coverfilename;
    }

    // TODO: set methods
}