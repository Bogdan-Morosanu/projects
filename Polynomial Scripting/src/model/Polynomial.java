package model;

import java.util.ArrayList;


public class Polynomial {
	
	private final ArrayList<Integer> coeffs;
	private Polynomial integral;
	private Polynomial derivative;
	
	public Polynomial() {
		coeffs = new ArrayList<Integer>();
	}
	
	public Polynomial(ArrayList<Integer> coeffs) {
		//even if cast is unchecked, it is safe, 
		//since the object is returned by the clone method
		this.coeffs = (ArrayList<Integer>) coeffs.clone();
	}
	
	
	public Polynomial(int[] coeffs) {
		this.coeffs = new ArrayList<Integer>(coeffs.length);
		for( int c : coeffs ) {
			this.coeffs.add(c);
		}
	}
	
	public int val(int x) {
		int accum = 0;
		
		for(int n = coeffs.size()-1; n >= 0; n++) {
			accum = accum *  x + coeffs.get(n);
		}
		
		return accum;
	}
	
	public int getCoeff(int k) {
		if(k >= 0 && k < coeffs.size()) {
			return coeffs.get(k);
		} else {
			return 0;
		}
	}
	
	public int deg() {
		return coeffs.size() - 1;
	}
	
	public Polynomial add( Polynomial other ) {
		int maxDeg = Math.max(this.deg(), other.deg());
		
		ArrayList<Integer> newCoeffs = new ArrayList<Integer>();
		
		for( int i = 0; i <= maxDeg; i++ ) {
			newCoeffs.add( this.getCoeff(i) + other.getCoeff(i) );
		}
		
		return new Polynomial( newCoeffs );
	}
	
	public Polynomial sub( Polynomial other ) {
		int maxDeg = Math.max(this.deg(), other.deg());
		
		ArrayList<Integer> newCoeffs = new ArrayList<Integer>(maxDeg + 1);
		
		for( int i = 0; i <= maxDeg; i++ ) {
			newCoeffs.add( this.getCoeff(i) - other.getCoeff(i) );
		}
		
		return new Polynomial( newCoeffs );		
	}
	
	
	public Polynomial mul( Polynomial other ) {
		Polynomial[] tempRes = new Polynomial[ other.deg() + 1 ];
		
				
		//create intermediate results, 
		//they will be the polynomials resulting from multiplying this
		//with a singly term of the other polynomial
		for(int k = 0; k <= other.deg(); k++) {
			tempRes[k] = monomialMultiply( other.getCoeff(k), k );
		}
		
		Polynomial p = new Polynomial();
		for(Polynomial temp : tempRes) {
			p = p.add( temp );
		}
		
		return p;
	}
	
	public Polynomial monomialMultiply( int scalar, int power ) {
		
		ArrayList<Integer> multipliedCoeffs = new ArrayList<Integer>( deg() + 1 + power );

		
		//we need to multiply each coefficient with our scalar
		for( int i = 0; i < coeffs.size(); i++ ) {
			multipliedCoeffs.add( coeffs.get(i) * scalar );
		}
		
		//then raise our degree by power zeros at the end
		int zerosAdded = 0;
		while(zerosAdded < power) {
			zerosAdded++; 
			multipliedCoeffs.add( 0, 0 );
		
		}
		
		return new Polynomial( multipliedCoeffs );
	}
	
	public Polynomial div( Polynomial divisor ) {
		return new Polynomial( divisionCoeffs(divisor) );
	}
	
	private ArrayList<Integer> divisionCoeffs(Polynomial divisor) {
		if( divisor.deg() > this.deg()) {
			return new ArrayList<Integer>();
		} else {
			
			int cntCoeff = getHead() / divisor.getHead(); 
			int degreesDifference = deg() - divisor.deg();
			
			//must drop head to ensure termination of algorithm
			Polynomial stillToDivide = this
										.sub( divisor.monomialMultiply( cntCoeff, degreesDifference ) )
										.dropHead();
			
			
			ArrayList<Integer> soFar = stillToDivide.divisionCoeffs( divisor );
			soFar.add( cntCoeff );
			
			
			return soFar;
		}
	}
	
