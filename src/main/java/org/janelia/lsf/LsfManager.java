package org.janelia.lsf;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class LsfManager {

    private ExecutorService executor;

    public Future<String> submitJob() {

        
        
        return executor.submit(new Callable<String>() {
            public String call() {
                return null;
            }
        });
    }

}
