package annotations;

/**
 * @author cleber
 *
 */
public class Intent extends Annotation {

	public Intent(String id) {
        super(id);
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName().substring(0, 1) + "[" + this.id + "]";
    }
    
	public Intent clone() {
		Intent clone = new Intent(this.id);
	
	    return clone;
	}

}