	public Polynomial rem(Polynomial divisor ) {
		return this.sub( this.div( divisor ).mul( divisor ) );
	}
	
	public Polynomial dropHead() {
		ArrayList<Integer> newCoeffs = new ArrayList<Integer>();
		for(int i = 0; i < deg(); i++) {
			newCoeffs.add( coeffs.get(i) );
		}
		
		return new Polynomial( newCoeffs );
	}
	
	
	public Integer getHead() {
		return getCoeff( deg() );
	}
	
	
	public Polynomial getIntegral() {
		if(integral == null) {
			return integral = computeIntegral();
		} else {
			return integral;
		}
	}
	
	public Polynomial getDerivative() {
		if(derivative == null) {
			return derivative = computeDerivative();
		} else {
			return derivative;
		}
	}
	
	/**
	 * since our coefficients are integers, 
	 * computing the integral does not always make sense 
	 * since our coeffs might not be divisible by their new rank.
	 * thus p.equals( p.getIntegral().getDerivative() ) might
	 * fail sometimes unexpectedly.
	 * @return
	 */
	private Polynomial computeIntegral() {
		ArrayList<Integer> newCoeffs = new ArrayList<Integer>();
		
		//shift in lower order zero
		newCoeffs.add(0);
		
		for(int i = 0; i < coeffs.size(); i++) {
			newCoeffs.add( coeffs.get(i) / (i + 1) );
		}
		
		return new Polynomial( newCoeffs );
	}
	
	private Polynomial computeDerivative() {
		ArrayList<Integer> newCoeffs = new ArrayList<Integer>();
		
		for(int i = 1; i < coeffs.size(); i++) {
			newCoeffs.add( i * coeffs.get(i) );
		}
		
		return new Polynomial( newCoeffs );
	}
	/**
	 * @param args
	 */
	
	@Override 
	public String toString() {
		String accum = "[";
		
		//higher order coeff
		
		if(coeffs.size() > 0) {
			accum += coeffs.get( coeffs.size() - 1 );
		}
		
		//lower order coeffs
		for(int n = coeffs.size() - 2; n > 0; n--) {
			accum += "x^" + (n+1) + " + " + coeffs.get(n);
		}
		
		
		//free term
		if(coeffs.size() > 1) {
			accum += "x^1 + " + coeffs.get(0);
		}
		
		accum += "]";
		
		return accum;
	}
	
	@Override 
	public boolean equals( Object other ) {
		
		return false;
	}
	
	
	public int eval( int x ) {
		int accum = 0;
		for(int i = coeffs.size() - 1; i >= 0; i--) {
			accum = accum * x + coeffs.get(i);
		}
		return accum;
	}
	
	public ArrayList<Integer> eval( ArrayList<Integer> xs ) {
		
		ArrayList<Integer> res = new ArrayList<Integer>();
		
		for(int x : xs ) {
			res.add( this.eval(x) );
		}
		
		return res;
	}
	
	public static void main(String[] args) {
		ArrayList<Integer> coeffs = new ArrayList<Integer>();
		for(int c : new int[] {1,1,1} ) {
			coeffs.add(c);
		}
		
		Polynomial p1 = new Polynomial(coeffs);
		Polynomial p2 = new Polynomial(new int[]{1,1,1});
		
		//System.out.println(p1);
		//System.out.println(p2);
		//System.out.println(p2.add(p1));
		
		//System.out.println(p1.mul(p2));
		
		Polynomial p3 = new Polynomial(new int[]{ 0, 1 });
		
		System.out.println(p2.div(p3));
		System.out.println("-------");
		System.out.println(p2.rem(p3));
		
		System.out.println( p1.getDerivative() );

		System.out.println( p1.getDerivative().getIntegral() );
	}

}
