/* loose threads:
 *   localDef -> valueDefinition | functionDefinition
 *     the alternatives conflict; need to be left-factored, I think
 *
 * QUOTE_LITERAL doesn't do the right thing with "0<x" (but "0< x" is fine)
 *
 */
grammar Cml;
options {
    language=Java;
    output=AST;
}

@members {
public String getErrorMessage(RecognitionException e, String[] tokenNames) {
    List stack = getRuleInvocationStack(e, this.getClass().getName());
    String msg = null;
    if (e instanceof NoViableAltException) {
        NoViableAltException nvae = (NoViableAltException)e;
        msg = " no viable alt; token="+e.token+
            " (decision="+nvae.decisionNumber+
            " state "+nvae.stateNumber+")"+
            " decision=<<"+nvae.grammarDecisionDescription+">>";
    } else {
        msg = super.getErrorMessage(e, tokenNames);
    }
    return stack+" "+msg;
}
public String getTokenErrorDisplay(Token t) {
    return t.toString();
}
}

source
    : programParagraph+
    ;
    
programParagraph
    : classDefinition
    | processDefinition
    | channelDefs
    | chansetDefs
    | typeDefs
    | valueDefs
    | functionDefs
    ;

classDefinition
    : 'class' IDENTIFIER ('extends' IDENTIFIER)? '=' 'begin' classDefinitionBlock* 'end'
    ;

processDefinition
    : 'process' IDENTIFIER '=' (declaration (';' declaration)* '@')? process
    ;

process
    : proc0 procOps process
    | replOp replicationDeclaration '@' ( '[' expression ']' )? process
    ;

procOps
    : ';' | '[]' | '|~|' | '||' | '|||'
    | '/\\' | '//' expression '\\\\' // not sure if the empty /\ and [> should be here
    | '[>' | '[[' expression '>>'
    | '[|' expression '|]'
    | '[' expression '||' expression ']'
    ;

proc0
    : proc1 ('[[' renamingExpr ']]')?
    ;

replOp
    : ';' | '[]' | '|~|' | '||' | '|||'
    | '[|' expression '|]'
    ;

proc1
    : proc2 proc1ops expression
    ;

proc1ops
    : '\\\\' | 'startsby' | 'endsby'
    ;

proc2
    : 'begin' processParagraph* '@' action 'end'
    // merge of (process) | identifier [({expression})] | (decl@proc)({expression})
    | ( IDENTIFIER | '(' (declaration (';' declaration)* '@')? process ')' ) ( '(' ( expression ( ',' expression )* )? ')'  )?
    ;    

declaration
    : PMODE? IDENTIFIER (',' IDENTIFIER)* (':' type)?
    ;

replicationDeclaration
    : replicationDecl (';' replicationDecl)*
    ;

replicationDecl
    : IDENTIFIER ( ',' IDENTIFIER )* ( ':' type | 'in' 'set' expression )  // FIXME
    ;

renamingExpr
    : renamePair ( ( ',' renamePair )+ | '|' bind+ ('@' expression)? )?
    ;

renamePair
    : IDENTIFIER ( '.' '(' expression ')' )* '<-' IDENTIFIER ( '.' '(' expression ')' )*
    ;

processParagraph
    : typeDefs
    | valueDefs
    | stateDefs 
    | functionDefs
    // | operationDefs
    | actionDefs
    | namesetDefs
    ;

actionDefs
    : 'actions' actionDef* 
    ;

actionDef
    : IDENTIFIER '=' (declaration '@')? action
    ;

action
    : action0 actionOps action
    | ( replOp | actionReplOp ) replicationDeclaration '@' ( '[' expression ( '|' expression )? ']' )? process
    ;

actionOps
    : ';' | '[]' | '|~|' 
    | '/\\' | '//' expression '\\\\' // not sure if the empty /\ and [> should be here
    | '[>' | '[[' expression '>>'
    | '||' | '|||'
    | '['  expression ( '|'  expression )? '||'  expression ( '|'  expression )? ']' 
    | '[|' expression ( '|'  expression ( '|'  expression )? )? '|]' 
    | '[||' expression '|' expression '||]' 
    ;

actionReplOp
    : '[||' expression '||]'
    ;

action0
    : action1 ( '[[' renamingExpr ']]' )?
    ;

