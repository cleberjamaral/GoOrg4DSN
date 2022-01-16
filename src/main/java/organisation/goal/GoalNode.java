package organisation.goal;

import java.util.ArrayList;
import java.util.List;

import annotations.Feature;
import annotations.Workload;
import organisation.exception.CircularReference;

/**
 * @author cleber
 *
 */
public class GoalNode {
	private String goalName;
	private String originalName;
	private GoalNode parent;
	private String operator;
	private List<GoalNode> descendants = new ArrayList<>();
	private List<Workload> workloads = new ArrayList<>();
	private List<Feature> features = new ArrayList<>();

	public GoalNode(GoalNode p, String name) {
		goalName = name;
		if (name.indexOf('$') > 0)
			originalName = name.substring(0, name.lastIndexOf('$'));
		else
			originalName = name;
		parent = p;
		operator = "sequence";
		if (parent != null) {
			parent.addDescendant(this);
		}
	}

	public void addWorkload(Workload workload) {
		Workload w = getWorkload(workload.getId());
		if (w != null) {
			w.setValue((double) w.getValue() + (double) workload.getValue());
		} else {
			workloads.add(workload);
		}
	}
	
	public Workload getWorkload(String id) {
		for (Workload w : workloads) 
			if (w.getId().equals(id)) return w;
		
		return null;
	}
	
	public List<Workload> getWorkloads() {
		return workloads;
	}

	public void addFeature(Feature feature) {
		this.features.add(feature);
	}
	
	public Feature getFeature(String id) {
		for (Feature w : features) 
			if (w.getId().equals(id)) return w;
		
		return null;
	}
	
	public List<Feature> getFeatures() {
		return features;
	}
	
	public double getSumWorkload() {
		double sumEfforts = 0;
		for (Workload w : getWorkloads())
			sumEfforts += (double) w.getValue();
		return sumEfforts;
	}
	
	public void addDescendant(GoalNode newDescendent) {
		descendants.add(newDescendent);
	}

	public List<GoalNode> getDescendants() {
		return descendants;
	}

	public String getGoalName() {
		return goalName;
	}

	public void setGoalName(String name) {
		this.goalName = name;
		if (name.indexOf('$') > 0)
			originalName = name.substring(0, name.indexOf('$'));
		else
			originalName = name;
	} 
	
	public String getOriginalName() {
		return originalName;
	}
	public GoalNode getParent() {
		return parent;
	}

	public void setParent(GoalNode parent) {
		this.parent = parent;
		if (this.parent != null) {
			this.parent.addDescendant(this);
		}
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String op) {
		this.operator = op;
	}
	
	public boolean containsWorkload() {
		return (this.getWorkloads().size() > 0);
	}
	
	public String toString() {
		return goalName;
	}

	public GoalNode cloneContent() throws CircularReference {
		GoalNode clone = new GoalNode(null, this.goalName);
		
		for (Workload w : getWorkloads()) 
			clone.addWorkload(w.clone());
		
		clone.operator = this.operator;
		
	    return clone;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((goalName == null) ? 0 : goalName.hashCode());
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
		GoalNode other = (GoalNode) obj;
		if (goalName == null) {
			if (other.goalName != null)
				return false;
		} else if (!goalName.equals(other.goalName))
			return false;
		return true;
	}
}