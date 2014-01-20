package com.taobao.diamond.server.service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.stereotype.Service;

import com.taobao.diamond.domain.ConfigInfo;
import com.taobao.diamond.domain.Page;
import com.taobao.diamond.server.utils.PaginationHelper;
import com.taobao.diamond.utils.ResourceUtils;
import com.taobao.diamond.utils.TimeUtils;


/**
 * 数据库服务，提供ConfigInfo在数据库的存取<br>
 * 
 * @author boyan
 * @author leiwen.zh
 * @since 1.0
 */

@Service
public class PersistService {

    private static final String JDBC_DRIVER_NAME = "com.mysql.jdbc.Driver";

    // 最大记录条数
    private static final int MAX_ROWS = 10000;
    // JDBC执行超时时间, 单位秒
    private static final int QUERY_TIMEOUT = 2;

    private static final ConfigInfoRowMapper CONFIG_INFO_ROW_MAPPER = new ConfigInfoRowMapper();

    private static final class ConfigInfoRowMapper implements ParameterizedRowMapper<ConfigInfo> {
        public ConfigInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            ConfigInfo info = new ConfigInfo();
            info.setId(rs.getLong("id"));
            info.setDataId(rs.getString("data_id"));
            info.setGroup(rs.getString("group_id"));
            info.setContent(rs.getString("content"));
            info.setMd5(rs.getString("md5"));
            return info;
        }
    }


    private static String ensurePropValueNotNull(String srcValue) {
        if (srcValue == null) {
            throw new IllegalArgumentException("property is illegal:" + srcValue);
        }

        return srcValue;
    }

    private JdbcTemplate jt;


    /**
     * 单元测试用
     * 
     * @return
     */
    public JdbcTemplate getJdbcTemplate() {
        return this.jt;
    }


    @PostConstruct
    public void initDataSource() throws Exception {
        // 读取jdbc.properties配置, 加载数据源
        Properties props = ResourceUtils.getResourceAsProperties("jdbc.properties");
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(JDBC_DRIVER_NAME);
        ds.setUrl(ensurePropValueNotNull(props.getProperty("db.url")));
        ds.setUsername(ensurePropValueNotNull(props.getProperty("db.user")));
        ds.setPassword(ensurePropValueNotNull(props.getProperty("db.password")));
        ds.setInitialSize(Integer.parseInt(ensurePropValueNotNull(props.getProperty("db.initialSize"))));
        ds.setMaxActive(Integer.parseInt(ensurePropValueNotNull(props.getProperty("db.maxActive"))));
        ds.setMaxIdle(Integer.parseInt(ensurePropValueNotNull(props.getProperty("db.maxIdle"))));
        ds.setMaxWait(Long.parseLong(ensurePropValueNotNull(props.getProperty("db.maxWait"))));
        ds.setPoolPreparedStatements(Boolean.parseBoolean(ensurePropValueNotNull(props
            .getProperty("db.poolPreparedStatements"))));

        this.jt = new JdbcTemplate();
        this.jt.setDataSource(ds);
        // 设置最大记录数，防止内存膨胀
        this.jt.setMaxRows(MAX_ROWS);
        // 设置JDBC执行超时时间
        this.jt.setQueryTimeout(QUERY_TIMEOUT);
    }


    public void addConfigInfo(final ConfigInfo configInfo) {
        final Timestamp time = TimeUtils.getCurrentTime();

        this.jt.update(
            "insert into config_info (data_id,group_id,content,md5,gmt_create,gmt_modified) values(?,?,?,?,?,?)",
            new PreparedStatementSetter() {
                public void setValues(PreparedStatement ps) throws SQLException {
                    int index = 1;
                    ps.setString(index++, configInfo.getDataId());
                    ps.setString(index++, configInfo.getGroup());
                    ps.setString(index++, configInfo.getContent());
                    ps.setString(index++, configInfo.getMd5());
                    ps.setTimestamp(index++, time);
                    ps.setTimestamp(index++, time);
                }
            });
    }


    public void removeConfigInfo(final ConfigInfo configInfo) {
        this.jt.update("delete from config_info where data_id=? and group_id=?", new PreparedStatementSetter() {
            public void setValues(PreparedStatement ps) throws SQLException {
                int index = 1;
                ps.setString(index++, configInfo.getDataId());
                ps.setString(index++, configInfo.getGroup());
            }
        });
    }


    public void updateConfigInfo(final ConfigInfo configInfo) {
        final Timestamp time = TimeUtils.getCurrentTime();

        this.jt.update("update config_info set content=?,md5=?,gmt_modified=? where data_id=? and group_id=?",
            new PreparedStatementSetter() {

                public void setValues(PreparedStatement ps) throws SQLException {
                    int index = 1;
                    ps.setString(index++, configInfo.getContent());
                    ps.setString(index++, configInfo.getMd5());
                    ps.setTimestamp(index++, time);
                    ps.setString(index++, configInfo.getDataId());
                    ps.setString(index++, configInfo.getGroup());
                }
            });
    }


    public ConfigInfo findConfigInfo(final String dataId, final String group) {
        try {
            return this.jt.queryForObject(
                "select id,data_id,group_id,content,md5 from config_info where data_id=? and group_id=?",
                new Object[] { dataId, group }, CONFIG_INFO_ROW_MAPPER);
        }
        catch (EmptyResultDataAccessException e) {
            // 是EmptyResultDataAccessException, 表明数据不存在, 返回null
            return null;
        }
    }


    public ConfigInfo findConfigInfo(long id) {
        try {
            return this.jt.queryForObject("select id,data_id,group_id,content,md5 from config_info where id=?",
                new Object[] { id }, CONFIG_INFO_ROW_MAPPER);
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
    }


    public Page<ConfigInfo> findConfigInfoByDataId(final int pageNo, final int pageSize, final String dataId) {
        PaginationHelper<ConfigInfo> helper = new PaginationHelper<ConfigInfo>();
        return helper.fetchPage(this.jt, "select count(id) from config_info where data_id=?",
            "select id,data_id,group_id,content,md5 from config_info where data_id=?", new Object[] { dataId }, pageNo,
            pageSize, CONFIG_INFO_ROW_MAPPER);
    }


    public Page<ConfigInfo> findConfigInfoByGroup(final int pageNo, final int pageSize, final String group) {
        PaginationHelper<ConfigInfo> helper = new PaginationHelper<ConfigInfo>();
        return helper.fetchPage(this.jt, "select count(id) from config_info where group_id=?",
            "select id,data_id,group_id,content,md5 from config_info where group_id=?", new Object[] { group }, pageNo,
            pageSize, CONFIG_INFO_ROW_MAPPER);
    }


    public Page<ConfigInfo> findAllConfigInfo(final int pageNo, final int pageSize) {
        PaginationHelper<ConfigInfo> helper = new PaginationHelper<ConfigInfo>();
        return helper.fetchPage(this.jt, "select count(id) from config_info order by id",
            "select id,data_id,group_id,content,md5 from config_info order by id ", new Object[] {}, pageNo, pageSize,
            CONFIG_INFO_ROW_MAPPER);
    }


    public Page<ConfigInfo> findConfigInfoLike(final int pageNo, final int pageSize, final String dataId,
            final String group) {
        if (StringUtils.isBlank(dataId) && StringUtils.isBlank(group)) {
            return this.findAllConfigInfo(pageNo, pageSize);
        }

        PaginationHelper<ConfigInfo> helper = new PaginationHelper<ConfigInfo>();

        String sqlCountRows = "select count(id) from config_info where ";
        String sqlFetchRows = "select id,data_id,group_id,content,md5 from config_info where ";
        boolean wasFirst = true;
        if (!StringUtils.isBlank(dataId)) {
            sqlCountRows += "data_id like ? ";
            sqlFetchRows += "data_id like ? ";
            wasFirst = false;
        }
        if (!StringUtils.isBlank(group)) {
            if (wasFirst) {
                sqlCountRows += "group_id like ? ";
                sqlFetchRows += "group_id like ? ";
            }
            else {
                sqlCountRows += "and group_id like ? ";
                sqlFetchRows += "and group_id like ? ";
            }
        }

        Object[] args = null;
        if (!StringUtils.isBlank(dataId) && !StringUtils.isBlank(group)) {
            args = new Object[] { generateLikeArgument(dataId), generateLikeArgument(group) };
        }
        else if (!StringUtils.isBlank(dataId)) {
            args = new Object[] { generateLikeArgument(dataId) };
        }
        else if (!StringUtils.isBlank(group)) {
            args = new Object[] { generateLikeArgument(group) };
        }

        return helper.fetchPage(this.jt, sqlCountRows, sqlFetchRows, args, pageNo, pageSize, CONFIG_INFO_ROW_MAPPER);
    }


    private String generateLikeArgument(String s) {
        if (s.indexOf("*") >= 0)
            return s.replaceAll("\\*", "%");
        else {
            return "%" + s + "%";
        }
    }

}