action1
    : action2 action1Ops expression
    ;

action1Ops
    : '\\\\' | 'startsby' | 'endsby'
    ;

action2
    : 'Skip' | 'Stop' | 'Chaos' | 'Div' | 'Wait' expression
    /* The mess below includes parenthesized actions, block
     * statements, parametrised actions, instantiated actions.
     */
    | '(' ( ( declaration (';' declaration)* | 'dcl' assignmentDefinition (';' assignmentDefinition)* ) '@' )? action ')' ( '(' expression (',' expression )* ')' )?
    | IDENTIFIER communication* '->' action
    | '[' expression ']' '&' action // Still need [] around the expr; conflict: action2 -> (action) and expression...-> (expression)
    | 'mu' IDENTIFIER '@'  action (',' action)*
    | statement
    ;

/* FIXME Ok, dots are still fragile
 *
 * In this case, it's IDENTIFIER '.' IDENTIFIER that kills us.  I
 * think we could get away with splitting the '.' case so that
 * arbitary expressions are ()'d, but constants and identifiers are
 * clear.
 *
 * ( '!' expression '.' IDENTIFER ) still creates an ambiguity as to
 * whether, say, ...!x.y was really ...(!x).y or ...!(x.y)
 */
communication
    : '.' '(' expression ')'
    // | '.' IDENTIFIER
    | '!' expression
    | '?' bindablePattern
    ;

statement
    : 'let' localDefinition (',' localDefinition)* 'in' action
    | '[' ('frame' frameSpec (',' frameSpec)* )? ('pre' expression)? 'post' expression ']'
    | 'do' nonDetStmtAlt ( '[]' nonDetStmtAlt )* 'end'
    | 'if' expression 
        ( '->' action ( '[]' nonDetStmtAlt )* 'end'
        | 'then' action ('elseif' expression 'then' action)* 'else' action
        )
    | 'cases' expression ':' (pattern (',' pattern)* '->' action (',' pattern (',' pattern)* '->' action)* )? (',' 'others' '->' expression)? 'end'
    | 'for' 
        (  bindablePattern 'in' expression // was pattern bind here only
        | 'all' bindablePattern 'in' 'set' expression
        | IDENTIFIER '=' expression 'to' expression ( 'by' expression )?
        ) 'do' action
    | 'return' expression?
    | 'while' expression 'do' action
    | stateDesignator ':=' ( expression | 'new' name '(' expression ( ',' expression )* ')' )//| callStatement )
    // | callStatement
    | 'atomic' '(' stateDesignator ':=' expression ( ';' stateDesignator ':=' expression )+ ')'
    ;

nonDetStmtAlt
    : expression '->' action
    ;

frameSpec
	: FRAMEMODE name (',' name)* (':' type)?
    ;

stateDesignator
    : name sDTail?
    ;

sDTail
    : '(' expression ')' ( '.' stateDesignator | sDTail )?
    ;

callStatement
    : name '(' ( expression ( ',' expression )* )? ')'
    // : ( objectDesignator '.' )? name '(' ( expression ( ',' expression )* )? ')'
    ;

// objectDesignator
// 	: ( name | 'self' ) oDTail?
//     ;

// oDTail
// 	: '(' expression ')' ( '.' objectDesignator | oDTail )?
// 	;

/* Ok, this is cute.  It works both with and without semicolons,
 * though it may not be visually obvious (without the semis) that it
 * is a space that separates untyped channels from typed ones. (-jwc)
 */
channelDefs
    : 'channels' channelDef*
    // : 'channels' ( channelDef (';' channelDef)+ )?
    ;

channelDef
    : IDENTIFIER (',' IDENTIFIER)* (':' type)?
    ;

chansetDefs
    : 'chansets' chansetDef*
    // : 'chansets' ( chansetDef (';' chansetDef)+ )?
    ;

chansetDef
    : IDENTIFIER '=' expression
    // : IDENTIFIER '=' chansetExpr
    ;

namesetDefs
    : 'namesets' namesetDef*
    // : 'chansets' ( chansetDef (';' chansetDef)+ )?
    ;

namesetDef
    : IDENTIFIER '=' expression
    // : IDENTIFIER '=' namesetExpr
    ;

