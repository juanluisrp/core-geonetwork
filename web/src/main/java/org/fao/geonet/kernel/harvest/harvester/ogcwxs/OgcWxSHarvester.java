//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.harvest.harvester.ogcwxs;

import jeeves.exceptions.BadInputEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.server.resources.ResourceManager;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.resources.Resources;
import org.jdom.Element;

import java.io.File;
import java.sql.SQLException;
import java.util.UUID;

//=============================================================================

public class OgcWxSHarvester extends AbstractHarvester
{
	//--------------------------------------------------------------------------
	//---
	//--- Static init
	//---
	//--------------------------------------------------------------------------

	public static void init(ServiceContext context) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Harvesting type
	//---
	//--------------------------------------------------------------------------

	public String getType() { return "ogcwxs"; }

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	protected void doInit(Element node) throws BadInputEx
	{
		params = new OgcWxSParams(dataMan);
        super.setParams(params);

        params.create(node);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Add
	//---
	//---------------------------------------------------------------------------

	protected String doAdd(Dbms dbms, Element node) throws BadInputEx, SQLException
	{
		params = new OgcWxSParams(dataMan);
        super.setParams(params);

		//--- retrieve/initialize information
		params.create(node);

		//--- force the creation of a new uuid
		params.uuid = UUID.randomUUID().toString();

		String id = settingMan.add(dbms, "harvesting", "node", getType());

		storeNode(dbms, params, "id:"+id);
		Lib.sources.update(dbms, params.uuid, params.name, true);
		Resources.copyLogo(context, "images" + File.separator + "harvesting" + File.separator + params.icon, params.uuid);
        
		return id;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Update
	//---
	//---------------------------------------------------------------------------

	protected void doUpdate(Dbms dbms, String id, Element node)
									throws BadInputEx, SQLException
	{
		OgcWxSParams copy = params.copy();

		//--- update variables
		copy.update(node);

		String path = "harvesting/id:"+ id;

		settingMan.removeChildren(dbms, path);

		//--- update database
		storeNode(dbms, copy, path);

		//--- we update a copy first because if there is an exception Params
		//--- could be half updated and so it could be in an inconsistent state

		Lib.sources.update(dbms, copy.uuid, copy.name, true);
		Resources.copyLogo(context, "images" + File.separator + "harvesting" + File.separator + copy.icon, copy.uuid);
		
		params = copy;
        super.setParams(params);
	}

	//---------------------------------------------------------------------------

	protected void storeNodeExtra(Dbms dbms, AbstractParams p, String path,
											String siteId, String optionsId) throws SQLException
	{
		OgcWxSParams params = (OgcWxSParams) p;
        super.setParams(params);

        settingMan.add(dbms, "id:"+siteId, "url",  params.url);
		settingMan.add(dbms, "id:"+siteId, "icon", params.icon);
		settingMan.add(dbms, "id:"+siteId, "ogctype", params.ogctype);
		settingMan.add(dbms, "id:"+optionsId, "lang",  params.lang);
		settingMan.add(dbms, "id:"+optionsId, "topic",  params.topic);
		settingMan.add(dbms, "id:"+optionsId, "createThumbnails",  params.createThumbnails);
		settingMan.add(dbms, "id:"+optionsId, "useLayer",  params.useLayer);
		settingMan.add(dbms, "id:"+optionsId, "useLayerMd",  params.useLayerMd);
		settingMan.add(dbms, "id:"+optionsId, "datasetCategory",  params.datasetCategory);
		settingMan.add(dbms, "id:"+optionsId, "outputSchema",  params.outputSchema);
	}

	//---------------------------------------------------------------------------
	//---
	//--- AddInfo
	//---
	//---------------------------------------------------------------------------

	protected void doAddInfo(Element node)
	{
		//--- if the harvesting is not started yet, we don't have any info

		if (result == null)
			return;

		//--- ok, add proper info

		Element info = node.getChild("info");
		Element res  = getResult();
		info.addContent(res);
	}

	//---------------------------------------------------------------------------
	//---
	//--- GetResult
	//---
	//---------------------------------------------------------------------------

	protected Element getResult() {
		Element res  = new Element("result");
		if (result != null) {
			add(res, "total",          		result.total);
			add(res, "added",          		result.added);
			add(res, "layer",          		result.layer);
			add(res, "layerUuidExist",		result.layerUuidExist);
			add(res, "layerUsingMdUrl",		result.layerUsingMdUrl);
			add(res, "unknownSchema",  		result.unknownSchema);
			add(res, "removed",        		result.locallyRemoved);
			add(res, "unretrievable",  		result.unretrievable);
			add(res, "badFormat",      		result.badFormat);
			add(res, "doesNotValidate",		result.doesNotValidate);
			add(res, "thumbnails",     		result.thumbnails);
			add(res, "thumbnailsFailed",	result.thumbnailsFailed);
		}
		return res;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Harvest
	//---
	//---------------------------------------------------------------------------

	protected void doHarvest(Logger log, ResourceManager rm) throws Exception
	{
		Dbms dbms = (Dbms) rm.open(Geonet.Res.MAIN_DB);

		Harvester h = new Harvester(log, context, dbms, params);
		result = h.harvest();
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private OgcWxSParams params;
	private OgcWxSResult result;
}

//=============================================================================

class OgcWxSResult
{
	public int total;			// = md for data and service (ie. data + 1)
	public int added;			// = total
	public int layer;			// = md for data
	public int layerUuidExist;	// = uuid already in catalogue
	public int layerUsingMdUrl;	// = md for data using metadata URL document if ok
	public int locallyRemoved;	// = md removed
	public int unknownSchema;	// = md with unknown schema (should be 0 if no layer loaded using md url)
	public int unretrievable;	// = http connection failed
	public int badFormat;		// 
	public int doesNotValidate;	// = 0 cos' not validated
	public int thumbnails;		// = number of thumbnail generated
	public int thumbnailsFailed;// = number of thumbnail creation which failed
}