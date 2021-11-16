package com.starsray.dynamic.ds.core;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DruidDataSourceCreator;
import com.baomidou.dynamic.datasource.ds.ItemDataSource;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.starsray.dynamic.ds.config.DataBaseConfig;
import com.starsray.dynamic.ds.config.DsSqlFileConfig;
import com.starsray.dynamic.ds.constant.DsDriverEnum;
import com.starsray.dynamic.ds.util.DatabaseUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.Serializable;
import java.util.*;

/**
 * Ds Operator
 *
 * @author starsray
 * @since 2021-11-13
 */
public interface Ds {

    /**
     * 列表数据源
     *
     * @return {@link Set<String> }
     */
    Set<String> listDatasource();

    /**
     * 添加数据源
     *
     * @param name     名称
     * @param database 数据库
     * @return {@link Set<String> }
     */
    Set<String> addDatasourceWithCurrent(String name, String database);


    /**
     * 添加数据源
     *
     * @param name     数据源名称
     * @param type     mysql类型 mysql5、mysql8
     * @param url      jdbcUrl
     * @param username 用户名
     * @param password 密码
     * @return {@link Set<String> }
     */
    Set<String> addDatasourceWithOnly(String name, String type, String url, String username, String password);

    /**
     * 额外添加数据源
     *
     * @param params params
     * @return {@link Set<String> }
     */
    Set<String> addDatasourceWithParams(Params params);

    /**
     * 删除数据源
     *
     * @param name 名称
     * @return boolean
     */
    boolean removeDatasource(String name);

    /**
     * 执行sql文本 执行指定数据源、指定位置处sql
     *
     * @param name       名称
     * @param sqlFileUrl sql文件url
     * @return boolean
     */
    boolean executeSql(String name, String sqlFileUrl);

    /**
     * 通过sql文件url执行sql 对所有数据源执行指定位置sql文件
     *
     * @param sqlFileUrl sql文件url
     * @return boolean
     */
    boolean executeSqlBySqlFileUrl(String sqlFileUrl);

    /**
     * 按名称执行sql文本 根据数据源名称执行默认配置文件路径
     *
     * @param name 名称
     * @return boolean
     */
    boolean executeSqlByName(String name);

    /**
     * 执行sql文本 对所有数据源执行默认配置文件路径
     *
     * @return boolean
     */
    boolean executeSql();

    /**
     * ds服务
     *
     * @author starsray
     * @since 2021-11-13
     */
    class DsService implements Ds {

        /**
         * 列表数据源
         *
         * @return {@link Set<String> }
         */
        @Override
        public Set<String> listDatasource() {
            DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
            return ds.getDataSources().keySet();
        }

        /**
         * 添加数据源
         *
         * @param params params
         * @return {@link Set<String> }
         */
        @Override
        public Set<String> addDatasourceWithParams(Params params) {
            validateParams(params);
            DsProperty property = new DsProperty();
            BeanUtils.copyProperties(params, property);

            DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
            DatasourceInstance db = DatasourceInstance.builder().jdbcTemplate(jdbcTemplate).build().of(property);
            Map<String, DataSource> dataSources = ds.getDataSources();
            if (dataSources.containsKey(db.getName())) {
                return dataSources.keySet();
            }

            DataSourceProperty dataSourceProperty = new DataSourceProperty();
            dataSourceProperty.setPoolName(db.getName());
            dataSourceProperty.setUrl(db.getUrl());
            dataSourceProperty.setUsername(db.getUsername());
            dataSourceProperty.setPassword(db.getPassword());
            dataSourceProperty.setDriverClassName(db.getDriver());

            List<String> opList = Arrays.asList("create", "alter");
            if (DatabaseUtils.validateConn(property)) {
                DatabaseUtils.createDatabase(property);
                List<String> sqlFileUrlList = dsSqlFileConfig.getSqlFileList();
                property.setOpList(opList);
                transactionTemplate.execute(status -> {
                    try {
                        for (String sqlFileUrl : sqlFileUrlList) {
                            property.setSqlFileUrl(sqlFileUrl);
                            DatabaseUtils.executeSql(property);
                        }
                    } catch (Exception e) {
                        status.isRollbackOnly();
                        e.printStackTrace();
                    }
                    return dataSources.keySet();
                });
            }
            db.save();
            DataSource dataSource = druidDataSourceCreator.createDataSource(dataSourceProperty);
            ds.addDataSource(db.getName(), dataSource);
            return dataSources.keySet();
        }

