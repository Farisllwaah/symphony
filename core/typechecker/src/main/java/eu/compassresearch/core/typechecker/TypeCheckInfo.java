package eu.compassresearch.core.typechecker;

import java.lang.reflect.Method;

import org.overture.ast.definitions.PDefinition;
import org.overture.ast.lex.LexIdentifierToken;
import org.overture.ast.lex.LexLocation;
import org.overture.ast.lex.LexNameToken;
import org.overture.ast.node.INode;
import org.overture.ast.types.PType;
import org.overture.ast.types.SBasicType;

import eu.compassresearch.ast.declarations.PDeclaration;
import eu.compassresearch.core.typechecker.api.TypeCheckQuestion;
import eu.compassresearch.core.typechecker.api.TypeIssueHandler;

/**
 * TypeCheckInfo tracks the three scopes we care about in CML. These are
 * Variables - mapping from variable name to their Definition (and type) Types -
 * mapping from type-name to the actual PType Channels - mapping from
 * channel-name to its declaration (and carried type of any)
 * 
 * Creating a new scope is the responsibility of this class.
 * 
 * @author rwl
 * 
 */
class TypeCheckInfo implements TypeCheckQuestion
  {
    
    public final String                     CML_SCOPE     = "CML";
    public final String                     DEFAULT_SCOPE = "Default";
    
    private final Environment<PDefinition>  variables;
    private final Environment<PType>        types;
    private final Environment<PDeclaration> channels;
    
    private final PDefinition               enclosingDefinition;
    private final TypeIssueHandler          issueHandler;
    
    private TypeCheckInfo(Environment<PDefinition> vars,
        Environment<PType> types, Environment<PDeclaration> channels,
        PDefinition enclosingDefinition, TypeIssueHandler issueHandler)
      {
        this.variables = new Environment<PDefinition>(vars, issueHandler);
        this.types = new Environment<PType>(types, issueHandler);
        this.channels = new Environment<PDeclaration>(channels, issueHandler);
        this.enclosingDefinition = enclosingDefinition;
        this.issueHandler = issueHandler;
      }
    
    /**
     * Create a new Type Check Info at top level
     * 
     * 
     * @param issueHandler
     *          -- Where to report issues
     * @return a fresh Type Check Info instance at top level
     */
    public static TypeCheckInfo getNewTopLevelInstance(
        TypeIssueHandler issueHandler)
      {
        
        Environment<PDefinition> variables = new Environment<PDefinition>(
            issueHandler);
        Environment<PType> types = new Environment<PType>(issueHandler);
        PDefinition enclosingDefinition = null;
        Environment<PDeclaration> channels = new Environment<PDeclaration>(
            issueHandler);
        
        return new TypeCheckInfo(variables, types, channels,
            enclosingDefinition, issueHandler);
      }
    
    @Override
    public PType lookupType(LexIdentifierToken ident)
      {
        return types.lookupName(ident);
      }
    
    @Override
    public void addType(LexIdentifierToken ident, PType type)
      {
        this.types.put(ident, type);
      }
    
    @Override
    public PDeclaration lookupChannel(LexIdentifierToken ident)
      {
        return channels.lookupName(ident);
      }
    
    @Override
    public void addChannel(LexIdentifierToken ident, PDeclaration channel)
      {
        channels.put(ident, channel);
      }
    
    @Override
    public PDefinition lookupVariable(LexIdentifierToken ident)
      {
        return variables.lookupName(ident);
      }
    
    @Override
    public void addVariable(LexIdentifierToken ident, PDefinition variable)
      {
        variables.put(ident, variable);
      }
    
    @Override
    public TypeCheckQuestion newScope(PDefinition def)
      {
        // Variables are scoped, types and channels are global (for now at
        // least)
        TypeCheckInfo res = new TypeCheckInfo(this.variables, types, channels,
            def, null);
        return res;
      }
    
//    @Override
//    public org.overture.typechecker.Environment getOvertureEnvironment()
//    
//      {
//        final org.overture.typechecker.Environment ve = variables
//            .getOvertureEnv();
//        final org.overture.typechecker.Environment te = types.getOvertureEnv();
//        
//        class CombinedEnv extends org.overture.typechecker.Environment
//          {
//            
//            public CombinedEnv()
//              {
//                super(null);
//              }
//            
//            @Override
//            public org.overture.ast.definitions.PDefinition findName(
//                LexNameToken name, NameScope scope)
//              {
//                return ve.findName(name, scope);
//              }
//            
//            @Override
//            public org.overture.ast.definitions.PDefinition findType(
//                LexNameToken name, String fromModule)
//              {
//                return te.findType(name, fromModule);
//              }
//            
//            @Override
//            public AStateDefinition findStateDefinition()
//              {
//                return null;
//              }
//            
//            @Override
//            public SClassDefinition findClassDefinition()
//              {
//                return null;
//              }
//            
//            @Override
//            public boolean isStatic()
//              {
//                return false;
//              }
//            
//            @Override
//            public void unusedCheck()
//              {
//                // TODO: Check this guy is invoked and make appropriate action
//                throw new RuntimeException(
//                    "Somebody better do something about this.");
//              }
//            
//            @Override
//            public boolean isVDMPP()
//              {
//                return true;
//              }
//            
//            @Override
//            public boolean isSystem()
//              {
//                return false;
//              }
//            
//            @Override
//            public Set<org.overture.ast.definitions.PDefinition> findMatches(
//                LexNameToken name)
//              {
//                return ve.findMatches(name);
//              }
//            
//          }
//        ;
//        return new CombinedEnv();
//      }
    
    @Override
    public void updateContextNameToCurrentScope(INode n)
      {
        
        if (n instanceof PType)
          {
            updateLocationToCurrentScope((PType) n);
            return;
          }
        
        updateLocationdAttr(n);
        updateNameAttr(n);
      }
    
    private void updateNameAttr(INode n)
      {
        if (enclosingDefinition != null)
          try
            {
              Class<?> clz = n.getClass();
              Method getLocation = clz.getMethod("getName", new Class<?>[0]);
              LexNameToken loc = (LexNameToken) getLocation
                  .invoke(n, new Object[0]);
              Method setLocation = clz.getMethod("setName",
                  new Class<?>[] { LexLocation.class });
              setLocation.invoke(n,
                  newNameModule(loc, enclosingDefinition.getName().name));
            } catch (Exception e)
            {
            }
      }
    
    private Object newNameModule(LexNameToken nme,
        String name)
      {
        if (enclosingDefinition != null)
          {
            String module = enclosingDefinition.getName().name;
            LexIdentifierToken id = nme.getIdentifier();
            nme = new LexNameToken(module, id);
          }
        return nme;
      }
    
    private void updateLocationdAttr(INode n)
      {
        if (enclosingDefinition != null)
          try
            {
              Class<?> clz = n.getClass();
              Method getLocation = clz
                  .getMethod("getLocation", new Class<?>[0]);
              LexLocation loc = (LexLocation) getLocation.invoke(n,
                  new Object[0]);
              Method setLocation = clz.getMethod("setLocation",
                  new Class<?>[] { LexLocation.class });
              setLocation.invoke(n,
                  newLocationModule(loc, enclosingDefinition.getName().name));
            } catch (Exception e)
            {
              System.out.println(e.getMessage());
            }
        
      }
    
    private void updateLocationToCurrentScope(PType typ)
      {
        LexLocation loc = typ.getLocation();
        
        if (typ instanceof SBasicType)
          {
            typ.setLocation(newLocationModule(loc, CML_SCOPE));
            return;
          }
        
        if (enclosingDefinition == null)
          return;
        
        typ.setLocation(newLocationModule(loc,
            enclosingDefinition.getName().name));
        
      }
    
    private LexLocation newLocationModule(LexLocation loc, String newModule)
      {
        return new LexLocation(loc.file, newModule, loc.startLine,
            loc.startPos, loc.endLine, loc.endPos, loc.startOffset,
            loc.endOffset);
      }
    
  }
