/* Copyright (c) 2015-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package expressivo;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

/**
 * Tests for the Expression abstract data type.
 */
public class ExpressionTest {
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    @Test
    public void testCaseSensitive() {
    	Expression exp1 = Expression.parse("x");
    	assertEquals("expected expression", "x", exp1.toString());
    	Expression exp2 = Expression.parse("X");
    	assertEquals("expected expression", "X", exp2.toString());
    	assertFalse("expected expression not equal", exp1.equals(exp2));
    	
    	Expression simplified = exp1.simplify(Map.of("x", 1.0));
    	assertEquals("expected simplify", Expression.parse("1.0"), simplified);
    	simplified = exp2.simplify(Map.of("x", 1.0));
    	assertEquals("expected simplify", Expression.parse("X"), simplified);
    }
    
    @Test
    public void testNoDeep() {
    	Expression exp1 = Expression.parse("x+y");
    	assertEquals("expected expression", "x+y", exp1.toString());
    	
    	Expression diff = exp1.differentiate("x");
    	assertEquals("expected differentiate", "1+0", diff.toString());
    	
    	Expression simplified = exp1.simplify(Map.of("x", 1.0));
    	assertEquals("expected differentiate",
    			Expression.add(Expression.parse("1.0"), Expression.parse("y")),
    			simplified);
    }
    @Test
    public void testBaseDifferentiate() {
    	Expression exp = Expression.parse("12");
    	Expression diff = exp.differentiate("y");
    	assertEquals("expected differentiate zero", Expression.parse("0"), diff);
    	
    	exp = Expression.parse("x");
    	diff = exp.differentiate("y");
    	assertEquals("expected differentiate zero", Expression.parse("0"), diff);
    	diff = exp.differentiate("x");
    	assertEquals("expected differentiate zero", Expression.parse("1"), diff);
    	
    	exp = Expression.parse("x+y");
    	diff = exp.differentiate("y");
    	assertEquals("expected differentiate zero", Expression.parse("0+1"), diff);
    }
    @Test
    public void testLeftChild() {
    	Expression exp1 = Expression.parse("x*y+100");
    	assertEquals("expected expression", "(x*y)+100", exp1.toString());
    	
    	Expression exp2 = Expression.add(Expression.parse("x * y"), Expression.parse("100"));
    	assertEquals("expected expression", "(x*y)+100", exp2.toString());
    	assertTrue("expected expression equals", exp1.equals(exp2));
    	assertEquals("expected hash code equals", exp1.hashCode(), exp2.hashCode());
    	
    	Expression diff = exp1.differentiate("y");
    	assertEquals("expected differentiate zero", Expression.parse("(x*1)*(y*0)+0"), diff);
    	diff = exp2.differentiate("y");
    	assertEquals("expected differentiate zero", Expression.parse("(x*1)*(y*0)+0"), diff);
    	
    	Expression simplified = exp1.simplify(Map.of("x", 1.0));
    	assertEquals("expected differentiate",
    			Expression.add(Expression.parse("1.0*y"), Expression.parse("100")),
    			simplified);
    	simplified = exp2.simplify(Map.of("x", 1.0));
    	assertEquals("expected differentiate",
    			Expression.add(Expression.parse("1.0*y"), Expression.parse("100")),
    			simplified);
    }
   
    @Test
    public void testRightChild() {
    	Expression exp1 = Expression.parse("200+x*y");
    	assertEquals("expected expression", "200+(x*y)", exp1.toString());
    	
    	Expression exp2 = Expression.add(Expression.parse("200"), Expression.parse("x*y"));
    	assertEquals("expected expression", "200+(x*y)", exp2.toString());
    	assertTrue("expected expression equals", exp1.equals(exp2));
    	assertEquals("expected hash code equals", exp1.hashCode(), exp2.hashCode());
    	
    	Expression diff = exp1.differentiate("y");
    	assertEquals("expected differentiate zero", Expression.parse("0+(x*1)*(y*0)"), diff);
    	
    	Expression simplified = exp1.simplify(Map.of("x", 1.0, "y", 2.0));
    	assertEquals("expected differentiate", Expression.parse("202"), simplified);
    }
    
