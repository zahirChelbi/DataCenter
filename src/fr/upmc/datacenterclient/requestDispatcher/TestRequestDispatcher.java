/**
 * 
 */
package fr.upmc.datacenterclient.requestDispatcher;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.datacenter.hardware.computers.Computer;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.hardware.processors.Processor;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.datacenterclient.requestDispatcher.components.RequestDispatcher;
import fr.upmc.datacenterclient.requestDispatcher.connectors.RequestDispatcherManagerConnector;
import fr.upmc.datacenterclient.requestDispatcher.ports.RequestDispatcherManagerOutboundPort;
import fr.upmc.datacenterclient.requestgenerator.RequestGenerator;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;

/**
 * @authors zahir CHELBI, hacene KEDJAR, lyes CHELFI
 *
 */
public class TestRequestDispatcher extends AbstractCVM {

	public static final String	ComputerServicesInboundPortURI 				= "cs-ibp" ;
	public static final String	ComputerServicesOutboundPortURI 			= "cs-obp" ;
	public static final String	ComputerStaticStateDataInboundPortURI 		= "css-dip" ;
	public static final String	ComputerDynamicStateDataInboundPortURI 		= "cds-dip" ;

	/**		 uri des ports du dispatcher 		*/
	public static final String	RepartiteurSubmissionInboundPortURI 		= "rpsibp" ;
	public static final String	RepartiteurSubmissionOutboundPortURI 		= "rpsobp" ;
	public static final String  RepartiteurNotificationInboundPortURI		= "rpnip";
	public static final String	RepartiteurNotificationOutboundPortURI 		= "rpnobp" ;
	public static final String	RepartiteurRequestManagementInboundPortURI 	= "rprmip" ;
	public static final String	RepartiteurRequestManagementOutboundPortURI = "rprmop" ;
	public static final String  RequestDispatcherDynamiceDataInboundPort 	= "rdddip" ;

	/**		 uri des ports de la vm 0 		*/
	public static final String	RequestSubmissionOutboundPortURI 			= "rsobp" ;
	public static final String	RequestNotificationInboundPortURI 			= "rnibp" ;
	public static final String	RequestGeneratorManagementInboundPortURI 	= "rgmip" ;
	public static final String	RequestGeneratorManagementOutboundPortURI 	= "rgmop" ;

	/**		 uri des ports de la vm 1 		*/
	public static final String	RequestSubmissionInboundPortURI1 			= "rsibp1" ;
	public static final String	RequestNotificationOutboundPortURI1 		= "rnobp1" ;
	public static final String	ApplicationVMManagementInboundPortURI1 		= "avm-ibp1" ;
	public static final String	ApplicationVMManagementOutboundPortURI1 	= "avm-obp1" ;

	/**		 uri des ports de la vm 2 		*/
	public static final String	RequestSubmissionInboundPortURI2 			= "rsibp2" ;
	public static final String	RequestNotificationOutboundPortURI2 		= "rnobp2" ;
	public static final String	ApplicationVMManagementInboundPortURI2 		= "avm-ibp2" ;
	public static final String	ApplicationVMManagementOutboundPortURI2		= "avm-obp2" ;

	/**		 uri des ports de la vm 3		*/
	public static final String	RequestSubmissionInboundPortURI3 			= "rsibp3" ;
	public static final String	RequestNotificationOutboundPortURI3			= "rnobp3" ;
	public static final String	ApplicationVMManagementInboundPortURI3 		= "avm-ibp3" ;
	public static final String	ApplicationVMManagementOutboundPortURI3 	= "avm-obp3" ;

	/**  	 Port de management des VMs 					*/
	protected ApplicationVMManagementOutboundPort		avmPort1 ;
	protected ApplicationVMManagementOutboundPort		avmPort2 ;
	protected ApplicationVMManagementOutboundPort		avmPort3 ;


	/** Port connected to the computer component to access its services.	*/
	protected ComputerServicesOutboundPort				csPort ;
	protected RequestDispatcherManagerOutboundPort 		rprmop ;

	/** Port of the request generator component sending requests to the
	 *  AVM component.														*/
	protected RequestSubmissionOutboundPort				rsobp ;
	/** Port of the request generator component used to receive end of
	 *  execution notifications from the AVM component.						*/
	protected RequestNotificationOutboundPort			nobp ;
	/** Port connected to the request generator component to manage its
	 *  execution (starting and stopping the request generation).			*/
	protected RequestGeneratorManagementOutboundPort	rgmop ;


	protected ApplicationVM								 mv1;
	protected ApplicationVM 							 mv2;
	protected ApplicationVM 							 mv3;

	public TestRequestDispatcher()
			throws Exception {
		super();
	}

