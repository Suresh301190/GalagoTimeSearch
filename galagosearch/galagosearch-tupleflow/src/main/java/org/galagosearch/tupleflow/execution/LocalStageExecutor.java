// BSD License (http://www.galagosearch.org/license)
package org.galagosearch.tupleflow.execution;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.galagosearch.tupleflow.ExNihiloSource;
import org.xml.sax.SAXException;

/**
 *
 * @author trevor
 */
public class LocalStageExecutor implements StageExecutor {
    public static class SequentialExecutionContext implements StageExecutionStatus, Runnable {
        String name;
        List<StageInstanceDescription> instances;
        ArrayList<Exception> exceptions = new ArrayList();

        int queuedInstances = 0;
        int runningInstances = 0;
        int completedInstances = 0;
        boolean done = false;

        SequentialExecutionContext(String name, List<StageInstanceDescription> instances) {
            this.name = name;
            this.instances = instances;
            this.queuedInstances = instances.size();
        }

        public synchronized void markDone() {
            completedInstances = instances.size();
            runningInstances = 0;
            completedInstances = 0;
            done = true;
        }

        public void run() {
            try {
                for (StageInstanceDescription instance : instances) {
                    NetworkedCounterManager counterManager = new NetworkedCounterManager();
                    counterManager.start();
                    synchronized(this) {
                        runningInstances++;
                        queuedInstances--;
                    }
                    NetworkedCounterManager manager = new NetworkedCounterManager();
                    StageInstanceFactory factory = new StageInstanceFactory(manager);
                    manager.start();
                    ExNihiloSource source = factory.instantiate(instance);
                    source.run();
                    manager.stop();
                    synchronized(this) {
                        runningInstances--;
                        completedInstances++;
                    }
                }
            } catch(Exception e) {
                synchronized(this) {
                    exceptions.add(e);
                }
            } finally {
                synchronized(this) {
                    done = true;
                }
            }
        }

        public String getName() {
            return name;
        }

        public int getBlockedInstances() {
            return 0;
        }

        public synchronized int getQueuedInstances() {
            return queuedInstances;
        }

        public synchronized int getRunningInstances() {
            return runningInstances;
        }

        public int getCompletedInstances() {
            return completedInstances;
        }

        public synchronized boolean isDone() {
            return done;
        }

        public synchronized List<Exception> getExceptions() {
            return exceptions;
        }

        private synchronized void addException(Exception e) {
            exceptions.add(e);
        }
    }

    public SequentialExecutionContext execute(StageGroupDescription stage, String temporary) {
        SequentialExecutionContext context =
                new SequentialExecutionContext(stage.getName(), stage.getInstances());
        context.run();
        return context;
    }

    public SequentialExecutionContext execute(StageInstanceDescription stage) {
        SequentialExecutionContext context =
                new SequentialExecutionContext(stage.getName(), Collections.singletonList(stage));
        context.run();
        return context;
    }

    public StageExecutionStatus execute(String descriptionFile) {
        File errorFile = new File(descriptionFile + ".error");
        File completeFile = new File(descriptionFile + ".complete");
        SequentialExecutionContext result = null;

        Logger logger = Logger.getLogger(JobExecutor.class.toString());
        StageInstanceDescription stage = null;

        // try to parse the stage description from disk
        try {
            ObjectInputStream stream =
                    new ObjectInputStream(new FileInputStream(new File(descriptionFile)));
            stage = (StageInstanceDescription) stream.readObject();
        } catch(Exception e) {
            return new ErrorExecutionStatus("unknown", e);
        }
        
        // check for a checkpoint file.  If one exists, we quit.
        if (completeFile.exists()) {
            logger.info("Exiting early because a complete checkpoint was found.");
            result = new SequentialExecutionContext(stage.getName(),
                    Collections.singletonList(stage));
            result.markDone();
            return result;
        }

        // get rid of any error checkpoints
        if (errorFile.exists()) {
            errorFile.delete();
        }

        result = execute(stage);

        try {
            if (result.getExceptions().size() > 0) {
                Throwable e = result.getExceptions().get(0);
                BufferedWriter writer = new BufferedWriter(new FileWriter(errorFile));
                writer.write(e.toString());
                writer.close();
            } else {
                BufferedWriter writer = new BufferedWriter(new FileWriter(completeFile));
                writer.close();
            }
        } catch(Exception e) {
            logger.warning("Trouble writing completion/error files: " + errorFile.toString());
        }

        return result;
    }

    public void shutdown() {
    }

    public static void main(String[] args) throws UnsupportedEncodingException, ParserConfigurationException, SAXException, IOException, ClassNotFoundException {
        Logger logger = Logger.getLogger(JobExecutor.class.toString());

        String stageDescriptionFile = args[0];
        logger.info("Initializing: " + stageDescriptionFile);
        StageExecutionStatus context = new LocalStageExecutor().execute(stageDescriptionFile);

        if (context.getExceptions().size() > 0) {
            logger.severe("Exception thrown: " + context.getExceptions().get(0).toString());
        } else {
            logger.info("Job complete");
        }
    }
}
