package es.um.asio.service.notification.service;

public interface NotificationService {

	Boolean notificationETL(String event);

	void stopPojoGeneralListener();
	
	void startPojoGeneralLinkListener();
	
	void stopPojoGeneralLinkListener();
	
	void startDiscoveryLinkListener();
	
	void stopDiscoveryLinkListener();
	
    Boolean isRunningQueues();
}
