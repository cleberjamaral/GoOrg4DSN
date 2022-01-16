package organisation;

import java.util.ArrayList;
import java.util.List;

import annotations.Feature;
import annotations.Workload;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;
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
			preferences.add(Cost.GENERALIST);

		// if a Moise XML file was not provided, use a sample organisation
		if ((args.length < 1) || (args[0].equals("0"))) {
		
			Parameters.getInstance();
			Parameters.setMaxWorkload(1.0);
			Parameters.setWorkloadGrain(1.0);
			Parameters.setOneSolution(false);
			
			LOG.info("Search algorithm: "+ search);

			GoalNode manage_nw = new GoalNode(null, "manage_NW");
			GoalTree gTree = GoalTree.getInstance();
			manage_nw.addWorkload(new Workload("manage_NW",0.6));
			gTree.setRootNode(manage_nw);
			gTree.addGoal("manage_NE", "manage_NW");
			gTree.findAGoalByName(manage_nw, "manage_NE").addWorkload(new Workload("manage_NE",0.6));
			gTree.addGoal("manage_SW", "manage_NW");
			gTree.findAGoalByName(manage_nw, "manage_SW").addWorkload(new Workload("manage_SW",0.6));
			gTree.addGoal("manage_SE", "manage_NW");
			gTree.findAGoalByName(manage_nw, "manage_SE").addWorkload(new Workload("manage_SE",0.6));
			gTree.addGoal("track_1", "manage_NW");
			gTree.findAGoalByName(manage_nw, "track_1").addWorkload(new Workload("2,1",0.2));

			// perform organisation generation (free design)
			Organisation org = orgGen.generateOrganisationFromTree("sample", preferences, search, Parameters.isOneSolution());

			// set available agents for this example
			AgentSet agents = AgentSet.getInstance();
			//sector nw
			agents.addAgent("sensor_02_12", new String("02,12"));
			agents.addAgent("sensor_02_18", new String("02,18"));
			agents.addAgent("sensor_05_15", new String("05,15"));
			agents.addAgent("sensor_08_12", new String("08,12"));
			agents.addAgent("sensor_08_18", new String("08,18"));
			//sector ne
			agents.addAgent("sensor_12_12", new String("12,12"));
			agents.addAgent("sensor_12_18", new String("12,18"));
			agents.addAgent("sensor_15_15", new String("15,15"));
			agents.addAgent("sensor_18_12", new String("18,12"));
			agents.addAgent("sensor_18_18", new String("18,18"));
			//sector sw
			agents.addAgent("sensor_02_02", new String("02,02"));
			agents.addAgent("sensor_02_08", new String("02,08"));
			agents.addAgent("sensor_05_05", new String("05,05"));
			agents.addAgent("sensor_08_02", new String("08,02"));
			agents.addAgent("sensor_08_08", new String("08,08"));
			//sector se
			agents.addAgent("sensor_12_02", new String("12,02"));
			agents.addAgent("sensor_12_08", new String("12,08"));
			agents.addAgent("sensor_15_05", new String("15,05"));
			agents.addAgent("sensor_18_02", new String("18,02"));
			agents.addAgent("sensor_18_08", new String("18,08"));
			
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