classDefinitionBlock
    : typeDefs
    | valueDefs
    | stateDefs
    | functionDefs
//  | operationDefs
//  | INITIAL operationDef
    ;

valueDefs
    : 'values' ( QUALIFIER? valueDefinition (';' QUALIFIER? valueDefinition)* )? ';'?
    ;

stateDefs
    : 'state' ( instanceVariableDefinition (';' instanceVariableDefinition)* )? ';'?
    ;

instanceVariableDefinition
    : QUALIFIER? assignmentDefinition
    | invariantDefinition
    ;

assignmentDefinition
    : bindablePattern ':' type ( ( ':=' | 'in' ) expression )?
    ;

invariantDefinition
    : 'inv' expression
    ;

functionDefs
    : 'functions' (QUALIFIER? functionDefinition)*
    ;

valueDefinition
    : bindablePattern (':' type)? ( '=' | 'be' 'st' ) expression
    ;

functionDefinition
    : IDENTIFIER (explicitFunctionDefintionTail | implicitFunctionDefintionTail)
    ;

explicitFunctionDefintionTail
    : ':' type IDENTIFIER parameterGroup+ '==' functionBody ('pre' expression )? ('post' expression)? ('measure' name)?
    ;

implicitFunctionDefintionTail
    : '(' parameterTypeList ')' IDENTIFIER ':' type (',' IDENTIFIER ':' type)* ('pre' expression )? 'post' expression
    ;

parameterTypeList
    : parameterTypeGroup (',' parameterTypeGroup)*
    ;

parameterTypeGroup
    : (bindablePattern (',' bindablePattern)* )? ':' type
    ;

parameterGroup
    : '(' bindablePattern (',' bindablePattern)* ')'
    ;

functionBody
    : expression
    | 'is' 'not' 'yet' 'specified'
    | 'is' 'subclass' 'responsibility'
    ;

typeDefs
    : 'types' typeDef*
    ;

typeDef
    : QUALIFIER? IDENTIFIER '=' type invariant?
    | QUALIFIER? IDENTIFIER '::' field+ invariant?
    ;

type
    : type0 (('+>'|'->') type0)?
    | '()' (('+>'|'->') type0)?
    ;

type0op : '*' | '|' ;
type0
    : type1 (type0op type1)*
    ;

type1
    : basicType
    | '(' type ')'
    | '[' type ']'
    | QUOTELITERAL
    | IDENTIFIER ('.' IDENTIFIER)*
    | 'compose' IDENTIFIER 'of' field+ 'end'
    | 'set of' type1
    | 'seq of' type1
    | 'seq1 of' type1
    | 'map of' type1 'to' type1
    | 'inmap of' type1 'to' type1
    ;

basicType
    : 'bool' | 'nat' | 'nat1' | 'int' | 'rat' | 'real' | 'char' | 'token'
    ;

field
    : type
    | IDENTIFIER ':' type
    | IDENTIFIER ':-' type
    ;

invariant 
    : 'inv' bindablePattern '==' expression
    ;

pattern
    : bindablePattern
    | matchValue
    ;   

bindablePattern
    : patternIdentifier
    | tuplePattern
    | recordPattern
    ;

patternIdentifier
    : IDENTIFIER
    | '-'
    ;

matchValue
    : symbolicLiteral
    | '(' expression ')'
    ;

symbolicLiteral
    : numLiteral
    | boolLiteral
    | 'nil'
    | CHARLITERAL
    | TEXTLITERAL
    | QUOTELITERAL
    ;

numLiteral
    : DECIMAL
    | HEXLITERAL
    ;

boolLiteral
    : 'true' | 'false'
    ;

tuplePattern
    : MKUNDERLPAREN pattern (',' pattern)* ')'
    ;

recordPattern
    : MKUNDERNAMELPAREN (pattern (',' pattern)*)? ')'
    ;

expression
    : expr0
    | 'let' localDefinition (',' localDefinition)* 'in' expression
    | 'if' expression 'then' expression ('elseif' expression 'then' expression)* 'else' expression
    | 'cases' expression ':' (pattern (',' pattern)* '->' expression (',' pattern (',' pattern)* '->' expression)* )? (',' 'others' '->' expression)? 'end'
    | 'forall' bind (',' bind)* '@' expression
    | 'exists' bind (',' bind)* '@' expression
    | 'exists1' bind '@' expression
    | 'iota' bind '@' expression
    | 'lambda' typeBind (',' typeBind)* '@' expression
    ;

