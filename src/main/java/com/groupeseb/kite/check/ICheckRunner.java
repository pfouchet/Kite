package com.groupeseb.kite.check;

import org.json.simple.parser.ParseException;

public interface ICheckRunner {
    void verify(Check check, String responseBody) throws ParseException;
}
