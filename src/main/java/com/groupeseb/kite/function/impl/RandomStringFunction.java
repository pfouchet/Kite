package com.groupeseb.kite.function.impl;

import com.groupeseb.kite.function.AbstractWithoutParametersFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class RandomStringFunction extends AbstractWithoutParametersFunction {

    RandomStringFunction() {
        super("RandomString");
    }

    @Override
    public String apply() {
        return UUID.randomUUID().toString();
    }
}
