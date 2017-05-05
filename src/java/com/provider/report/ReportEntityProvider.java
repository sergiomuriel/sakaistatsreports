package com.provider.report;

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.*;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.util.request.RequestUtils;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.tool.api.Session;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: chand
 * Date: 22/9/13
 * Time: 11:33 AM
 * To change this template use File | Settings | File Templates.
 */

public class ReportEntityProvider implements IReportEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider, ActionsExecutable, Outputable, RequestAware {

    static {
        RequestUtils.formatToMimeType.put("xls", "application/xls");
        RequestUtils.formatToMimeType.put("csv", "application/csv");
        RequestUtils.mimeTypeToFormat.put("application/xls", "xls");
        RequestUtils.mimeTypeToFormat.put("application/csv", "csv");
    }

    public void init() {
        functionManager.registerFunction(TOOL_PERM_NAME);
    }
    
    private ReportManager reportManager;
    private transient SiteService siteService;
    private RequestGetter requestGetter;
    private FunctionManager functionManager;
    

    public ReportManager getReportManager() {
        return reportManager;
    }

    public void setReportManager(ReportManager reportManager) {
        this.reportManager = reportManager;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    @Override
    public boolean entityExists(String id) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public String getEntityPrefix() {
        return ENTITY_PREFIX;
    }

    @EntityCustomAction(action = "report", viewKey = EntityView.VIEW_LIST)
    public Object getReport(OutputStream out, Map map) {

        String path = (String) map.get("pathInfo");
        if (path == null) {
            throw new EntityException("Not able to parse sitename, reportname and format", path, 501);
        }

        Session session = (Session) map.get("sakai.session");
        if (session == null) {
            throw new EntityException("Invalid session", path, 406);
        }

        if (session.getUserId() == null) {
            throw new EntityException("No authentication is provided", path, 401);
        }

        String format = "xml";
        if (path.contains(".")) {
            String path1 = path.toLowerCase();
            if (path1.endsWith(".xml")) {
                format = "xml";
            } else if (path1.endsWith(".csv")) {
                format = "csv";
            } else if (path1.endsWith(".xls")) {
                format = "xls";
            } else if (path1.endsWith(".json")) {
                format = "json";
            } else {
                throw new EntityException("Invalid format requested. Formats supported are xml, csv, xls and json", path, 406);
            }
        }

        path = path.replaceAll("reports/report/", "");

        if (path.startsWith("/"))
            path = path.substring(1);
        String[] paths = path.split("/");

        if (paths.length != 2) {
            throw new EntityException("Not able to parse sitename, reportname and format", path, 501);
        }

        if (paths[1].contains(".")) {
            paths[1] = paths[1].substring(0, paths[1].length() - format.length() - 1);
        }


        if (format == null || format.trim().length() == 0)
            format = "xml";
        else
            format = format.toLowerCase();

        if (!("xml".equals(format) || "json".equals(format) || "xls".equals(format) || "csv".equals(format)))
            throw new EntityException("Invalid format requested. Formats supported are xml, csv, xls and json", path, 406);

        Map headers = new HashMap();

        if (requestGetter != null) {
            requestGetter.getResponse().setHeader("Content-Type", "application/" + format);
        }

        try {
            Object o = execute(paths[0], paths[1], format, session.getUserId(), path);
            if (o instanceof String) {
                if ("csv".equalsIgnoreCase(format)) {
                    headers.put("Content-Disposition", "attachment; filename=\"" + paths[1] + ".csv\"");
                    if (requestGetter != null) {
                        requestGetter.getResponse().setHeader("Content-Disposition", "attachment; filename=\"" + paths[1] + ".csv\"");
                    }
                }
                ActionReturn action = new ActionReturn(System.getProperty("file.encoding", "utf-8"), "application/" + format, (String) o);
                action.setHeaders(headers);
                action.setFormat(format);
                return action;
            } else {
                out.write((byte[]) o);
                headers.put("Content-Disposition", "attachment; filename=\"" + paths[1] + ".xls\"");
                if (requestGetter != null) {
                    requestGetter.getResponse().setHeader("Content-Disposition", "attachment; filename=\"" + paths[1] + ".csv\"");
                }
                ActionReturn action = new ActionReturn(System.getProperty("file.encoding", "utf-8"), "application/xls", out);
                action.setFormat(format);

                action.setHeaders(headers);

                return action;
            }
        } catch (Exception e) {
            if (e instanceof EntityException) {
                throw (EntityException) e;
            }
            throw new EntityException(e.getLocalizedMessage(), path, 500);
        }

    }


    public Object execute(String siteName, String reportName, String format, String user, String path) throws Exception {

        if (siteName == null) {
            throw new IllegalArgumentException("Invalid site title");
        }

        Site site = getSite(siteName);

        if (site == null) {
            throw new IllegalArgumentException("No site found with site title : " + siteName);
        }

        org.sakaiproject.authz.api.Member member = site.getMember(user);

        if (member == null || member.getRole() == null || !member.getRole().isAllowed("reports.view")) {
            throw new EntityException("User is not allowed to view the requested report", path, 401);
        }

        String siteID = site.getId();


        List<ReportDef> reportDefs = reportManager.getReportDefinitions(siteID, true, true);

        if (reportDefs == null || reportDefs.size() == 0)
            throw new IllegalArgumentException("No reports exists with site : " + siteName);

        for (ReportDef reportDef : reportDefs) {

            if (reportDef.getTitle() == null) {
                continue;
            }

            if (reportDef.getTitle().equalsIgnoreCase(reportName)) {
                return getReport(reportDef, format);
            }
        }

        throw new IllegalArgumentException("Report " + reportName + " doesn't exists with site : " + siteName);
    }

    private Object getReport(ReportDef reportDef, String format) throws Exception {

        Report report = reportManager.getReport(reportDef, true);

        String reportCSV = reportManager.getReportAsCsv(report);

        if (reportCSV == null || reportCSV.trim().length() == 0)
            return "";

        if ("csv".equalsIgnoreCase(format))
            return reportCSV;
        else if ("xml".equalsIgnoreCase(format)) {
            try {
                return Utils.xmlFromCSV(reportCSV);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        } else if ("json".equalsIgnoreCase(format)) {
            try {
                return Utils.jsonFromCSV(reportCSV);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        } else {
            return reportManager.getReportAsExcel(report, reportDef.getTitle() == null ? "sheet1" : reportDef.getTitle());
        }

    }


    private Site getSite(String siteName) {
        List<Site> sites = siteService.getSites(SiteService.SelectionType.ANY, null, null, null, SiteService.SortType.NONE, null);
        for (Site site : sites) {
            String title = site.getTitle();
            if (siteName.equalsIgnoreCase(title)) {
                return site;
            }

        }
        return null;

    }

    @Override
    public String[] getHandledOutputFormats() {
        return new String[]{"xml", "json", "csv", "xls"};
    }

    @Override
    public void setRequestGetter(RequestGetter requestGetter) {
        this.requestGetter = requestGetter;
    }

    public void setFunctionManager(FunctionManager functionManager) {
        this.functionManager = functionManager;
    }
    
}