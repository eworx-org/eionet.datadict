package eionet.meta.savers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.LinkedHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import eionet.acl.AccessController;

import eionet.meta.DDUser;
import eionet.meta.DElemAttribute;
import eionet.util.sql.INParameters;
import eionet.util.sql.SQL;

public class AttributeHandler extends BaseHandler {

    private String mode = null;
    private String attr_id = null;
    private String lastInsertID = null;

    private String ns_id = null;

    private String name = null;
    private String shortName = null;
    private String definition = null;
    private String obligation = null;

    public AttributeHandler(Connection conn, HttpServletRequest req, ServletContext ctx) {
        this(conn, new Parameters(req), ctx);
    }

    public AttributeHandler(Connection conn, Parameters req, ServletContext ctx) {
        this.conn = conn;
        this.req = req;
        this.ctx = ctx;
        this.mode = req.getParameter("mode");
        this.attr_id = req.getParameter("attr_id");
        this.name = req.getParameter("name");
        this.shortName = req.getParameter("short_name");
        this.definition = req.getParameter("definition");
        this.obligation = req.getParameter("obligation");
        this.ns_id = req.getParameter("ns");
    }

    public AttributeHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode) {
        this(conn, req, ctx);
        this.mode = mode;
    }

    public void execute_() throws Exception {

        if (mode == null || (!mode.equalsIgnoreCase("add") &&
                !mode.equalsIgnoreCase("edit") &&
                !mode.equalsIgnoreCase("delete"))) {
            throw new Exception("AttributeHandler mode unspecified!");
        }

        if (mode.equalsIgnoreCase("add")) {
            executeAnInsert();
        } else if (mode.equalsIgnoreCase("edit")) {
            executeAnUpdate();
        } else {
            executeADelete();
            cleanVisuals();
        }
    }

    /**
     *
     * @throws Exception
     */
    private void executeAnInsert() throws Exception {

        INParameters inParams = new INParameters();
        LinkedHashMap map = new LinkedHashMap();

        map.put("SHORT_NAME", inParams.add(shortName));
        map.put("NAME", inParams.add(name));

        if (definition != null) {
            map.put("DEFINITION", inParams.add(definition));
        }
        if (ns_id != null) {
            map.put("NAMESPACE_ID", inParams.add(ns_id, Types.INTEGER));
        }

        String dispOrder = req.getParameter("dispOrder");
        if (dispOrder != null && dispOrder.length() > 0) {
            map.put("DISP_ORDER", inParams.add(dispOrder, Types.INTEGER));
        }

        String inherit = req.getParameter("inheritable");
        if (inherit != null && inherit.length() > 0) {
            map.put("INHERIT", inParams.add(inherit));
        }

        map.put("DISP_WHEN", inParams.add(getDisplayWhen(), Types.INTEGER));
        map.put("OBLIGATION", inParams.add(obligation));

        String dispType = req.getParameter("dispType");
        map.put("DISP_TYPE", inParams.add(dispType == null || dispType.length() == 0 ? null : dispType));

        String dispWidth = req.getParameter("dispWidth");
        if (dispWidth != null && dispWidth.length() > 0) {
            map.put("DISP_WIDTH", inParams.add(dispWidth, Types.INTEGER));
        }

        String dispHeight = req.getParameter("dispHeight");
        if (dispHeight != null && dispHeight.length() > 0) {
            map.put("DISP_HEIGHT", inParams.add(dispHeight));
        }

        String dispMultiple = req.getParameter("dispMultiple");
        if (dispMultiple != null && dispMultiple.length() > 0) {
            map.put("DISP_MULTIPLE", inParams.add(dispMultiple));
        }

        String rdfPropertyName = req.getParameter("rdfPropertyName");
        int rdfNamespaceId = Integer.parseInt(req.getParameter("rdfNamespaceId"));
        if (StringUtils.isEmpty(rdfPropertyName) || rdfNamespaceId == 0) {
            map.put("RDF_PROPERTY_NAME", inParams.add(null));
            map.put("RDF_PROPERTY_NAMESPACE_ID", inParams.add(null));
        } else {
            map.put("RDF_PROPERTY_NAME", inParams.add(rdfPropertyName));
            map.put("RDF_PROPERTY_NAMESPACE_ID", inParams.add(rdfNamespaceId));
        }

        PreparedStatement stmt = null;
        try {
            stmt = SQL.preparedStatement(SQL.insertStatement("M_ATTRIBUTE", map), inParams, conn);
            stmt.executeUpdate();
            setLastInsertID();
        } finally {
            SQL.close(stmt);
        }

        // add acl
        if (user != null) {
            String idPrefix = "s";
            String aclPath = "/attributes/" + idPrefix + getLastInsertID();
            String aclDesc = "Short name: " + shortName;
            AccessController.addAcl(aclPath, user.getUserName(), aclDesc);
        }
    }

    /**
     *
     * @throws SQLException
     */
    private void executeAnUpdate() throws SQLException {
        lastInsertID = attr_id;

        INParameters inParams = new INParameters();
        LinkedHashMap map = new LinkedHashMap();
        map.put("SHORT_NAME", inParams.add(shortName));
        map.put("NAME", inParams.add(name));

        if (definition != null) {
            map.put("DEFINITION", inParams.add(definition));
        }
        if (ns_id != null) {
            map.put("NAMESPACE_ID", inParams.add(ns_id, Types.INTEGER));
        }

        String dispOrder = req.getParameter("dispOrder");
        map.put("DISP_ORDER", inParams.add((dispOrder == null || dispOrder.length() == 0) ? "999" : dispOrder, Types.INTEGER));

        String inherit = req.getParameter("inheritable");
        map.put("INHERIT", inParams.add((inherit != null && inherit.length() > 0) ? inherit : "0"));

        map.put("OBLIGATION", inParams.add(obligation));
        map.put("DISP_WHEN", inParams.add(getDisplayWhen(), Types.INTEGER));

        String dispType = req.getParameter("dispType");
        map.put("DISP_TYPE", inParams.add(dispType == null || dispType.length() == 0 ? null : dispType));

        String dispWidth = req.getParameter("dispWidth");
        map.put("DISP_WIDTH", inParams.add((dispWidth == null || dispWidth.length() == 0) ? "20" : dispWidth, Types.INTEGER));

        String dispHeight = req.getParameter("dispHeight");
        map.put("DISP_HEIGHT", inParams.add((dispHeight == null || dispHeight.length() == 0) ? "20" : dispHeight, Types.INTEGER));

        String dispMultiple = req.getParameter("dispMultiple");
        map.put("DISP_MULTIPLE", inParams.add(dispMultiple != null && dispMultiple.length() > 0 ? dispMultiple : "0"));

        String rdfPropertyName = req.getParameter("rdfPropertyName");
        int rdfNamespaceId = Integer.parseInt(req.getParameter("rdfNamespaceId"));
        if (StringUtils.isEmpty(rdfPropertyName) || rdfNamespaceId == 0) {
            map.put("RDF_PROPERTY_NAME", inParams.add(null));
            map.put("RDF_PROPERTY_NAMESPACE_ID", inParams.add(null));
        } else {
            map.put("RDF_PROPERTY_NAME", inParams.add(rdfPropertyName));
            map.put("RDF_PROPERTY_NAMESPACE_ID", inParams.add(rdfNamespaceId));
        }

        PreparedStatement stmt = null;
        try {
            StringBuffer buf = new StringBuffer(SQL.updateStatement("M_ATTRIBUTE", map));
            buf.append(" where M_ATTRIBUTE_ID=").append(inParams.add(attr_id, Types.INTEGER));

            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            stmt.executeUpdate();
        } finally {
            SQL.close(stmt);
        }
    }

    /**
     *
     * @throws Exception
     */
    private void executeADelete() throws Exception {
        String[] simpleAttrs = req.getParameterValues("simple_attr_id");

        PreparedStatement stmt = null;
        INParameters inParams = new INParameters();
        try {
            if (simpleAttrs != null && simpleAttrs.length != 0) {

                StringBuffer buf = new StringBuffer("delete from M_ATTRIBUTE where ");
                for (int i = 0; i < simpleAttrs.length; i++) {
                    if (i > 0) {
                        buf.append(" or ");
                    }
                    buf.append("M_ATTRIBUTE_ID=");
                    buf.append(inParams.add(simpleAttrs[i], Types.INTEGER));
                }
                stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
                stmt.executeUpdate();

                deleteSimpleAttributeValues(simpleAttrs);
                deleteFixedValues(simpleAttrs);
            }
        } finally {
            SQL.close(stmt);
        }

        // remove acls
        for (int i = 0; simpleAttrs != null && i < simpleAttrs.length; i++) {
            try {
                AccessController.removeAcl("/attributes/s" + simpleAttrs[i]);
            } catch (Exception e) {
            }
        }
    }

    /**
     *
     * @param attr_ids
     * @throws SQLException
     */
    private void deleteSimpleAttributeValues(String[] attr_ids) throws SQLException {

        if (attr_ids == null || attr_ids.length == 0) {
            return;
        }

        PreparedStatement stmt = null;
        INParameters inParams = new INParameters();
        try {
            StringBuffer buf = new StringBuffer("delete from ATTRIBUTE where ");
            for (int i = 0; i < attr_ids.length; i++) {
                if (i > 0) {
                    buf.append(" or ");
                }
                buf.append("M_ATTRIBUTE_ID=");
                buf.append(inParams.add(attr_ids[i], Types.INTEGER));
            }
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            stmt.executeUpdate();
        } finally {
            SQL.close(stmt);
        }
    }

    /**
     *
     * @param attr_ids
     * @throws Exception
     */
    private void deleteFixedValues(String[] attr_ids) throws Exception {

        if (attr_ids == null || attr_ids.length == 0) {
            return;
        }

        INParameters inParams = new INParameters();
        StringBuffer buf = new StringBuffer();
        buf.append("select distinct FXV_ID from FXV where OWNER_TYPE='attr' and (");
        for (int i = 0; i < attr_ids.length; i++) {
            if (i > 0) {
                buf.append(" or ");
            }
            buf.append("OWNER_ID=");
            buf.append(inParams.add(attr_ids[i], Types.INTEGER));
        }
        buf.append(")");

        ResultSet rs = null;
        PreparedStatement stmt = null;
        Parameters pars = new Parameters();
        try {
            stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
            rs = stmt.executeQuery();
            while (rs.next()) {
                pars.addParameterValue("del_id", rs.getString("FXV_ID"));
            }
        } finally {
            SQL.close(rs);
            SQL.close(stmt);
        }

        if (pars.getSize() > 0) {
            pars.addParameterValue("mode", "delete");
            FixedValuesHandler fvHandler = new FixedValuesHandler(conn, pars, ctx);
            fvHandler.execute();
        }
    }

    /**
     *
     * @throws SQLException
     */
    private void setLastInsertID() throws SQLException {

        String qry = "SELECT LAST_INSERT_ID()";

        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(qry);
            rs.clearWarnings();
            if (rs.next()) {
                lastInsertID = rs.getString(1);
            }
        } finally {
            SQL.close(rs);
            SQL.close(stmt);
        }
    }

    /**
     *
     * @return
     */
    public String getLastInsertID() {
        return lastInsertID;
    }

    /**
     * Construct bit set from "dispWhen" parameters. It is entirely possible to
     * want to create an attribute that won't be displayed if there are plans
     * to display in the future.
     * Note: an earlier version of this method used Math.pow(2, x). That returns a
     * floating point. Integer operation is 1 &lt;&lt; x
     *
     * @return the int-value of the bit-set as string
     */
    private String getDisplayWhen() {

        String[] dispWhen = req.getParameterValues("dispWhen");
        if (dispWhen == null || dispWhen.length == 0) {
            return "0";
        }

        int k = 0;
        for (int i = 0; i < dispWhen.length; i++) {
            Integer weight = (Integer) DElemAttribute.typeWeights.get(dispWhen[i]);
            if (weight != null) {
                k = k + weight.intValue();
            }
        }
        return String.valueOf(k);
    }

    public void setUser(DDUser user) {
        this.user = user;
    }

}
