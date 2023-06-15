package com.Interpreter.lox;

import com.Interpreter.lox.Expr.Binary;
import com.Interpreter.lox.Expr.Grouping;
import com.Interpreter.lox.Expr.Literal;
import com.Interpreter.lox.Expr.Unary;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>{
	
	final Environment globals = new Environment();
	private Environment environment = globals;
	private final Map<Expr, Integer> locals = new HashMap<>();
	
	Interpreter() {
		globals.define("clock", new LoxCallable() {
			@Override
			public int arity() { return 0; }
			
			@Override
			public Object call(Interpreter interpreter, List<Object> arguments) {
				return (double)System.currentTimeMillis() / 1000.0;
			}
			
			@Override
			public String toString() {
				return "<native fn>";
			}
		});				
		
		globals.define("printf", new LoxCallable() {
			@Override
			public int arity() { return 1; } //globals.return_size(); }
			
			@Override
			public Object call(Interpreter interpreter, List<Object> arguments) {
				
				String s = new String("");
				
				for (Object argument : arguments) {
					String temp = String.valueOf(argument);
					s += temp;
				}
				
				System.out.println(s);
				
				return s;
			}
			
			@Override
			public String toString() {
				return "<native fn>";
			}
		});		
	}
	
	void interpret (List<Stmt> statements) {
		try {
			for (Stmt statement : statements) {
				execute(statement);
			}
		}
		catch (RuntimeError error) {
			Lox.runtimeError(error);
		}
	}
	
	@Override
	public Object visitBinaryExpr(Binary expr) {
		// TODO Auto-generated method stub
		Object left = evaluate(expr.left);
		Object right = evaluate(expr.right);
		
		switch (expr.operator.type) {
			case BANG_EQUAL:
			return !isEqual(left, right);
			case EQUAL:
			return isEqual(left, right);
			case GREATER:
				checkNumberOperands(expr.operator, left, right);
				return (double) left > (double) right;
			case GREATER_EQUAL:
				checkNumberOperands(expr.operator, left, right);
				return (double) left >= (double) right;
			case LESS:
				checkNumberOperands(expr.operator, left, right);
				return (double) left < (double) right;
			case LESS_EQUAL:
				checkNumberOperands(expr.operator, left, right);
				return (double) left <= (double) right;		
			case MINUS: 
				checkNumberOperands(expr.operator, left, right);
				return (double) left - (double) right;
			case PLUS:
				if ( left instanceof Double && right instanceof Double) {
					return (double) left + (double) right;
				}
				
				if ( left instanceof String && right instanceof String) {
					return (String) left + (String) right;
				}
				
				if ((left instanceof String && right instanceof Double)) {
					
					String text = right.toString();
					if (text.endsWith(".0")) {
						text = text.substring(0, text.length()-2);
					}
					return  ((String) left).concat(text);
				}
				
				throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings or either of them.");
				
			case SLASH:
				checkNumberOperands(expr.operator, left, right);
				return (double) left / (double) right;
			case STAR:
				checkNumberOperands(expr.operator, left, right);
				return (double) left * (double) right;
		}
		return null;
	}
	
	@Override 
	public Object visitCallExpr(Expr.Call expr) {
		Object callee = evaluate(expr.callee);
		
		List<Object> arguments = new ArrayList<>();
		
		for (Expr argument : expr.arguments) {
			arguments.add(evaluate(argument));
		}
		
		if (!(callee instanceof LoxCallable)) {
			throw new RuntimeError (expr.paren, "Can only call functions and classes.");
		}
		
		LoxCallable function = (LoxCallable)callee;
		
		if (arguments.size() != function.arity()) {
			throw new RuntimeError(expr.paren, "Expected " + function.arity() + " argument but got " + arguments.size() + ".");
		}
		return function.call(this, arguments);
	}
	
	@Override
	public Object visitGetExpr (Expr.Get expr) {
		Object object = evaluate (expr.object);
		if (object instanceof LoxInstance) {
			return ((LoxInstance) object).get(expr.name);
		}
		
		throw new RuntimeError(expr.name, "Only instance have properties.");
	}

	@Override
	public Object visitGroupingExpr(Grouping expr) {
		// TODO Auto-generated method stub
		return evaluate(expr.expression);
	}

	@Override
	public Object visitLiteralExpr(Literal expr) {
		// TODO Auto-generated method stub
		return expr.Value;
	}
	
	@Override 
	public Object visitLogicalExpr(Expr.Logical expr) {
		Object left = evaluate(expr.left);
		
		if (expr.operator.type == TokenType.OR) {
			if(isTruthy(left)) return left;
		} else {
			if (!isTruthy(left)) return left;
		}
		
		return evaluate(expr.right);
	}

	@Override
	public Object visitUnaryExpr(Unary expr) {
		// TODO Auto-generated method stub
		
		Object right = evaluate(expr.right);
		
		switch(expr.operator.type) {
			case BANG:
				return !isTruthy(right);
			case MINUS:
				checkNumberOperand(expr.operator, right);
				return -(double)right;
		}
		
		return null;
	}
	
	private Object evaluate(Expr expr) {
		return expr.accept(this);
	}
	
	private boolean isTruthy(Object object) {
		if (object == null) return false;
		if (object instanceof Boolean) return (boolean)object;
		
		return true;
	}
	
	private boolean isEqual(Object a, Object b) {
		if (a == null && b == null) return true;
		if (a == null) return false;
		
		return a.equals(b);
	}
	
	private void checkNumberOperand(Token operator, Object operand) {
		if (operand instanceof Double) return;
		throw new RuntimeError(operator, "Operand must be a number");
	}
	
	private void checkNumberOperands(Token token, Object left, Object right) {
		
		if (token.type.equals(TokenType.SLASH) && right instanceof Double  && right.equals(0d))	throw new RuntimeError(token, "Divide by Zero '0'");
		
		if (left instanceof Double && right instanceof Double) return;
		
		throw new RuntimeError(token, "Operands must be numbers");
	}
	
//	void interpret (Expr expression) {
//		try {
//			Object value = evaluate(expression);
//			
//			System.out.println(stringify(value));
//		}
//		catch(RuntimeError error) {
//			Lox.runtimeError(error);
//		}
//	}
	
	private void execute(Stmt stmt) {
		stmt.accept(this);
	}
	
	void resolve(Expr expr, int depth) {
		locals.put(expr, depth);
	}
	
	private String stringify(Object object) {
		if (object == null) return "nil";
		
		if (object instanceof Double) {
			String text = object.toString();
			if (text.endsWith(".0")) {
				text = text.substring(0, text.length()-2);
			}
			return text;
		}
		
		return object.toString();
	}
	
	
	@Override
	public Void visitExpressionStmt(Stmt.Expression stmt) {
		evaluate(stmt.expression);
		return null;
	}
	
	@Override 
	public Void visitFunctionsStmt(Stmt.Functions stmt) {
		
		LoxFunction function = new LoxFunction(stmt, environment);
		environment.define(stmt.name.lexeme, function);
		
		return null;
	}
	
	@Override 
	public Void visitIfStmt(Stmt.If stmt) {
		if (isTruthy(evaluate(stmt.condition))) {
			execute(stmt.thenBranch);
		}
		else if (stmt.elseBranch != null) {
			execute(stmt.elseBranch);
		}
		
		return null;
	}
	
	@Override
	public Void visitPrintStmt (Stmt.Print stmt) {
		Object value = evaluate(stmt.expression);
		System.out.println(stringify(value));
		return null;
	}
	
	@Override
	public Void visitReturnStmt (Stmt.Return stmt) {
		Object value = null;
		if (stmt.value != null) value = evaluate(stmt.value);
		
		throw new Return(value);
	}
	
	@Override
	public Void visitVarStmt(Stmt.Var stmt) {
		Object value = null;
		if (stmt.initializer != null) {
			value = evaluate(stmt.initializer);
		}
		
		environment.define(stmt.name.lexeme, value);
		return null;
	}
	
	@Override
	public Void visitWhileStmt(Stmt.While stmt) {
		while(isTruthy(evaluate(stmt.condition))) {
			execute(stmt.body);
		}
		return null;
	}
	
	@Override
	public Void visitClassStmt (Stmt.Class stmt) {
		environment.define(stmt.name.lexeme, null);
		LoxClass klass = new LoxClass(stmt.name.lexeme);
		environment.assign(stmt.name, klass);
		return null;
	}
	
	@Override
	public Object visitVariableExpr(Expr.Variable expr) {
		//return environment.get(expr.name);
		return lookupVariable(expr.name, expr);
	}
	
	@Override
	public Object visitAssignExpr(Expr.Assign expr) {
		Object value = evaluate(expr.value);
		
		Integer distance = locals.get(expr);
		if (distance != null) {
			environment.assignAt (distance, expr.name, value);
		}
		else {
			globals.assign(expr.name, value);
		}
		
		//environment.assign(expr.name, value);
		return value;
	}
	
	@Override
	public Void visitBlockStmt(Stmt.Block stmt) {
		executeBlock(stmt.statements, new Environment(environment));
		return null;
	}
	
	void executeBlock(List<Stmt> statements, Environment environment) {
		Environment previous = this.environment;
		
		try {
			this.environment = environment;
			
			for (Stmt statement : statements) {
				execute(statement);
			}
		} finally {
			this.environment = previous;
		}
	}
	
	private Object lookupVariable (Token name, Expr expr) {
		Integer distance = locals.get(expr);
		
		if (distance != null ) {
			return environment.getAt(distance, name.lexeme);
		}
		else {
			return globals.get(name);
		}
	}
}
