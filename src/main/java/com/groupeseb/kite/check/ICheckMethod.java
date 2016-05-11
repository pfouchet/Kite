package com.groupeseb.kite.check;

import com.groupeseb.kite.Json;

public interface ICheckMethod {
    boolean match(String name);

    Object apply(Object obj, Json parameters);
}
