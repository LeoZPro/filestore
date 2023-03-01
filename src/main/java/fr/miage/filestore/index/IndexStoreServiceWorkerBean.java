package fr.miage.filestore.index;

import fr.miage.filestore.files.FileService;
import fr.miage.filestore.files.entity.Node;
import fr.miage.filestore.store.BinaryStore;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedThreadFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

@Startup
@Singleton
public class IndexStoreServiceWorkerBean implements IndexStoreServiceWorker {

    private static final Logger LOGGER = Logger.getLogger(IndexStoreServiceWorkerBean.class.getName());
    private static final String[] EVENTS_HANDLE = {"folder.create", "folder.delete", "file.create", "file.delete"};

    @Resource
    private ManagedThreadFactory managedThreadFactory;

    @EJB
    FileService fileService;

    @EJB
    IndexStoreService indexStore;

    @EJB
    BinaryStore binaryStore;

    private JobWorkerThread worker;
    private Thread workerThread;
    private BlockingQueue<IndexStoreJob> queue;

    @Override
    @PostConstruct
    public void start() {
        if (workerThread != null && workerThread.isAlive()) {
            LOGGER.log(Level.WARNING, "Job Worker already started");
            return;
        }
        LOGGER.log(Level.INFO, "Starting job worker thread");
        worker = new JobWorkerThread();
        queue = new LinkedBlockingQueue<>();
        workerThread = managedThreadFactory.newThread(worker);
        workerThread.setName("Job Worker Thread");
        workerThread.start();
        Thread.UncaughtExceptionHandler h = (th, ex) -> {
            LOGGER.log(Level.SEVERE, "Uncaught exception", ex);
            start();
        };
        workerThread.setUncaughtExceptionHandler(h);
        LOGGER.log(Level.INFO, "Job worker thread started: " + workerThread.getState().toString());
    }

    @Override
    @PreDestroy
    public void stop() {
        LOGGER.log(Level.INFO, "Stopping job worker thread");
        worker.stop();
    }

    @Override
    public List<IndexStoreJob> getQueue() {
        return new ArrayList<>(queue);
    }

    @Override
    public void submit(String type, String node) {
        LOGGER.log(Level.INFO, "Submitting new job to worker");
        IndexStoreJob job = new IndexStoreJob();
        job.setId(UUID.randomUUID().toString());
        job.setStatus(IndexStoreJob.Status.PENDING);
        job.setType(type);
        job.setNode(node);
        try {
            queue.put(job);
        } catch ( InterruptedException e ) {
            LOGGER.log(Level.INFO, "Unable to put job in worker queue");
        }
    }

    class JobWorkerThread implements Runnable {

        private boolean run = true;

        void stop() {
            this.run = false;
        }

        @Override
        public void run() {
            while (run) {
                try {
                    LOGGER.log(Level.INFO, "Ready to take next job");
                    IndexStoreJob job = queue.take();
                    LOGGER.log(Level.INFO, "Take job: " + job.getId());
                    try {
                        StringBuffer report = new StringBuffer();

                        if (job.getType().endsWith("create") || job.getType().endsWith("update")) {
                            Node node = fileService.get(job.getNode());
                            IndexStoreObject object = new IndexStoreObject();
                            object.setIdentifier(job.getNode());
                            object.setScope(IndexStoreObject.Scope.PRIVATE);
                            //TODO This should be delegated to File Service into a system method (systemGetIndexableContent(key))
                            if (job.getType().startsWith("file")) {
                                object.setContent(node.getName() + " " + node.getMimetype() + " " + binaryStore.extract(node.getContentId(), node.getName(), node.getMimetype()));
                            }
                            if (job.getType().startsWith("folder")) {
                                object.setContent(node.getName() + " " + node.getMimetype());
                            }
                            indexStore.index(object);
                        }

                        if (job.getType().endsWith("remove")) {
                            indexStore.remove(job.getNode());
                        }

                        report.append("Job done.");
                        job.setOutput(report.toString());
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Something wrong happened: " + e.getMessage(), e);
                    }
                    LOGGER.log(Level.INFO, "Job done.");
                } catch (InterruptedException e) {
                    if ( run ) {
                        LOGGER.log(Level.SEVERE, "Interrupted while trying to take next job", e);
                    } else {
                        LOGGER.log(Level.INFO, "Worker interrupted");
                    }
                }
            }
            LOGGER.log(Level.INFO, "End of life");
        }
    }


}
