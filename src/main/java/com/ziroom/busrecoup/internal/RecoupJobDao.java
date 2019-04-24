package com.ziroom.busrecoup.internal;

import com.ziroom.busrecoup.JobStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
public class RecoupJobDao {
    @Autowired(required = false)
    @Qualifier("masterJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    /**
     * 得到所有待执行job
     *
     * @return
     */
    public com.ziroom.busrecoup.internal.RecoupJobEntity findOneByBusCode(String busCode) {
        com.ziroom.busrecoup.internal.RecoupJobEntity recoupJobEntity = null;
        try {
            recoupJobEntity = (com.ziroom.busrecoup.internal.RecoupJobEntity) this.jdbcTemplate.queryForObject("select id,job_name,job_class,job_status,job_json_param,start_time,complete_time,create_time,effect_time,retry_cur_times,retry_total_times,bus_code from " + com.ziroom.busrecoup.internal.RecoupJobDaoHelper.TABLE_NAME + " t where t.bus_code = ?", new Object[]{busCode}, new BeanPropertyRowMapper(com.ziroom.busrecoup.internal.RecoupJobEntity.class));
        } catch (EmptyResultDataAccessException e) {//queryForObject查询不到会抛异常
        } catch (IncorrectResultSizeDataAccessException e) {//queryForObject结果多于一个会抛异常
            throw new RuntimeException("busCode[" + busCode + "]查询结果不唯一");
        }
        return recoupJobEntity;
    }

    /**
     * 得到所有待执行job
     *
     * @return
     */
    public List<Long> getAllToBeExecutedIdList() {
        List<Long> idList = this.jdbcTemplate.queryForList("select id from " + com.ziroom.busrecoup.internal.RecoupJobDaoHelper.TABLE_NAME + " where job_status in (?, ?) and start_time <= ? order by id",
                new Object[]{JobStatusEnum.EXECUTE.getCode(), JobStatusEnum.EXECUTING.getCode(), com.ziroom.busrecoup.internal.DateUtils.formatNow2Long()},
                Long.class);
        return idList;
    }

    public com.ziroom.busrecoup.internal.RecoupJobEntity findOneById(long id) {
        com.ziroom.busrecoup.internal.RecoupJobEntity recoupJobEntity = null;
        try {
            recoupJobEntity = (com.ziroom.busrecoup.internal.RecoupJobEntity) this.jdbcTemplate.queryForObject("select * from " + com.ziroom.busrecoup.internal.RecoupJobDaoHelper.TABLE_NAME + " t where t.id = ?", new Object[]{id}, new BeanPropertyRowMapper(com.ziroom.busrecoup.internal.RecoupJobEntity.class));
        } catch (EmptyResultDataAccessException e) {//queryForObject查询不到会抛异常
        }
        return recoupJobEntity;
    }

    public com.ziroom.busrecoup.internal.RecoupJobEntity findSimpleOneById(long id) {
        com.ziroom.busrecoup.internal.RecoupJobEntity recoupJobEntity = null;
        try {
            recoupJobEntity = (com.ziroom.busrecoup.internal.RecoupJobEntity) this.jdbcTemplate.queryForObject("select id,job_name,job_class,job_status,job_json_param,start_time,complete_time,create_time,effect_time,retry_cur_times,retry_total_times,bus_code from " + com.ziroom.busrecoup.internal.RecoupJobDaoHelper.TABLE_NAME + " t where t.id = ?", new Object[]{id}, new BeanPropertyRowMapper(com.ziroom.busrecoup.internal.RecoupJobEntity.class));
        } catch (EmptyResultDataAccessException e) {//queryForObject查询不到会抛异常
        }
        return recoupJobEntity;
    }

    public void recordFailStatus(final com.ziroom.busrecoup.internal.RecoupJobEntity recoupJobEntity, final String failReason) {
        final long completeTime = com.ziroom.busrecoup.internal.DateUtils.formatNow2Long();
        final int retryCurTimes = recoupJobEntity.getRetryCurTimes() + 1;

        int count = this.jdbcTemplate.update("update " + com.ziroom.busrecoup.internal.RecoupJobDaoHelper.TABLE_NAME + " set job_status=?,retry_cur_times=?,complete_time=?,fail_reason=? where id = ? and job_status in (?, ?)",
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps) throws SQLException {
                        ps.setString(1, JobStatusEnum.FAILED.getCode());
                        ps.setInt(2, retryCurTimes);
                        ps.setLong(3, completeTime);
                        ps.setString(4, failReason);
                        ps.setLong(5, recoupJobEntity.getId());
                        ps.setString(6, JobStatusEnum.EXECUTE.getCode());
                        ps.setString(7, JobStatusEnum.EXECUTING.getCode());
                    }
                });
        if (count == 1) {
            recoupJobEntity.setJobStatus(JobStatusEnum.FAILED.getCode());
            recoupJobEntity.setRetryCurTimes(retryCurTimes);
            recoupJobEntity.setCompleteTime(completeTime);
            recoupJobEntity.setFailReason(failReason);
        }
    }

    public void recordSuccessStatus(final com.ziroom.busrecoup.internal.RecoupJobEntity recoupJobEntity) {
        final long completeTime = com.ziroom.busrecoup.internal.DateUtils.formatNow2Long();
        final int retryCurTimes = recoupJobEntity.getRetryCurTimes() + 1;

        int count = this.jdbcTemplate.update("update " + com.ziroom.busrecoup.internal.RecoupJobDaoHelper.TABLE_NAME + " set job_status=?,retry_cur_times=?,complete_time=? where id = ? and job_status in (?, ?)",
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps) throws SQLException {
                        ps.setString(1, JobStatusEnum.SUCCESSED.getCode());
                        ps.setInt(2, retryCurTimes);
                        ps.setLong(3, completeTime);
                        ps.setLong(4, recoupJobEntity.getId());
                        ps.setString(5, JobStatusEnum.EXECUTE.getCode());
                        ps.setString(6, JobStatusEnum.EXECUTING.getCode());
                    }
                });
        if (count == 1) {
            recoupJobEntity.setJobStatus(JobStatusEnum.SUCCESSED.getCode());
            recoupJobEntity.setRetryCurTimes(retryCurTimes);
            recoupJobEntity.setCompleteTime(completeTime);
        }
    }

    public void recordExecutingStatus(final com.ziroom.busrecoup.internal.RecoupJobEntity recoupJobEntity, final String failReason) {
        final int retryCurTimes = recoupJobEntity.getRetryCurTimes() + 1;

        int count = this.jdbcTemplate.update("update " + com.ziroom.busrecoup.internal.RecoupJobDaoHelper.TABLE_NAME + " set job_status=?,retry_cur_times=?,fail_reason=? where id = ? and job_status in (?, ?)",
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps) throws SQLException {
                        ps.setString(1, JobStatusEnum.EXECUTING.getCode());
                        ps.setInt(2, retryCurTimes);
                        ps.setString(3, failReason);
                        ps.setLong(4, recoupJobEntity.getId());
                        ps.setString(5, JobStatusEnum.EXECUTE.getCode());
                        ps.setString(6, JobStatusEnum.EXECUTING.getCode());
                    }
                });
        if (count == 1) {
            recoupJobEntity.setJobStatus(JobStatusEnum.EXECUTING.getCode());
            recoupJobEntity.setRetryCurTimes(retryCurTimes);
        }
    }

    public int updateStartTimeById(final long id, final long startTime) {
        int count = this.jdbcTemplate.update("update " + com.ziroom.busrecoup.internal.RecoupJobDaoHelper.TABLE_NAME + " set start_time=? where id = ?",
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps) throws SQLException {
                        ps.setLong(1, startTime);
                        ps.setLong(2, id);
                    }
                });
        return count;
    }

    public int updateEffectTimeById(final long id, final long effectTime) {
        int count = this.jdbcTemplate.update("update " + com.ziroom.busrecoup.internal.RecoupJobDaoHelper.TABLE_NAME + " set effect_time=? where id = ?",
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps) throws SQLException {
                        ps.setLong(1, effectTime);
                        ps.setLong(2, id);
                    }
                });
        return count;
    }

    public int updateRetryTotalTimesById(final long id, final int retryTotalTimes) {
        int count = this.jdbcTemplate.update("update " + com.ziroom.busrecoup.internal.RecoupJobDaoHelper.TABLE_NAME + " set retry_total_times=? where id = ?",
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps) throws SQLException {
                        ps.setInt(1, retryTotalTimes);
                        ps.setLong(2, id);
                    }
                });
        return count;
    }

    public void insert(final com.ziroom.busrecoup.internal.RecoupJobEntity recoupJobEntity) {
        String insertSql = "insert into " + com.ziroom.busrecoup.internal.RecoupJobDaoHelper.TABLE_NAME + "(job_class,job_name,job_desc,job_json_param," +
                "job_status,start_time,complete_time,create_time,effect_time," +
                "retry_cur_times,retry_total_times," +
                "fail_reason,async,bus_code,fail_alarm_flag) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        if (com.ziroom.busrecoup.internal.RecoupJobDaoHelper.isOracle()) {
            insertSql = "insert into " + com.ziroom.busrecoup.internal.RecoupJobDaoHelper.TABLE_NAME + "(id,job_class,job_name,job_desc,job_json_param," +
                    "job_status,start_time,complete_time,create_time,effect_time," +
                    "retry_cur_times,retry_total_times," +
                    "fail_reason,async,bus_code,fail_alarm_flag) values(aseq_t_recoup_job.nextval,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        }
        this.jdbcTemplate.update(insertSql,
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps) throws SQLException {
                        ps.setString(1, recoupJobEntity.getJobClass());
                        ps.setString(2, recoupJobEntity.getJobName());
                        ps.setString(3, recoupJobEntity.getJobDesc());
                        ps.setString(4, recoupJobEntity.getJobJsonParam());
                        ps.setString(5, recoupJobEntity.getJobStatus());
                        ps.setLong(6, recoupJobEntity.getStartTime());
                        ps.setLong(7, recoupJobEntity.getCompleteTime());
                        ps.setLong(8, recoupJobEntity.getCreateTime());
                        ps.setLong(9, recoupJobEntity.getEffectTime());
                        ps.setInt(10, recoupJobEntity.getRetryCurTimes());
                        ps.setInt(11, recoupJobEntity.getRetryTotalTimes());
                        ps.setString(12, recoupJobEntity.getFailReason());
                        ps.setInt(13, recoupJobEntity.getAsync());
                        ps.setString(14, recoupJobEntity.getBusCode());
                        ps.setInt(15, recoupJobEntity.getFailAlarmFlag());
                    }
                });
    }

    /**
     * 完成自动创建业务补偿表
     *
     * @throws SQLException
     */
    @PostConstruct
    void afterPropertiesSet() {
        com.ziroom.busrecoup.internal.RecoupJobDaoHelper.createTable(this.jdbcTemplate);
    }
}
