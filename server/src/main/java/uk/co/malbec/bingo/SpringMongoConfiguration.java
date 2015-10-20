package uk.co.malbec.bingo;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;

@Configuration
@SuppressWarnings({"UnusedDeclaration"})
public class SpringMongoConfiguration extends AbstractMongoConfiguration {

    @Override
    protected String getDatabaseName() {
        return "bingo";
    }

    @Override
    public Mongo mongo() throws Exception {
        MongoClient client =  new MongoClient("127.0.0.1");
        client.setWriteConcern(WriteConcern.FSYNCED);
        return client;
    }
}


