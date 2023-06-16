package com.Interpreter.lox;

import com.Interpreter.lox.Expr.Assign;
import com.Interpreter.lox.Expr.Call;
import com.Interpreter.lox.Expr.Get;
import com.Interpreter.lox.Expr.Logical;
import com.Interpreter.lox.Expr.Set;
import com.Interpreter.lox.Expr.Super;
import com.Interpreter.lox.Expr.This;
import com.Interpreter.lox.Expr.Variable;

class AstPrinter implements Expr.Visitor<String> {
	String print(Expr expr) {
		return expr.accept(this);
	}
	
	@Override
	public String visitBinaryExpr(Expr.Binary expr) {
		return parenthesize(expr.operator.lexeme, expr.left, expr.right);
	}
	
	@Override
	public String visitGroupingExpr(Expr.Grouping expr) {
		return parenthesize("group", expr.expression);
	}
	
	@Override 
	public String visitLiteralExpr(Expr.Literal expr) {
		if (expr.Value == null) return "nil";
		return expr.Value.toString();
	}
	
	@Override 
	public String visitUnaryExpr(Expr.Unary expr) {
		return parenthesize(expr.operator.lexeme, expr.right);
	}
	
	private String parenthesize(String name, Expr... exprs) {
		StringBuilder builder = new StringBuilder();
		
		builder.append("(").append(name);
		for (Expr expr : exprs) {
			builder.append(" ");
			builder.append(expr.accept(this));
		}
		
		builder.append(")");
		
		return builder.toString();
	}

	@Override
	public String visitVariableExpr(Variable expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitAssignExpr(Assign expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitLogicalExpr(Logical expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitCallExpr(Call expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitGetExpr(Get expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitSetExpr(Set expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitThisExpr(This expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String visitSuperExpr(Super expr) {
		// TODO Auto-generated method stub
		return null;
	}
	
//	// Tester Code
//	public static void main(String[] args) {
//	    Expr expression = new Expr.Binary(
//	        new Expr.Unary(
//	            new Token(TokenType.MINUS, "-", null, 1),
//	            new Expr.Literal(123)),
//	        new Token(TokenType.STAR, "*", null, 1),
//	        new Expr.Grouping(
//	            new Expr.Literal(45.67)));
//
//	    System.out.println(new AstPrinter().print(expression));
//	  }

}