        @Override
        public Set<String> addDatasourceWithOnly(String name, String type, String url, String username, String password) {
            if (StringUtils.isBlank(name)) {
                throw new RuntimeException("*** dynamic ds *** method add datasource with only params name can't empty");
            }
            if (StringUtils.isBlank(password)) {
                throw new RuntimeException("*** dynamic ds *** method add datasource with only params password can't empty");
            }
            if (StringUtils.isBlank(type)) {
                throw new RuntimeException("*** dynamic ds *** method add datasource with only params type can't empty");
            }
            if (StringUtils.isBlank(url)) {
                throw new RuntimeException("*** dynamic ds *** method add datasource with only params url can't empty");
            }
            if (StringUtils.isBlank(username)) {
                throw new RuntimeException("*** dynamic ds *** method add datasource with only params username can't empty");
            }
            String driverClassName = DsDriverEnum.getDriverClassName(type);

            DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
            Map<String, DataSource> dataSources = ds.getDataSources();
            if (dataSources.containsKey(name)) {
                return dataSources.keySet();
            }
            DataSourceProperty dataSourceProperty = new DataSourceProperty();
            dataSourceProperty.setPoolName(name);
            dataSourceProperty.setUrl(url);
            dataSourceProperty.setUsername(username);
            dataSourceProperty.setPassword(password);
            dataSourceProperty.setDriverClassName(driverClassName);

            DataSource dataSource = druidDataSourceCreator.createDataSource(dataSourceProperty);
            ds.addDataSource(name, dataSource);

            DsProperty property = new DsProperty();
            DatasourceInstance db = DatasourceInstance.builder().jdbcTemplate(jdbcTemplate).build().of(property);
            property.setType(type);
            property.setUrl(url);
            property.setUsername(username);
            property.setPassword(password);
            property.setName(name);
            db.save();
            return dataSources.keySet();
        }


        @Override
        public Set<String> addDatasourceWithCurrent(String name, String database) {

            if (StringUtils.isBlank(name)) {
                throw new RuntimeException("*** dynamic ds *** method add datasource with current params name can't empty");
            }
            if (StringUtils.isBlank(database)) {
                throw new RuntimeException("*** dynamic ds *** method add datasource with current params database can't empty");
            }
            DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
            Map<String, DataSource> dataSources = ds.getDataSources();
            if (dataSources.containsKey(name)) {
                return dataSources.keySet();
            }
            DsProperty dsProperty = getDsProperty(dataBaseConfig.getPrimary(), dataSources);
            dsProperty.setName(name);

            DataSourceProperty dataSourceProperty = new DataSourceProperty();
            dataSourceProperty.setPoolName(name);
            String curUrl = DatabaseUtils.replaceDatabase(dsProperty.getUrl(), database);
            dsProperty.setUrl(curUrl);
            dataSourceProperty.setUrl(curUrl);
            dataSourceProperty.setUsername(dsProperty.getUsername());
            dataSourceProperty.setPassword(dsProperty.getPassword());
            dataSourceProperty.setDriverClassName(DsDriverEnum.getDriverClassName(dsProperty.getType()));

            DatasourceInstance db = DatasourceInstance.builder().jdbcTemplate(jdbcTemplate).build().of(dsProperty);
            DataSource dataSource = druidDataSourceCreator.createDataSource(dataSourceProperty);
            ds.addDataSource(name, dataSource);
            DatabaseUtils.createDatabase(jdbcTemplate, database);

            transactionTemplate.execute(status -> {
                try {
                    executeSqlByName(name);
                } catch (Exception e) {
                    status.isRollbackOnly();
                    e.printStackTrace();
                }
                return dataSources.keySet();
            });
            db.save();
            return dataSources.keySet();
        }

