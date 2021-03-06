package com.telekom.m2m.cot.restsdk.users;

import com.telekom.m2m.cot.restsdk.util.ExtensibleObject;

/**
 * A class that represents a reference to a group. A reference is an object that
 * holds the URL of the original object that it refers to. Created by Ozan
 * Arslan on 25.07.2017
 */
public class GroupReference extends ExtensibleObject {


    public GroupReference() {
        super();
    }

    public GroupReference(ExtensibleObject extensibleObject) {
        super(extensibleObject);
    }

    /**
     * The method to retrieve the group that the group reference holds.
     * 
     * @return Group hold by the reference.
     */
    public Group getGroup() {
        ExtensibleObject obj = (ExtensibleObject) anyObject.get("group");
        return new Group(obj);
    }

    /**
     * The method to retrieve the URL of a group.
     * 
     * @return the URL of the group.
     */
    public String getSelf() {
        return (String) anyObject.get("self");

    }

    /**
     * The method to set the reference to a group.
     * 
     * @param group {@link Group} object to set.
     */
    public void setGroup(Group group) {
        anyObject.put("group", group);

    }

}
