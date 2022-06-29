package v1.authentication.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.validation.constraints.Email;

@Entity
public class Business extends PanacheEntity {

    @Column(nullable=false, unique = true)
    public String businessName;

    @Column(nullable=false)
    public String address;

    @Column(nullable=false)
    @Email
    public String email;

    @Column(nullable=false)
    public String contact;

    @Lob
    @JsonbTransient
    @Column(nullable = false)
    public String logo;

    public Business() {
    }

    public Business(String businessName, String address, String contact, String email, String logo) {
        this.businessName = businessName;
        this.address = address;
        this.contact = contact;
        this.email = email;
        this.logo = logo;
    }

    public static Business findByName(String name){
        return find("businessName", name).firstResult();
    }
}
