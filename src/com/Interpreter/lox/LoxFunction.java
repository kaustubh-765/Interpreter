package com.Interpreter.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {
	private final Stmt.Functions declaration;
	private final Environment closure;
	
	LoxFunction(Stmt.Functions declaration, Environment closure) {
		this.declaration = declaration;
		this.closure = closure;
	}
	
	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
//		Environment environment = new Environment(interpreter.globals);
		Environment environment = new Environment(closure);
		
		
		for (int i=0 ; i < declaration.params.size() ; i++ ) {
			environment.define(declaration.params.get(i).lexeme, arguments.get(i));
		}
//		interpreter.executeBlock(declaration.body, environment);
		
		try {
			interpreter.executeBlock(declaration.body, environment);
		}
		catch (Return returnValue) {
			return returnValue.value;
		}
		return null;
	}

	@Override
	public int arity() {
		// TODO Auto-generated method stub
		return declaration.params.size();
	}
	
	@Override
	public String toString() {
		return "<fn " + declaration.name.lexeme + ">";
	}
}
