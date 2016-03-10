package lti;

import java.util.Arrays;
import java.util.Iterator;

import lti.agent.ambulance.LTIAmbulanceTeam;
import lti.agent.fire.LTIFireBrigade;
import lti.agent.police.LTIPoliceForce;
import rescuecore2.components.Component;
import rescuecore2.components.ComponentLauncher;
import rescuecore2.components.TCPComponentLauncher;
import rescuecore2.connection.ConnectionException;
import rescuecore2.registry.Registry;
import rescuecore2.config.Config;
import rescuecore2.Constants;
import rescuecore2.log.Logger;
import rescuecore2.misc.CommandLineOptions;
import rescuecore2.standard.entities.StandardEntityFactory;
import rescuecore2.standard.entities.StandardPropertyFactory;
import rescuecore2.standard.messages.StandardMessageFactory;


public final class LaunchLTIAgents {
	
	private static final int MAX_PLATOONS = 50;
	private static final int MAX_CENTRES = 50;
	private static final String VERBOSE_FLAG = "-v";
	private static final String DEBUG_FLAG = "-d";
	
	private static final String FIRE_BRIGADE_CLASS = "lti.agent.fire.LTIFireBrigade";
	private static final String POLICE_FORCE_CLASS = "lti.agent.police.LTIPoliceForce";
	private static final String AMBULANCE_TEAM_CLASS = "lti.agent.ambulance.LTIAmbulanceTeam";
	private static final String CENTER_CLASS = "lti.centre.SimpleCentre";
	
	private static boolean verbose_launch = false;
	private static boolean debug = false;
	
	private static ComponentLauncher launcher;
	
	/**
	 * Launch LTI Agent Rescue
	 */
	public static void main(String[] args) {
		Logger.setLogContext("lti");

		try {
			Registry.SYSTEM_REGISTRY
					.registerEntityFactory(StandardEntityFactory.INSTANCE);
			Registry.SYSTEM_REGISTRY
					.registerMessageFactory(StandardMessageFactory.INSTANCE);
			Registry.SYSTEM_REGISTRY
					.registerPropertyFactory(StandardPropertyFactory.INSTANCE);
			Config config = new Config();
			
	        Iterator<String> it = Arrays.asList(args).iterator();
	        while (it.hasNext()) {
	        	String next = it.next();
	            if (VERBOSE_FLAG.equals(next)) {
	                verbose_launch = true;
	            } else if (DEBUG_FLAG.equals(next)) {
	            	verbose_launch = true;
	            	debug = true;
	            }
	        }
	        
	        args = CommandLineOptions.processArgs(args, config);
	        if(args.length >= 7){
				int fb = Integer.parseInt(args[0]);
				int fs = Integer.parseInt(args[1]);
				int pf = Integer.parseInt(args[2]);
				int po = Integer.parseInt(args[3]);
				int at = Integer.parseInt(args[4]);
				int ac = Integer.parseInt(args[5]);
				
				int port = config.getIntValue(Constants.KERNEL_PORT_NUMBER_KEY,
						Constants.DEFAULT_KERNEL_PORT_NUMBER);
				// String host = config.getValue(Constants.KERNEL_HOST_NAME_KEY,
				// Constants.DEFAULT_KERNEL_HOST_NAME);
				
				String host = args[6];
				
				launcher = new TCPComponentLauncher(host, port, config);
				connect(fb, fs, pf, po, at, ac);
			}
		} catch (Exception e) {
			Logger.error("Error connecting agents", e);
		}
		System.out.println("FIM 2015-amb3");

	}

	private static void connect(int fb, int fs,
			int pf, int po, int at, int ac) throws InterruptedException,
			ConnectionException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		if(fb == -1){
			connect(FIRE_BRIGADE_CLASS, MAX_PLATOONS);
		} else if(fb > 0){
			connect(FIRE_BRIGADE_CLASS, fb);
		}
		
		if((fs > 0) || (fs == -1)){
			connect(CENTER_CLASS, MAX_CENTRES);
		}
		
		if(pf == -1){
			connect(POLICE_FORCE_CLASS, MAX_PLATOONS);
		} else if(pf > 0){
			connect(POLICE_FORCE_CLASS, pf);
		}
		
		if((po > 0) || (po == -1)){
			connect(CENTER_CLASS, MAX_CENTRES);
		}
		
		if(at == -1){
			connect(AMBULANCE_TEAM_CLASS, MAX_PLATOONS);
		} else if(at > 0){
			connect(AMBULANCE_TEAM_CLASS, at);
		}
		
		if((ac > 0) || (ac == -1)){
			connect(CENTER_CLASS, MAX_CENTRES);
		}
	}

	/**
	 * @param classname
	 * @param max_agents
	 * @throws InterruptedException
	 * @throws ConnectionException
	 */
	private static void connect(String classname, int max_agents)
			throws InterruptedException, ConnectionException {
		String[] cnsplit = classname.split("[.]");
		String agentname = cnsplit[cnsplit.length - 1];
		Component c = null;
		
		log("Started launching " + agentname + "s >>>");
		try {
			for (int i = 1; i < max_agents; i++) {
				log("Launching " + agentname + " " + i + "... ");
				c = (Component) Class.forName(classname).newInstance();
				if (c instanceof LTIAmbulanceTeam){
					((LTIAmbulanceTeam)c).setVerbose(debug);
					}
				if (c instanceof LTIPoliceForce){
					((LTIPoliceForce)c).setVerbose(debug);
				}
				if (c instanceof LTIFireBrigade){
					((LTIFireBrigade)c).setVerbose(debug);
				}
				launcher.connect(c);
				log(agentname + " " + i + " launched.");
			}
		} catch (Exception e) {
			log(e.toString());
		}
		log("<<< Finished launching " + agentname + "s.");
		log_newline();
	}
	
	private static void log_newline() {
		log("");
	}
	
	private static void log(String msg) {
		if (verbose_launch)
			System.out.println(msg);
	}
}