binExpr0op
    : '+' | '-' | '*' | '/' | 'div' | 'rem' | 'mod' | '<' | '<=' | '>' | '>='
    | '=' | '<>' | 'or' | 'and' | '=>' | '<=>' | 'in' 'set' | 'not' 'in' 'set'
    | 'subset' | 'psubset' | 'union' | '\\' | 'inter' | '^' | '++' | 'munion'
    | '<:' | '<-:' | ':->' | ':>' | 'comp' | '**'
    ;

expr0
    : expr1 (binExpr0op expression)?
    ;

unaryExpr1op
    : '+' | '-' | 'abs' | 'floor' | 'not' | 'card' | 'power' | 'dunion'
    | 'dinter' | 'hd' | 'tl' | 'len' | 'elems' | 'inds' | 'reverse'
    | 'conc' | 'dom' | 'rng' | 'merge' | 'inverse'
    ;

expr1
    : unaryExpr1op exprbase
    | '{' setMapExpr? '}'
    | '[' seqExpr? ']'
    | MKUNDERLPAREN expression (',' expression)+ ')'
    | MKUNDERNAMELPAREN ( expression (',' expression)* )? ')'
    | ISOFCLASSLPAREN IDENTIFIER ('.' IDENTIFIER)* ',' expression ')'
    | ISUNDERLPAREN expression ',' type ')'
    | ISUNDERBASICLPAREN expression ')'
    | ISUNDERNAMELPAREN expression ')'
    | PREUNDERLPAREN expression (',' expression)* ')'
    | expr2 TUPLESELECTOR?
    ;

expr2
// | subsequence
// | apply
    : exprbase ( '(' ( expression (',' '...' ',' expression | (',' expression)+ )? )? ')' )?
    ;

exprbase
    : '(' expression ')'
    | 'self'
// | name
// | old name
// | field select
    | name '~'?
    | symbolicLiteral
    ;

name
    : IDENTIFIER ('.' IDENTIFIER)*
    ;

setMapExpr
    : expression setMapExprTail?
    | '|->'
    ;

setMapExprTail
    : ',' '...' ',' expression
    | ( ',' expression )+
    | '|->' expression mapExprTail?
    | setMapExprBinding
    ;

mapExprTail
    : setMapExprBinding
    | ( ',' expression '|->' expression )+
    ;

setMapExprBinding
    : '|' bind+ ('@' expression)? 
    ;

/* sequence enumeration = '[', [ expression list ], ']' ;
 * sequence comprehension = '[', expression, '|', set bind, [ '@', expression ], ']' ;
 */
seqExpr
    : expression ( (',' expression)* | '|' setBind ('@' expression)? | ',' '...' ','  expression )
    ;

localDefinition
    : valueDefinition
    //| functionDefinition
    ;
    
bind: bindablePattern ('in' 'set' expression | ':' type)
    ;

setBind
    : bindablePattern 'in' 'set' expression
    ;

typeBind
    : bindablePattern ':' type
    ;

/* ********************************************************** */
/* ***               LEXER PRODUCTION RULES               *** */
/* ********************************************************** */


WHITESPACE
    : (' ' | '\t' | '\r' | '\n')+ { $channel=HIDDEN; }
    ;

LINECOMMENT
    : ( '//' | '--' ) .* '\n' { $channel=HIDDEN; }
    ;

MLINECOMMENT
    : '/*' .* '*/' { $channel=HIDDEN; }
    ;

QUALIFIER
    : 'public' | 'protected' | 'private' | 'logical'
    ;

// in/out/bidi params
PMODE
    : 'val' | 'res' | 'vres'
    ;

FRAMEMODE
    : 'rd' | 'wr'
    ;


// NILLITERAL
//     : 'nil'
//     ;

QUOTELITERAL
    : '<' IDENTIFIER '>'
    ;

CHARLITERAL
    : '\\\\' | '\\r' | '\\n' | '\\t' | '\\f' | '\\e' | '\\a' | '\\"' | '\\\''
    | '\\x' HEXDIGIT HEXDIGIT
    | '\\u' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT
    | '\\' OCTDIGIT OCTDIGIT OCTDIGIT
    // | '\\c' character
    ;

