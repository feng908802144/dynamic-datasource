package com.starsray.dynamic.ds.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.starsray.dynamic.ds.constant.DsDriverEnum;
import com.starsray.dynamic.ds.core.DsProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * 数据源管理
 * </p>
 *
 * @author starsray
 * @since 2021-11-12
 */
@Slf4j
public class DatabaseUtils {


    /**
     * 验证连接
     *
     * @param property 连接参数
     * @return boolean
     */
    public static boolean validateConn(DsProperty property) {
        try {
            if (StringUtils.isBlank(property.getType())) {
                property.setType(DsDriverEnum.MYSQL5.getType());
            }
            Class.forName(DsDriverEnum.getDriverClassName(property.getType()));
            DriverManager.getConnection(property.getUrl(), property.getUsername(), property.getPassword());
        } catch (Exception e) {
            log.error("*** dynamic ds *** can't connect may database info error");
            return false;
        }
        return true;
    }

    /**
     * 创建数据库
     *
     * @param jdbcTemplate jdbc模板
     * @param database     数据库
     */
    public synchronized static void createDatabase(JdbcTemplate jdbcTemplate, String database) {
        if (StringUtils.isBlank(database)) {
            throw new RuntimeException("*** dynamic ds *** database info can't empty");
        }
        jdbcTemplate.execute(String.format("CREATE DATABASE IF NOT EXISTS `%s` DEFAULT CHARACTER SET UTF8", database));
        log.info("*** Dynamic Ds *** create dataBase = {}", database);
    }

    /**
     * 创建数据库
     *
     * @param property 属性
     */
    public synchronized static void createDatabase(DsProperty property) {
        JdbcTemplate jdbcTemplate = createJdbcTemplate(property);
        String database = getDatabase(property.getUrl());
        jdbcTemplate.execute(String.format("CREATE DATABASE IF NOT EXISTS `%s` DEFAULT CHARACTER SET UTF8", database));
        log.info("*** Dynamic Ds *** create dataBase = {}", database);

    }

    /**
     * 执行sql
     *
     * @param property 所有物
     */
    public synchronized static void executeSql(DsProperty property) {
        String sqlFileUrl = property.getSqlFileUrl();
        List<String> opList = property.getOpList();
        JdbcTemplate jdbcTemplate = createJdbcTemplate(property);
        execute(sqlFileUrl, jdbcTemplate, opList);
    }

    private static void execute(String sqlFileUrl, JdbcTemplate jdbcTemplate, List<String> opList) {
        String sqlText = null;
        if (sqlFileUrl.contains("http")) {
            try {
                sqlText = IOUtils.toString(URI.create(sqlFileUrl), "utf-8");
            } catch (IOException e) {
                log.error("*** dynamic ds *** load remote sql file error :{}", sqlFileUrl);
            }
        } else {
            InputStream inputStream = DatabaseUtils.class.getClassLoader().getResourceAsStream(sqlFileUrl);
            assert inputStream != null;
            try {
                sqlText = IOUtils.toString(inputStream, "utf-8");
            } catch (IOException e) {
                log.error("*** dynamic ds *** load local sql file error : {}", sqlFileUrl);
            }
        }
        assert sqlText != null;
        String[] sqlStrings = sqlText.split(";");
        int execute = 0;
        int total = sqlStrings.length;
        for (String sql : sqlStrings) {
            if (validateSql(sql, opList)) {
                jdbcTemplate.execute(sql);
                execute++;
            } else {
                log.info("*** dynamic ds *** not execute sql \n{}", sql);
            }
        }
        log.info("*** dynamic ds *** execute sql file complete total:{},execute:{}", total, execute);
    }

    public static boolean validateSql(String sql, List<String> opList) {
        boolean bool = false;
        for (String op : opList) {
            if (sql.toLowerCase().contains(op)) {
                bool = true;
            }
            if (bool) {
                return true;
            }
        }
        return false;
    }

    private static String getDatabase(String url) {
        Pattern p = url.contains("?") ? Pattern.compile("(?<=\\d/).*(?=\\?)") : Pattern.compile("(?<=\\d/).*");
        Matcher m = p.matcher(url);
        if (!m.find()) {
            throw new RuntimeException("can't match database from url");
        }
        return m.group();
    }

    public static String replaceDatabase(String url, String database) {
        return url.contains("?") ?
                url.replaceAll(("(?<=\\d/).*(?=\\?)"), database) :
                url.replaceAll(("(?<=\\d/).*"), database);
    }

    private static JdbcTemplate createJdbcTemplate(DsProperty property) {
        String url = property.getUrl();
        String username = property.getUsername();
        String password = property.getPassword();
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return new JdbcTemplate(dataSource);
    }

    private static String getHost(String url) {
        Pattern p = Pattern.compile("(?<=\\/\\/).*(?=:)");
        Matcher m = p.matcher(url);
        if (!m.find()) {
            throw new RuntimeException("can't match host from url");
        }
        return m.group();
    }

    public static void main(String[] args) {
        System.out.println(replaceDatabase("jdbc:mysql://127.0.0.1:3306/slave","cur"));
    }
}