    @Test
    public void testTwoDeepLeftGroup() {
    	Expression exp1 = Expression.parse("(x*y)*z+200");
    	assertEquals("expected expression", "((x*y)*z)+200", exp1.toString());
    	
    	Expression diff = exp1.differentiate("y");
    	assertEquals("expected differentiate zero", Expression.parse("(((x*y)*0)*(z*((x*1)*(y*0))))+0"), diff);
    	
    	Expression simplified = exp1.simplify(Map.of("z", 1.0, "j", 2.0));
    	assertEquals("expected differentiate", Expression.parse("((x*y)*1.0)+200"), simplified);
    }
    @Test
    public void testOneDeepLeftTwoDeepRigft() {
    	Expression exp1 = Expression.parse("x*y*z+200");
    	assertEquals("expected expression", "(x*(y*z))+200", exp1.toString());
    	
    	Expression diff = exp1.differentiate("x");
    	assertEquals("expected differentiate zero", Expression.parse("((x*((y*0)*(z*0)))*((y*z)*1))+0"), diff);
    	
    	Expression simplified = exp1.simplify(Map.of("x", 2.0, "y", 2.0, "z", 2.0));
    	assertEquals("expected differentiate", Expression.parse("208"), simplified);
    }
    @Test
    public void testOneDeepRightTwoDeepLeft() {
    	Expression exp1 = Expression.parse("200+(x*y)*z");
    	assertEquals("expected expression", "200+((x*y)*z)", exp1.toString());
    	
    	Expression diff = exp1.differentiate("x");
    	assertEquals("expected differentiate zero", Expression.parse("0+(((x*y)*0)*(z*((x*0)*(y*1))))"), diff);
    	
    	Expression simplified = exp1.simplify(Map.of("x", 2.0, "y", 2.0, "j", 2.0));
    	assertEquals("expected differentiate", Expression.parse("200+(4.0*z)"), simplified);
    }
   
    @Test
    public void testMoreDeepLevel() {
    	Expression exp1 = Expression.parse("x*y*z*200*300*j");
    	assertEquals("expected expression", "x*(y*(z*(200*(300*j))))", exp1.toString());
    	
    	Expression diff = exp1.differentiate("y");
    	assertEquals("expected differentiate zero", Expression.parse("(x*((y*((z*((200*((300*0)*(j*0)))*((300*j)*0)))*((200*(300*j))*0)))*((z*(200*(300*j)))*1)))*((y*(z*(200*(300*j))))*0)"), diff);
    
    	Expression simplified = exp1.simplify(Map.of("x", 2.0, "y", 2.0, "z", 2.0, "j", 1.0));
    	assertEquals("expected differentiate", Expression.parse("480000"), simplified);
    }
    @Test
    public void testConstant() {
    	// decimal places 0, 1, 2-4, 5, >5
    	Expression expression = Expression.parse("100");
    	assertEquals("expected number expression", "100", expression.toString());
    	assertTrue("expected equals", Expression.parse("100").equals(expression));
    	assertEquals("expected hash code equal", 
    			Expression.parse("100").hashCode(), expression.hashCode());
    	
    	expression = Expression.parse("4.5");
    	assertEquals("expected number expression", "4.5", expression.toString());
    	assertTrue("expected equals", Expression.parse("4.5").equals(expression));
    	assertEquals("expected hash code equal", 
    			Expression.parse("4.5").hashCode(), expression.hashCode());
    	
    	expression = Expression.parse("4.56");
    	assertEquals("expected number expression", "4.56", expression.toString());
    	assertTrue("expected equals", Expression.parse("4.56").equals(expression));
    	assertEquals("expected hash code equal", 
    			Expression.parse("4.56").hashCode(), expression.hashCode());
    	
    	expression = Expression.parse("4.56789");
    	assertEquals("expected number expression", "4.5678", expression.toString());
    	assertTrue("expected equals", Expression.parse("4.56781").equals(expression));
    	assertEquals("expected hash code equal", 
    			Expression.parse("4.56781").hashCode(), expression.hashCode());
    	
    	expression = Expression.parse("4.567891");
    	assertEquals("expected number expression", "4.5678", expression.toString());
    	assertTrue("expected equals", Expression.parse("4.567856").equals(expression));
    	assertEquals("expected hash code equal", 
    			Expression.parse("4.567856").hashCode(), expression.hashCode());
    	
    	Expression simplified = expression.simplify(Map.of("x", 1.0));
    	assertEquals("expected simplify", "4.5678", simplified.toString());
    }
}
