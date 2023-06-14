package com.Interpreter.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.Interpreter.lox.TokenType.*;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private static final Map<String, TokenType> keywords;
    private int start = 0;
    private int current = 0;
    private int line = 1;

    Scanner (String source) {
        this.source = source;
    }

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    List<Token> scanTokens() {
        while(!isAtEnd()) {
            // Starting from the beginning of the next lexeme
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;

            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
                
            //Special Attention to '/'
            case '/':
                if(match('/')) {
                    while( peek() != '\n' && !isAtEnd()) advance();
                }
                else if (match('*')) {
                	consume_block_comment();
                }
                else {
                    addToken(SLASH);
                }
                break;
            
            // A little meaningless whitespace and newline feed
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;
        
            case '\n':
                line++;
                break;
            
            //handling the String literals
            case '"': string(); break;

            case 'o':
                if (match('r')) {
                    addToken(OR);
                }
                break;
            // For checking the case if any unexpected character arrives
            // Although it may be ignored but we throw error for reporting
            // Characters like @#^
            default : 
            if (isDigit(c)) {
                number();
            }
            else if (isAlpha(c)) {
                identifier();
            }
            else {
                Lox.error(line, "Unexpected character.");
            }
            break;
        }
    }

    // Consumes the next character in the file and return it
    private char advance() {
        return source.charAt(current++);
    }

    // addToken is for output, grabs the text of the current lexeme and creates a new token for it
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    // It is like a conditional advance(), it's what we are looking for or nothing
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    // Smaller version of Advance where it is just a lookahead because it don't consume the lexeme
    private char peek() {
        if(isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        // If line ended without the termination of the string
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // The closing "
        advance();

        // Trim the surrounding quotes

        String value = source.substring(start+1, current-1);
        addToken(STRING, value);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void number() {
        // Going till end to find the end of the number literal
        while (isDigit(peek())) advance();

        // Look for a fractional part
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the .
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    // To have a peek at the next character, 
    // It is a separate function to handle the numbers to avoid the lookahead everytime peek is called 
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    // Checking if it's a alphabet
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }
    // Checking for alpha-numeric character
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;

        addToken(type);
    }
    
    private void consume_block_comment() {
    	advance();
    	
    	while ( peek() != '*' && !isAtEnd()) {
    		
    		if (isAtEnd()) {
    			Lox.error(line, "Unclosed Blocked Comment.");
    			return;
    		}
    		
    		char temp = advance();
    		
    		if ( peek() == '\n' ) line++;
    		
    		else if ( temp == '*' && peek() == '/' ) {
    			advance();
    			return;
    		}
    		else if (temp == '/' && peek() == '*') {
    			consume_block_comment();
    		}
    	}
    }
}
