package fit;

import java.util.Set;

import annotations.Annotation;

/**
 * @author cleber
 *
 */
public interface Requirement {

	/**
	 * An identification to the requirement
	 * @return a string
	 */
	public String getRequirement();
	
	/**
	 * Return annotations
	 * @return Set of annotations
	 */
	public Set<Annotation> getAnnotations();

	/**
	 * Return strings representing items that must be fulfilled
	 * @return Set of strings
	 */
	public Set<String> getAnnotationIds();
}
