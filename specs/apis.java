/**
 * @author myzone
 */

interface PestControlEnterprise {

	/*
	 * Workers are stored in DB, admins entries are stored separetly in configs 
	 */
	Optional<User> getUser(String userName); 

	EquipmentType getEquipment(PestType pestType);

	Optional<Address> getAddress(String textAddress);

}

interface User {

	String getName();

	UserSession beginSession(String password) throws AuthException, IllegalStateException;

}

interface Admin extends User {

	@Override
	AdminsSession beginSession(String password) throws AuthException, IllegalStateException;

}

interface Worker extends User {

	@Override
	WorkerSession beginSession(String password) throws AuthException, IllegalStateException;

}

interface UserSession extends AutoClosable {

	User getUser();

}

interface WorkerSession extends UserSession {

	@Override
	Worker getUser();

	/**
	 * Returns set of tasks which could be procced by current worker 
	 */ 
	ImmutableSet<Task> getOpenTasks();

	/**
	 * Returns set of tasks which are assigned to current worker 
	 */ 
	ImmutableSet<Task> getAssignedTasks();

	/**
	 * Returns set of tasks which are currenly in progress by current worker 
	 */ 
	ImmutableSet<Task> getCurrentTasks();


	/**
	 * Sets task's status to ASSIGNED
	 */
	void assignTask(Task task, String comment) throws IllegalStateException;

	/**
	 * Sets task's status to OPEN
	 */
	void discardTask(Task task, String comment) throws IllegalStateException;

	/**
	 * Sets task's status to IN_PROGRESS
	 */
	void startTask(Task task, String comment) throws IllegalStateException;

	/**
	 * Sets task's status to RESOLVED
	 */
	void finishTask(Task task, String comment) throws IllegalStateException;

}

interface AdminsSession extends UserSession {

	@Override
	Admin getUser();

	Task allocateTask(
		Status status,
		Optional<Worker> worker,
		ImmutableSet<Period> availabilityTime,
		Consumer consumer,
		PestType pestType,
		String problemDescription,
		String comment
	);

	Task editTask(
		Task task,
		Optional<Worker> worker,
		Status status,
		ImmutableSet<Period> availabilityTime,
		Consumer consumer,
		PestType pestType,
		String problemDescription,
		String comment
	);

	void closeTaks(Task task, String comment);

	/**
	 * TODO: consider about filters
	 */
	ImmutableSet<Task> getTasks(Filter filter);

	Consumer createConsumer(
		String name,
		Address address,
		String cellPhone,
		String email
	);

	Consumer createConsumer(
		Consumer consumer,
		String name,
		Address address,
		String cellPhone,
		String email
	);

	Optional<Consumer> getConsumer(String name);

}

interface Task {

	Status getStatus();

	Optional<Worker> getCurrentWorker();

	ImmutableSet<Period> getAvailabilityTime();

	Consumer getConsumer();

	PestType getPestType();

	String getProblemDescription();

	ImmutableList<TaskHistoryEntry> getTaskHistory();

	enum Status {
		OPEN, 
		ASSIGNED, 
		IN_PROGRESS, 
		RESOLVED, 
		CLOSED
	}

	interface TaskHistoryEntry {

		long getTimeStamp();

		User getCauser();

		String getComment();

	}

	interface StatusChangeHistoryEntry extends TaskHistoryEntry {

		Status getOldStatus();

		Status getNewStatus();

	}

	interface DataChangeHistoryEntry extends TaskHistoryEntry {

	}

}

/**
 * The end-user, the persons who order the pest control service
 */
interface Consumer {
	
	String getName();
	
	Address getAddress();

	String getCellPhone();

	String getEmail();

}