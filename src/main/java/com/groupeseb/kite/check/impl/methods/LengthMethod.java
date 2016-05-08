package com.groupeseb.kite.check.impl.methods;

import com.groupeseb.kite.Json;
import com.groupeseb.kite.check.ICheckMethod;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class LengthMethod implements ICheckMethod {
	@Override
	public Boolean match(String name) {
		return "length".equalsIgnoreCase(name);
	}

	@Override
	public Object apply(Object o, Json parameters) {
		if (o instanceof Collection) {
			return ((Collection) o).size();
		}
		if (o instanceof String) {
			return ((String) o).length();
		}
		throw new IllegalArgumentException("The input argument of 'length' must be a collection or a string, actual :" + o);
	}
}
