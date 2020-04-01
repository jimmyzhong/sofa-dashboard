package me.izhong.db.mongo.config;

import com.mongodb.MongoCommandException;
import com.mongodb.MongoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

@Slf4j
public class RetryMongoTransactionManager extends MongoTransactionManager {

    public RetryMongoTransactionManager(MongoDbFactory factory) {
        super(factory);
    }

    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        super.doBegin(transaction,definition);
    }

    @Override
    protected void doCommit(MongoTransactionObject transactionObject) throws Exception {
        int retries = 3;
          do {
              try {
                  transactionObject.commitTransaction();
                  break;
              } catch (Throwable e) {
                  if(e instanceof MongoCommandException) {
                      MongoCommandException comExp = (MongoCommandException) e;
                      if (comExp.hasErrorLabel(MongoException.TRANSIENT_TRANSACTION_ERROR_LABEL)) {
                          log.info("重试事务，次数{}", retries);
                          continue;
                      }
                      if (comExp.hasErrorLabel(MongoException.UNKNOWN_TRANSACTION_COMMIT_RESULT_LABEL)) {
                          log.info("重试事务，次数{}", retries);
                          continue;
                      }
                  }
                  throw e;
              } finally {
                  //log.info("session over");
              }
          } while (--retries > 0);
    }
}
