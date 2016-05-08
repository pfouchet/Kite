package com.groupeseb.kite.check.impl.methods;


import com.groupeseb.kite.Json;
import com.groupeseb.kite.check.ICheckMethod;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component
public class ExistsMethod implements ICheckMethod {
    @Override
    public Boolean match(String name) {
        return "exists".equalsIgnoreCase(name);
    }

    @Override
    public Object apply(@Nullable Object obj, Json parameters) {
        return obj != null;
    }
}
