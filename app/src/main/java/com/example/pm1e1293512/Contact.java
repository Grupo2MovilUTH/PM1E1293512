package com.example.pm1e1293512;

public class Contact {
    private String name;
    private String phone;
    private String note;

    public Contact(String name, String phone, String note) {
        this.name = name;
        this.phone = phone;
        this.note = note;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getNote() {
        return note;
    }
}
