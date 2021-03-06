package scott.barleydb.api.core.entity;

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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import scott.barleydb.api.config.EntityType;
import scott.barleydb.api.config.NodeType;
import scott.barleydb.api.core.entity.Entity;
import scott.barleydb.api.core.entity.Node;
import scott.barleydb.api.core.entity.RefNode;
import scott.barleydb.api.core.entity.ToManyNode;
import scott.barleydb.api.core.entity.ValueNode;

/**
 * Contains information on how this node refers to many entities.
 * To look up matching entities in the node context we need
 * the entity type which we are referring to and the primary key of the entity which we belong to.
 * For example for the syntax.mappings ToManyNode this translates to the Mappings entityType and the syntax id.
 * 
 * The ToMany node also tracks which entities have been deleted from the list and which new entities have been added to the list.
 * 
 * 
 */
public class ToManyNode extends Node {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(ToManyNode.class);

    /**
     * The entity type that we refer to
     */
    private EntityType entityType;

    /*
     * tracks all entities that we reference currently
     */
    private List<Entity> entities;
    /*
     * only refers to new entities
     */
    private List<Entity> newEntities;
    /*
     * tracks entities which have been removed
     */
    private List<Entity> removedEntities;
    private boolean fetched;

    public ToManyNode(Entity parent, String name, EntityType entityType) {
        super(parent, name);
        this.entityType = entityType;
        this.entities = new LinkedList<Entity>();
        this.newEntities = new LinkedList<Entity>();
        this.removedEntities = new LinkedList<Entity>();
    }

    @Override
    public Element toXml(Document doc) {
        final Element element = doc.createElement(getName());
        element.setAttribute("fetched", String.valueOf(fetched));
        for (final Entity en : entities) {
            Element el = doc.createElement("ref");
            if (en.getKey().getValue() != null) {
                el.setAttribute("key", en.getKey().getValue().toString());
            }
            else {
                el.setAttribute("uuid", en.getUuid().toString());
            }
            element.appendChild(el);
        }
        return element;
    }

    public boolean isFetched() {
        return fetched;
    }
    
    public void unloadAndClear() {
    	entities.clear();
    	newEntities.clear();
    	removedEntities.clear();
    	fetched = false;
    }

    public void setFetched(boolean fetched) {
        LOG.debug(getParent() + "." + getName() + "=" + this + " fetched == " + fetched);
        this.fetched = fetched;
    }

    public void refresh() {
        if (!isFetched()) {
            return;
        }

    	List<Entity> preventGc = new LinkedList<Entity>(entities);
    	preventGc.addAll(removedEntities);

        /*
         * remove entities from the newEntities list which are no longer new.
         */
        for (Iterator<Entity> i = newEntities.iterator(); i.hasNext();) {
        	Entity e = i.next();
            if (!e.isNew()) {
            	LOG.trace("ToManyNode {} has new entity {} which is now saved, removing from newEntities list", this, e);
                i.remove();
            }
        }
        /*
         * if the removed entities have state "new" then remove them from our removedEntities list
         * as this means that they have been deleted.
         */
        for (Iterator<Entity> i = removedEntities.iterator(); i.hasNext();) {
            Entity e = i.next();
            if (e.isNew()) {
            	LOG.trace("ToManyNode {} has deleted entity {} which is now new, removing from removedEntities list", this, e);                
            	i.remove();
            }
        }
        /*
         * do the refresh
         */
        if (getParent().getKey().getValue() != null) {
            List<Entity> result = Collections.emptyList();
            if (getNodeType().getForeignNodeName() != null) {
                result = getEntityContext().getEntitiesWithReferenceKey(
                        entityType,
                        getNodeType().getForeignNodeName(),
                        getParent().getEntityType(),
                        getParent().getKey().getValue());

                result.removeAll(removedEntities);
            }

            /*
             * We only touch the entities list if something has changes, this prevents
             * needless concurrent modification exceptions.
             */
            List<Entity> refreshedEntities = new LinkedList<>(result);
            refreshedEntities.addAll(newEntities);
            if (entities.size() != refreshedEntities.size() || !entities.containsAll(refreshedEntities)) {
                entities.clear();
                entities.addAll(refreshedEntities);
                if (entities.size() > 0) {
                    //the list of entities must have a consistent order
                    //we sort on the sort column or the PL if not specified.
                    String sortNodeName = getNodeType().getSortNode();
                    if (sortNodeName == null) {
                        sortNodeName = entityType.getKeyNodeName();
                    }
                    Collections.sort(entities, new MyComparator(sortNodeName));
                }
                if (result.isEmpty()) {
                    LOG.debug("no entities for " + getParent() + "." + getName() + "=" + this);
                }
                else {
                    LOG.debug("resolved " + result.size() + " entities for " + getParent() + "." + getName() + "=" + this);
                }
            }
        }
    }
   
