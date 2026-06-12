package com.simpleerp.finance;

import com.simpleerp.shared.AuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** A party that buys from us; owner of AR invoices. Shared with Sales via CustomerService. */
@Entity
@Table(name = "customers")
public class Customer extends AuditableEntity {

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
