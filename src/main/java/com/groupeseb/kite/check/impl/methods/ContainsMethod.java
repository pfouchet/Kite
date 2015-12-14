package com.groupeseb.kite.check.impl.methods;


import com.google.common.base.Preconditions;
import com.groupeseb.kite.Json;
import com.groupeseb.kite.check.ICheckMethod;

import net.minidev.json.JSONArray;
import org.springframework.stereotype.Component;

@Component
public class ContainsMethod implements ICheckMethod {
    @Override
    public Boolean match(String name) {
        return "contains".equalsIgnoreCase(name);
    }

    @Override
    public Object apply(Object obj, Json parameters) {
        Preconditions.checkArgument(
                Iterable.class.isAssignableFrom(obj.getClass()),
                "The source input must be iterable"
        );

        if (parameters.isIterable()) {
            for (Integer i = 0; i < parameters.getLength(); ++i) {
               try {
                   if (!(Boolean) apply(obj, parameters.get(i))) {
                       return false;
                   }
               } catch(ClassCastException ignored) {
                   // This is a primitive type
                   Object item = parameters.getObject(i);
                   if(! ((JSONArray) obj).contains(item)) {
                       return false;
                   }
               }
            }

            return true;
        } else {
            return ((JSONArray) obj).contains(parameters.getRootObject());
        }
    }
}
