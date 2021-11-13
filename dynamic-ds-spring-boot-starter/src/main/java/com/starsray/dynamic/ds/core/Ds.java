package com.starsray.dynamic.ds.core;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DruidDataSourceCreator;
import com.baomidou.dynamic.datasource.ds.ItemDataSource;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.starsray.dynamic.ds.config.DefaultDsSqlFileConfig;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Ds Operator
 *
 * @author starsray
 * @since 2021-11-13
 */
@Service
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
     * @param param 停止
     * @return {@link Set<String> }
     */
    Set<String> addDatasource(DsParam param) throws IOException;

    /**
     * 删除数据源
     *
     * @param name 名称
     * @param drop 滴
     * @return boolean
     */
    boolean removeDatasource(String name, boolean drop);

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
    @Service
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
         * @param param param
         * @return {@link Set<String> }
         */
        @Override
        @Transactional(rollbackFor = Exception.class)
        public Set<String> addDatasource(DsParam param) {
            DsProperty property = new DsProperty();
            BeanUtils.copyProperties(param,property);
            DatabaseUtils.validateDsProperty(property);
            DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
            DatasourceInstance db = DatasourceInstance.builder().jdbcTemplate(jdbcTemplate).build().of(property);
            if (ds.getDataSources().containsKey(db.getName())) {
                return ds.getDataSources().keySet();
            }
            // 如果正常连接在新数据源执行，如果连接不通畅 在当前数据源执行SQL
            List<String> opList = Arrays.asList("create", "alter");
            if (DatabaseUtils.validateConn(property)) {
                DatabaseUtils.createDatabase(property);
                List<String> sqlFileUrlList = defaultDsSqlFileConfig.getSqlFileList();
                property.setOpList(opList);
                sqlFileUrlList.forEach(sqlFileUrl -> {
                    property.setSqlFileUrl(sqlFileUrl);
                    DatabaseUtils.executeSql(property);
                });
            } else {
                DatabaseUtils.createDatabse(jdbcTemplate, property.getDatabase());
                List<String> sqlFileUrlList = defaultDsSqlFileConfig.getSqlFileList();
                sqlFileUrlList.forEach(sqlFileUrl -> DatabaseUtils.executeSql(jdbcTemplate, sqlFileUrl, opList));
            }
            db.save();
            DataSourceProperty dataSourceProperty = new DataSourceProperty();
            dataSourceProperty.setPoolName(db.getName());
            dataSourceProperty.setUrl(db.getUrl());
            dataSourceProperty.setUsername(db.getUsername());
            dataSourceProperty.setPassword(db.getPassword());
            dataSourceProperty.setDriverClassName(db.getDriver());
            DataSource dataSource = druidDataSourceCreator.createDataSource(dataSourceProperty);
            ds.addDataSource(db.getName(), dataSource);
            return ds.getDataSources().keySet();
        }


        /**
         * 删除数据源
         *
         * @param name 名称
         * @param drop 滴
         * @return boolean
         */
        @Override
        @Transactional(rollbackFor = Exception.class)
        public boolean removeDatasource(String name, boolean drop) {
            if (name.equals("master")) {
                throw new RuntimeException("datasource master can't remove!");
            }
            DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
            if (!ds.getDataSources().containsKey(name)) {
                throw new RuntimeException("datasource " + name + " not exist!");
            }
            DatasourceInstance datasourceInstance = DatasourceInstance.builder().jdbcTemplate(jdbcTemplate).name(name).build();
            datasourceInstance.remove();
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
        @Transactional(rollbackFor = Exception.class)
        public boolean executeSql(String name, String sqlFileUrl) {
            if (StringUtils.isBlank(name) || StringUtils.isBlank(sqlFileUrl)) {
                throw new RuntimeException("*** dynamic ds *** datasource name or sqlFileUrl can't empty");
            }
            DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
            Map<String, DataSource> currentDataSources = ds.getDataSources();
            List<String> opList = Arrays.asList("create", "update", "alter", "delete");
            execute(sqlFileUrl, currentDataSources, name, opList);
            return true;
        }

        /**
         * 通过sql文件url执行sql
         *
         * @param sqlFileUrl sql文件url
         * @return boolean
         */
        @Override
        @Transactional(rollbackFor = Exception.class)
        public boolean executeSqlBySqlFileUrl(String sqlFileUrl) {
            if (StringUtils.isBlank(sqlFileUrl)) {
                throw new RuntimeException("*** dynamic ds *** sqlFileUrl can't empty");
            }
            DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
            Map<String, DataSource> currentDataSources = ds.getDataSources();
            List<String> opList = Arrays.asList("create", "alter", "update");
            for (String name : currentDataSources.keySet()) {
                execute(sqlFileUrl, currentDataSources, name, opList);
            }
            return true;
        }

        /**
         * 按名称执行sql
         *
         * @param name 名称
         * @return boolean
         */
        @Override
        @Transactional(rollbackFor = Exception.class)
        public boolean executeSqlByName(String name) {
            if (StringUtils.isBlank(name)) {
                throw new RuntimeException("*** dynamic ds *** datasource name can't empty");
            }
            List<String> sqlFileList = defaultDsSqlFileConfig.getSqlFileList();
            if (CollectionUtils.isEmpty(sqlFileList)) {
                throw new RuntimeException("*** dynamic ds *** can't find any sql files");
            }
            List<String> opList = Arrays.asList("create", "alter", "update");
            DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
            Map<String, DataSource> currentDataSources = ds.getDataSources();
            DsProperty dsProperty = getDsProperty(name, currentDataSources);
            dsProperty.setOpList(opList);
            for (String sqlFileUrl : sqlFileList) {
                dsProperty.setSqlFileUrl(sqlFileUrl);
                DatabaseUtils.executeSql(dsProperty);
            }
            return true;
        }

        /**
         * 执行sql
         *
         * @return boolean
         */
        @Override
        @Transactional(rollbackFor = Exception.class)
        public boolean executeSql() {
            List<String> opList = Collections.singletonList("create");
            List<String> sqlFileList = defaultDsSqlFileConfig.getSqlFileList();
            if (CollectionUtils.isEmpty(sqlFileList)) {
                throw new RuntimeException("*** dynamic ds *** can't find any sql files");
            }
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
            return true;
        }

        /**
         * 获取ds属性
         *
         * @param name               名称
         * @param currentDataSources 当前数据源
         * @return {@link DsProperty }
         */
        @Transactional(rollbackFor = Exception.class)
        public DsProperty getDsProperty(String name, Map<String, DataSource> currentDataSources) {
            DruidDataSource druidDataSource = null;
            DataSource source = currentDataSources.get(name);
            if (source == null) {
                throw new RuntimeException("*** dynamic ds *** can't find current datasource name" + name);
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
            DsProperty dsProperty = new DsProperty();
            dsProperty.setUrl(url);
            dsProperty.setUsername(username);
            dsProperty.setPassword(password);
            return dsProperty;
        }

        /**
         * 处决
         *
         * @param sqlFilePath        sql文件路径
         * @param currentDataSources 当前数据源
         * @param name               名称
         */
        @Transactional(rollbackFor = Exception.class)
        public void execute(String sqlFilePath, Map<String, DataSource> currentDataSources, String name, List<String> opList) {
            DsProperty dsProperty = getDsProperty(name, currentDataSources);
            dsProperty.setSqlFileUrl(sqlFilePath);
            dsProperty.setOpList(opList);
            DatabaseUtils.executeSql(dsProperty);
        }

        @Resource
        private DataSource dataSource;
        @Resource
        private DruidDataSourceCreator druidDataSourceCreator;
        @Resource
        private JdbcTemplate jdbcTemplate;
        @Resource
        private DefaultDsSqlFileConfig defaultDsSqlFileConfig;
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
