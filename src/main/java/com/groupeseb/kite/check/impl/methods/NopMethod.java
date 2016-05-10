package com.groupeseb.kite.check.impl.methods;

import com.groupeseb.kite.Json;
import com.groupeseb.kite.check.ICheckMethod;
import org.springframework.stereotype.Component;

@Component
public class NopMethod implements ICheckMethod {
    @Override
    public boolean match(String name) {
        return "nop".equalsIgnoreCase(name);
    }

    @Override
    public Object apply(Object obj, Json parameters) {
        return obj;
    }
}
