package eionet.meta.schemas;

import java.io.File;
import java.io.IOException;

import net.sourceforge.stripes.action.FileBean;

import org.apache.commons.lang.StringUtils;

import eionet.util.Props;
import eionet.util.PropsIF;

/**
 * 
 * @author Jaanus Heinlaid
 *
 */
public class SchemaRepository {

    /** */
    public static final String REPO_PATH = Props.getRequiredProperty(PropsIF.SCHEMA_REPO_LOCATION);

    /**
     * 
     */
    public SchemaRepository(){
        // default constructor
    }

    /**
     * 
     * @return
     * @throws IOException
     */
    public File add(FileBean fileBean, String schemaSetIdentifier, boolean overwrite) throws IOException{

        if (fileBean==null || StringUtils.isBlank(schemaSetIdentifier)){
            throw new IllegalArgumentException("File bean and schema set identifier must not be null or blank!");
        }

        File repoLocation = new File(REPO_PATH);
        if (!repoLocation.exists() || !repoLocation.isDirectory()){
            repoLocation.mkdir();
        }

        File schemaSetLocation = new File(REPO_PATH, schemaSetIdentifier);
        if (!schemaSetLocation.exists() || !schemaSetLocation.isDirectory()){
            schemaSetLocation.mkdir();
        }

        File schemaLocation = new File(schemaSetLocation, fileBean.getFileName());
        if (schemaLocation.exists() && schemaLocation.isFile()){
            if (overwrite==false){
                throw new SchemaRepositoryException("File already exists, but overwrite not requested!");
            }
            else{
                schemaLocation.delete();
            }
        }

        fileBean.save(schemaLocation);
        if (schemaLocation.exists() && schemaLocation.isFile()){
            return schemaLocation;
        }
        else{
            throw new SchemaRepositoryException("Schema file creation threw no exceptions, yet the file does not exist!");
        }
    }

    /**
     * 
     * @param file
     */
    public static void deleteQuietly(File file){
        if (file != null){
            try {
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