	@Override
	public void deploy() throws Exception 
	{
		Processor.DEBUG = true ;

		// --------------------------------------------------------------------
		// Create and deploy a computer component with its 2 processors and
		// each with 2 cores.
		// --------------------------------------------------------------------
		String computerURI = "computer0" ;
		int numberOfProcessors = 4 ;
		int numberOfCores = 4 ;
		Set<Integer> admissibleFrequencies = new HashSet<Integer>() ;
		admissibleFrequencies.add(1500) ;	// Cores can run at 1,5 GHz
		admissibleFrequencies.add(3000) ;	// and at 3 GHz
		Map<Integer,Integer> processingPower = new HashMap<Integer,Integer>() ;
		processingPower.put(1500, 1500000) ;	// 1,5 GHz executes 1,5 Mips
		processingPower.put(3000, 3000000) ;	// 3 GHz executes 3 Mips

		Computer c = new Computer(
				computerURI,
				admissibleFrequencies,
				processingPower,  
				1500,		// Test scenario 1, frequency = 1,5 GHz
				// 3000,	// Test scenario 2, frequency = 3 GHz
				1500,		// max frequency gap within a processor
				numberOfProcessors,
				numberOfCores,
				ComputerServicesInboundPortURI,
				ComputerStaticStateDataInboundPortURI,
				ComputerDynamicStateDataInboundPortURI) ;
		this.addDeployedComponent(c) ;

		// Create a mock-up computer services port to later allocate its cores
		// to the application virtual machine.
		this.csPort = new ComputerServicesOutboundPort(
				ComputerServicesOutboundPortURI,
				new AbstractComponent() {}) ;
		this.csPort.publishPort() ;
		this.csPort.doConnection(
				ComputerServicesInboundPortURI,
				ComputerServicesConnector.class.getCanonicalName()) ;

		// instanciation de la vm 1
		mv1 =new ApplicationVM("vm1",	
				ApplicationVMManagementInboundPortURI1,
				RequestSubmissionInboundPortURI1,
				RequestNotificationOutboundPortURI1) ;
		this.addDeployedComponent(mv1) ;

		this.avmPort1 = new ApplicationVMManagementOutboundPort(
				ApplicationVMManagementOutboundPortURI1,
				new AbstractComponent() {}) ;
		this.avmPort1.publishPort() ;
		this.avmPort1.
		doConnection(
				ApplicationVMManagementInboundPortURI1,
				ApplicationVMManagementConnector.class.getCanonicalName()) ;
		mv1.toggleTracing() ;
		mv1.toggleLogging() ;

		// instanciation de la vm 2
		mv2 =new ApplicationVM("vm2",	
				ApplicationVMManagementInboundPortURI2,
				RequestSubmissionInboundPortURI2,
				RequestNotificationOutboundPortURI2) ;
		this.addDeployedComponent(mv2) ;
		this.avmPort2 = new ApplicationVMManagementOutboundPort(
				ApplicationVMManagementOutboundPortURI2,
				new AbstractComponent() {}) ;
		this.avmPort2.publishPort() ;
		this.avmPort2.
		doConnection(
				ApplicationVMManagementInboundPortURI2,
				ApplicationVMManagementConnector.class.getCanonicalName()) ;
		mv2.toggleTracing() ;
		mv2.toggleLogging() ;

		// instanciation de la vm 3
		mv3 =new ApplicationVM("vm3",	
				ApplicationVMManagementInboundPortURI3,
				RequestSubmissionInboundPortURI3,
				RequestNotificationOutboundPortURI3) ;
		this.addDeployedComponent(mv3) ;
		this.avmPort3 = new ApplicationVMManagementOutboundPort(
				ApplicationVMManagementOutboundPortURI3,
				new AbstractComponent() {}) ;
		this.avmPort3.publishPort() ;
		this.avmPort3.
		doConnection(
				ApplicationVMManagementInboundPortURI3,
				ApplicationVMManagementConnector.class.getCanonicalName()) ;
		mv3.toggleTracing() ;
		mv3.toggleLogging() ;

		// instanciation du RequestDispatcher
		RequestDispatcher rep = 
				new RequestDispatcher("rep",
						RepartiteurSubmissionInboundPortURI,
						RepartiteurNotificationInboundPortURI,
						RepartiteurSubmissionOutboundPortURI,
						RepartiteurNotificationOutboundPortURI,
						RepartiteurRequestManagementInboundPortURI,
						"fsd",
						RequestDispatcherDynamiceDataInboundPort);
		this.addDeployedComponent(rep);		
		rep.toggleLogging();
		rep.toggleTracing();

		this.rprmop= new RequestDispatcherManagerOutboundPort(
				RepartiteurRequestManagementOutboundPortURI,
				new AbstractComponent() {});
		this.rprmop.publishPort();
		this.rprmop.doConnection(
				RepartiteurRequestManagementInboundPortURI,
				RequestDispatcherManagerConnector.class.getCanonicalName());

		// Creating the request generator component.
		RequestGenerator rg =
				new RequestGenerator(
						"rg",  50.0  , 6000000000L,	
						RequestGeneratorManagementInboundPortURI,
						RequestSubmissionOutboundPortURI,
						RequestNotificationInboundPortURI) ;
		this.addDeployedComponent(rg) ;
		rg.toggleTracing() ;
		rg.toggleLogging() ;
		this.rsobp =(RequestSubmissionOutboundPort) rg.
				findPortFromURI(RequestSubmissionOutboundPortURI) ;
		rsobp.doConnection(
				RepartiteurSubmissionInboundPortURI,
				RequestSubmissionConnector.class.getCanonicalName()) ;

		this.nobp =(RequestNotificationOutboundPort) rep.
				findPortFromURI(RepartiteurNotificationOutboundPortURI) ;
		nobp.doConnection(
				RequestNotificationInboundPortURI,
				RequestNotificationConnector.class.getCanonicalName()) ;

		this.rgmop = new RequestGeneratorManagementOutboundPort(
				RequestGeneratorManagementOutboundPortURI,
				new AbstractComponent() {}) ;
		this.rgmop.publishPort() ;
		this.rgmop.doConnection(
				RequestGeneratorManagementInboundPortURI,
				RequestGeneratorManagementConnector.class.getCanonicalName()) ;
		super.deploy();
	}

