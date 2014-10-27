package scott.sort.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scott.sort.api.config.DefinitionsSet;
import scott.sort.api.core.Environment;
import scott.sort.api.core.IEntityContextServices;
import scott.sort.api.core.QueryBatcher;
import scott.sort.api.core.entity.EntityContext;
import scott.sort.api.core.util.EnvironmentAccessor;
import scott.sort.api.exception.execution.SortServiceProviderException;
import scott.sort.api.exception.execution.TransactionsNoSupportedException;
import scott.sort.api.exception.execution.jdbc.CommitWithoutTransactionException;
import scott.sort.api.exception.execution.jdbc.RollbackWithoutTransactionException;
import scott.sort.api.exception.execution.persist.SortPersistException;
import scott.sort.api.exception.execution.query.SortQueryException;
import scott.sort.api.persist.PersistAnalyser;
import scott.sort.api.persist.PersistRequest;
import scott.sort.api.query.QueryObject;
import scott.sort.api.query.RuntimeProperties;
import scott.sort.server.jdbc.query.QueryResult;

/**
 * A fake remote client entity context services
 * which serializes and deserializes data, simulating a remote client
 * @author scott
 *
 */
public class RemoteClientEntityContextServices implements IEntityContextServices {

    private static final Logger LOG = LoggerFactory.getLogger(RemoteClientEntityContextServices.class);

    /**
     * The "server" entity context services
     */
    private final IEntityContextServices serverEntityContextServices;

    private Environment clientEnvironment;

    private Environment serverEnvironment;

    public RemoteClientEntityContextServices(IEntityContextServices serverEntityContextServices) {
        this.serverEntityContextServices = serverEntityContextServices;
    }

    public void setClientEnvironment(Environment clientEnvironment) {
        this.clientEnvironment = clientEnvironment;
    }

    public void setServerEnvironment(Environment serverEnvironment) {
        this.serverEnvironment = serverEnvironment;
    }

    @Override
    public DefinitionsSet getDefinitionsSet() {
        LOG.info("Getting definitions set from server");
        try {
            DefinitionsSet definitionsSet = serverEntityContextServices.getDefinitionsSet();

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oot = new ObjectOutputStream(bout);
            oot.writeObject(definitionsSet);
            oot.flush();
            oot.close();

            ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
            definitionsSet = (DefinitionsSet)oin.readObject();
            oin.close();
            return definitionsSet;
        }
        catch(Exception x) {
            throw new IllegalStateException("Serialization error", x);
        }
    }

    @Override
    public void setAutoCommit(EntityContext entityContext, boolean value) throws SortServiceProviderException {
        if (!value) {
            throw new TransactionsNoSupportedException("Transactions are not supported in this test remote client environment");
        }
    }

    @Override
    public boolean getAutoCommit(EntityContext entityContext) throws SortServiceProviderException {
        return true;
    }

    @Override
    public void joinTransaction(EntityContext newContext, EntityContext contextWithTransaction) {
        //NOOP: as there are never transactions on the client
    }

    @Override
    public void commit(EntityContext entityContext) throws SortServiceProviderException {
        throw new CommitWithoutTransactionException("Remote client cannot have a transaction");
    }

    @Override
    public void rollback(EntityContext entityContext) throws SortServiceProviderException {
        throw new RollbackWithoutTransactionException("Remote client cannot have a transaction");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> QueryResult<T> execute(EntityContext entityContext, QueryObject<T> query, RuntimeProperties props) throws SortServiceProviderException, SortQueryException {
        LOG.info("Executing on server via serialization/de-serialization");
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oot = new ObjectOutputStream(bout);
            oot.writeObject(entityContext);
            oot.writeObject(query);
            oot.writeObject(props);
            oot.flush();
            oot.close();

            EnvironmentAccessor.set(serverEnvironment);

            ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
            entityContext = (EntityContext)oin.readObject();
            query = (QueryObject<T>)oin.readObject();
            props = (RuntimeProperties)oin.readObject();
            oin.close();

            QueryResult<T> result = serverEntityContextServices.execute(entityContext, query, props);
            LOG.info("Sending result back via serialization/de-serialization");

            bout = new ByteArrayOutputStream();
            oot = new ObjectOutputStream(bout);
            oot.writeObject(result);
            oot.flush();
            oot.close();

            EnvironmentAccessor.set(clientEnvironment);

            oin = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
            result = (QueryResult<T>)oin.readObject();
            oin.close();

            return result;
        }
        catch(IOException x) {
            throw new IllegalStateException("Serialization error", x);
        }
        catch(ClassNotFoundException x) {
            throw new IllegalStateException("Serialization error", x);
        }
        finally {
            EnvironmentAccessor.remove();
        }
    }

