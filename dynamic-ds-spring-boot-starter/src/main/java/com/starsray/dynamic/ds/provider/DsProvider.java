package com.starsray.dynamic.ds.provider;

import com.baomidou.dynamic.datasource.provider.AbstractJdbcDataSourceProvider;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.starsray.dynamic.ds.config.DefaultDsConfig;
import com.starsray.dynamic.ds.constant.DsDriverEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@Primary
@Configuration
public class DsProvider {
    @Resource
    private DefaultDsConfig defaultDsConfig;

    @Bean
    public DynamicDataSourceProvider jdbcDynamicDataSourceProvider() {
        return new AbstractJdbcDataSourceProvider(defaultDsConfig.getDriverClassName(), defaultDsConfig.getUrl(), defaultDsConfig.getUsername(), defaultDsConfig.getPassword()) {
            @Override
            protected Map<String, DataSourceProperty> executeStmt(Statement statement) {
                Map<String, DataSourceProperty> dataSourcePropertiesMap = null;
                ResultSet rs = null;
                try {
                    dataSourcePropertiesMap = new HashMap<>();
                    String createSql = "CREATE TABLE IF NOT EXISTS DYNAMIC_DATASOURCE_INSTANCE (type VARCHAR(255) NULL, name VARCHAR(255) NULL, username VARCHAR(255) NULL, password VARCHAR(255) NULL, url VARCHAR(255) NULL, driver VARCHAR(255) NULL, create_time DATETIME DEFAULT CURRENT_TIMESTAMP)";
                    statement.executeUpdate(createSql);
                    rs = statement.executeQuery("SELECT * FROM DYNAMIC_DATASOURCE_INSTANCE");
                    while (rs.next()) {
                        String name = rs.getString("name");
                        DataSourceProperty property = new DataSourceProperty();
                        property.setDriverClassName(rs.getString("driver"));
                        property.setUrl(rs.getString("url"));
                        property.setUsername(rs.getString("username"));
                        property.setPassword(rs.getString("password"));
                        dataSourcePropertiesMap.put(name, property);
                    }
//                    if (dataSourcePropertiesMap.size() == 0) {
//                        statement.execute(insertSql());
//                        DataSourceProperty dataSourceProperty = new DataSourceProperty();
//                        dataSourceProperty.setDriverClassName(defaultDsConfig.getDriverClassName());
//                        dataSourceProperty.setUrl(defaultDsConfig.getUrl());
//                        dataSourceProperty.setUsername(defaultDsConfig.getUsername());
//                        dataSourceProperty.setPassword(defaultDsConfig.getPassword());
//                        dataSourcePropertiesMap.put(defaultDsConfig.getPrimary(), dataSourceProperty);
//                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (rs != null) {
                            rs.close();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                return dataSourcePropertiesMap;
            }
        };
    }

    public String insertSql() {
        return "INSERT INTO DYNAMIC_DATASOURCE_INSTANCE (type, name, username, password, url, driver) VALUES (" + "'" + DsDriverEnum.getType(defaultDsConfig.getDriverClassName()) + "'," +
                "'" + defaultDsConfig.getPrimary() + "'," +
                "'" + defaultDsConfig.getUsername() + "'," +
                "'" + defaultDsConfig.getPassword() + "'," +
                "'" + defaultDsConfig.getUrl() + "'," +
                "'" + defaultDsConfig.getDriverClassName() + "'" +
                ")";
    }
}
