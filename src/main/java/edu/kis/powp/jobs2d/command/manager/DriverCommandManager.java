package edu.kis.powp.jobs2d.command.manager;

import edu.kis.powp.jobs2d.command.DefaultCompoundCommand;
import edu.kis.powp.jobs2d.command.DriverCommand;
import edu.kis.powp.jobs2d.command.ICompoundCommand;
import edu.kis.powp.jobs2d.command.analyzer.ICommandUsageAnalyzer;
import edu.kis.powp.jobs2d.drivers.DriverManager;
import edu.kis.powp.jobs2d.features.CommandFactory;
import edu.kis.powp.observer.Publisher;
import edu.kis.powp.observer.Subscriber;

import java.util.ArrayList;
import java.util.List;

/**
 * Driver command Manager.
 */
public class DriverCommandManager {
	private DriverCommand currentCommand = null;

	private Publisher changePublisher = new Publisher();
	private DriverManager driverManager;
	private List<Subscriber> cache = null;
	private ICommandUsageAnalyzer analyzer = null;

	/**
	 * Constructor of driver command manager, I had to choose whether keep accessing static method of DriverFeature or assign reference only one time
	 * @param driverManager
	 */
	public DriverCommandManager(DriverManager driverManager) {
		this.driverManager = driverManager;
	}

	/**
	 * Sets current command.
	 *
	 * @param commandList list of commands representing a compound command.
	 * @param name        name of the command.
	 */
	public synchronized void setCurrentCommand(List<DriverCommand> commandList, String name) {
		setCurrentCommand(new DefaultCompoundCommand(commandList, name));
	}

	/**
	 * Returns current command.
	 *
	 * @return Current command.
	 */
	public synchronized DriverCommand getCurrentCommand() {
		return currentCommand;
	}

	/**
	 * Sets current command.
	 *
	 * @param commandList Set the command as current.
	 */
	public synchronized void setCurrentCommand(DriverCommand commandList) {
		this.currentCommand = commandList;
		changePublisher.notifyObservers();
	}


	public synchronized  String getStatistics(){
		return analyzer.exportStatistics();
	}

	public synchronized void clearCurrentCommand() {
		currentCommand = null;
	}

	public synchronized String getCurrentCommandString() {
		if (getCurrentCommand() == null) {
			return "No command loaded";
		} else
			return getCurrentCommand().toString();
	}

	/**
	 * Runs current command, if set.
	 *
	 */

	public synchronized void runCurrentCommand(){
		if(currentCommand != null){
			currentCommand.execute(driverManager.getCurrentDriver());
		}
	}

	/**
	 * Deletes observers and move observers collection to cache
	 */

	public synchronized void deleteCurrentObservers(){

		if(changePublisher.getSubscribers().size() > 0){
			cache = new ArrayList<>(changePublisher.getSubscribers());
		}

		changePublisher.clearObservers();
	}

	/**
	 * Restores observers from cache
	 * NOTE:
	 * It's probable if addObserver/s feature would be implemented, the cache should be empty
	 */

	public synchronized void resetObservers(){
		addObservers(cache);
	}

	/**
	 * Clears any stored cache in DriverCommandManager.
	 */
	public synchronized void clearCache(){
		if(cache != null && cache.size() > 0){
			cache.clear();
			cache = null;
		}
	}

	/**
	 * Add observers to existing collection of subscribers. Clears cache.
	 * @param subscribers list to append.
	 */
	public synchronized void addObservers(List<Subscriber> subscribers){
		if(subscribers != null){

			subscribers.forEach(subscriber -> {
				if(subscriber != null){
					changePublisher.addSubscriber(subscriber);
				}
			});

			clearCache();
		}
	}

	public Publisher getChangePublisher() {
		return changePublisher;
	}

	public void setAnalyzer(ICommandUsageAnalyzer analyzer) {
		this.analyzer = analyzer;
	}
}
