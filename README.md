# sakaistatsreports
Sakai Statistics Reports



Project Description

Sakai Stats Reports is a RESTful web service that delivers Statistics Tool reports on demand via HTTP. It can be called from reporting  platforms like JasperReports to give it a nice user interface so that faculty and other stakeholders can benefit from the reports.

Syntax:
http://[sakai.server]/direct/reports/report/[site%20title]/[report%20name].[valid%20extension]
•	The report name must match the previously generated Stats report name.
•	Blanks in either site title or report name are encoded using %20.
•	Formats supported are xml, csv, xls and json. 



Installation
1.	Copy all the content of install/tomcat/ into CATALINA_HOME.
2.	Restart Tomcat.



Implementation

This web service was initially created in 2013, but later in 2016 it presented this error:

After logging in to Sakai, having a valid URL and a report previously generated for the specific course site, the web service is throwing:
"User is not allowed to view the requested report." 
The cause was that the necessary permission could not be set from the admin settings page for a specific role. It just did not appear in there.

Several resources on the internet were looked at to solve this problem. These were the most useful:
•	Sakai function keys list: [https://gist.github.com/zathomas/7ca8e9b83932a99deff1](https://gist.github.com/zathomas/7ca8e9b83932a99deff1)

•	"reports.view" permission question in the Sakai forum: [https://groups.google.com/a/apereo.org/forum/#!topic/sakai-dev/BY0lBKm2I60](https://groups.google.com/a/apereo.org/forum/#!topic/sakai-dev/BY0lBKm2I60)

•	How to define an entity provider: [https://confluence.sakaiproject.org/display/SAKDEV/Defining+EntityProviders](https://confluence.sakaiproject.org/display/SAKDEV/Defining+EntityProviders)

•	How to register a new permission (Authz) from a Sakai tool: [https://confluence.sakaiproject.org/display/BOOT/Using+the+FunctionManager+Service](https://confluence.sakaiproject.org/display/BOOT/Using+the+FunctionManager+Service)

 

 
Configuration

These configuration steps are meant for every site. Screenshots are from Sakai 11.
1.	Go to the site realm:
 ![config01](https://github.com/sergiomuriel/sakaistatsreports/tree/master/doc/img/config01.png)
 
2.	Click on Instructor role:
 ![config02](https://github.com/sergiomuriel/sakaistatsreports/tree/master/doc/img/config02.png)

3.	Select "reports.view" permission:
 ![config03](https://github.com/sergiomuriel/sakaistatsreports/tree/master/doc/img/config03.png)

4.	Save changes.
 ![config04](https://github.com/sergiomuriel/sakaistatsreports/tree/master/doc/img/config04.png)
 
 
 
Testing 
1.	Go to the site page and click on Statistics:
 ![test01](https://github.com/sergiomuriel/sakaistatsreports/tree/master/doc/img/test01.png)

2.	Click on REPORTS tab:
 ![test02](https://github.com/sergiomuriel/sakaistatsreports/tree/master/doc/img/test02.png)

3.	Create a new report by clicking on the Add link:
 ![test03](https://github.com/sergiomuriel/sakaistatsreports/tree/master/doc/img/test03.png)

There are many different ways to create these reports. Here is an example: [https://youtu.be/kvnt6Nk77-o](https://youtu.be/kvnt6Nk77-o)

4.	Go to the report via the URL in the same way it was explained above in the Project Description section. Make sure that the report name in the URL matches the name that you give to the report in the previous step. For this particular example it would be:
http://[server]/direct/reports/report/[site%20id]/[faculty%20forum].valid%20extension


