package annotations;

/**
 * @author cleber
 *
 */
public class Feature extends Annotation {

	public Feature(String id) {
        super(id);
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName().substring(0, 1) + "[" + this.id + "]";
    }
    
	public Feature clone() {
		Feature clone = new Feature(this.id);
	
	    return clone;
	}

}
