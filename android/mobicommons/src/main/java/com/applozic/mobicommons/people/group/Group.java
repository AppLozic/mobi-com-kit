package com.applozic.mobicommons.people.group;

import com.applozic.mobicommons.people.contact.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by devashish on 5/9/14.
 */
public class Group {

    private Long groupId;
    private String name;

    private List<Contact> contacts = new ArrayList<Contact>();

    public Group() {

    }

    public Group(Long groupId, String name) {
        this.groupId = groupId;
        this.name = name;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }
}
