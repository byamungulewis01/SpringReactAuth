package com.bmglewis.model.enums;

/**
 * FILE TYPE: ENUM
 * System modules enumeration
 */
public enum Module {
    USER_MANAGEMENT("User Management"),
    VENDOR_MANAGEMENT("Vendor Management"),
    PURCHASE_REQUISITION("Purchase Requisition"),
    PURCHASE_ORDER("Purchase Order"),
    RFQ("Request for Quotation"),
    INVOICE("Invoice Management"),
    CONTRACT("Contract Management"),
    BUDGET("Budget Management"),
    REPORTS("Reports & Analytics"),
    SETTINGS("System Settings");

    private final String displayName;

    Module(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}