PREUNDERLPAREN
    : 'pre_('
    ;

MKUNDERLPAREN
    : 'mk_('
    ;

MKUNDERNAMELPAREN
    : 'mk_' IDENTIFIER ('.' IDENTIFIER)* '('
    ;

ISOFCLASSLPAREN
    : 'isofclass('
    ;

ISUNDERLPAREN
    : 'is_('
    ;

ISUNDERBASICLPAREN
    : 'is_' ('bool' | 'nat' | 'nat1' | 'int' | 'rat' | 'real' | 'char' | 'token') '('
    ;

ISUNDERNAMELPAREN
    : 'is_' IDENTIFIER ('.' IDENTIFIER)* '('
    ;

/* Need to fix this, yet
 */
TEXTLITERAL
    : '"' .* '"'
    ;

/* This only tracks the non-unicode chunk of the characters; this
 * needs to be extended into the unicode set per the basic VDM spec.
 *
 * initial letter:
 *   if codepoint < U+0100
 *   then Any character in categories Ll, Lm, Lo, Lt, Lu or U+0024 (a dollar sign)
 *   else Any character except categories Cc, Zl, Zp, Zs, Cs, Cn, Nd, Pc
 * following letter:
 *   if codepoint < U+0100
 *   then Any character in categories Ll, Lm, Lo, Lt, Lu, Nd or U+0024 (a dollar sign) or U+005F (underscore) or U+0027 (apostrophe)
 *   else Any character except categories Cc, Zl, Zp, Zs, Cs, Cn
 */
fragment
INITIAL_LETTER
    : '\u0024' | '\u0041'..'\u005a' | '\u0061'..'\u007a' | '\u00AA' | '\u00BA' | '\u00B5' | '\u00c0'..'\u00FF'
    ;
fragment
FOLLOW_LETTER
    : INITIAL_LETTER | DIGIT
    ;

IDENTIFIER
    : INITIAL_LETTER FOLLOW_LETTER*
    ;

// NAME
//     : IDENTIFIER ('.' IDENTIFIER)?
//     ;

fragment
OCTDIGIT
    : '0'..'7'
    ;

fragment
DIGIT
    : '0'..'9'
    ;

fragment
HEXDIGIT
    : DIGIT
    | 'a'..'f'
    | 'A'..'F'
    ;

HEXLITERAL
    : ('0x'|'0X') HEXDIGIT+
    ;

DECIMAL
    : DIGIT+ ('.' DIGIT+)? ( ('E'|'e') ('+'|'-')? DIGIT+ )?
    ;

TUPLESELECTOR
    : '.#' DIGIT+
    ;

