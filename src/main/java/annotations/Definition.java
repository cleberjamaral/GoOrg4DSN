package annotations;

/**
 * @author cleber
 *
 */
public class Definition extends Annotation {

	public Definition(String id) {
        super(id);
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName().substring(0, 1) + "[" + this.id + "]";
    }
    
	public Definition clone() {
		Definition clone = new Definition(this.id);
	
	    return clone;
	}

}
