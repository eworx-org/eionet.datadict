
package eionet.datadict.infrastructure.scheduling.impl;

import eionet.datadict.dal.AsyncTaskDao;
import eionet.datadict.dal.AsyncTaskHistoryDao;
import eionet.datadict.dal.QuartzSchedulerDao;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskDataSerializer;
import eionet.datadict.infrastructure.asynctasks.AsyncTaskManagementException;
import eionet.datadict.infrastructure.asynctasks.impl.AsyncJob;
import eionet.datadict.infrastructure.asynctasks.impl.AsyncJobDataMapAdapter;
import eionet.datadict.infrastructure.asynctasks.impl.AsyncJobKeyBuilder;
import java.util.Map;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import eionet.datadict.infrastructure.scheduling.ScheduleJobService;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.model.AsyncTaskExecutionStatus;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import org.apache.log4j.Logger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import static org.quartz.TriggerBuilder.newTrigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
@Service
public class ScheduleJobServiceImpl implements ScheduleJobService{

        private static final Logger LOGGER = Logger.getLogger(ScheduleJobServiceImpl.class);
    
     private final Scheduler scheduler;
    private final AsyncJobKeyBuilder asyncJobKeyBuilder;
    private final @Qualifier("asyncTaskDao")AsyncTaskDao asyncTaskDao;
    private final QuartzSchedulerDao quartzSchedulerDao;
    private final JobListener asyncJobListener;
    private final AsyncTaskDataSerializer asyncTaskDataSerializer;
    private final AsyncTaskHistoryDao asyncTaskHistoryDao;

    @Autowired
    public ScheduleJobServiceImpl(Scheduler scheduler, AsyncJobKeyBuilder asyncJobKeyBuilder, AsyncTaskDao asyncTaskDao, QuartzSchedulerDao quartzSchedulerDao, JobListener asyncJobListener, AsyncTaskDataSerializer asyncTaskDataSerializer, AsyncTaskHistoryDao asyncTaskHistoryDao) {
        this.scheduler = scheduler;
        this.asyncJobKeyBuilder = asyncJobKeyBuilder;
        this.asyncTaskDao = asyncTaskDao;
        this.quartzSchedulerDao = quartzSchedulerDao;
        this.asyncJobListener = asyncJobListener;
        this.asyncTaskDataSerializer = asyncTaskDataSerializer;
        this.asyncTaskHistoryDao = asyncTaskHistoryDao;
    }

   

     @PostConstruct
    public void init() {
        try {
            this.scheduler.getListenerManager().addJobListener(asyncJobListener, GroupMatcher.jobGroupEquals(asyncJobKeyBuilder.getGroup()));
        }
        catch(SchedulerException ex) {
            throw new AsyncTaskManagementException(ex);
        }
    }

   
    @Override
    public <T> String scheduleJob(Class<T> taskType, Map<String, Object> parameters, Integer intervalMinutes) {
          if (taskType == null) {
            throw new IllegalArgumentException("Task type cannot be null.");
        }
        AsyncJobDataMapAdapter dataMapAdapter = new AsyncJobDataMapAdapter(new JobDataMap());
        dataMapAdapter.setTaskType(taskType);
        if (parameters != null) {
            dataMapAdapter.putParameters(parameters);
        }
        
        JobKey jobKey = this.asyncJobKeyBuilder.createNew();   
        JobDetail jobDetail = JobBuilder.newJob(AsyncJob.class)
                .withIdentity(jobKey)
                .setJobData(dataMapAdapter.getDataMap())
                .build();
        
          SimpleTrigger trigger = newTrigger()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(intervalMinutes).repeatForever()
                        .withMisfireHandlingInstructionIgnoreMisfires())
                .forJob(jobDetail.getKey())
                .build();
        try {
            this.scheduler.scheduleJob(jobDetail, trigger);
        }
        catch (SchedulerException ex) {
            throw new AsyncTaskManagementException(ex);
        }  
        this.createTaskEntry(jobKey, dataMapAdapter.getTaskTypeName(), dataMapAdapter.getParameters());
        return this.asyncJobKeyBuilder.getTaskId(jobKey);
    }


    @Override
    public String getJobStatus(String jobId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

 protected void createTaskEntry(JobKey jobKey, String taskTypeName, Map<String, Object> parameters) {
        AsyncTaskExecutionEntry entry = new AsyncTaskExecutionEntry();
        entry.setTaskId(this.asyncJobKeyBuilder.getTaskId(jobKey));
        entry.setTaskClassName(taskTypeName);
        entry.setExecutionStatus(AsyncTaskExecutionStatus.SCHEDULED);
        entry.setScheduledDate(new Date());
        entry.setSerializedParameters(this.asyncTaskDataSerializer.serializeParameters(parameters));
        this.asyncTaskDao.create(entry);
        LOGGER.info(String.format("Created async task with id: %s", entry.getTaskId()));
    }

    @Override
    public List<AsyncTaskExecutionEntry> getAllScheduledTaskEntries() {
        return this.asyncTaskDao.getAllEntries();
    }

    @Override
    public List<AsyncTaskExecutionEntry> getTaskEntriesHistory() {
        return this.asyncTaskHistoryDao.retrieveAllTasksHistory();
    }

    @Override
    public AsyncTaskExecutionEntry getTaskEntry(String jobId) {
        return this.asyncTaskDao.getFullEntry(jobId);
    }    
}