// CSPCHAOS : 'Chaos';
// CSPSKIP : 'Skip';
// CSPSTOP : 'Stop';
// CSPWAIT : 'Wait';
// ABS : 'abs';
// ACTIONS : 'actions';
// ALL : 'all';
// AND : 'and';
// ATOMIC : 'atomic';
// BEGIN : 'begin';
// TBOOL : 'bool';
// BY : 'by';
// CARD : 'card';
// CASES : 'cases';
// CHANNELS : 'channels';
// CHANSETS : 'chansets';
// TCHAR : 'char';
// CLASS : 'class';
// COMP : 'comp';
// COMPOSE : 'compose';
// CONC : 'conc';
// DCL : 'dcl';
// DINTER : 'dinter';
// CSPDIV : 'Div';
// DIV : 'div';
// DO : 'do';
// DOM : 'dom';
// DUNION : 'dunion';
// ELEMS : 'elems';
// ELSE : 'else';
// ELSEIF : 'elseif';
// END : 'end';
// ENDSBY : 'endsby';
// EXISTS : 'exists';
// EXISTS1 : 'exists1';
// EXTENDS : 'extends';
// FALSE : 'false';
// FLOOR : 'floor';
// FOR : 'for';
// FORALL : 'forall';
// FRAME : 'frame';
// FUNCTIONS : 'functions';
// HD : 'hd';
// IF : 'if';
// INSET : 'in set';
// IN : 'in';
// INDS : 'inds';
// INITIAL : 'initial';
// INMAPOF : 'inmap';
// TINT : 'int';
// INTER : 'inter';
// INV : 'inv';
// INVERSE : 'inverse';
// IOTA : 'iota';
// ISOFCLASS : 'isofclass';
// NOTYETSPEC : 'is not yet specified';
// SUBCLASSRESP : 'is subclass responsibility';
// LAMBDA : 'lambda';
// LEN : 'len';
// LET : 'let';
// LOGICAL : 'logical';
// MAPOF : 'map';
// MEASURE : 'measure';
// MERGE : 'merge';
// MOD : 'mod';
// MU : 'mu';
// MUNION : 'munion';
// NAMESETS : 'namesets';
// TNAT : 'nat';
// TNAT1 : 'nat1';
// NEW : 'new';
// NIL : 'nil';
// NOTINSET : 'not in set';
// NOT : 'not';
// OPERATIONS : 'operations';
// OF : 'of';
// OR : 'or';
// OTHERS : 'others';
// POST : 'post';
// POWER : 'power';
// PRE : 'pre';
// PREUNDER : 'pre_';
// PRIVATE : 'private';
// PROCESS : 'process';
// PROTECTED : 'protected';
// PSUBSET : 'psubset';
// PUBLIC : 'public';
// TRAT : 'rat';
// RD : 'rd';
// TREAL : 'real';
// REM : 'rem';
// RES : 'res';
// RETURN : 'return';
// REVERSE : 'reverse';
// RNG : 'rng';
// SELF : 'self';
// SEQOF : 'seq of';
// SEQ1OF : 'seq1 of';
// SETOF : 'set of';
// STARTBY : 'startby';
// STATE : 'state';
// SUBSET : 'subset';
// THEN : 'then';
// TL : 'tl';
// TO : 'to';
// TTOKEN : 'token';
// TRUE : 'true';
// TYPES : 'types';
// UNION : 'union';
// VAL : 'val';
// VALUES : 'values';
// VRES : 'vres';
// WHILE : 'while';
// WR : 'wr';

// AMP : '&';
// AT : '@';
// BACKSLASH : '\\';
// BANG : '!';
// BAR : '|';
// BARRARROW : '|->';
// BARRCURLY : '|}';
// BARGT : '|>';
// BARRSQUARE : '|]';
// BARTILDEBAR : '|~|';
// CARET : '^';
// COLON : ':';
// COLONBACKSLASH : ':\\';
// COLONDASH : ':-';
// COLONDASHGT : ':->';
// COLONEQUALS : ':=';
// COLONGT : ':>';
// COMMA : ',';
// DBACKSLASH : '\\\\';
// DBAR : '||';
// DBARRSQUARE : '||]';
// DCOLON : '::';
// DEQRARROW : '==>';
// DEQUALS : '==';
// DLSQUARE : '[[';
// DOT : '.';
// DOTHASH : '.#';
// DPLUS : '++';
// DRSQUARE : ']]';
// DSTAR : '**';

// yes, the ellipsis includes the commas all as a single token
// ELLIPSIS
//     : ',' '...' ','
//     ;
// EMPTYMAP
//     : '{' '|->' '}'
//     ;

// EQRARROW : '=>';
// EQUALS : '=';
// GT : '>';
// GTE : '>=';
// LARROW : '<-';
// LCURLY : '{';
// LCURLYBAR : '{|';
// LPAREN : '(';

// LRPAREN
//     : '(' ')'
//     ;

// LRSQUARE : '[]';
// LSQUARE : '[';
// LSQUAREBAR : '[|';
// LSQUAREDBAR : '[||';
// LSQUAREGT : '[>';
// LT : '<';
// LTCOLON : '<:';
// LTDASHCOLON : '<-:';
// LTE : '<=';
// LTEQUALSGT : '<=>';
// MINUS : '-';
// NEQ : '<>';
// PLUS : '+';
// PLUSGT : '+>';
// QUESTION : '?';
// RARROW : '->';
// RCURLY : '}';
// RPAREN : ')';
// RSQUARE : ']';
// SEMI : ';';
// SLASH : '/';
// SLASHBACKSLASH : '/\\';
// SLASHCOLON : '/:';
// STAR : '*';
// TBAR : '|||';
// TILDE : '~';
// BACKTICK : '`';

/* ---- complex terminals below ---- */

