package com.cairn.ui;

import org.springframework.beans.factory.annotation.Value;

public class Constants {
	@Value("${waypoint.dashboard-api.base-url}")
	static public String api_server;
	
	@Value("${waypoint.authorization-api.base-url}")
	static public String auth_server;
	
	static public final String api_ep_protocol = "/api/protocol";
	static public final String api_me = "/api/oauth/me";
	static public final String api_ep_protocolaccount= "/api/protocol/account/";
	static public final String api_ep_protocoltemplate = "/api/protocol-template";
	static public final String api_ep_protocoltemplateget = "/api/protocol-template/";
	static public final String api_ep_protocolsteptemplate = "/api/protocol-step-template";
	static public final String api_ep_protocolsteptemplate_get = "/api/protocol-step-template/";
	static public final String api_ep_protocolsteptemplate_assign = "/api/protocol-template/";

	static public final String api_userlist_get = "/api/account";
	static public final String api_household = "/api/household";
	static public final String api_household_get ="/api/household/";
	static public final String api_dashboard_get = "/api/dashboard/protocol";
	static public final String api_homeworktemplate = "/api/homework-template";	
	static public final String api_homeworktemplate_get = "/api/homework-template/";	
	static public final String api_homework_response_file = "/api/file/homework-response/";
	static public final String api_homework_question = "/api/homework-question";
	static public final String api_homework_question_get = "/api/homework-question/";
	static public final String api_homework = "/api/homework/";	
}