package com.smartstream.sort.server.jdbc.queryexecution;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.smartstream.sort.api.config.Definitions;
import com.smartstream.sort.api.config.EntityType;
import com.smartstream.sort.api.config.NodeDefinition;
import com.smartstream.sort.api.query.QJoin;
import com.smartstream.sort.api.query.QueryObject;

public class Projection implements Iterable<ProjectionColumn> {

    private final Definitions definitions;
    private List<ProjectionColumn> columns = new LinkedList<>();

    public Projection(Definitions definitions) {
        this.definitions = definitions;
    }

    @Override
    public Iterator<ProjectionColumn> iterator() {
        return columns.iterator();
    }

    public void build(QueryObject<?> query) {
        EntityType entityType = definitions.getEntityTypeMatchingInterface(query.getTypeName(), true);
        QJoin qj = query.getJoined();

        /*
         * add all table columns defined by the EntityType for this QueryObject
         */
        for (NodeDefinition nd : entityType.getNodeDefinitions()) {
            if (nd.getColumnName() != null) {
                ProjectionColumn pCol = new ProjectionColumn(this, query, qj, nd);
                if (!query.isDisabled(nd.getName())) {
                    columns.add(pCol);
                }
            }
        }
        /*
         * add the table columns for the other query objects which we join to
         */
        for (QJoin join : query.getJoins()) {
            build(join.getTo());
        }
    }

    int indexOf(ProjectionColumn column) {
        int i = columns.indexOf(column);
        if (i == -1) {
            throw new IllegalStateException("Projection column not found in projection: " + column);
        }
        return i + 1; //resultset style 1-N index
    }

    public List<ProjectionColumn> getColumns() {
        return columns;
    }

    public List<ProjectionColumn> getColumnsFor(QueryObject<?> queryObject) {
        List<ProjectionColumn> result = new LinkedList<>();
        for (ProjectionColumn column : columns) {
            if (column.getQueryObject() == queryObject) {
                result.add(column);
            }
        }
        return result;
    }

}