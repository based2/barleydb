package scott.barleydb.server.jdbc.persist;

/*
 * #%L
 * BarleyDB
 * %%
 * Copyright (C) 2014 Scott Sinclair <scottysinclair@gmail.com>
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.sql.BatchUpdateException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scott.barleydb.api.config.Definitions;
import scott.barleydb.api.core.entity.Entity;
import scott.barleydb.api.exception.execution.jdbc.AddBatchException;
import scott.barleydb.api.exception.execution.jdbc.SortJdbcException;
import scott.barleydb.api.exception.execution.persist.IllegalPersistStateException;
import scott.barleydb.api.exception.execution.persist.PreparingPersistStatementException;
import scott.barleydb.api.exception.execution.persist.SortPersistException;
import scott.barleydb.server.jdbc.JdbcEntityContextServices;
import scott.barleydb.server.jdbc.vendor.Database;

/**
 * Executes batch operations on a set of entities across various tables.
 * Contiguous entities of the same type will participate together in a JDBC batch operation.
 * @author scott
 *
 */
abstract class BatchExecuter {

    private static final Logger LOG = LoggerFactory.getLogger(BatchExecuter.class);

    private final OperationGroup group;
    private final String operationName;
    private final Database database;

    public BatchExecuter(OperationGroup group, String operationName, Database database) {
        this.group = group;
        this.operationName = operationName;
        this.database = database;
    }

    public void execute(JdbcEntityContextServices jdbcEntityContextServices, Definitions definitions) throws PreparingPersistStatementException, SortPersistException, SortJdbcException {
        if (group.getEntities().isEmpty()) {
            return;
        }
        try ( PreparedStatementPersistCache psCache = new PreparedStatementPersistCache(jdbcEntityContextServices, definitions);) {
            PreparedStatement psLast = null;
            List<Entity> entities = new LinkedList<>();
            for (Entity entity : group.getEntities()) {
                PreparedStatement ps = prepareStatement(psCache, entity);
                if (psLast != null && psLast != ps) {
                    executeBatch(psLast, entities);
                    entities.clear();
                }
                try {
                    ps.addBatch();
                }
                catch(SQLException x) {
                    throw new AddBatchException("SQLException adding batch", x);

                }
                entities.add(entity);
                psLast = ps;
            }
            executeBatch(psLast, entities);
        }
    }

    private void executeBatch(PreparedStatement ps, List<Entity> entities) throws SortPersistException  {
        final String contextInfo = "executing " + operationName + " batch for " + entities.get(0).getEntityType() + " of size " + entities.size();
        LOG.debug(contextInfo);
        try {
            int counts[] = ps.executeBatch();
            if (counts.length != entities.size()) {
                throw new SortPersistException("The number update counts returned, does not match the size of the batch counts=" + counts.length + ", entities=" + entities.size() );
            }
            int totalMods = 0;
            for (int i = 0; i < counts.length; i++) {
                if (counts[i] == Statement.SUCCESS_NO_INFO) {
                    if (database.supportsBatchUpdateCounts())  {
                        throw new IllegalPersistStateException("Received SUCCESS_NO_INFO from database: " + database + " which should support batch update counts.");
                    }
                }
                else if (counts[i] == Statement.EXECUTE_FAILED) {
                    /*
                     * This makes no sense, a batch update exception should have been thrown.
                     *
                     */
                    handleFailure(entities.get(i), null);
                }
                else if (counts[i] == 0) {
                   handleNoop(entities.get(i), null);
                }
                else {
                    totalMods += counts[i];
                }
            }
            if (database.supportsBatchUpdateCounts())  {
                LOG.debug(totalMods + " rows were modified in total");
            }
            else {
                LOG.debug(database.getInfo() + " does not support batch update counts, pemissistic locking was used to guarantee optimistic lock.");
            }
        }
        catch (BatchUpdateException x) {
            int counts[] = x.getUpdateCounts();
            if (counts.length < entities.size()) {
                /*
                 * the counts are less then the batch size
                 * so the counts were the successfull ones
                 */
                for (int i = counts.length, n = entities.size(); i < n; i++) {
                    handleFailure(entities.get(i), x);
                }
            }
            else {
                /*
                 * all rows were processed, and we have to check the count status
                 */
                for (int i = 0; i < counts.length; i++) {
                    if (counts[i] == Statement.EXECUTE_FAILED) {
                        handleFailure(entities.get(i), x);
                    }

                }
            }
        }
        catch(SQLException x) {
            //thrown by the executeBatch call for a generic problem
            throw new SortPersistException("SQLException when " + contextInfo, x);
        }

    }

    protected abstract void handleFailure(Entity entity, Throwable throwable) throws SortPersistException;

    protected abstract void handleNoop(Entity entity, Throwable throwable) throws SortPersistException;

    protected abstract PreparedStatement prepareStatement(PreparedStatementPersistCache psCache, Entity entity) throws SortPersistException;
}
