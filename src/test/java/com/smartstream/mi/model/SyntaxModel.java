package com.smartstream.mi.model;

import java.util.List;

import scott.sort.api.core.entity.Entity;
import scott.sort.api.core.entity.ValueNode;
import scott.sort.api.core.entity.RefNode;
import scott.sort.api.core.entity.ToManyNode;
import scott.sort.api.core.proxy.AbstractCustomEntityProxy;
import scott.sort.api.core.proxy.RefNodeProxyHelper;
import scott.sort.api.core.proxy.ToManyNodeProxyHelper;

import com.smartstream.mac.model.AccessArea;
import com.smartstream.mi.types.StructureType;
import com.smartstream.mi.types.SyntaxType;
import com.smartstream.mac.model.User;



public class SyntaxModel extends AbstractCustomEntityProxy {

  private final ValueNode id;
  private final RefNodeProxyHelper accessArea;
  private final ValueNode uuid;
  private final ValueNode modifiedAt;
  private final ValueNode name;
  private final ValueNode structureType;
  private final ValueNode syntaxType;
  private final RefNodeProxyHelper user;
  private final ValueNode structure;


  public SyntaxModel(Entity entity) {
    super(entity);
    id = entity.getChild("id", ValueNode.class, true);
    accessArea = new RefNodeProxyHelper(entity.getChild("accessArea", RefNode.class, true));
    uuid = entity.getChild("uuid", ValueNode.class, true);
    modifiedAt = entity.getChild("modifiedAt", ValueNode.class, true);
    name = entity.getChild("name", ValueNode.class, true);
    structureType = entity.getChild("structureType", ValueNode.class, true);
    syntaxType = entity.getChild("syntaxType", ValueNode.class, true);
    user = new RefNodeProxyHelper(entity.getChild("user", RefNode.class, true));
    structure = entity.getChild("structure", ValueNode.class, true);
  }

  public Long getId() {
    return id.getValue();
  }

  public void setId(Long id) {
    this.id.setValue(id);
  }

  public AccessArea getAccessArea() {
    return super.getFromRefNode(accessArea.refNode);
  }

  public void setAccessArea(AccessArea accessArea) {
    setToRefNode(this.accessArea.refNode, accessArea);
  }

  public String getUuid() {
    return uuid.getValue();
  }

  public void setUuid(String uuid) {
    this.uuid.setValue(uuid);
  }

  public Long getModifiedAt() {
    return modifiedAt.getValue();
  }

  public void setModifiedAt(Long modifiedAt) {
    this.modifiedAt.setValue(modifiedAt);
  }

  public String getName() {
    return name.getValue();
  }

  public void setName(String name) {
    this.name.setValue(name);
  }

  public StructureType getStructureType() {
    return structureType.getValue();
  }

  public void setStructureType(StructureType structureType) {
    this.structureType.setValue(structureType);
  }

  public SyntaxType getSyntaxType() {
    return syntaxType.getValue();
  }

  public void setSyntaxType(SyntaxType syntaxType) {
    this.syntaxType.setValue(syntaxType);
  }

  public User getUser() {
    return super.getFromRefNode(user.refNode);
  }

  public void setUser(User user) {
    setToRefNode(this.user.refNode, user);
  }

  public Long getStructure() {
    return structure.getValue();
  }

  public void setStructure(Long structure) {
    this.structure.setValue(structure);
  }
}
