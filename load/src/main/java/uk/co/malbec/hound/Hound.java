package uk.co.malbec.hound;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.joda.time.DateTime;
import org.slf4j.LoggerFactory;
import uk.co.malbec.hound.impl.OperationRecord;
import uk.co.malbec.hound.impl.Sampler;
import uk.co.malbec.hound.impl.Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static org.joda.time.DateTime.now;

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

    private Sampler sampler = new Sampler();

    private DateTime shutdownTime = now().plusDays(10);

    public <T> Hound register(OperationType operationType, Class<T> clz, Operation<T> operation) {
        operations.put(operationType, new OperationRecord( operation, clz));
        return this;
    }

    public Context createUser(){
        return new Context();
    }

    public void shutdownTime(DateTime shutdownTime) {
        this.shutdownTime = shutdownTime;
    }

    public class Context {
        private Map<Class<?>, Supplier<?>> resourceSuppliers = new HashMap<Class<?>, Supplier<?>>();

        private Map<String, Object> session = new HashMap<>();

        private List<BiConsumer<String, String>> traceLoggers = new ArrayList<>();

        public <T> Context registerSupplier(Class<T> clazz, Supplier<T> supplier){
            resourceSuppliers.put(clazz, supplier);
            return this;
        }

        public Context addToSession(String key, Object value){
            session.put(key, value);
            return this;
        }

        public <T>  void  start(String name, Transition transition) {

            boolean firstStart = false;
            if (server == null) {
                server = new Server(operations, shutdownTime, sampler);
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