	@Override
	public void start() throws Exception {
		super.start();

		AllocatedCore[] ac1 = this.csPort.allocateCores(4) ;
		AllocatedCore[] ac2 = this.csPort.allocateCores(4) ;
		AllocatedCore[] ac3 = this.csPort.allocateCores(4) ;

		this.avmPort1.allocateCores(ac1);
		this.avmPort2.allocateCores(ac2);
		this.avmPort3.allocateCores(ac3);

		String inBoundPortDispatcher1 = rprmop.ajouterVM(RequestSubmissionInboundPortURI1);
		RequestNotificationOutboundPort rpnop1 =(RequestNotificationOutboundPort) 
				mv1.findPortFromURI(RequestNotificationOutboundPortURI1) ;
		rpnop1.doConnection(
				inBoundPortDispatcher1,
				RequestNotificationConnector.class.getCanonicalName()) ;
		/*
		String inBoundPortDispatcher2 = rprmop.ajouterVM(RequestSubmissionInboundPortURI2);
		RequestNotificationOutboundPort rpnop2 =(RequestNotificationOutboundPort) 
				mv2.findPortFromURI(RequestNotificationOutboundPortURI2) ;
		rpnop2.doConnection(
				inBoundPortDispatcher2,
				RequestNotificationConnector.class.getCanonicalName()) ;

		String inBoundPortDispatcher3 = rprmop.ajouterVM(RequestSubmissionInboundPortURI3);
		RequestNotificationOutboundPort rpnop3 =(RequestNotificationOutboundPort) 
				mv3.findPortFromURI(RequestNotificationOutboundPortURI3) ;
		rpnop3.doConnection(
				inBoundPortDispatcher3,
				RequestNotificationConnector.class.getCanonicalName()) ;
		*/
	}

	@Override
	public void shutdown() throws Exception {

		this.avmPort1.doDisconnection();
		this.avmPort2.doDisconnection();
		this.avmPort3.doDisconnection();

		this.csPort.doDisconnection() ;
		this.rsobp.doDisconnection() ;
		this.nobp.doDisconnection() ;
		this.rgmop.doDisconnection() ;
		this.rprmop.doDisconnection();

		super.shutdown();
	}

	public void	testScenario() throws Exception
	{
		// start the request generation in the request generator.
		this.rgmop.startGeneration() ;
		// wait 20 seconds
		System.out.println("######################################");
		Thread.sleep(20000L);
		
		//this.rprmop.supprimerVM(RequestSubmissionInboundPortURI2);
		//Thread.sleep(1000L) ;
		// then stop the generation.
		this.rgmop.stopGeneration() ;
	}

	public static void	main(String[] args)
	{
		// Uncomment next line to execute components in debug mode.
		// AbstractCVM.toggleDebugMode() ;
		try {
			final TestRequestDispatcher trg = new TestRequestDispatcher() ;
			// Deploy the components
			trg.deploy() ;
			System.out.println("starting...") ;
			// Start them.
			trg.start() ;
			// Execute the chosen request generation test scenario in a
			// separate thread.
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						trg.testScenario() ;
					} catch (Exception e) {
						throw new RuntimeException(e) ;
					}
				}
			}).start() ;
			// Sleep to let the test scenario execute to completion.
			Thread.sleep(90000L) ;
			// Shut down the application.
			System.out.println("shutting down...") ;
			trg.shutdown() ;
			System.out.println("ending...") ;
			// Exit from Java.
			System.exit(0) ;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}


}
