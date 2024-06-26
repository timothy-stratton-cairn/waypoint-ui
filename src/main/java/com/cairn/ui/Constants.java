package com.cairn.ui;

public class Constants {
	// Dev is at Timothys, Demo is AWS
	// Demo servers
	//Authorization API 18.234.242.104
	//Dashboard API 18.215.187.94
	//UI 3.236.204.96
	static public final String api_server = "http://18.215.187.94:8080";
	static public final String auth_server = "http://18.234.242.104:8080";
	// Dev servers
	//static public final String api_server = "http://96.61.158.12:8083";
	//static public final String auth_server = "http://96.61.158.12:8082";
	//static public final String api_server = "http://127.0.0.1:8083";
	//static public final String auth_server = "http://127.0.0.1:8082";
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