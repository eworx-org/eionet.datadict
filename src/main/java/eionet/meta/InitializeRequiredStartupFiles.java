package eionet.meta;

import eionet.datadict.errors.ClassPathLoadResourceException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import eionet.datadict.services.ClassPathResourcesLoadService;
import eionet.util.Props;
import eionet.util.PropsIF;
import java.nio.file.Files;

/**
 *
 * @author Vasilis Skiadas<vs@eworx.gr>
 */
public class InitializeRequiredStartupFiles {

    private static final String ACL_FOLDER_NAME = "acl";
    private static final String MS_ACCESS_FOLDER_HOME = "msaccess";
    private static final String OPENDOC_FOLDER_HOME = "opendoc";
    private static final String VERSION_FILE = "VERSION.txt";
    private static final String TEMP_FOLDER = "tmp";
    
    private ClassPathResourcesLoadService classPathResourcesLoadService = null;
    private String appHomeDirectory;

    @Autowired
    public InitializeRequiredStartupFiles(ClassPathResourcesLoadService classPathResourcesLoadService) {
        this.classPathResourcesLoadService = classPathResourcesLoadService;
    }

    public void initialize() throws RuntimeException {
        try {
            appHomeDirectory = Props.getRequiredProperty(PropsIF.APP_HOME);
        } catch (Exception e) {
            throw new BeanInitializationException("app.home property not found in properties file ", e);
        }

        try {
            initializeAclFiles();
            initializeOpenDocFiles();
            initializeMsAccessFiles();
            overwriteVersionFile();
            createTMPFolder();
        } catch (ClassPathLoadResourceException ex) {
            Logger.getLogger(InitializeRequiredStartupFiles.class.getName()).log(Level.ALL, ex.getMessage(), ex);
            throw new BeanInitializationException(ex.getMessage(), ex);
        } catch (IOException ex) {
            Logger.getLogger(InitializeRequiredStartupFiles.class.getName()).log(Level.ALL, ex.getMessage(), ex);
            throw new BeanInitializationException(ex.getMessage(), ex);
        }
    }

    private void initializeAclFiles() throws ClassPathLoadResourceException, IOException {
        File directory = FileUtils.getFile(appHomeDirectory, ACL_FOLDER_NAME);
        directory.mkdir();
        File[] sourceFiles = classPathResourcesLoadService.loadAllFilesFromFolder(ACL_FOLDER_NAME + "/");
        
        for (File sourceFile : sourceFiles) {
            if (sourceFile.getName().endsWith(".prms") || sourceFile.getName().endsWith(".permissions")) {
                // Always overwrite .prms files.
                FileUtils.copyFileToDirectory(sourceFile, directory);
                continue;
            }
            
            File destinationFile = FileUtils.getFile(directory, sourceFile.getName());
            
            if (!FileUtils.directoryContains(directory, destinationFile)) {
                // Other files must be copied only if they do not exist in destination folder.
                FileUtils.copyFileToDirectory(sourceFile, directory);
            }
        }
    }

    private void initializeMsAccessFiles() throws ClassPathLoadResourceException, IOException {
        File directory = FileUtils.getFile(appHomeDirectory, MS_ACCESS_FOLDER_HOME);
        File[] files = classPathResourcesLoadService.loadAllFilesFromFolder(MS_ACCESS_FOLDER_HOME + "/");
        
        for (File file : files) {
            FileUtils.copyFileToDirectory(file, directory);
        }
    }

    private void initializeOpenDocFiles() throws ClassPathLoadResourceException, IOException {
        File directory = FileUtils.getFile(appHomeDirectory, OPENDOC_FOLDER_HOME, "ods");
        File[] files = classPathResourcesLoadService.loadAllFilesFromFolder(OPENDOC_FOLDER_HOME + "/");
        
        for (File file : files) {
            FileUtils.copyDirectory(file, directory);
        }
    }

    private void overwriteVersionFile() throws ClassPathLoadResourceException, IOException {
        File directory = FileUtils.getFile(appHomeDirectory);
        File oldFile = classPathResourcesLoadService.loadFileFromRootClasspathDirectory(VERSION_FILE);
        FileUtils.copyFileToDirectory(oldFile, directory);
    }

    public void createTMPFolder() throws IOException {
        File directory = FileUtils.getFile(appHomeDirectory, TEMP_FOLDER);
        
        if (directory.isDirectory()) {
            return;
        }
        
        directory.setWritable(true);
        directory.setReadable(true);
        
        if (!directory.mkdirs()) {
            throw new IOException("tmp directory could not be created");
        }
    }

}
