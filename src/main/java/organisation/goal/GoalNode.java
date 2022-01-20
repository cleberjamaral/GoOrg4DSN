package organisation.goal;

import java.util.ArrayList;
import java.util.List;

import annotations.Sector;
import annotations.Annotation;
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
	private List<Annotation> annotations = new ArrayList<>();

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
	 * A goal is supposed to have only one annotation of type sector
	 * @return
	 */
	public Sector getSector() {
		for (Annotation w : annotations) 
			if (w instanceof Sector) 
				return (Sector) w;
		
		return null;
	}

	public void addAnnotation(Annotation annotation) {
		this.annotations.add(annotation);
	}
	
	public Annotation getAnnotation(String id) {
		for (Annotation w : annotations) 
			if (w.getId().equals(id)) return w;
		
		return null;
	}
	
	public List<Annotation> getAnnotations() {
		return annotations;
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
			clone.addAnnotation(w.clone());
		
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