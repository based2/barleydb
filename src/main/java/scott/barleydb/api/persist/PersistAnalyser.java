package scott.barleydb.api.persist;

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

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scott.barleydb.api.config.EntityType;
import scott.barleydb.api.core.entity.Entity;
import scott.barleydb.api.core.entity.EntityContext;
import scott.barleydb.api.core.entity.EntityContextHelper;
import scott.barleydb.api.core.entity.RefNode;
import scott.barleydb.api.core.entity.ToManyNode;
import scott.barleydb.api.exception.execution.persist.EntityMissingException;
import scott.barleydb.api.exception.execution.persist.IllegalPersistStateException;
import scott.barleydb.server.jdbc.persist.OperationGroup;

public class PersistAnalyser implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(PersistAnalyser.class);

    private final OperationGroup createGroup;

    private final OperationGroup updateGroup;

    private final OperationGroup deleteGroup;

    private final OperationGroup dependsOnGroup;

    private final OperationGroup allGroups[];

    private final EntityContext entityContext;

    private final Set<Entity> loadedDuringAnalysis = new HashSet<>();

    private final Set<Entity> analysing = new HashSet<>();

    public PersistAnalyser(EntityContext entityContext) {
        this(entityContext, new OperationGroup(), new OperationGroup(), new OperationGroup(), new OperationGroup());
    }

    private PersistAnalyser(EntityContext entityContext, OperationGroup createGroup, OperationGroup updateGroup, OperationGroup deleteGroup, OperationGroup dependsOnGroup) {
        this.entityContext = entityContext;
        this.createGroup = createGroup;
        this.updateGroup = updateGroup;
        this.deleteGroup = deleteGroup;
        this.dependsOnGroup = dependsOnGroup;
        this.allGroups = new OperationGroup[] { createGroup, updateGroup, deleteGroup, dependsOnGroup };
    }

    public EntityContext getEntityContext() {
        return entityContext;
    }

    /**
     * Clones the entity context and all the entities
     * @return
     */
    public PersistAnalyser deepCopy() {
        EntityContext newContext = entityContext.newEntityContextSharingTransaction();
        newContext.beginSaving();
        PersistAnalyser copyAnalyser = new PersistAnalyser(newContext);

        copyEntityValues(createGroup, copyAnalyser.createGroup, newContext);
        copyEntityValues(updateGroup, copyAnalyser.updateGroup, newContext);
        copyEntityValues(deleteGroup, copyAnalyser.deleteGroup, newContext);
        copyEntityValues(dependsOnGroup, copyAnalyser.dependsOnGroup, newContext);
        EntityContextHelper.copyRefStates(entityContext, newContext, newContext.getEntitiesSafeIterable(), new EntityContextHelper.EntityFilter() {
            @Override
            public boolean includesEntity(Entity entity) {
                return containsEntity(entity);
            }
        });
//    	LOG.debug("Printing copied context");
//    	LOG.debug(newContext.printXml());
        return copyAnalyser;
    }

    private boolean containsEntity(Entity entity) {
        for (OperationGroup group : allGroups) {
            for (Entity e : group.getEntities()) {
                if (e == entity) {
                    return true;
                }
            }
        }
        return false;
    }

    public PersistAnalyser optimizedCopy() {
        return new PersistAnalyser(
                entityContext,
                createGroup.optimizedForInsertCopy(),
                updateGroup.optimizedForUpdateCopy(),
                deleteGroup.optimizedForDeleteCopy(),
                dependsOnGroup);
    }

    public OperationGroup getCreateGroup() {
        return createGroup;
    }

    public OperationGroup getUpdateGroup() {
        return updateGroup;
    }

    public OperationGroup getDeleteGroup() {
        return deleteGroup;
    }

    public OperationGroup getDependsOnGroup() {
        return dependsOnGroup;
    }

    public String report() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nCreate Group ----------------\n");
        reportGroup(sb, createGroup);
        sb.append("\nUpdate Group ----------------\n");
        reportGroup(sb, updateGroup);
        sb.append("\nDelete Group ----------------\n");
        reportGroup(sb, deleteGroup);
        sb.append("\nDepends On Group ----------------\n");
        reportGroup(sb, dependsOnGroup);
        return sb.toString();
    }

    private void reportGroup(StringBuilder sb, OperationGroup group) {
        for (Entity entity : group.getEntities()) {
            sb.append(entity.toString());
            sb.append('\n');
        }
    }

    /**
     * Analyzes the persistRequest
     *
     * @param persistRequest
     * @throws IllegalPersistStateException
     * @throws EntityMissingException
     */
    public void analyse(PersistRequest persistRequest) throws IllegalPersistStateException, EntityMissingException {
        try {
            for (Entity entity : persistRequest.getToInsert()) {
                /*
                 * top level toInsert entities get analyzed by themselves
                 * so if they have been analyzed already then clear that.
                 */
                removeAnalysis(entity);

                if (entity.getEntityContext() != entityContext) {
                    throw new IllegalPersistStateException("Cannot persist entity from a different context");
                }
                analyseCreate(entity);
            }
            for (Entity entity : persistRequest.getToUpdate()) {
                /*
                 * top level toInsert entities get analyzed by themselves
                 * so if they have been analyzed already then clear that.
                 */
                removeAnalysis(entity);

                if (entity.getEntityContext() != entityContext) {
                    throw new IllegalPersistStateException("Cannot persist entity from a different context");
                }
                analyseUpdate(entity);
            }
            for (Entity entity : persistRequest.getToSave()) {
                /*
                 * top level toSave entities get analyzed by themselves
                 * so if they have been analyzed already then clear that.
                 */
                removeAnalysis(entity);

                if (entity.getEntityContext() != entityContext) {
                    throw new IllegalPersistStateException("Cannot persist entity from a different context");
                }
                if (entity.isNew()) {
                    analyseCreate(entity);
                } else {
                    analyseUpdate(entity);
                }
            }
            for (Entity entity : persistRequest.getToDelete()) {
                removeAnalysis(entity);
                if (entity.getEntityContext() != entityContext) {
                    throw new IllegalPersistStateException("Cannot persist entity from a different context");
                }
                analyseDelete(entity);
            }
        } finally {
            analysing.clear();
        }
    }

    /**
     *
     * @param persistRequest
     * @throws IllegalPersistStateException
     */
    public void analysePhase2(PersistRequest persistRequest) throws IllegalPersistStateException {

    }

    private void removeAnalysis(Entity entity) {
        if (analysing.remove(entity)) {
            for (OperationGroup og : allGroups) {
                og.getEntities().remove(entity);
            }

        }

    }

    private void analyseCreate(Entity entity) throws EntityMissingException {
        if (!analysing.add(entity)) {
            return;
        }
        LOG.debug("analysing " + entity + " for create");
        /*
         * I can only be created after any of my FK references are created.
         */
        analyseRefNodes(entity, true);

        /*
         * Schedule ourselves for creation
         */
        createGroup.add(entity);

        /*
         * Look at the to many relations, they must require creation
         * as our PK doesn't exist yet
         */
        for (ToManyNode toManyNode : entity.getChildren(ToManyNode.class)) {
            for (Entity refEntity : toManyNode.getList()) {
            	if (!refEntity.isNew()) {
            		LOG.error("A new entity has a tomany node containing entities which exist in the database, the data model is incorrect...");
            	}
                analyseCreate(refEntity);
            }
        }
    }

    private void analyseUpdate(Entity entity) throws EntityMissingException {
        if (!analysing.add(entity)) {
            return;
        }
        LOG.debug("analysing " + entity + " for update");
        /*
         * I may now refer to different FKs so they should be processed before my update.
         */
        analyseRefNodes(entity, true);
        /*
         * Schedule ourselves for update
         */
        updateGroup.add(entity);
        /*
         * if we own any references which were removed then we have to delete the
         * entities which the references pointed at
         */
        for (RefNode refNode : entity.getChildren(RefNode.class)) {
            final Object entityKey = checkForRemovedReference(refNode);
            if (entityKey != null) {
                Entity removedEntity = getOrLoadForAnalysis(entity.getEntityContext(), refNode.getEntityType(), entityKey);
                analyseDelete(removedEntity);
            }
        }
        /*
         * Look at the to many relations, some items may have been added, updated or deleted
         */
        for (ToManyNode toManyNode : entity.getChildren(ToManyNode.class)) {
            if (!toManyNode.isFetched()) {
                /*
                 * if the many relation was never fetched, then nothing was changed, skip.
                 */
                continue;
            }
            for (Entity refEntity : toManyNode.getList()) {
                if (refEntity.isNew()) {
                    analyseCreate(refEntity);
                }
                else if (toManyNode.getNodeType().isOwns()) {
                    analyseUpdate(refEntity);
                }
                else if (toManyNode.getNodeType().isDependsOn()) {
                	analyseDependsOn(refEntity);
                }
            }
            for (Entity refEntity : toManyNode.getRemovedEntities()) {
                if (refEntity.isNew()) {
                	LOG.error("Found remove entity in ToManyNode which is new, this makes no sense, and indicates a bug.");
                }
            	else if (toManyNode.getNodeType().isRefers()) {
            		LOG.error("TODO: an entity for update has a deleted tomany entity which it doesn't own");
            	}
                else if (toManyNode.getNodeType().isOwns()) {
                	analyseDelete(refEntity);
            	}
            	else if (toManyNode.getNodeType().isDependsOn()) {
            		LOG.error("TODO: an entity for update has a deleted tomany entity which it doesn't own");
            		analyseDependsOn(refEntity);
            	}
            }
        }
    }

    private void analyseDelete(Entity entity) throws EntityMissingException {
        if (!analysing.add(entity)) {
            return;
        }
        LOG.debug("analysing " + entity + " for delete");
        /*
         * Look at the to many relations, some items may have been added, updated or deleted
         */
        for (ToManyNode toManyNode : entity.getChildren(ToManyNode.class)) {
            /*
             * we only delete the many side if we own it.
             * todo: how should it work for a ref then? 
             * If a syntax only referred to a mapping and the syntax got deleted, then
             * the mapping's fk relation to syntax would have to be set to null.
             * this means that the entity would have to be updated so that it's FK gets set to null, 
             * unless we knew that cascade delete was used.
             */
            if (!toManyNode.getNodeType().isOwns()) {
                //TODO: we should also check if the entities in the many side
                //are part of the same delete request, of so we should analyze them too
                continue;
            }
            /*
             *  fetch the relation if required so that we can process the many nodes.
             */
            if (!toManyNode.isFetched()) {
                //load the relation if required, so we can delete the items
                LOG.debug("Fetching 1:N relation as part of delete analysis");
                entityContext.fetchSingle(toManyNode, true);
                for (Entity fetchedEntity: toManyNode.getList()) {
                	//we track the entites which were loaded during analysis, so we don't sync them back
                	//to the original context after persist.
                    loadedDuringAnalysis.add(fetchedEntity);
                }
            }
            /*
             *  schedule any many entities which exist in the database for deletion.
             */
            for (Entity refEntity : toManyNode.getList()) {
            	/*
            	 * the list really can contain new entities added by the user.
            	 */
                if (!refEntity.isNew()) {
                    analyseDelete(refEntity);
                }
            }
            for (Entity refEntity : toManyNode.getRemovedEntities()) {
            	if (!refEntity.isNew()) {
            		analyseDelete(refEntity);
            	}
            	else {
            		LOG.error("Found a new entity in the list of removed entities, this makes no sense!");
            	}
            }
        }
        /*
         * Schedule ourselves for deletion.
         */
        LOG.debug("adding " + entity + " for delete");
        deleteGroup.add(entity);

        /*
         * Then delete any references which we own
         */
        for (RefNode refNode : entity.getChildren(RefNode.class)) {
            final Entity refEntity = refNode.getReference();
            /*
             * An empty reference, nothing to analyze
             */
            if (refEntity == null) {
                continue;
            }
            if (refNode.getNodeType().isOwns()) {
                /*
                 * If the reference is used then schedule the ref'd entity for deletion
                 */
                if (refEntity.getKey().getValue() != null) {
                    if (refEntity.isFetchRequired()) {
                        entityContext.fetchSingle(refEntity, true);
                    }
                    analyseDelete(refEntity);
                }
                /*
                 * If the reference has a removed reference, then this needs to be deleted also.
                 */
                final Object entityKey = checkForRemovedReference(refNode);
                if (entityKey != null) {
                    Entity removedEntity = getOrLoadForAnalysis(entityContext, refNode.getEntityType(),  entityKey);
                    analyseDelete(removedEntity);
                }
            }
        }
    }

    private Entity getOrLoadForAnalysis(EntityContext entityContext, EntityType entityType, Object entityKey) throws EntityMissingException {
        Entity entity = entityContext.getEntity(entityType, entityKey, false);
        if (entity == null) {
            entity = entityContext.getOrLoad(entityType, entityKey);
            if (entity != null) {
                loadedDuringAnalysis.add(entity);
            }
            else {
                throw new EntityMissingException(entityType, entityKey);
            }
        }
        return entity;
    }

    private void analyseDependsOn(Entity entity) throws EntityMissingException {
        if (!analysing.add(entity)) {
            return;
        }

        LOG.debug("analysing " + entity + " for depends on");
        /*
         * I may myself depend on other entities which should also be up-to-date
         */
        analyseRefNodes(entity, false);

        dependsOnGroup.add(entity);

        /*
         * Look at the to many relations, we also need to make sure that they are fresh if we depend on them
         */
        for (ToManyNode toManyNode : entity.getChildren(ToManyNode.class)) {
            if (!toManyNode.isFetched()) {
                /*
                 * if the many relation was never fetched, it is not used and has no impact to freshness.
                 */
                continue;
            }
            if (!toManyNode.getNodeType().dependsOrOwns()) {
                /*
                 * We only need to look at ToMany references if we depend-on or own them
                 */
                continue;
            }
            for (Entity refEntity : toManyNode.getList()) {
                analyseDependsOn(refEntity);
            }
            for (Entity refEntity : toManyNode.getRemovedEntities()) {
                analyseDependsOn(refEntity);
            }
        }
    }

    private Object checkForRemovedReference(RefNode refNode) {
        return refNode.getNodeType().isOwns() ? refNode.getRemovedEntityKey() : null;
    }

    /**
     * 
     * @param entity
     * @param updateOwnedRefs if references that the entity own's should be updated.
     * @throws EntityMissingException
     */
    private void analyseRefNodes(Entity entity, boolean updateOwnedRefs) throws EntityMissingException {
        for (RefNode refNode : entity.getChildren(RefNode.class)) {
            final Entity refEntity = refNode.getReference();
            /*
             * An empty reference, nothing to analyze
             */
            if (refEntity == null) {
                continue;
            }
            if (refEntity.isNew()) {
                /*
                 * we are referring to an entity which is not yet created, process it first
                 */
                analyseCreate(refEntity);
            } 
            else if (updateOwnedRefs && refNode.getNodeType().isOwns() && !refEntity.isFetchRequired()) {
                /*
                 * the refEntity already exists in the database, but we own it so we are also going to perform an update.
                 * as long as it was loaded
                 */
                analyseUpdate(refEntity);
            }
            else if (refNode.getNodeType().dependsOrOwns() && !refEntity.isFetchRequired()) {
                /*
                 * We don't own, but logically depend on this reference to be considered valid
                 * since the entity was loaded, we need to analyze this dependency in-case the version
                 * is out of date.
                 * i.e. we don't want to allow saving of a syntax based on an out-of-date structure.
                 *
                 */
                analyseDependsOn(refEntity);
            }
        }
    }

    private void copyEntityValues(OperationGroup fromGroup, OperationGroup toGroup, EntityContext newContext) {
        for (Entity entity : fromGroup.getEntities()) {
            copyInto(entity, newContext, toGroup);
        }
    }

    private void copyInto(Entity entity, EntityContext newContext, OperationGroup toGroup) {
        Entity copy = new Entity(newContext, entity);
        newContext.add(copy);
        copy.setEntityState(entity.getEntityState());
        copy.copyValueNodesToMe(entity);
        toGroup.add(copy);
    }

    public void applyChanges(EntityContext otherContext) {
        LOG.debug("APPLYING CHANGES -------------------------");
        OperationGroup changed = createGroup.mergedCopy(updateGroup).mergedCopy(deleteGroup);

        EntityContextHelper.EntityFilter filter = new EntityContextHelper.EntityFilter() {
            @Override
            public boolean includesEntity(Entity entity) {
             // everything gets copied back apart from entities loaded during analysis.
                return !loadedDuringAnalysis.contains(entity);
            }
        };

        List<Entity> otherEntities = EntityContextHelper.applyChanges(changed.getEntities(), otherContext, filter);
        EntityContextHelper.copyRefStates(entityContext, otherContext, otherEntities, filter);
    }

}
