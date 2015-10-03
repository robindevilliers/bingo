package uk.co.malbec.hound;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.joda.time.DateTime;
import org.slf4j.LoggerFactory;
import uk.co.malbec.hound.impl.*;
import uk.co.malbec.hound.reporter.HtmlReporter;
import uk.co.malbec.hound.sampler.HybridSampler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.joda.time.DateTime.now;
import static uk.co.malbec.hound.Utils.pause;

public class Hound {

    Logger apache = (Logger) LoggerFactory.getLogger("org.apache.http");

    {
        apache.setLevel(Level.INFO);
    }

    Logger resteasy = (Logger) LoggerFactory.getLogger("org.jboss.resteasy");

    {
        resteasy.setLevel(Level.INFO);
    }

    private Map<OperationType, OperationRecord> operations = new HashMap<>();

    private Server server;

    private Sampler sampler;

    private DateTime shutdownTime = now().plusDays(10);

    private Reporter reporter;

    private int threadSize = 3000;
    private String description;

    public Hound(){

        File sampleDirectory = new File(format("%s/sample/%s", System.getProperty("user.dir"), System.currentTimeMillis()));
        if (!sampleDirectory.isDirectory()) {
            sampleDirectory.mkdirs();
        }

        sampler = new HybridSampler(threadSize, sampleDirectory);
    }

    public <T> Hound register(OperationType operationType, Class<T> clz, Operation<T> operation) {
        operations.put(operationType, new OperationRecord(operation, clz));
        return this;
    }

    public <Q extends Reporter> Q configureReporter(Class<Q> clazz){
        try {
            reporter = clazz.newInstance();
            return (Q) reporter;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Context createUser() {
        return new Context();
    }

    public Hound shutdownTime(DateTime shutdownTime) {
        this.shutdownTime = shutdownTime;
        return this;
    }

    public void waitFor() {
        while (server.isAlive()){
            pause(50);
        }

    }

    public class Context {
        private Map<Class<?>, Supplier<?>> resourceSuppliers = new HashMap<Class<?>, Supplier<?>>();

        private Map<String, Object> session = new HashMap<>();

        private List<BiConsumer<String, String>> traceLoggers = new ArrayList<>();

        public <T> Context registerSupplier(Class<T> clazz, Supplier<T> supplier) {
            resourceSuppliers.put(clazz, supplier);
            return this;
        }

        public Context addToSession(String key, Object value) {
            session.put(key, value);
            return this;
        }

        public <T> void start(String name, Transition transition) {

            boolean firstStart = false;
            if (server == null) {
                server = new Server(threadSize, operations, shutdownTime, sampler, () -> {
                    reporter.generate(sampler.getAllSamples());
                });
                firstStart = true;
            }

            new OperationContext(name, server.getQueue(), resourceSuppliers, session, traceLoggers).schedule(transition);

            if (firstStart) {
                server.start();
            }
        }

        public Context addTraceLogger(BiConsumer<String, String> logger) {
            traceLoggers.add(logger);
            return this;
        }
    }
}
