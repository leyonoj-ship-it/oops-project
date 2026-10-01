package com.smartdine.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * OOP INHERITANCE:
 * EstablishmentOwner extends Person, representing manager/owner of a Hotel or Canteen.
 */
@Entity
@Table(name = "establishment_owners")
public class EstablishmentOwner extends Person {

    public EstablishmentOwner() {
        super();
        setRole(UserRole.ESTABLISHMENT_OWNER);
    }

    public EstablishmentOwner(String name, String email, String phone) {
        super(name, email, phone, UserRole.ESTABLISHMENT_OWNER);
    }

    @Override
    public String getRoleDescription() {
        return "Establishment Owner/Manager: Manages menu, orders, tables, reservations, and clearance offers.";
    }

    @Override
    public boolean canPerformAdminActions() {
        return false;
    }

    @Override
    public boolean canManageEstablishment() {
        return true;
    }
}
