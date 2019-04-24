package com.ziroom.busrecoup.internal;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 自动建表
 *
 * @Author zhoutao
 * @Date 2017/5/3
 */
@Slf4j
public class RecoupJobDaoHelper {
    private RecoupJobDaoHelper() {
    }

    private static String mysqlCreateTableSql = "CREATE TABLE `t_recoup_job` (" +
            "`id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '补偿作业表ID'," +
            "`job_class` varchar(200) NOT NULL COMMENT '执行补偿业务逻辑的类,全类名,必须实现IRecoup接口'," +
            "`job_name` varchar(30) NOT NULL COMMENT '补偿任务名称,全英文'," +
            "`job_desc` varchar(90) NOT NULL COMMENT '补偿任务描述'," +
            "`job_json_param` varchar(4000) NOT NULL COMMENT '补偿任务执行类的json参数'," +
            "`job_status` varchar(10) NOT NULL COMMENT '补偿job状态,execute待执行，executing执行中，successed执行成功，failed执行失败, paused暂停'," +
            "`start_time` bigint(20) unsigned NOT NULL COMMENT '补偿job开始时间,到秒绝对时间如20160720121212'," +
            "`complete_time` bigint(20) unsigned NOT NULL COMMENT '补偿job完成时间,到秒绝对时间如20160720121212'," +
            "`create_time` bigint(20) unsigned NOT NULL COMMENT '补偿job创建时间,到秒绝对时间如20160720121212'," +
            "`effect_time` bigint(20) unsigned NOT NULL COMMENT '补偿截止时间,到秒绝对时间如20160720121212，超过该时间没成功放弃补偿'," +
            "`retry_cur_times` int(10) unsigned NOT NULL COMMENT '当前重试次数'," +
            "`retry_total_times` int(10) unsigned NOT NULL COMMENT '重试总次数,超过该次数还没有补偿成功自动放弃补偿'," +
            "`fail_reason` varchar(4000) NOT NULL COMMENT '执行失败原因'," +
            "`async` int(1) unsigned NOT NULL COMMENT '是否开启异步执行,0同步执行 1异步执行'," +
            "`bus_code` varchar(40) DEFAULT NULL COMMENT '业务编码'," +
            "`fail_alarm_flag` int(1) unsigned NOT NULL COMMENT '执行失败报警标志，0未报警 1已报警'," +
            "PRIMARY KEY (`id`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";

    private static List<String> oracleCreateTableSql = Lists.newArrayList();

