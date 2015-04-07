package generics;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class Polynomial<T extends Number> {
	
	private ArrayList<T> coeffs;
	private Polynomial<T> deriv;
	
	
	public Polynomial(ArrayList<T> coeffs) {
		this.coeffs = coeffs;
	}
	
	public Number val(T x) {
		Number accum = getNumericZero(x);
		
		
		for(int i = coeffs.size()-1 ; i >= 0; i--) {
			accum = hornerStep(accum , x ,coeffs.get(i));
		}
		
		return accum;
	}
	
	public Number getCoeff(int k) {
		if(k < 0 || k >= coeffs.size()) {
			//base case, return 0
			return 0;
		} else {
			return coeffs.get(k);
		}
	}
	
	public Polynomial<? extends Number> add(Polynomial<? extends Number> other) {
		//TODO
		return null;
	}
	
	public Polynomial<? extends Number> sub(Polynomial<? extends Number> other) {
		//TODO
		return null;
	}
	
	public Polynomial<? extends Number> mul(Polynomial<? extends Number> other) {
		//TODO
		return null;
	}
	
	public Polynomial<? extends Number> div(Polynomial<? extends Number> other) {
		//TODO
		return null;
	}
	
	public Polynomial<? extends Number> rem(Polynomial<? extends Number> other) {
		//TODO
		return null;
	}

	private static <T extends Number> Number hornerStep(T accumulator, T x, T coefficient) {
		switch( TypeHandler.resolve( x.getClass() ) ) {
			case BYTE: return new Byte( 
					(byte)(accumulator.byteValue() * x.byteValue() + coefficient.byteValue()) 
					);
			
			case SHORT: return new Short( 
					(short)(accumulator.shortValue() * x.shortValue() + coefficient.shortValue()) 
					);
			
			case INT: return new Integer( 
						accumulator.intValue() * x.intValue() + coefficient.intValue() 
					);
			
			case LONG: return new Long( 
						accumulator.longValue() * x.longValue() + coefficient.longValue() 
					);
			
			case FLOAT: return new Float( 
					accumulator.floatValue() * x.floatValue() + coefficient.floatValue() 
				);
			
			case DOUBLE: return new Double( 
					accumulator.doubleValue() * x.doubleValue() + coefficient.doubleValue() 
				);
			
			default : throw new IllegalStateException("It seems " + x.getClass().getCanonicalName() 
							+ " is not a Primitive Numerical type... you probably passed in" 
							+ " BigInteger or some other fancy thing");
		}
	}
	
	
	/**
	 * returns numeric zero representation instance that is of class T.
	 * encapsulates exception checking.
	 * @param actualType
	 * @return
	 */
	private static <T extends Number> Number getNumericZero(T actualType) {
		Number zero = null;
		try {
			
			zero = actualType.getClass()
					.getConstructor(String.class)
					.newInstance( "0" );
			//king of a Hack.
			//all numeric types have a string based-constructor;
			//and "0" is a valid zero string representation of any type
			//I used this because getClass().newInstance() returned a null pointer
			//for some non-obvious reason...
			
		} catch(IllegalAccessException e) {
			//can't really happen, 
			//all subclasses of Number have public default constructors
		} catch (InstantiationException e) {
			//can't really happen
			//all default constructors of Number subclasses
			//can parse Strings to Number values
		} catch (IllegalArgumentException e) {
			//can't really happen
			//all numeric types can represent byte values
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			//can't really happen
			//Numeric classes ?hopefully? written without bugs
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			
			//can't really happen,
			//all numeric classes have string constructors
			e.printStackTrace();
		} catch (SecurityException e) {
			
			//might be thrown in an applet depending on the security settings
			e.printStackTrace();
		}
		
		return zero;
	}
	
	
	public enum TypeHandler {
		BYTE, SHORT, INT, LONG, FLOAT, DOUBLE;
		
		public static TypeHandler resolve(Class<? extends Number> classLiteral) {
			if(classLiteral == Byte.class) {
				return BYTE;
			} else if(classLiteral == Short.class) {
				return SHORT;
			} else if(classLiteral == Integer.class ) {
				return INT;
			} else if(classLiteral == Long.class) {
				return LONG;
			} else if(classLiteral == Float.class) {
				return FLOAT;
			} else if(classLiteral == Double.class) {
				return DOUBLE;
			} 
			
			throw new IllegalStateException("It seems " + classLiteral.getCanonicalName() 
					+ " is not a Primitive Numerical type... you probably passed in" 
					+ " BigInteger or some other fancy thing");
		}
		
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArrayList<Integer> coeffList = new ArrayList<Integer>(3);
		for(int x : new int[]{1,2,1}) {
			coeffList.add(x);
		}
		
		Polynomial<Integer> pInt = new Polynomial<Integer>(coeffList);
		
		for(int i = 0; i < 4; i++) {
			System.out.println(pInt.val(i).intValue());
		}
	}

}
