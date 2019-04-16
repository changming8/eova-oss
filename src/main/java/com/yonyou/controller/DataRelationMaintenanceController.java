package com.yonyou.controller;

import com.eova.common.base.BaseController;

public class DataRelationMaintenanceController extends BaseController{

	public void dataRelationMaintenance() {
		render("/eova/dataRelationMaintenance/dataRelationMaintenance.html");
	}
	public void dataFlow() {
		render("/eova/dataflow/dataFlow.html");
	}
}
