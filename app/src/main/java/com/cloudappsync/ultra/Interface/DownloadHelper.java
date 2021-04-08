package com.cloudappsync.ultra.Interface;

public interface DownloadHelper {

    void afterExecutionIsComplete();

    void whenExecutionStarts();

    void whileInProgress(int i);

}
