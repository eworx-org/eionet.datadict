package eionet.datadict.dal;

import eionet.datadict.model.AsyncTaskExecutionEntry;
import eionet.datadict.model.AsyncTaskExecutionEntryHistory;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public interface AsyncTaskHistoryDao {

    AsyncTaskExecutionEntryHistory retrieveTaskById(String id);
    
    List<AsyncTaskExecutionEntryHistory> retrieveTasksByTaskId(String taskId);
    void storeAsyncTaskEntry(AsyncTaskExecutionEntry entry);
    
    List<AsyncTaskExecutionEntryHistory> retrieveAllTasksHistory();
    
    void deleteRecordsOlderThan(Date date);
}
