package com.groupeseb.kite;

public interface ICommandRunner {
    void execute(Command command, CreationLog creationLog) throws Exception;
}