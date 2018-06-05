package cdt.headless.proj.parser;

import org.eclipse.cdt.core.model.IMethod;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethod;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementVisitor;
import org.eclipse.core.runtime.CoreException;

public class MyVisitor implements ICElementVisitor {

	@Override
	public boolean visit(ICElement element) throws CoreException {
		// TODO Auto-generated method stub
        if (element instanceof  IMethod) {
        	 IMethod method = (IMethod) element;
        	 System.out.println("URI:" + method.getLocationURI());
        	 System.out.println("Sig:" +  method.getSignature());
        	 method.getTranslationUnit().getAST().accept(new IASTVisitor());
        	 //            System.out.println("qualified: " + String.join(".", method.getQualifiedName());
        }
 
//        if (element instanceof  ITranslationUnit) {
//        	System.out.println("itranslate: ");
//        	ITranslationUnit method = (ITranslationUnit) element;
//       	 System.out.println("URI:" + method.getLocationURI());
////       	 System.out.println("Sig:" +  method.getSignature());
//       	 method.getTranslationUnit().getAST().accept(new IASTVisitor());
//       	 //            System.out.println("qualified: " + String.join(".", method.getQualifiedName());
//       }

		return true;
	}

}
