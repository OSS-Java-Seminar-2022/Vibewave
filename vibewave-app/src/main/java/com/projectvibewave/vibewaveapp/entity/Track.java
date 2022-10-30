import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import java.time.LocalDate;

@Entity
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private int durationinseconds;
    private int timesplayed;

    private LocalDate publishdate;
    private int albumid;

    public Track () { 
        //
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDurationInSeconds() {
        return durationinseconds;
    }

    public int getTimesPlayed() {
        return timesplayed;
    }

    public LocalDate getPublishDate() {
        return publishdate;
    }

    public int getAlbumId() {
        return albumid;
    }

    // TODO: set methods
}