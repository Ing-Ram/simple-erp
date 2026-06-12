package com.simpleerp.finance;

import com.simpleerp.shared.AuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** A party we buy from; owner of AP bills. */
@Entity
@Table(name = "vendors")
public class Vendor extends AuditableEntity {

    private String name;
    private String email;
    private int paymentTermsDays;
    private boolean active = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getPaymentTermsDays() {
        return paymentTermsDays;
    }

    public void setPaymentTermsDays(int paymentTermsDays) {
        this.paymentTermsDays = paymentTermsDays;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
