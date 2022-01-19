package annotations;

/**
 * @author cleber
 *
 */
public class Sector extends Annotation {

	public Sector(String id) {
        super(id);
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName().substring(0, 1) + "[" + this.id + "]";
    }
    
	public Sector clone() {
		Sector clone = new Sector(this.id);
	
	    return clone;
	}

}
