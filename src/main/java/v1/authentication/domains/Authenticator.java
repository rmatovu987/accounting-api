package v1.authentication.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import v1.business.domains.Business;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
public class Authenticator extends PanacheEntity {
    @Column(nullable=false)
    public String apiKey;

    @OneToOne
    @JoinColumn(nullable=false)
    public Business business;

    public Authenticator() {
    }

    public Authenticator(String apiKey, Business business) {
        this.apiKey = apiKey;
        this.business = business;
    }
}