    @Override
    public QueryBatcher execute(EntityContext entityContext, QueryBatcher queryBatcher, RuntimeProperties props) throws SortServiceProviderException, SortQueryException {
        LOG.info("Executing on server via serialization/de-serialization");
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oot = new ObjectOutputStream(bout);
            oot.writeObject(entityContext);
            oot.writeObject(queryBatcher);
            oot.writeObject(props);
            oot.flush();
            oot.close();

            EnvironmentAccessor.set(serverEnvironment);

            ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
            entityContext = (EntityContext)oin.readObject();
            queryBatcher = (QueryBatcher)oin.readObject();
            props = (RuntimeProperties)oin.readObject();
            oin.close();

            queryBatcher = serverEntityContextServices.execute(entityContext, queryBatcher, props);
            LOG.info("Sending result back via serialization/de-serialization");

            bout = new ByteArrayOutputStream();
            oot = new ObjectOutputStream(bout);
            oot.writeObject(queryBatcher);
            oot.flush();
            oot.close();

            EnvironmentAccessor.set(clientEnvironment);

            oin = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
            queryBatcher = (QueryBatcher)oin.readObject();
            oin.close();

            return queryBatcher;
        }
        catch(IOException x) {
            throw new IllegalStateException("Serialization error", x);
        }
        catch(ClassNotFoundException x) {
            throw new IllegalStateException("Serialization error", x);
        }
        finally {
            EnvironmentAccessor.remove();
        }
    }

    @Override
    public PersistAnalyser execute(PersistRequest persistRequest, RuntimeProperties runtimeProperties) throws SortServiceProviderException, SortPersistException {
        LOG.info("Executing on server via serialization/de-serialization");
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oot = new ObjectOutputStream(bout);
            oot.writeObject(persistRequest);
            oot.writeObject(runtimeProperties);
            oot.flush();
            oot.close();

            EnvironmentAccessor.set( serverEnvironment );

            ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
            persistRequest = (PersistRequest)oin.readObject();
            runtimeProperties = (RuntimeProperties)oin.readObject();
            oin.close();

            PersistAnalyser analysis = serverEntityContextServices.execute(persistRequest, runtimeProperties);
            LOG.info("Sending result back via serialization/de-serialization");

            bout = new ByteArrayOutputStream();
            oot = new ObjectOutputStream(bout);
            oot.writeObject(analysis);
            oot.flush();
            oot.close();

            EnvironmentAccessor.set( clientEnvironment );

            oin = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
            analysis = (PersistAnalyser)oin.readObject();
            oin.close();

            return analysis;
        }
        catch(SortServiceProviderException x) {
            throw serialize(x);
        }
        catch(SortPersistException x) {
            throw serialize(x);
        }
        catch(IOException x) {
            throw new IllegalStateException("Serialization error", x);
        }
        catch(ClassNotFoundException x) {
            throw new IllegalStateException("Serialization error", x);
        }
        finally {
            EnvironmentAccessor.remove();
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Throwable> T serialize(T exception)  {
        EnvironmentAccessor.set( clientEnvironment );
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oot = new ObjectOutputStream(bout);
            oot.writeObject(exception);
            oot.flush();
            oot.close();

            ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
            exception = (T)oin.readObject();
            oin.close();
            return exception;
        }
        catch(Exception x) {
            throw new IllegalStateException("Error serializing/de-serializing exception", x);
        }
        finally {
            EnvironmentAccessor.remove();
        }
    }


}