    static {
        oracleCreateTableSql.add("create sequence aseq_t_recoup_job minvalue 1 maxvalue 999999999999 start with 1 increment by 1 cache 20");
        oracleCreateTableSql.add("create table t_recoup_job (  " +
                "id                 NUMBER(20) not null,  " +
                "job_class             VARCHAR2(200) not null,  " +
                "job_name    VARCHAR2(30) not null,  " +
                "job_desc          VARCHAR2(90) not null,  " +
                "job_json_param          VARCHAR2(4000) not null,  " +
                "job_status          VARCHAR2(10) not null,  " +
                "start_time           NUMBER(20) not null,  " +
                "complete_time           NUMBER(20) not null,  " +
                "create_time             NUMBER(20) not null,  " +
                "effect_time         NUMBER(20) not null,  " +
                "retry_cur_times       NUMBER(10) not null,  " +
                "retry_total_times        NUMBER(10) not null,  " +
                "fail_reason   VARCHAR2(4000) not null,  " +
                "async NUMBER(1) not null,  " +
                "bus_code VARCHAR2(40),  " +
                "fail_alarm_flag NUMBER(1) default 0 not null,  " +
                "constraint pk_t_recoup_job primary key (id)" +
                ")");
        oracleCreateTableSql.add("comment on column t_recoup_job.id   is '补偿作业表ID'");
        oracleCreateTableSql.add("comment on column t_recoup_job.job_class   is '执行补偿业务逻辑的类,全类名,必须实现IRecoup接口'");
        oracleCreateTableSql.add("comment on column t_recoup_job.job_name   is '补偿任务名称,全英文'");
        oracleCreateTableSql.add("comment on column t_recoup_job.job_desc   is '补偿任务描述'");
        oracleCreateTableSql.add("comment on column t_recoup_job.job_json_param   is '补偿任务执行类的json参数'");
        oracleCreateTableSql.add("comment on column t_recoup_job.job_status   is '补偿job状态,execute待执行，executing执行中，successed执行成功，failed执行失败, paused暂停'");
        oracleCreateTableSql.add("comment on column t_recoup_job.start_time   is '补偿job开始时间,到秒绝对时间如20160720121212'");
        oracleCreateTableSql.add("comment on column t_recoup_job.complete_time   is '补偿job完成时间,到秒绝对时间如20160720121212'");
        oracleCreateTableSql.add("comment on column t_recoup_job.create_time   is '补偿job创建时间,到秒绝对时间如20160720121212'");
        oracleCreateTableSql.add("comment on column t_recoup_job.effect_time   is '补偿截止时间,到秒绝对时间如20160720121212，超过该时间没成功放弃补偿'");
        oracleCreateTableSql.add("comment on column t_recoup_job.retry_cur_times   is '当前重试次数'");
        oracleCreateTableSql.add("comment on column t_recoup_job.retry_total_times   is '重试总次数,超过该次数还没有补偿成功自动放弃补偿'");
        oracleCreateTableSql.add("comment on column t_recoup_job.fail_reason   is '执行失败原因'");
        oracleCreateTableSql.add("comment on column t_recoup_job.async   is '是否开启异步执行,0同步执行 1异步执行'");
        oracleCreateTableSql.add("comment on column t_recoup_job.bus_code   is '业务编码'");
        oracleCreateTableSql.add("comment on column t_recoup_job.fail_alarm_flag   is '执行失败报警标志，0未报警 1已报警'");
    }

    private static String dbName;
    static final String TABLE_NAME = "t_recoup_job";

    static boolean isOracle() {
        return dbName.contains("oracle");
    }

    static boolean isMysql() {
        return dbName.contains("mysql");
    }

    static void createTable(JdbcTemplate jdbcTemplate) {
        if (jdbcTemplate == null) {
            log.warn("=========================================================================");
            log.warn("警告：没有为com.ziroom.busrecoup.internal.RecoupJobDao" +
                    "注入Name等于masterJdbcTemplate的Bean,如果不需要使用业务补偿可忽略此警告,否则无法使用。");
            log.warn("==========================================================================");
            return;
        }

        Connection connection = null;
        try {
            connection = jdbcTemplate.getDataSource().getConnection();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            dbName = databaseMetaData.getDatabaseProductName().toLowerCase();
            ResultSet rs = databaseMetaData.getTables(null, null, TABLE_NAME, new String[]{"TABLE"});
            if (rs.next()) {
                return;//表存在直接返回
            }
            rs = databaseMetaData.getTables(null, null, TABLE_NAME.toUpperCase(), new String[]{"TABLE"});
            if (rs.next()) {
                return;//表存在直接返回
            }
        } catch (SQLException e) {
            JdbcUtils.closeConnection(connection);
            throw new RuntimeException("无法获取数据库类型,请检查masterJdbcTemplate的Bean配置,Error:" + e.getMessage(), e);
        }

        try {
            if (isMysql()) {
                jdbcTemplate.execute(mysqlCreateTableSql);
            } else if (isOracle()) {
                jdbcTemplate.batchUpdate(oracleCreateTableSql.toArray(new String[0]));
            } else {
                throw new RuntimeException("暂时只支持mysql,oracle自动建表,其他类型数据库请先手动建表t_recoup_job");
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("无法创建补偿作业表,Error:" + e.getMessage(), e);
        }
    }
}
