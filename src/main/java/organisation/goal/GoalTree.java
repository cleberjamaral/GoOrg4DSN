package organisation.goal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import annotations.Workload;
import organisation.Parameters;
import organisation.exception.CircularReference;
import organisation.exception.GoalNotFound;

/**
 * @author cleber
 *
 */
public class GoalTree {

	private static GoalTree instance = null;
    private GoalNode rootNode;
    private Set<GoalNode> tree = new HashSet<>();
    Set<Workload> allDiffWorkloads = new HashSet<>();
    Set<String> allOriginalGoals = new HashSet<>();

    private GoalTree() {}
    
	public static GoalTree getInstance() 
    { 
        if (instance == null) 
        	instance = new GoalTree();
  
        return instance; 
    }
	
    /**
     * Add a goal to this goals tree
     * 
     * @param rootNode the root node object
     */
    public void setRootNode(GoalNode rootNode) {
        this.rootNode = rootNode;
        if (!treeContains(this.rootNode)) {
            tree.add(this.rootNode);
            allOriginalGoals.add(this.rootNode.getOriginalName());
        }
    }

    /**
     * Add a goal to this goals tree
     * 
     * @param rootNode the root name
     */
    public void setRootNode(String rootNode) {
        this.rootNode = new GoalNode(null, rootNode);
        if (!treeContains(this.rootNode)) {
            tree.add(this.rootNode);
            allOriginalGoals.add(this.rootNode.getOriginalName());
        }
    }

    /**
     * get root node of this tree
     * 
     * @return the object root node
     */
    public GoalNode getRootNode() {
        return this.rootNode;
    }


    /**
     * get tree of goals
     * 
     * @return a set of GoalNode
     */
    public Set<GoalNode> getTree() {
        return this.tree;
    }
    
    /**
     * Add a goal to this tree
     * 
     * @param name    of the nonexistent goal
     * @param parent, an existing parent node for this goal
     * @return the created goal node
     */
    public GoalNode addGoal(String name, GoalNode parent) {
        GoalNode goal = new GoalNode(parent, name);
        if (!treeContains(goal)) {
            tree.add(goal);
            allOriginalGoals.add(goal.getOriginalName());
        }
        
        return goal;
    }
    
    public void addGoal(GoalNode goal) {
        if (!treeContains(goal)) {
            tree.add(goal);
            allOriginalGoals.add(goal.getOriginalName());
        }
    }

    /**
     * Add a goal to this tree
     * 
     * @param name    of the nonexistent goal
     * @param parent, the name of an existing goal
     */
    public void addGoal(String name, String parent) {
        GoalNode parentGoal = findAGoalByName(this.rootNode, parent);
        addGoal(name, parentGoal);
    }

    /**
     * Add a goal to this tree and a 'report' inform annotation on it
     * 
     * @param name          of the nonexistent goal
     * @param parent,       the name of an existing goal
     * @param reportAmount, the amount of data the descendant report
     * @throws CircularReference
     */
    public void addGoal(String name, String parent, double reportAmount) throws CircularReference {
        GoalNode parentGoal = findAGoalByName(this.rootNode, parent);
        addGoal(name, parentGoal);
    }

    /**
     * check if this tree contains a given goal object
     * 
     * @param g, a goal object
     * @return true when the goal was found
     */
    public boolean treeContains(GoalNode g) {
        for (GoalNode gn : tree)
            if (gn.getGoalName().equals(g.getGoalName()))
                return true;

        return false;
    }

    /**
     * Built a tree adding the descendants of a root node This method is usually
     * used when the tree was created with linked nodes but not using this GoalTree
     * class which provide some extra facilities
     * 
     * @param root, a node that is linked to descendants
     */
    public void addAllDescendants(GoalNode root) {
        for (GoalNode g : root.getDescendants()) {
            if (!treeContains(g))
                tree.add(g);
            addAllDescendants(g);
        }
    }

	/**
	 * A recursive function to get unique workloads.
	 * Number of different workloads is used to calculate generalness
	 * and other rates. When the tree is build from goals node and later
	 * imported to the tree it should be calculated.
	 * 
	 * @param root goal
	 */
	public void updateNumberDiffWorkloads(GoalNode root) {
        for (Workload w : root.getWorkloads()) 
    		allDiffWorkloads.add(w);
        for (GoalNode g : root.getDescendants()) 
            updateNumberDiffWorkloads(g);
	}

	/**
	 * A recursive function to get unique goals.
	 * Number of original goals is used to calculate generalness
	 * and other rates. When the tree is build from goals node and later
	 * imported to the tree it should be calculated.
	 * 
	 * @param root goal
	 */
	public void updateNumberOriginalGoals(GoalNode root) {
		allOriginalGoals.add(root.getOriginalName());
        for (GoalNode g : root.getDescendants()) 
        	updateNumberOriginalGoals(g);
	}
	
    /**
     * Add a workload annotation to a given goal
     * 
     * @param goal,     the goal to add an annotation
     * @param workload, the id of the workload
     * @param effort,   a double the necessary effort
     * @throws GoalNotFound 
     */
    public void addWorkload(String goal, String workload, double effort) throws GoalNotFound {
        GoalNode g = findAGoalByName(getRootNode(), goal);
        
        if (g == null) 
        	throw new GoalNotFound("Goal '"+goal+"' not found!");
        
        Workload w = new Workload(workload, effort);
        g.addWorkload(w);
        allDiffWorkloads.add(w);
    }

	/**
	 * Return the goal object descendant of a given goal
	 * @param root, the higher kinship of the node
	 * @param name
	 * @return
	 */
	public GoalNode findAGoalByName(GoalNode root, String name) {
		if (root.getGoalName().equals(name)) {
			return root;
		} 
		for (GoalNode goal : root.getDescendants()) {
			GoalNode d = findAGoalByName(goal, name);
			if (d != null) return d;
		}
		return null;
	}
	
	/**
	 * Add to the given successors list all descendants of the given goal
	 * 
	 * @param successors, a list to receive the descendants
	 * @param gn, the parent goal of the successors
	 */
	public void addSuccessorsToList(List<GoalNode> successors, GoalNode gn) {
		for (GoalNode goal : gn.getDescendants()) {
			successors.add(goal);
			addSuccessorsToList(successors, goal);
		}
	}
	
	/**
	 * Give the sum of efforts of the whole tree
	 * 
	 * @return a double
	 */
	public double getSumEfforts() {
		double sumEfforts = 0;
		for (GoalNode g : this.getTree()) {
			for (Workload w : g.getWorkloads()) 
				sumEfforts += (double) w.getValue();
			
		}
		return sumEfforts;
	}

	/**
	 * This is the lower bound of number of positions according to max workloads
	 * 
	 * @return integer representing bet number of positions (lower bound)
	 */
	public int getBestNumberOfPositions() {
		return (int) Math.ceil(getSumEfforts() / Parameters.getMaxWorkload());
	}
	
	public int getNumberDiffWorkloads() {
		return allDiffWorkloads.size();
	}
	
	public Set<String> getOriginalGoals() {
		return allOriginalGoals;
	}
}
