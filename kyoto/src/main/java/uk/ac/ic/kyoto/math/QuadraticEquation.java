package uk.ac.ic.kyoto.math;
/**
 * 
 * @author Adam
 */
public class QuadraticEquation {
	private double a;
	private double b;
	private double c;
	
	public QuadraticEquation(double a, double b, double c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	public double getRootOne() {
		double root;
		double determinant = getDeterminant();
		root = ((-b - Math.sqrt(determinant)) / (2 * a) );
		return root;
	}
	
	public double getRootTwo() {
		double root;
		double determinant = getDeterminant();
		root = ((-b + Math.sqrt(determinant)) / (2 * a) );
		return root;
	}
	
	private double getDeterminant() {
		return (b * b - 4 * a * c);
	}
}
