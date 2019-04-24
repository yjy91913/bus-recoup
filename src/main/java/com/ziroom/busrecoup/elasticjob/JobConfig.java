package com.ziroom.busrecoup.elasticjob;

import com.google.common.base.MoreObjects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 任务设置
 *
 * @Author zhoutao
 * @Date 2017/2/27
 */
public class JobConfig {
    private String jobName;
    private String jobDesc;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobDesc() {
        return jobDesc;
    }

    public void setJobDesc(String jobDesc) {
        this.jobDesc = jobDesc;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("jobName", jobName)
                .add("jobDesc", jobDesc)
                .toString();
    }
}