    /**
     * We are adding an entity to this list.
     * 
     * @param index
     * @param entity
     */
    public void add(int index, Entity entity) {
        if (entities.contains(entity)) {
            throw new IllegalStateException("ToMany relation already contains '" + entity + "'");
        }
        if (entity.getEntityType() != entityType) {
            throw new IllegalStateException("Cannot add " + entity.getEntityType() + " to " + getParent() + "." + getName());
        }
        if (entity.isNew()) {
            newEntities.add(entity);
        }
        entities.add(index, entity);
    }

    public void add(Entity entity) {
        if (entities.contains(entity)) {
            throw new IllegalStateException("ToMany relation already contains '" + entity + "'");
        }
        if (entity.getEntityType() != entityType) {
            throw new IllegalStateException("Cannot add " + entity.getEntityType() + " to " + getParent() + "." + getName());
        }
        if (entity.isNew()) {
            newEntities.add(entity);
        }
        entities.add(entity);
    }

    public List<Entity> getList() {
        return entities;
    }

    public Entity remove(int index) {
        Entity entity = entities.remove(index);
        if (entity != null) {
            newEntities.remove(entity);
            if (!entity.isNew()) {
                removedEntities.add(entity);
            }
        }
        return entity;
    }

    public List<Entity> getNewEntities() {
        return newEntities;
    }

    public List<Entity> getRemovedEntities() {
        return removedEntities;
    }

    @Override
    public String toString() {
        return getList().toString();
    }

    @Override
    public Entity getParent() {
        return (Entity) super.getParent();
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public NodeType getNodeType() {
        return getParent().getEntityType().getNodeType(getName(), true);
    }

    public void copyFrom(ToManyNode other) {
        this.fetched = other.fetched;
        this.entities = new LinkedList<Entity>(other.entities);
        this.newEntities = new LinkedList<Entity>(other.newEntities);
        this.removedEntities = new LinkedList<Entity>(other.removedEntities);
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        LOG.trace("Serializing many references {}", this);
        oos.writeUTF(entityType.getInterfaceName());
        oos.writeBoolean(fetched);
        oos.writeObject(entities);
        oos.writeObject(newEntities);
        oos.writeObject(removedEntities);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        String interfaceName = ois.readUTF();
        entityType = getEntityContext().getDefinitions().getEntityTypeMatchingInterface(interfaceName, true);
        fetched = ois.readBoolean();
        entities = (List<Entity>)ois.readObject();
        newEntities = (List<Entity>)ois.readObject();
        removedEntities = (List<Entity>)ois.readObject();
        //trace at end once object is constructed
        LOG.trace("Deserialized many references {}", this);
   }

    /**
     * Provides standard sorting for the list in the to many relation.
     * @author scott
     *
     */
    private final class MyComparator implements Comparator<Entity> {
        private final String sortNodeName;
        
        public MyComparator(String sortNodeName) {
        	if (LOG.isTraceEnabled()) {
        		LOG.trace("Comparing entities for {} according to {}", getNodeType().getShortId(), sortNodeName);
        	}
            this.sortNodeName = sortNodeName;
        }

        @SuppressWarnings("unchecked")
        @Override
        public int compare(Entity o1, Entity o2) {
            Object value1 = getValue(o1, sortNodeName);
            Object value2 = getValue(o2, sortNodeName);
            if (value1 != null) {
                if (value2 == null) return 1;
                else return ((Comparable<Object>) value1).compareTo(value2);
            }
            else if (value2 == null) return 0;
            else return -1;
        }
        
        private Object getValue(Entity entity, String sortNodeName) {
            Node node = entity.getChild(sortNodeName, Node.class);
            if (node instanceof ValueNode) {
                return ((ValueNode) node).getValue();
            }
            else if (node instanceof RefNode) {
                return ((RefNode) node).getEntityKey();
            }
            else {
                return null;
            }
        }
    }
    
}

