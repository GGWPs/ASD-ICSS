grammar ICSS;

//--- LEXER: ---
// IF support:
IF: 'if';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';

// STYLE ATTRIBUTES
STYLE_ATTRIBUTES: 'background-color' | 'width' | 'color' | 'height';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;

//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';

//--- PARSER: ---
//Level 0:
stylesheet: (variableassignment|stylerule)* EOF;

//Level 1:
variableassignment:variablereference ASSIGNMENT_OPERATOR expression SEMICOLON;
stylerule: selector OPEN_BRACE (variableassignment|declaration|if_statement)+ CLOSE_BRACE;

//Level 2
expression: literal #literalexpression | variablereference #varref | expression MUL expression #multiplyOperation | expression MIN expression #substractOperation| expression PLUS expression #addOperation;

variablereference: CAPITAL_IDENT;
selector: ID_IDENT #selectorId | CLASS_IDENT #selectorClass| LOWER_IDENT #selectorTag;
declaration: propertyName COLON (expression+) SEMICOLON | variableassignment;

//Level 3
if_statement: IF BOX_BRACKET_OPEN (variablereference|boolliteral) BOX_BRACKET_CLOSE OPEN_BRACE (declaration|if_statement)+ CLOSE_BRACE;
scalarliteral: SCALAR;
pixelliteral:PIXELSIZE;
percentageliteral:PERCENTAGE;
colorliteral: COLOR;
boolliteral: TRUE|FALSE;
literal: scalarliteral|pixelliteral|percentageliteral|colorliteral| boolliteral;
propertyName: STYLE_ATTRIBUTES;
