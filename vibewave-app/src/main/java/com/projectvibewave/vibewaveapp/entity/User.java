import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import java.time.LocalDate;

@Entity
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    private String username;
    private String email;
    private String pass;

    private String artistname;
    private boolean isprivate;

    private LocalDate createdon;
    private int roleid;

    private String imagefilename;
    private boolean isverified;

    private boolean isemailconfirmed;

    public User () { 
        //
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getArtistName() {
        return artistname;
    }

    public boolean isPrivate() {
        return isprivate;
    }

    public LocalDate getCreatedOn() {
        return createdon;
    }

    public int getRoleId() {
        return roleid;
    }

    public String getImageFileName() {
        return imagefilename;
    }

    public boolean isVerified() {
        return isverified;
    }

    public boolean isEmailConfirmed() {
        return isemailconfirmed;
    }

    // TODO: set methods
}