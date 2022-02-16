package org.json.junit.data;

import org.json.jsonjava.JSONPropertyIgnore;
import org.json.jsonjava.JSONPropertyName;

public interface MyBeanCustomNameInterface {
    @JSONPropertyName("InterfaceField")
    float getSomeFloat();
    @JSONPropertyIgnore
    int getIgnoredInt();
}