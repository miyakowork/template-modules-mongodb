package org.templateproject.test;

import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Created by wuwenbin on 2017/4/22.
 */
public class Idea {
    private String _id;
    private String name;
    @Field("cn_name")
    private String cnName;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCnName() {
        return cnName;
    }

    public void setCnName(String cnName) {
        this.cnName = cnName;
    }

    @Override
    public String toString() {
        return "Idea{" +
                "_id='" + _id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
