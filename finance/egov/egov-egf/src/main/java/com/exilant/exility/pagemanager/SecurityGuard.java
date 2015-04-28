/*******************************************************************************
 * eGov suite of products aim to improve the internal efficiency,transparency, 
 *    accountability and the service delivery of the government  organizations.
 * 
 *     Copyright (C) <2015>  eGovernments Foundation
 * 
 *     The updated version of eGov suite of products as by eGovernments Foundation 
 *     is available at http://www.egovernments.org
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or 
 *     http://www.gnu.org/licenses/gpl.html .
 * 
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 * 
 * 	1) All versions of this program, verbatim or modified must carry this 
 * 	   Legal Notice.
 * 
 * 	2) Any misrepresentation of the origin of the material is prohibited. It 
 * 	   is required that all modified versions of this material be marked in 
 * 	   reasonable ways as different from the original version.
 * 
 * 	3) This license does not grant any rights to any user of the program 
 * 	   with regards to rights under trademark law for use of the trade names 
 * 	   or trademarks of eGovernments Foundation.
 * 
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 ******************************************************************************/
package com.exilant.exility.pagemanager;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.exilant.eGov.src.domain.User;
import com.exilant.exility.common.DataCollection;
import com.exilant.exility.common.TaskFailedException;

public class SecurityGuard {

	private static final Logger LOGGER = Logger.getLogger(SecurityGuard.class);
	public SecurityGuard() {
		super();
	}

	public boolean clearedSecurity(DataCollection dc, HttpServletRequest request)
	{
		//request.getAttribute("UserID");
		try{
		if (request.getUserPrincipal() != null)
		{
			String principalName = request.getUserPrincipal().getName();
			int userLen = principalName.indexOf("<:1>");
			if(userLen > -1)
			{
				principalName = principalName.substring(0,userLen);
			}

			//This fix is for Phoenix Migration.
			/*dc.addValue("current_loginName",principalName);
			if (requestHibernateUtil.getCurrentSession().getAttribute("current_UserID") == null)
			{
				if(LOGGER.isDebugEnabled())     LOGGER.debug("getting $$$UsesrID$$$ = " );
				User aUser = new User(principalName);
				if(LOGGER.isDebugEnabled())     LOGGER.debug(" got " + aUser.getId());
				requestHibernateUtil.getCurrentSession().setAttribute("current_UserID",String.valueOf(aUser.getId()));
				requestHibernateUtil.getCurrentSession().setAttribute("current_loginName",principalName);
			}
			dc.addValue("current_UserID",requestHibernateUtil.getCurrentSession().getAttribute("current_UserID").toString());
			dc.addValue("egUser_id",requestHibernateUtil.getCurrentSession().getAttribute("current_UserID").toString());
			if(LOGGER.isDebugEnabled())     LOGGER.debug("$$$UsesrID$$$ = " + requestHibernateUtil.getCurrentSession().getAttribute("current_UserID"));*/
		}
		else
		{
			dc.addValue("current_loginName","");
		}
		}
		catch (Exception te){
			if(LOGGER.isDebugEnabled())     LOGGER.debug("Exp="+te.getMessage());
			//dc.addMessage();
		}
		return true;
	}

}
