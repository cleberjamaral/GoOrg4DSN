package organisation.position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import annotations.Annotation;
import annotations.Workload;
import fit.Requirement;
import fit.RequirementSet;
import organisation.Parameters;
import organisation.exception.PositionNotFound;
import organisation.goal.GoalNode;
import organisation.goal.GoalTree;

/**
 * @author cleber
 *
 */
public class PositionsTree implements RequirementSet {

	private int numberOfLevels = 0;
	private Set<PositionNode> tree = new HashSet<>();

	public PositionsTree() {
	}

	public int getNumberOfLevels() {
		return numberOfLevels;
	}

	private void setNumberOfLevels(int numberOfLevels) {
		this.numberOfLevels = numberOfLevels;
	}

	public int size() {
		return tree.size();
	}

	public Set<PositionNode> getTree() {
		return tree;
	}

	public void addPositionToTree(PositionNode position) {
		updateNumberOfLevels(position);
		
		tree.add(position);
	}

	private void updateNumberOfLevels(PositionNode position) {
		int levels = 1;
		while (position.getParent() != null) {
			position = position.getParent();
			levels++;
		}

		if (levels > getNumberOfLevels())
			setNumberOfLevels(levels);
	}

	public PositionNode createPosition(PositionNode parent, String name, GoalNode g) {
		PositionNode nr = new PositionNode(parent, name);

		assignGoalToPosition(nr, g);
		addPositionToTree(nr);

		return nr;
	}

	public PositionNode findPositionByName(String positionName) throws PositionNotFound {
		for (PositionNode or : this.tree) {
			if (or.getPositionName().equals(positionName))
				return or;
		}
		throw new PositionNotFound("There is no position with signature = '" + positionName + "'!");
	}

	public PositionsTree cloneContent() throws PositionNotFound {
		PositionsTree clonedTree = new PositionsTree();

		// first clone all positions
		for (PositionNode or : this.tree) {
			PositionNode nnewS = or.cloneContent();
			// not using addPositionToTree because parent is still unknown
			clonedTree.getTree().add(nnewS);
		}

		// finding right parents in the new tree
		for (PositionNode or : clonedTree.getTree()) {

			// it is not the root position
			if (!or.getParentName().equals("")) {
				or.setParent(clonedTree.findPositionByName(or.getParentName()));
			}
		}
		
		// update number of levels after knowning parents
		clonedTree.setNumberOfLevels(1);
		for (PositionNode or : clonedTree.getTree()) {
			clonedTree.updateNumberOfLevels(or);
		}
		
		return clonedTree;
	}

	public PositionNode assignGoalToPositionByPositionName(String positionName, GoalNode newGoal) throws PositionNotFound {
		PositionNode position = this.findPositionByName(positionName);

		assignGoalToPosition(position, newGoal);

		return position;
	}

	public void assignGoalToPosition(PositionNode position, GoalNode newGoal) {
		position.assignGoal(newGoal);

		// Copy all workloads of the goal to this new position
		for (Annotation w : newGoal.getAnnotations())
			position.addAnnotation(w.clone());
	}

	/**
	 * Give the sum of efforts of the whole tree
	 * 
	 * @return a double
	 */
	public double getSumWorkload() {
		double sumWorkload = 0;
		for (PositionNode r : this.tree) {
			for (Workload w : r.getWorkloads()) {
				sumWorkload += (double) w.getValue();
			}
		}
		return sumWorkload;
	}

	@Override
	public String toString() {
		List<String> signatureByPositions = new ArrayList<>();
		if ((getTree() != null) && (!getTree().isEmpty())) {
			Iterator<PositionNode> iterator = getTree().iterator();
			while (iterator.hasNext()) {
				PositionNode n = iterator.next();
				signatureByPositions.add(n.toString());
			}
			Collections.sort(signatureByPositions);
		}
		return signatureByPositions.toString();
	}

	public String getSkillsTree() {
		List<String> skillsTree = new ArrayList<>();
		if ((getTree() != null) && (!getTree().isEmpty())) {
			Iterator<PositionNode> treeIterator = getTree().iterator();
			while (treeIterator.hasNext()) {
				PositionNode n = treeIterator.next();
				List<String> skills = new ArrayList<>();
				Iterator<Workload> skillIterator = n.getWorkloads().iterator();
				while (skillIterator.hasNext()) {
					Workload w = skillIterator.next();
					//skills.add("\""+w.getId()+"\"");
					skills.add(w.getId());
				}
				skillsTree.add(skills.toString());
			}
			Collections.sort(skillsTree);
		}
		return skillsTree.toString();
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PositionsTree other = (PositionsTree) obj;
		if (tree == null) {
			if (other.tree != null)
				return false;
		} else if (!tree.toString().equals(other.tree.toString()))
			return false;
		return true;
	}

	/**
	 * Generalness is about how similar are the positions. If all positions are
	 * assigned to the same goals, they request same skills so they are similar.
	 * 
	 * @return a compensate factor from 0 to 1 according to the progress of the
	 *         search
	 */
	public double getGeneralness() {
		return compensateWhenSearchInProgress(getGeneralnessMinMax());
	}
	
	/**
	 * Specificness is the complement part of generalness.
	 * 
	 * @return a compensate factor from 0 to 1 according to the progress of the
	 *         search
	 */
	public double getSpecificness() {
		return compensateWhenSearchInProgress(1 - getGeneralnessMinMax());
	}
	
