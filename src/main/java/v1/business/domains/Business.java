package v1.business.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.Email;

@Entity
public class Business extends PanacheEntity {

    @Column(nullable=false)
    public String businessName;

    @Column(nullable=false)
    public String address;

    @Column(nullable=false)
    @Email
    public String email;

    @Column(nullable=false)
    public String contact;

    public Business() {
    }

    public Business(String businessName, String address, String contact, String email) {
        this.businessName = businessName;
        this.address = address;
        this.contact = contact;
        this.email = email;
    }
}
