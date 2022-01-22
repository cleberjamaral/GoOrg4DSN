package organisation;

import java.util.ArrayList;
import java.util.List;

import annotations.Definition;
import annotations.Sector;
import annotations.Workload;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
import organisation.resource.Agent;
import organisation.resource.AgentSet;
import organisation.search.Organisation;
import organisation.search.cost.Cost;
import simplelogger.SimpleLogger;

/**
 * @author cleber
 *
 */
public class OrganisationApp {

	private static SimpleLogger LOG = SimpleLogger.getInstance();

	public static void main(String[] args) {

		List<Cost> preferences = new ArrayList<>();
		String search = "BFS";

		OrganisationGenerator orgGen = new OrganisationGenerator();
		OrganisationBinder orgBin = new OrganisationBinder();

		// if an argument to choose a cost function was given
		for (int i = 1; i < args.length; i++)
			preferences.add(Cost.valueOf(args[i]));

		if (preferences.size() == 0)
			preferences.add(Cost.NEAR);
			preferences.add(Cost.IDLE);

		// if a Moise XML file was not provided, use a sample organisation
		if ((args.length < 1) || (args[0].equals("0"))) {
		
			Parameters.getInstance();
			Parameters.setMaxWorkload(1.0);
			Parameters.setWorkloadGrain(1.0);
			Parameters.setOneSolution(false);
			
			LOG.info("Search algorithm: "+ search);

			GoalNode manage_sector_nw = new GoalNode(null, "manage_sector_NW");
			GoalTree gTree = GoalTree.getInstance();
			manage_sector_nw.addAnnotation(new Workload("manage_sector",0.6));
			manage_sector_nw.addAnnotation(new Sector("nw"));
			gTree.setRootNode(manage_sector_nw);
			gTree.addGoal("manage_sector_NE", "manage_sector_NW");
			gTree.findAGoalByName(manage_sector_nw, "manage_sector_NE").addAnnotation(new Workload("manage_sector",0.6));
			gTree.findAGoalByName(manage_sector_nw, "manage_sector_NE").addAnnotation(new Sector("ne"));
			gTree.addGoal("manage_sector_SW", "manage_sector_NW");
			gTree.findAGoalByName(manage_sector_nw, "manage_sector_SW").addAnnotation(new Workload("manage_sector",0.6));
			gTree.findAGoalByName(manage_sector_nw, "manage_sector_SW").addAnnotation(new Sector("sw"));
			gTree.addGoal("manage_sector_SE", "manage_sector_NW");
			gTree.findAGoalByName(manage_sector_nw, "manage_sector_SE").addAnnotation(new Workload("manage_sector",0.6));
			gTree.findAGoalByName(manage_sector_nw, "manage_sector_SE").addAnnotation(new Sector("se"));
			gTree.addGoal("track_1", "manage_sector_SE");
			gTree.findAGoalByName(manage_sector_nw, "track_1").addAnnotation(new Workload("manage_track",0.2));
			gTree.findAGoalByName(manage_sector_nw, "track_1").addAnnotation(new Sector("se"));

			// perform organisation generation (free design)
			Organisation org = orgGen.generateOrganisationFromTree("sample", preferences, search, Parameters.isOneSolution());

			// set available agents for this example
			AgentSet agents = AgentSet.getInstance();
			//sector nw
			agents.addAgent("sensor_02_12").addAnnotation(new Sector("nw"));
			agents.addAgent("sensor_02_18").addAnnotation(new Sector("nw"));

			Agent a = agents.addAgent("sensor_05_15");
			a.addAnnotation(new Sector("nw"));
			a.addAnnotation(new Definition("manage_sector"));
			
			agents.addAgent("sensor_08_12").addAnnotation(new Sector("nw"));
			agents.addAgent("sensor_08_18").addAnnotation(new Sector("nw"));
			
			//sector ne
			agents.addAgent("sensor_12_12").addAnnotation(new Sector("ne"));
			agents.addAgent("sensor_12_18").addAnnotation(new Sector("ne"));
			
			a = agents.addAgent("sensor_15_15");
			a.addAnnotation(new Sector("ne"));
			a.addAnnotation(new Definition("manage_sector"));
			
			agents.addAgent("sensor_18_12").addAnnotation(new Sector("ne"));
			agents.addAgent("sensor_18_18").addAnnotation(new Sector("ne"));
			
			//sector sw
			agents.addAgent("sensor_02_02").addAnnotation(new Sector("sw"));
			agents.addAgent("sensor_02_08").addAnnotation(new Sector("sw"));

			a = agents.addAgent("sensor_05_05");
			a.addAnnotation(new Sector("sw"));
			a.addAnnotation(new Definition("manage_sector"));
			
			agents.addAgent("sensor_08_02").addAnnotation(new Sector("sw"));
			agents.addAgent("sensor_08_08").addAnnotation(new Sector("sw"));
			
			//sector se
			agents.addAgent("sensor_12_02").addAnnotation(new Sector("se"));
			agents.addAgent("sensor_12_08").addAnnotation(new Sector("se"));

			a = agents.addAgent("sensor_15_05");
			a.addAnnotation(new Sector("se"));
			a.addAnnotation(new Definition("manage_sector"));

			agents.addAgent("sensor_18_02").addAnnotation(new Sector("se"));
			agents.addAgent("sensor_18_08").addAnnotation(new Sector("se"));
			
			// bind agents and positions
			orgBin.bindOrganisations(org, agents);

		} else {
			// Expected input example:
			// ./gradlew run --args="examples/Full_Link_ultramegasimple.xml GENERALIST BFS"
			OrganisationXMLParser parser = new OrganisationXMLParser();
			parser.parseOrganisationSpecification(args[0]);
			parser.parseDesignParameters(args[0]);
			parser.parseAvailableAgents(args[0]);
			
			String path[] = args[0].split("/");
			String name = path[path.length - 1];
			name = name.substring(0, name.length() - 4);

			LOG.info("Search algorithm: "+ search);

			// generate organisations
			Organisation org = orgGen.generateOrganisationFromTree(name, preferences, search, Parameters.isOneSolution());
			
			// get parsed agents set
			AgentSet agents = AgentSet.getInstance();
			
			// perform organisation generation (free design)
			orgBin.bindOrganisations(org, agents);
		}
	}
}
