package v1.authentication.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.*;

@Entity
public class Authenticator extends PanacheEntity {
    @Lob
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

    public static Authenticator findByKey(String key){
        return find("apiKey", key).firstResult();
    }

    public static Authenticator findByName(String name){
        return find("business.businessName", name).firstResult();
    }
}
