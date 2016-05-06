package com.groupeseb.kite;

public interface ICommandRunner {
    void execute(Command command, KiteContext kiteContext) throws Exception;
}