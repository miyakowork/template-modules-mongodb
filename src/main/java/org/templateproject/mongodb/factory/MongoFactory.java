package org.templateproject.mongodb.factory;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.stereotype.Component;
import org.templateproject.mongodb.dao.MongoAccessor;
import org.templateproject.mongodb.dao.MongoDbTemplate;
import org.templateproject.mongodb.exception.DataSourceKeyNotExistException;
import org.templateproject.mongodb.support.core.MongoDataSource;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * 和spring一起初始化做一些操作,并实例化MongoClient集群
 * Created by wuwenbin on 2017/4/22.
 */
@Component
public class MongoFactory implements InitializingBean {


    /**
     * 多个mongo连接（一个集群是在一个mongo中）此处的多个仅仅是指配置上的多台mongo数据源，并不是集群
     */
    private Map<String, MongoDataSource> mongoDataSources;

    /**
     * 存放mongoDao的map类
     */
    private Map<String, MongoAccessor> mongoDaoMap = new Hashtable<>();

    private MongoDataSource defaultMongoDataSource;

    private MongoDataSource dynamicMongoDataSource;

    /**
     * 此Mongo连接的默认操作的数据库的dao
     */
    public MongoAccessor defaultMongoDao;

    /**
     * 此Mongo连接的动态操作数据库的dao，默认值和{@link #defaultMongoDao}一样
     */
    public MongoAccessor dynamicMongoDao;

    public void setMongoDataSources(Map<String, MongoDataSource> mongoDataSources) {
        this.mongoDataSources = mongoDataSources;
    }

    /**
     * 根据xml中配置设置默认的MongoDao
     *
     * @param dataSource
     */
    public void setDefaultMongoDao(MongoDataSource dataSource) {
        this.defaultMongoDataSource = dataSource;
        this.dynamicMongoDataSource = this.defaultMongoDataSource;
        List<ServerAddress> seeds = dataSource.getSeeds();
        MongoClientOptions options = dataSource.getMongoClientOptions();
        List<MongoCredential> credentials = dataSource.getMongoCredentials();
        MongoClient mongoClient = new MongoClient(seeds, credentials, options);
        MongoDbFactory dbFactory = new SimpleMongoDbFactory(mongoClient, dataSource.getDefaultDatabase());
        this.defaultMongoDao = new MongoDbTemplate(new MongoTemplate(dbFactory, dataSource.getMongoConverter()));
        this.dynamicMongoDao = this.defaultMongoDao;
    }


    /**
     * 动态数据源切换
     * <p>
     * 有3中情况切换.
     * 1、仅切换数据源，即根据xml中配置的key来切换不同数据源的mongo。变动数据源，默认数据库按照初始化的不变
     * 2、仅切换默认操作文档（数据库），即变更此同一数据源下的文档（数据库）。数据源不变，变动操作文档（数据库）
     * 3、以上两种情况同时发生
     *
     * @param key
     * @param database
     */
    public void setDynamicMongoDao(String key, String database) {
        if (!mongoDataSources.containsKey(key)) throw new DataSourceKeyNotExistException();
        this.dynamicMongoDataSource = mongoDataSources.get(key);
        this.dynamicMongoDao = getMongoDbDaoByKeyAndDataBase(key, database);
    }

    public void setDynamicMongoDaoByKey(String key) {
        if (!mongoDataSources.containsKey(key)) throw new DataSourceKeyNotExistException();
        this.dynamicMongoDataSource = mongoDataSources.get(key);
        this.dynamicMongoDao = getMongoDbDaoByKey(key);
    }

    public void setDynamicMongoDaoByDatabase(String database) {
        this.dynamicMongoDao = getMongoDbDaoByDataBase(database);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (mongoDataSources != null && mongoDataSources.size() > 0 && !mongoDataSources.isEmpty()) {
            for (String key : mongoDataSources.keySet()) {
                if (mongoDaoMap == null || !mongoDaoMap.containsKey(key)) {
                    mongoDaoMap.put(key, getMongoDbDaoByKey(key));
                }
            }
        }
    }


    /**
     * 根据key和默认数据库（文档）来获取MongoDbDao实例
     *
     * @param key
     * @param defaultDatabase
     * @return
     */
    private MongoAccessor getMongoDbDaoByKeyAndDataBase(String key, String defaultDatabase) {
        if (mongoDataSources.containsKey(key)) {
            MongoDataSource dataSource = mongoDataSources.get(key);
            List<ServerAddress> seeds = dataSource.getSeeds();
            MongoClientOptions options = dataSource.getMongoClientOptions();
            List<MongoCredential> credentials = dataSource.getMongoCredentials();
            MongoClient mongoClient = new MongoClient(seeds, credentials, options);
            MongoDbFactory dbFactory = new SimpleMongoDbFactory(mongoClient, defaultDatabase);
            return new MongoDbTemplate(new MongoTemplate(dbFactory, dataSource.getMongoConverter()));
        } else throw new DataSourceKeyNotExistException();

    }


    /**
     * 同{@link #getMongoDbDaoByKeyAndDataBase(String, String)}，默认数据库为初始化设置的
     *
     * @param key
     * @return
     */
    private MongoAccessor getMongoDbDaoByKey(String key) {
        if (mongoDataSources.containsKey(key)) {
            MongoDataSource dataSource = mongoDataSources.get(key);
            List<ServerAddress> seeds = dataSource.getSeeds();
            MongoClientOptions options = dataSource.getMongoClientOptions();
            List<MongoCredential> credentials = dataSource.getMongoCredentials();
            MongoClient mongoClient = new MongoClient(seeds, credentials, options);
            MongoDbFactory dbFactory = new SimpleMongoDbFactory(mongoClient, dataSource.getDefaultDatabase());
            return new MongoDbTemplate(new MongoTemplate(dbFactory, dataSource.getMongoConverter()));
        } else throw new DataSourceKeyNotExistException();

    }


    /**
     * 同{@link #getMongoDbDaoByKeyAndDataBase(String, String)}，默认数据源不变，切换数据库（文档）
     *
     * @param database
     * @return
     */
    private MongoAccessor getMongoDbDaoByDataBase(String database) {
        MongoDataSource dataSource = this.defaultMongoDataSource;
        List<ServerAddress> seeds = dataSource.getSeeds();
        MongoClientOptions options = dataSource.getMongoClientOptions();
        List<MongoCredential> credentials = dataSource.getMongoCredentials();
        MongoClient mongoClient = new MongoClient(seeds, credentials, options);
        MongoDbFactory factory = new SimpleMongoDbFactory(mongoClient, database);
        return new MongoDbTemplate(new MongoTemplate(factory, dataSource.getMongoConverter()));
    }

}
