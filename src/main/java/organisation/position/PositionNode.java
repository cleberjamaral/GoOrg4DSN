package organisation.position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import annotations.Annotation;
import annotations.Sector;
import annotations.Workload;
import fit.Requirement;
import organisation.goal.GoalNode;

/**
 * @author cleber
 *
 */
public class PositionNode implements Requirement {
	// positionName and parentName are unique names for this position and its parent in this tree (ex: r0, r1...)
	private String positionName;
	private String parentName;

	private PositionNode parent;
	private List<PositionNode> descendants = new ArrayList<>();
	private Set<Annotation> annotations = new HashSet<>();
	private Set<GoalNode> assignedGoals = new HashSet<>();


	public PositionNode(PositionNode parent, String positionName) {
		setParent(parent);
		this.positionName = positionName;
	}
	
	public void addWorkload(Workload workload) {
		Workload w = getWorkload(workload.getId());
		if (w != null) {
			w.setValue((double) w.getValue() + (double) workload.getValue());
		} else {
			this.annotations.add(workload);
		}
	}
	
	public void addAnnotation(Annotation annotation) {
		for (Annotation w : annotations) 
			if (w.getClass().isAssignableFrom(annotation.getClass()) && annotation.getId() == w.getId()) 
				// There is an annotation of same type and id
				return;
		
		this.annotations.add(annotation);
	}

	public Workload getWorkload(String id) {
		for (Annotation w : annotations) 
			if (w instanceof Workload && w.getId().equals(id)) return (Workload) w;
		
		return null;
	}
	
	public List<Workload> getWorkloads() {
		List<Workload> workloads = new ArrayList<Workload>();
		for (Annotation w : annotations) 
			if (w instanceof Workload) 
				workloads.add((Workload) w);
		
		return workloads;
	}

	/**
	 * A position may have more than one sector if it has joined goals from different sectors
	 * @return
	 */
	public List<Sector> getSectors() {
		List<Sector> sectors = new ArrayList<Sector>();
		for (Annotation w : annotations) 
			if (w instanceof Sector) 
				sectors.add((Sector) w);
		
		return sectors;
	}

	public double getSumWorkload() {
		double sumEfforts = 0;
		for (Workload w : getWorkloads())
			sumEfforts += (double) w.getValue();
		return sumEfforts;
	}

	public void assignGoal(GoalNode g) {
		this.assignedGoals.add(g);
	}

	public Set<GoalNode> getAssignedGoals() {
		return this.assignedGoals;
	}

	private void addDescendant(PositionNode newDescendant) {
		this.descendants.add(newDescendant);
	}

	public List<PositionNode> getDescendants() {
		return this.descendants;
	}

	public String getPositionName() {
		return this.positionName;
	}

	public PositionNode getParent() {
		return this.parent;
	}

	public String getParentName() {
		return this.parentName;
	}
	
	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public void setParent(PositionNode parent) {
		this.parent = parent;
		if (getParent() != null) {
			setParentName(parent.getPositionName());
			getParent().addDescendant(this);
		} else {
			setParentName("");
		}
	}

	/**
	 * Check if this position has a parent position
	 * @param g
	 * @return
	 */
	public boolean hasParent() {
		return getParent() != null;
	}

	/**
	 * Generate a signature of this position which will be used to make a signature of
	 * the tree which makes a search state unique
	 */
	public String toString() {
		String r = "";

		List<String> signatureByGoals = new ArrayList<>();
		if ((getAssignedGoals() != null) && (!getAssignedGoals().isEmpty())) {
			Iterator<GoalNode> iterator = getAssignedGoals().iterator(); 
			while (iterator.hasNext()) {
				GoalNode n = iterator.next(); 
				signatureByGoals.add(n.getOriginalName());
			}
			Collections.sort(signatureByGoals);
		}
		r += "G{" + signatureByGoals + "}";

		if (getParent() != null) {
			r += "^";
			r += getParent().toString();
		}
		
		return r;
	}
	
	public PositionNode cloneContent() {
		// parent is not cloned it must be resolved by the tree
		PositionNode clone = new PositionNode(null, getPositionName());
		// parent is resolved by its cloned source parent's name
		clone.setParentName(getParentName());

		for (Annotation w : getAnnotations()) 
			clone.addAnnotation(w.clone());

		for (GoalNode goal : getAssignedGoals()) 
			if (!clone.getAssignedGoals().contains(goal)) 
				clone.getAssignedGoals().add(goal);

	    return clone;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getAssignedGoals() == null) ? 0 : getAssignedGoals().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PositionNode other = (PositionNode) obj;
		if (this.getAssignedGoals() == null) {
			if (other.getAssignedGoals() != null)
				return false;
		} else if (!getAssignedGoals().equals(other.getAssignedGoals()))
			return false;
		return true;
	}
	
	public boolean containsGoalByOriginalName(GoalNode g1) {
		Iterator<GoalNode> i = assignedGoals.iterator();
		while (i.hasNext()) {
			GoalNode g2 = i.next();
			if (g2.getOriginalName().equals(g1.getOriginalName()))
				return true;
		}
		return false;
    }

	@Override
	public Set<Annotation> getAnnotations() {
		return annotations;
	}

	@Override
	public String getRequirement() {
		return getPositionName();
	}

	@Override
	public Set<String> getAnnotationIds() {
		Set<String> strings = new HashSet<>();
		annotations.forEach(w -> {strings.add(w.getId());});
		return strings;
	}
}