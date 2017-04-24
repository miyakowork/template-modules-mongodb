package org.templateproject.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.templateproject.mongodb.annotation.DynamicMongoSource;
import org.templateproject.mongodb.factory.MongoFactory;

/**
 * Created by wuwenbin on 2017/4/23.
 */
@Service
public class MongoService {

    @Autowired
    MongoFactory mongoFactory;

    @DynamicMongoSource(db = "admin")
    public void findAdmin() {
        System.err.println(mongoFactory.dynamicMongoDao.getMongoTemplate().findAll(Admin.class, "system.users"));
    }

    @DynamicMongoSource(key = "localMongo", db = "idea")
    public void findIdea() {
        System.err.println(mongoFactory.dynamicMongoDao.findListBean(Idea.class));
    }
}