        /**
         * 删除数据源
         *
         * @param name 名称
         * @return boolean
         */
        @Override
        public boolean removeDatasource(String name) {
            if ("master".equals(name)) {
                throw new RuntimeException("*** dynamic ds *** datasource master can't remove!");
            }
            DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
            if (!ds.getDataSources().containsKey(name)) {
                throw new RuntimeException("*** dynamic ds *** datasource " + name + " not exist!");
            }
            DatasourceInstance db = DatasourceInstance.builder().jdbcTemplate(jdbcTemplate).name(name).build();
            db.remove();
            ds.removeDataSource(name);
            return true;
        }

        /**
         * 执行sql
         *
         * @param name       名称
         * @param sqlFileUrl sql文件url
         * @return boolean
         */
        @Override
        public boolean executeSql(String name, String sqlFileUrl) {
            if (StringUtils.isBlank(name) || StringUtils.isBlank(sqlFileUrl)) {
                throw new RuntimeException("*** dynamic ds *** datasource name or sqlFileUrl can't empty");
            }
            transactionTemplate.execute(status -> {
                try {
                    DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
                    Map<String, DataSource> currentDataSources = ds.getDataSources();
                    List<String> opList = Arrays.asList("create", "update", "alter", "delete");
                    execute(sqlFileUrl, currentDataSources, name, opList);
                } catch (Exception e) {
                    status.isRollbackOnly();
                    e.printStackTrace();
                    return false;
                }
                return true;
            });
            return true;
        }

        /**
         * 通过sql文件url执行sql
         *
         * @param sqlFileUrl sql文件url
         * @return boolean
         */
        @Override
        public boolean executeSqlBySqlFileUrl(String sqlFileUrl) {
            if (StringUtils.isBlank(sqlFileUrl)) {
                throw new RuntimeException("*** dynamic ds *** sqlFileUrl can't empty");
            }
            transactionTemplate.execute(status -> {
                try {
                    DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
                    Map<String, DataSource> currentDataSources = ds.getDataSources();
                    List<String> opList = Arrays.asList("create", "alter", "update");
                    for (String name : currentDataSources.keySet()) {
                        execute(sqlFileUrl, currentDataSources, name, opList);
                    }
                } catch (Exception e) {
                    status.isRollbackOnly();
                    e.printStackTrace();
                    return false;
                }
                return true;
            });
            return true;
        }

        /**
         * 按名称执行sql
         *
         * @param name 名称
         * @return boolean
         */
        @Override
        public boolean executeSqlByName(String name) {
            if (StringUtils.isBlank(name)) {
                throw new RuntimeException("*** dynamic ds *** datasource name can't empty");
            }
            List<String> sqlFileList = dsSqlFileConfig.getSqlFileList();
            if (CollectionUtils.isEmpty(sqlFileList)) {
                throw new RuntimeException("*** dynamic ds *** can't find any sql files");
            }
            transactionTemplate.execute(status -> {
                try {
                    List<String> opList = Arrays.asList("create", "alter", "update");
                    DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
                    Map<String, DataSource> currentDataSources = ds.getDataSources();
                    DsProperty dsProperty = getDsProperty(name, currentDataSources);
                    dsProperty.setOpList(opList);
                    for (String sqlFileUrl : sqlFileList) {
                        dsProperty.setSqlFileUrl(sqlFileUrl);
                        DatabaseUtils.executeSql(dsProperty);
                    }
                } catch (Exception e) {
                    status.isRollbackOnly();
                    e.printStackTrace();
                    return false;
                }
                return true;
            });
            return true;
        }

        /**
         * 执行sql
         *
         * @return boolean
         */
        @Override
        public boolean executeSql() {
            List<String> opList = Collections.singletonList("create");
            List<String> sqlFileList = dsSqlFileConfig.getSqlFileList();
            if (CollectionUtils.isEmpty(sqlFileList)) {
                throw new RuntimeException("*** dynamic ds *** can't find any sql files");
            }
            transactionTemplate.execute(status -> {
                try {
                    DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
                    Map<String, DataSource> currentDataSources = ds.getDataSources();
                    for (String name : currentDataSources.keySet()) {
                        DsProperty dsProperty = getDsProperty(name, currentDataSources);
                        dsProperty.setOpList(opList);
                        for (String sqlFileUrl : sqlFileList) {
                            dsProperty.setSqlFileUrl(sqlFileUrl);
                            DatabaseUtils.executeSql(dsProperty);
                        }
                    }
                } catch (Exception e) {
                    status.isRollbackOnly();
                    e.printStackTrace();
                }
                return true;
            });
            return true;
        }

