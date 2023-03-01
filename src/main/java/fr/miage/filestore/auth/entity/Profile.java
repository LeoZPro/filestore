package fr.miage.filestore.auth.entity;

import fr.miage.filestore.auth.AuthenticationService;
import org.apache.commons.codec.digest.DigestUtils;

public class Profile {

    public static final Profile ANONYMOUS_PROFILE = new Profile(AuthenticationService.UNAUTHENTIFIED_IDENTIFIER, "anonymous", "Anonymous User", "user@anonymous.org", false);

    private String id;
    private String username;
    private String fullname;
    private String email;
    private boolean owner;

    public Profile() {
    }

    public Profile(String id, String username, String fullname, String email, boolean owner) {
        this.id = id;
        this.username = username;
        this.fullname = fullname;
        this.email = email;
        this.owner = owner;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGravatarHash() {
        return DigestUtils.md5Hex(email);
    }

    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", fullname='" + fullname + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
