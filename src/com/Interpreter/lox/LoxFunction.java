package com.Interpreter.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {
	private final Stmt.Functions declaration;
	private final Environment closure;
	private final boolean isInitializer;
	
	LoxFunction(Stmt.Functions declaration, Environment closure, boolean isInitializer) {
		this.declaration = declaration;
		this.closure = closure;
		this.isInitializer = isInitializer;
	}
	
	LoxFunction bind(LoxInstance instance) {
		Environment environment = new Environment(closure);
		environment.define("this", instance);
		
		return new LoxFunction(declaration, environment, isInitializer);
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
			if (isInitializer) return closure.getAt(0, "this");
			
			return returnValue.value;
		}
		
		if (isInitializer) return closure.getAt(0, "this");
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
