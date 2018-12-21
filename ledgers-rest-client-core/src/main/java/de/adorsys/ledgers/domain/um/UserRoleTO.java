package de.adorsys.ledgers.domain.um;

public enum UserRoleTO {

    CUSTOMER, // A customer with associated bank accounts
    STAFF, // a staff member. Can access all accounts
    TECHNICAL, // a technical user. No SCA
    SYSTEM // A system user. FOr application management tasks.
}

