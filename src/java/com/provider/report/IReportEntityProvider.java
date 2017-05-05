package com.provider.report;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * Created with IntelliJ IDEA.
 * User: chand
 * Date: 27/9/13
 * Time: 10:57 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IReportEntityProvider extends EntityProvider {

    public final static String ENTITY_PREFIX = "reports";
    
    public final static String TOOL_PERM_NAME = "reports.view";
    
}
