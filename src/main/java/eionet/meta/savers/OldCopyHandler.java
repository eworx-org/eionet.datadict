package eionet.meta.savers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Vector;
import eionet.meta.DDSearchEngine;
import eionet.meta.DElemAttribute;
import eionet.util.sql.INParameters;
import eionet.util.sql.SQL;
import eionet.util.sql.SQLGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public abstract class OldCopyHandler {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(OldCopyHandler.class);

    /**  */
    protected Connection conn = null;

    /**  */
    protected DDSearchEngine searchEngine = null;

    /**
     * Create a working copy of an object. Convenience method.
     *
     * @param dstGen
     * @param srcConstraint
     * @return
     * @throws SQLException if database access fails.
     */
    public String copy(SQLGenerator dstGen, String srcConstraint) throws SQLException {

        return copy(dstGen, srcConstraint, true);
    }

    /**
     * Create a working copy of an object. Table name and preset values of the working copy are given in <code>SQLGenerator</code>.
     * The <code>String</code> provides the constraint for selecting the object to copy (e.g. "DATAELEM_ID=123"). If the
     * <code>boolean</code> is <code>false</code>, the fields in <code>SQLGenerator</code> will not be selected from the original
     * and they will be omitted from the final insert query (i.e. they will be auto_generated by the DB).
     *
     * @param dstGen
     * @param srcConstraint
     * @param includeDstGenFields
     * @return id of the working copy.
     * @throws SQLException if database access fails.
     */
    public String copy(SQLGenerator dstGen, String srcConstraint, boolean includeDstGenFields) throws SQLException {

        if (dstGen == null) {
            return null;
        }
        srcConstraint = srcConstraint == null ? "" : " where " + srcConstraint;

        String tableName = dstGen.getTableName();
        Vector colNames = getTableColumnNames(tableName);
        if (colNames == null || colNames.size() == 0) {
            throw new SQLException("Failed to retreive any column names of this table: " + tableName);
        }

        String q = "select * from " + dstGen.getTableName() + srcConstraint;

        Statement stmt = null;
        Statement stmt1 = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(q);
            while (rs.next()) {
                SQLGenerator gen = (SQLGenerator) dstGen.clone();
                for (int i = 0; i < colNames.size(); i++) {
                    String colName = (String) colNames.get(i);
                    String colValue = rs.getString(colName);
                    if ((dstGen.getFieldValue(colName)) == null) {
                        if (colValue != null) {
                            gen.setField(colName, colValue);
                        }
                    } else if (!includeDstGenFields) {
                        if (dstGen.getFieldValue(colName).equals("")) {
                            gen.removeField(colName);
                        }
                    }
                }
                LOGGER.debug(gen.insertStatement());

                if (stmt1 == null) {
                    stmt1 = conn.createStatement();
                }
                stmt1.executeUpdate(gen.insertStatement());
            }
        } finally {

            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (stmt1 != null) {
                    stmt1.close();
                }
            } catch (Exception e) {
            }
        }

        if (includeDstGenFields) {
            return null;
        } else {
            return searchEngine.getLastInsertID();
        }
    }

    /**
     *
     * @param tableName
     * @return
     * @throws SQLException if database access fails.
     */
    private Vector getTableColumnNames(String tableName) throws SQLException {

        Vector result = new Vector();
        if (tableName == null || tableName.length() == 0) {
            return result;
        }

        INParameters inParams = new INParameters();

        String q = "select * from " + tableName + " limit 0,1";

        int colCount = 0;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ResultSetMetaData rsmd = null;
        try {
            stmt = SQL.preparedStatement(q, inParams, conn);
            rs = stmt.executeQuery();
            rsmd = rs.getMetaData();
            if (rsmd != null) {
                colCount = rsmd.getColumnCount();
                for (int i = 1; colCount > 0 && i <= colCount; i++) {
                    String colName = rsmd.getColumnName(i);
                    if (colName != null && colName.length() > 0) {
                        result.add(colName);
                    }
                }
            }
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception e) {
            }
        }

        if (result.size() < colCount) {
            throw new SQLException("Failed to retreive names of all columns of this table: " + tableName);
        }

        return result;
    }

    /**
     *
     *
     * @param newOwner
     * @param oldOwner
     * @param ownerType
     * @throws SQLException if database access fails.
     */
    public void copyFxv(String newOwner, String oldOwner, String ownerType) throws SQLException {

        INParameters inParams = new INParameters();

        String q =
            "select * from FXV where " + "OWNER_ID=" + inParams.add(oldOwner) + " and OWNER_TYPE=" + inParams.add(ownerType);

        Vector v = new Vector();
        PreparedStatement stmt = null;
        stmt = SQL.preparedStatement(q, inParams, conn);
        ResultSet rs = stmt.executeQuery();
        while (rs != null && rs.next()) {
            SQLGenerator gen = new SQLGenerator();
            gen.setTable("FXV");
            gen.setFieldExpr("OWNER_ID", newOwner);
            gen.setField("OWNER_TYPE", ownerType);
            gen.setField("VALUE", rs.getString("VALUE"));
            gen.setField("IS_DEFAULT", rs.getString("IS_DEFAULT"));
            gen.setField("DEFINITION", rs.getString("DEFINITION"));
            gen.setField("SHORT_DESC", rs.getString("SHORT_DESC"));
            v.add(gen);
        }
        rs.close();
        for (int i = 0; i < v.size(); i++) {
            SQLGenerator gen = (SQLGenerator) v.get(i);
            stmt.executeUpdate(gen.insertStatement());
        }

        stmt.close();
    }

    /**
     *
     *
     * @param newID
     * @param oldID
     * @param type
     * @throws SQLException if database access fails.
     */
    public void copyComplexAttrs(String newID, String oldID, String type) throws SQLException {
        copyComplexAttrs(newID, oldID, type, null, null);
    }

    /**
     *
     * @param newID
     * @param oldID
     * @param type
     * @param newType
     * @param mAttrID
     * @throws SQLException if database access fails.
     */
    public void copyComplexAttrs(String newID, String oldID, String type, String newType, String mAttrID) throws SQLException {

        if (newID == null || oldID == null || type == null) {
            return;
        }

        // get the attributes of the parent to copy and loop over them
        Vector v = searchEngine.getComplexAttributes(oldID, type, mAttrID);
        for (int i = 0; v != null && i < v.size(); i++) {

            DElemAttribute attr = (DElemAttribute) v.get(i);
            String attrID = attr.getID();

            // get the attribute fields
            Vector fields = searchEngine.getAttrFields(attrID);
            if (fields == null || fields.size() == 0) {
                continue;
            }

            Statement stmt = null;
            try {
                stmt = conn.createStatement();

                // get the attribute rows
                Vector valueRows = attr.getRows();
                for (int j = 0; valueRows != null && j < valueRows.size(); j++) {

                    Hashtable rowHash = (Hashtable) valueRows.get(j);
                    String rowPos = (String) rowHash.get("position");
                    rowPos = rowPos == null ? "0" : rowPos;

                    // insert a new row
                    if (newType != null) {
                        type = newType;
                    }
                    String rowID = "md5('" + newID + type + attrID + rowPos + "')";

                    SQLGenerator gen = new SQLGenerator();
                    gen.setTable("COMPLEX_ATTR_ROW");
                    gen.setField("PARENT_ID", newID);
                    gen.setField("PARENT_TYPE", type);
                    gen.setField("M_COMPLEX_ATTR_ID", attrID);
                    gen.setFieldExpr("ROW_ID", rowID);
                    gen.setFieldExpr("POSITION", rowPos);

                    stmt.executeUpdate(gen.insertStatement());

                    // get the value of each field in the given row
                    int insertedFields = 0;
                    for (int t = 0; rowID != null && t < fields.size(); t++) {
                        Hashtable fieldHash = (Hashtable) fields.get(t);
                        String fieldID = (String) fieldHash.get("id");
                        String fieldValue = (String) rowHash.get(fieldID);

                        // insert the field
                        if (fieldID != null && fieldValue != null) {

                            gen.clear();
                            gen.setTable("COMPLEX_ATTR_FIELD");
                            gen.setFieldExpr("ROW_ID", rowID);
                            gen.setField("M_COMPLEX_ATTR_FIELD_ID", fieldID);
                            gen.setField("VALUE", fieldValue);

                            StringBuffer buf =
                                new StringBuffer(gen.insertStatement()).append(" on duplicate key update VALUE=").append(
                                        eionet.util.sql.SQL.toLiteral(fieldValue));

                            stmt.executeUpdate(buf.toString());
                            insertedFields++;
                        }
                    }

                    // if no fields were actually inserted, delete the row
                    if (insertedFields == 0) {
                        stmt.executeUpdate("delete from COMPLEX_ATTR_ROW " + "where ROW_ID=" + rowID);
                    }
                }
            } catch (SQLException e) {
                LOGGER.error(e.getMessage(), e);
                throw e;
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                } catch (SQLException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     *
     *
     * @param newID
     * @param oldID
     * @param newType
     * @param oldType
     * @param mAttrID
     * @throws SQLException if database access fails.
     */
    public void copyAttribute(String newID, String oldID, String newType, String oldType, String mAttrID) throws SQLException {
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("ATTRIBUTE");
        gen.setField("DATAELEM_ID", newID);
        gen.setField("PARENT_TYPE", newType);
        copy(gen, "M_ATTRIBUTE_ID=" + mAttrID + " and DATAELEM_ID=" + oldID + " and PARENT_TYPE='" + oldType + "'");
    }
}
