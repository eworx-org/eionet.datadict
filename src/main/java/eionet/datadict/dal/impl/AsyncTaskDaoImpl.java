package eionet.datadict.dal.impl;

import eionet.datadict.dal.AsyncTaskDao;
import eionet.datadict.dal.impl.converters.DateTimeToLongConverter;
import eionet.datadict.dal.impl.converters.ExecutionStatusToByteConverter;
import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.commons.util.IterableUtils;
import eionet.datadict.commons.sql.ResultSetUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AsyncTaskDaoImpl extends JdbcRepositoryBase implements AsyncTaskDao {

    private final ExecutionStatusToByteConverter executionStatusToByteConverter;
    private final DateTimeToLongConverter dateTimeToLongConverter;
    
    @Autowired
    public AsyncTaskDaoImpl(DataSource dataSource, 
            ExecutionStatusToByteConverter executionStatusToByteConverter,
            DateTimeToLongConverter dateTimeToLongConverter) {
        super(dataSource);
        this.executionStatusToByteConverter = executionStatusToByteConverter;
        this.dateTimeToLongConverter = dateTimeToLongConverter;
    }

    @Override
    public AsyncTaskExecutionEntry getStatusEntry(String taskId) {
        String sql = "select TASK_ID, EXECUTION_STATUS, START_DATE, END_DATE from ASYNC_TASK_ENTRY where TASK_ID = :taskId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("taskId", taskId);
        List<AsyncTaskExecutionEntry> results = this.getNamedParameterJdbcTemplate().query(sql, params, 
                new StatusEntryRowMapper());
        
        return IterableUtils.firstOrDefault(results);
    }

    @Override
    public AsyncTaskExecutionEntry getResultEntry(String taskId) {
        String sql = "select * from ASYNC_TASK_ENTRY where TASK_ID = :taskId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("taskId", taskId);
        List<AsyncTaskExecutionEntry> results = this.getNamedParameterJdbcTemplate().query(sql, params, 
                new ResultEntryRowMapper());
        
        return IterableUtils.firstOrDefault(results);
    }
    
    @Override
    public void create(AsyncTaskExecutionEntry entry) {
        String sql = 
            "insert into ASYNC_TASK_ENTRY(TASK_ID, TASK_CLASS_NAME, EXECUTION_STATUS, START_DATE, SERIALIZED_PARAMETERS) " + 
            "values (:taskId, :className, :executionStatus, :startDate, :serializedParameters)";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("taskId", entry.getTaskId());
        params.put("className", entry.getTaskClassName());
        params.put("executionStatus", this.executionStatusToByteConverter.convert(entry.getExecutionStatus()));
        params.put("startDate", this.dateTimeToLongConverter.convert(entry.getStartDate()));
        params.put("serializedParameters", entry.getSerializedParameters());
        this.getNamedParameterJdbcTemplate().update(sql, params);
        
    }

    @Override
    public void updateStatus(AsyncTaskExecutionEntry entry) {
        String sql = 
            "update ASYNC_TASK_ENTRY set END_DATE = :endDate, EXECUTION_STATUS = :executionStatus, SERIALIZED_RESULT = :serializedResult where TASK_ID = :taskId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("taskId", entry.getTaskId());
        params.put("executionStatus", this.executionStatusToByteConverter.convert(entry.getExecutionStatus()));
        params.put("endDate", this.dateTimeToLongConverter.convert(entry.getEndDate()));
        params.put("serializedResult", entry.getSerializedResult());
        this.getNamedParameterJdbcTemplate().update(sql, params);
    }
    
    protected class StatusEntryRowMapper implements RowMapper<AsyncTaskExecutionEntry> {
        
        @Override
        public AsyncTaskExecutionEntry mapRow(ResultSet rs, int i) throws SQLException {
            AsyncTaskExecutionEntry result = new AsyncTaskExecutionEntry();
            result.setTaskId(rs.getString("TASK_ID"));
            result.setExecutionStatus(executionStatusToByteConverter.convertBack(rs.getByte("EXECUTION_STATUS")));
            result.setStartDate(dateTimeToLongConverter.convertBack(rs.getLong("START_DATE")));
            result.setEndDate(dateTimeToLongConverter.convertBack(ResultSetUtils.getLong(rs, "END_DATE")));
            
            return result;
        }
        
    }
    
    protected class ResultEntryRowMapper implements RowMapper<AsyncTaskExecutionEntry> {

        @Override
        public AsyncTaskExecutionEntry mapRow(ResultSet rs, int i) throws SQLException {
            AsyncTaskExecutionEntry result = new AsyncTaskExecutionEntry();
            result.setTaskId(rs.getString("TASK_ID"));
            result.setTaskClassName("TASK_CLASS_NAME");
            result.setExecutionStatus(executionStatusToByteConverter.convertBack(rs.getByte("EXECUTION_STATUS")));
            result.setStartDate(dateTimeToLongConverter.convertBack(rs.getLong("START_DATE")));
            result.setEndDate(dateTimeToLongConverter.convertBack(ResultSetUtils.getLong(rs, "END_DATE")));
            result.setSerializedParameters(rs.getString("SERIALIZED_PARAMETERS"));
            result.setSerializedResult(rs.getString("SERIALIZED_RESULT"));
            
            return result;
        }
        
    }
    
}
