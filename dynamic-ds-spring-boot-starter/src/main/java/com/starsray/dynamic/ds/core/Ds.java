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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Ds Operator
 *
 * @author starsray
 * @since 2021-11-10
 */
@Service
public interface Ds {

    Set<String> listDatasource();

    Set<String> addDatasource(DsProperty property) throws IOException;

    boolean removeDatasource(String name, boolean drop);

    boolean executeSqlText(String name, String sqlFilePath);

    boolean executeSqlText(String sqlFilePath);

    @Service
    class DsService implements Ds {

        public Set<String> listDatasource() {
            DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
            return ds.getDataSources().keySet();
        }

        @Transactional(rollbackFor = Exception.class)
        public Set<String> addDatasource(DsProperty property) {
            DatabaseUtils.validateDsProperty(property);
            DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
            DatasourceInstance db = DatasourceInstance.builder().jdbcTemplate(jdbcTemplate).build().of(property);
            if (ds.getDataSources().containsKey(db.getName())) {
                return ds.getDataSources().keySet();
            }
            if (DatabaseUtils.validateConn(property)) {
                DatabaseUtils.createDatabase(property);
                List<String> sqlFileUrlList = defaultDsSqlFileConfig.getSqlFileList();
                sqlFileUrlList.forEach(sqlFileUrl -> {
                    property.setSqlFileUrl(sqlFileUrl);
                    DatabaseUtils.executeSql(property);
                });
            } else {
                DatabaseUtils.createDatabse(jdbcTemplate, property.getDatabase());
                List<String> sqlFileUrlList = defaultDsSqlFileConfig.getSqlFileList();
                sqlFileUrlList.forEach(sqlFileUrl -> DatabaseUtils.executeSql(jdbcTemplate, sqlFileUrl));
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

        public boolean executeSqlText(String name, String sqlFilePath) {
            DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
            Map<String, DataSource> currentDataSources = ds.getDataSources();
            execute(sqlFilePath, currentDataSources, name);
            return true;
        }

        public boolean executeSqlText(String sqlFilePath) {
            DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
            Map<String, DataSource> currentDataSources = ds.getDataSources();
            for (String name : currentDataSources.keySet()) {
                execute(sqlFilePath, currentDataSources, name);
            }
            return true;
        }

        private void execute(String sqlFilePath, Map<String, DataSource> currentDataSources, String name) {
            DruidDataSource druidDataSource = null;
            DataSource source = currentDataSources.get(name);
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
            dsProperty.setSqlFileUrl(sqlFilePath);
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
