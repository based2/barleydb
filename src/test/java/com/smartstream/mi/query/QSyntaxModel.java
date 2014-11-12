package com.smartstream.mi.query;

import scott.sort.api.query.QProperty;
import scott.sort.api.query.QueryObject;
import com.smartstream.mi.model.SyntaxModel;
import com.smartstream.mac.query.QAccessArea;
import com.smartstream.mi.types.StructureType;
import com.smartstream.mi.types.SyntaxType;
import com.smartstream.mac.query.QUser;

/**
 * Generated from Entity Specification on Wed Nov 12 16:58:49 CET 2014
 *
 * @author scott
 */
public class QSyntaxModel extends QueryObject<SyntaxModel> {
  private static final long serialVersionUID = 1L;
  public QSyntaxModel() {
    super(SyntaxModel.class);
  }

  public QSyntaxModel(QueryObject<?> parent) {
    super(SyntaxModel.class, parent);
  }


  public QProperty<Long> id() {
    return new QProperty<Long>(this, "id");
  }

  public QAccessArea joinToAccessArea() {
    QAccessArea accessArea = new QAccessArea();
    addLeftOuterJoin(accessArea, "accessArea");
    return accessArea;
  }

  public QAccessArea existsAccessArea() {
    QAccessArea accessArea = new QAccessArea();
    addExists(accessArea, "accessArea");
    return accessArea;
  }

  public QProperty<String> uuid() {
    return new QProperty<String>(this, "uuid");
  }

  public QProperty<Long> modifiedAt() {
    return new QProperty<Long>(this, "modifiedAt");
  }

  public QProperty<String> name() {
    return new QProperty<String>(this, "name");
  }

  public QProperty<StructureType> structureType() {
    return new QProperty<StructureType>(this, "structureType");
  }

  public QProperty<SyntaxType> syntaxType() {
    return new QProperty<SyntaxType>(this, "syntaxType");
  }

  public QUser joinToUser() {
    QUser user = new QUser();
    addLeftOuterJoin(user, "user");
    return user;
  }

  public QUser existsUser() {
    QUser user = new QUser();
    addExists(user, "user");
    return user;
  }
}