        /**
         * 获取ds属性
         *
         * @param name               名称
         * @param currentDataSources 当前数据源
         * @return {@link DsProperty }
         */
        private DsProperty getDsProperty(String name, Map<String, DataSource> currentDataSources) {
            DruidDataSource druidDataSource = null;
            DataSource source = currentDataSources.get(name);
            if (source == null) {
                throw new RuntimeException("*** dynamic ds *** can't find datasource name " + name);
            }
            if (source instanceof ItemDataSource) {
                druidDataSource = (DruidDataSource) ((ItemDataSource) source).getRealDataSource();
            }
            if (source instanceof DruidDataSource) {
                druidDataSource = (DruidDataSource) source;
            }
            assert druidDataSource != null;
            String url = druidDataSource.getUrl();
            String username = druidDataSource.getUsername();
            String password = druidDataSource.getPassword();
            String driverClassName = druidDataSource.getDriverClassName();
            DsProperty dsProperty = new DsProperty();
            dsProperty.setUrl(url);
            dsProperty.setUsername(username);
            dsProperty.setPassword(password);
            dsProperty.setType(DsDriverEnum.getType(driverClassName));
            return dsProperty;
        }

        /**
         * 处决
         *
         * @param sqlFilePath        sql文件路径
         * @param currentDataSources 当前数据源
         * @param name               名称
         */
        private void execute(String sqlFilePath, Map<String, DataSource> currentDataSources, String name, List<String> opList) {
            DsProperty dsProperty = getDsProperty(name, currentDataSources);
            dsProperty.setSqlFileUrl(sqlFilePath);
            dsProperty.setOpList(opList);
            DatabaseUtils.executeSql(dsProperty);
        }

        private void validateParams(Params params) {
            if (StringUtils.isBlank(params.getName())) {
                throw new RuntimeException("*** dynamic ds *** method add datasource with params params name can't empty");
            }
            if (StringUtils.isBlank(params.getPassword())) {
                throw new RuntimeException("*** dynamic ds *** method add datasource with params params password can't empty");
            }
            if (StringUtils.isBlank(params.getType())) {
                throw new RuntimeException("*** dynamic ds *** method add datasource with params params type can't empty");
            }
            if (StringUtils.isBlank(params.getUrl())) {
                throw new RuntimeException("*** dynamic ds *** method add datasource with params params url can't empty");
            }
            if (StringUtils.isBlank(params.getUsername())) {
                throw new RuntimeException("*** dynamic ds *** method add datasource with params params username can't empty");
            }
            if (StringUtils.isBlank(params.getSqlFileUrl())) {
                throw new RuntimeException("*** dynamic ds *** method add datasource with params params sql file url can't empty");
            }
        }

        @Resource
        private DataSource dataSource;
        @Resource
        private DruidDataSourceCreator druidDataSourceCreator;
        @Resource
        private JdbcTemplate jdbcTemplate;
        @Resource
        private DsSqlFileConfig dsSqlFileConfig;
        @Resource
        private DataBaseConfig dataBaseConfig;
        @Resource
        private TransactionTemplate transactionTemplate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    class DatasourceInstance implements Serializable {

        private JdbcTemplate jdbcTemplate;
        private String type;
        private String name;
        private String username;
        private String password;
        private String url;
        private String driver;

        public DatasourceInstance of(DsProperty property) {
            this.type = property.getType();
            this.name = property.getName();
            this.username = property.getUsername();
            this.password = property.getPassword();
            this.url = property.getUrl();
            this.driver = DsDriverEnum.getDriverClassName(type);
            return this;
        }

        public void save() {
            String sql = "INSERT INTO DYNAMIC_DATASOURCE_INSTANCE (type, name, username, password, url, driver) VALUES (?,?,?,?,?,?)";
            this.jdbcTemplate.update(sql, this.type, this.name, this.username, this.password, this.url, this.driver);
        }

        public void remove() {
            String sql = "delete from DYNAMIC_DATASOURCE_INSTANCE where name = ?";
            this.jdbcTemplate.update(sql, this.name);
        }
    }
}
