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
package com.exilant.exility.dataservice;

import com.exilant.exility.common.*;

public class SQLTemplate{
	
	protected String id;
	protected String template;
	protected boolean addColumnHeading = false;
	public SQLParameter[] parameters;
	
	public SQLTemplate()
	{
		super();
	}
	
/*
 * Generates a SQL statement based on this template and values for paramaters provided in DC
 */

	public String getSQL(DataCollection dc) throws TaskFailedException
	{
		StringBuffer sql = new StringBuffer(this.template);
		if (this.parameters == null) return sql.toString();

		int paramCount = 0; //tracks howmany values were actually specified

		for(int k = 0; k < this.parameters.length; k++)
		{	
			SQLParameter parameterName  = parameters[k];

			// value for this parameter is in DC with name= dataSource
			String val = dc.getValue(parameterName.dataSource);

			//if value is not there, try the default value specified in Paramater
			if(null == val || val.length() == 0) val =  parameterName.defaultValue;

			if(val != null && val.length() > 0) paramCount ++;
			//if it has to be replaced with a list (like 'a','b' etc..
			if (parameterName.isList)
			{
				String[] vals;
				vals = dc.getValueList(parameterName.dataSource);

				if (null == vals || vals.length == 0) //list not found
				{
					if (null == val || val.length() == 0)// value also was not found, nor was there a default
					{
						if (parameterName.isRequired)//and this was a must-find..
						{
							dc.addMessage("exilNoValueForParameter",parameterName.dataSource, this.id);
							throw new TaskFailedException();
						}
						continue; // try the next parameter
					}// no list, but a single value. Push that to the array
					vals = new String[1];
					vals[0] = val;
				}

				paramCount++; // may be we are double counting,.. but it is OK
				val = ""; //build val as a list of values
				String prefix = ""; //small trick to handle the comma for values other than first
				for (int i=0; i<vals.length; i++)
				{
					if (parameterName.listRequiresQuotes) val = val + prefix + "'" + vals[i] + "'";
					else val = val + prefix + vals[i];
					prefix = ",";
				}
			}
			else // it is a single (non-list) parameter
			{
				if ( (null == val || val.length() == 0) && parameterName.isRequired)
				{ 
					dc.addMessage("exilNoValueForParameter",parameterName.dataSource);
					throw new TaskFailedException();
				}
				
			}
			substitute(k+1,sql,val, parameterName.isRequired, dc); //substitute @(k+1) with val
		}// end of for loop
		//was there any parameter found at all?
		boolean removeText = false;
		if (paramCount == 0) {
			removeText =  true;// no params found. Check if we have to remove any ....{.....}....
			removeBraces(sql, dc, removeText);
		}
		return sql.toString();
	}

/*
 * Substitutes .... { ...@1...} type of stub with value.
 * If value is specified, it replaces @i with the value, and removes { and }
 * if value is not specified, it removes the substring between the brackets, including the brackets 
 */
	private void substitute(int paramNumber, 
			StringBuffer sbf, 
			String value, 
			boolean isRequired,
			DataCollection dc)throws TaskFailedException
	{
		int paramAt = sbf.indexOf("@" + (paramNumber));
		int paramSize=String.valueOf(paramNumber).length();
		if (paramAt < 0)
		{
			dc.addMessage("exilParameterMismatch", this.id, Integer.valueOf(paramNumber).toString());
			throw new TaskFailedException(); 
		}
		
		//If it is a must enter, we just have to replace teh value. No Braces business
		if (isRequired )
		{
			sbf.replace(paramAt,paramAt+paramSize+1,value);
			return;
		}

		int openBracketAt = sbf.lastIndexOf("{",paramAt);
		int closeBracketAt = sbf.indexOf("}",paramAt);
		
		if(openBracketAt < 0 || closeBracketAt < 0) //at least one brace not found..
		{
			dc.addMessage("exilBracesNotFound", this.id, Integer.valueOf(paramNumber).toString());
			throw new TaskFailedException(); 
		}

		if (value == null || value.length() == 0)
		{ // paramater not supplied
			sbf.replace(openBracketAt,closeBracketAt+1,""); //remove all that
			return;
		}

		sbf.deleteCharAt(openBracketAt); 
		sbf.deleteCharAt(closeBracketAt-1); 
		sbf.replace(paramAt-1,paramAt+paramSize,value); //one character removed before this..
	}

	private void removeBraces(StringBuffer sql, 
			DataCollection dc,
			boolean removeText) throws TaskFailedException
	{
		int openBracketAt = sql.lastIndexOf("{");
		int closeBracketAt = sql.indexOf("}");

		if(openBracketAt < 0 && closeBracketAt < 0) return; // no work

		if (openBracketAt < 0 || closeBracketAt < 0 || openBracketAt > closeBracketAt)
		{
			dc.addMessage("exilUnmatchedBraces", this.id );
			throw new TaskFailedException(); 
		}
		
		if(removeText){
			int numberOfCharactersToRemove = closeBracketAt - openBracketAt + 1;
			sql.delete(openBracketAt,numberOfCharactersToRemove);
			
		}
		else
		{
			sql.deleteCharAt(closeBracketAt);
			sql.deleteCharAt(openBracketAt);
		}

		return ;
	}

}
