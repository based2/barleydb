package scott.sort.api.specification;

/*
 * #%L
 * Simple Object Relational Framework
 * %%
 * Copyright (C) 2014 Scott Sinclair <scottysinclair@gmail.com>
 * %%
 * All rights reserved.
 * #L%
 */

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import scott.sort.api.specification.constraint.ForeignKeyConstraintSpec;


@XmlRootElement(name = "Definitions")
@XmlAccessorType(XmlAccessType.NONE)
public class DefinitionsSpec implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlAttribute(name="namespace")
    private String namespace;

    @XmlElement(name="import")
    private final Set<String> imports = new HashSet<>();

    @XmlJavaTypeAdapter(EntitySpecAdapter.class)
    @XmlElement(name="EntitySpecs")
    private final LinkedHashMap<String,EntitySpec> entitySpecs = new LinkedHashMap<>();

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Set<String> getImports() {
        return imports;
    }

    public void addImport(String namespace) {
        imports.add( namespace );
    }

    public void add(EntitySpec entitySpec) {
        entitySpecs.put(entitySpec.getClassName(), entitySpec);
    }

    public Collection<EntitySpec> getEntitySpecs() {
        return Collections.unmodifiableCollection( entitySpecs.values() );
    }

    public void verify() {
        for (EntitySpec entitySpec: entitySpecs.values()) {
            entitySpec.verify();
        }
        Set<String> constraintNames = new HashSet<String>();
        for (EntitySpec entitySpec: entitySpecs.values()) {
            if (entitySpec.getPrimaryKeyConstraint() != null) {
                if (!constraintNames.add(entitySpec.getPrimaryKeyConstraint().getName())) {
                    throw new IllegalStateException("Constraint already exists: " + entitySpec.getPrimaryKeyConstraint());
                }
            }
            for (ForeignKeyConstraintSpec spec: entitySpec.getForeignKeyConstraints()) {
                if (!constraintNames.add(spec.getName())) {
                    throw new IllegalStateException("Constraint already exists: " + spec);
                }
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("namespace: ");
        sb.append(namespace);
        sb.append('\n');
        for (EntitySpec spec: entitySpecs.values()) {
            sb.append(spec.toString());
            sb.append('\n');
        }
        return sb.toString();
    }

    public static class EntitiesList {
        @XmlElement(name="EntitySpec")
        private final List<EntitySpec> data = new LinkedList<>();
    }

    public static class EntitySpecAdapter extends XmlAdapter<EntitiesList, LinkedHashMap<String,EntitySpec>> {
        @Override
        public LinkedHashMap<String, EntitySpec> unmarshal(EntitiesList nodeSpecs) throws Exception {
            LinkedHashMap<String, EntitySpec> map = new LinkedHashMap<String, EntitySpec>();
            for (EntitySpec spec: nodeSpecs.data) {
                map.put(spec.getClassName(), spec);
            }
            return map;
        }

        @Override
        public EntitiesList marshal(LinkedHashMap<String, EntitySpec> entitySpecs) throws Exception {
            EntitiesList list = new EntitiesList();
            list.data.addAll( entitySpecs.values() );
            return list;
        }
    }


}
