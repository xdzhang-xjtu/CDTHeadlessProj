package cdt.headless.proj.parser;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethod;

public class IASTVisitor extends ASTVisitor {
	
	public IASTVisitor() {
		super();
		super.shouldVisitNames = true;
		// TODO Auto-generated constructor stub
	}
	  @Override
	    public int visit(IASTName name) {
	        IBinding binding = name.resolveBinding();
	        if (binding instanceof CPPMethod) {
	  		  System.out.println(name + "\t Line @: " + name.getFileLocation().getStartingLineNumber());

	            CPPMethod method = (CPPMethod) binding;
	           
//	            System.out.println("qualified: " + String.join(".", method.getQualifiedName());
	        }
	        return 3;
	    }
}