	/**
	 * Generalness is about how goals are distributed across positions. When all
	 * positions have all given goals it means all agents are responsible for all
	 * tasks and they can play any position, i.e., maximum generalness.
	 * 
	 * For instance, for a GDT with a set of goals that together have 3 different
	 * goals ideally these goals should be broken into 9 goals and the PositionsTree 
	 * should prefer structures in which all positions have one part of each of 
	 * those 3 goals. So, the 3 agents that will plays those positions can be
	 * actually allocated in any one of those positions
	 * 
	 * The result uses a min-max function to ensure the max generalness is 1 and
	 * minimum is 0, which means max specificness 
	 * 
	 * @return generalness index from 0 to 1 (less to maximum generalness possible)
	 * @throws Exception 
	 */	
	private double getGeneralnessMinMax() {
		int nAllOriginalGoalsAssigned = 0;

		for (PositionNode or : this.tree) {
			Set<String> allOriginalGoalsOfPosition = new HashSet<>();
			for (GoalNode g : or.getAssignedGoals()) {
				allOriginalGoalsOfPosition.add(g.getOriginalName());
			}
			// Accumulates only different goals, sum all we have on each position 
			nAllOriginalGoalsAssigned += allOriginalGoalsOfPosition.size();
		}
		
		// the most specialist positions tree must have all workloads distributed
		// without splitting them (if may be impossible if the sumofefforts if higher
		// than maxWorkload, but efficiency/idleness should not be taken into account
		int nMinOriginalGoalsSpread = Math.min(GoalTree.getInstance().getOriginalGoals().size(), this.tree.size());
		
		// ideal situation: each original goal has a broken part that can be distributed
		// equally across all positions
		int nMaxOriginalGoalsSpread = GoalTree.getInstance().getOriginalGoals().size() * this.tree.size();

		// the actual generalness of the current positions tree
		double generalness = (double) (nAllOriginalGoalsAssigned - nMinOriginalGoalsSpread)
				/ (double) (nMaxOriginalGoalsSpread - nMinOriginalGoalsSpread);
		
		return generalness;
	}
	
	/**
	 * Idleness is the complement part of efficiency.
	 * 
	 * @return a compensated factor from 0 to 1 according to the progress of the
	 *         search
	 */
	public double getIdleness() {
		return compensateWhenSearchInProgress(1 - getEfficiencyFactor());
	}
	
	/**
	 * Efficiency is the perfect occupancy of the work force.
	 * 
	 * @return a compensated factor from 0 to 1 according to the progress of the
	 *         search
	 */
	public double getEfficiency() {
		return compensateWhenSearchInProgress(getEfficiencyFactor());
	}
	
	/**
	 * It is about efficiency of the workforce distribution. If the sum of workloads
	 * is not multiple of the max workload, efficiency will never be 100%. If the
	 * algorithm is creating more positions than the minimum (ideal) it is
	 * decreasing lessIdlenessRate.
	 * 
	 * Low efficiency means high idleness. As said, sometimes, it is possible to do
	 * a perfect split, so so idleness is often inevitable.
	 * 
	 * @return Efficiency varies from 0 to 1, being 1 for max efficiency
	 */
	public double getEfficiencyFactor() {
		double capacity = this.tree.size() * Parameters.getMaxWorkload();
		double occupancy = this.getSumWorkload();

		return occupancy / capacity;
	}

	/**
	 */
	public double getNearness() {
		return compensateWhenSearchInProgress(getPropinquityFactor());
	}
	
	/**
	 */
	public double getFarness() {
		return compensateWhenSearchInProgress(1 - getPropinquityFactor());
	}

	/**
	 * Propinquity returns how close the goal of same sectors are assigned to 
	 * positions in a hierarchy.
	 * 
	 * @return propiquity rate from 0 to 1, from far to near
	 */
	public double getPropinquityFactor() {
		// Check if two position in a hierarchy are of the same sector
		for (PositionNode p : this.tree) {
			if (p.hasParent() && p.getSectors().size() == 1) {
				// The superior is not of the same sector as the subordinate
				if (!p.getSectors().get(0).getId().equals(p.getParent().getSectors().get(0).getId())) {
					return 0;
				}
			}
		}

		// Check if a joined position has different sectors
		for (PositionNode p : this.tree) {
			if (p.getSectors().size() > 1) {
				return 0;
			}
		}
		
		return 1;
	}
	
	/**
	 * When the search is in progress, the returned rates must be compensated in order 
	 * to minimize errors on cost resolution of new states creation
	 * 
	 * @param rate is the rate that may need compensation
	 * @return a new rate eventually compensated
	 */
	private double compensateWhenSearchInProgress(double rate) {
		int nGoalsAssigned = 0;
		for (PositionNode or : this.tree) {
			// Accumulates all goals (all broken goals) 
			nGoalsAssigned += or.getAssignedGoals().size();
		}
		// if it is a partial generalness, add a penalty according to the number of goals to assign
		int nGoalsToAssing = GoalTree.getInstance().getTree().size() - nGoalsAssigned;

		// a penalty for partial generalness
		if (nGoalsToAssing > 0)
			return rate / (nGoalsToAssing * GoalTree.getInstance().getTree().size() * 10);
		else
			return rate;
	}
	
	@Override
	public Set<Requirement> getRequirements() {
		//TODO: (Set<Requirement>) tree should work!!!
		Set<Requirement> requirements = new HashSet<>();
		tree.forEach(r -> {requirements.add(r);});
		return requirements;
	}
}
