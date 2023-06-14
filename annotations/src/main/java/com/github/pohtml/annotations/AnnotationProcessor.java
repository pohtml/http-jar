package com.github.pohtml.annotations;

import static javax.tools.Diagnostic.Kind.ERROR;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes({"com.github.pohtml.annotations.DynamicHtml", "com.github.pohtml.annotations.Get"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AnnotationProcessor extends AbstractProcessor {

	Types types;
	Elements elements;
	Filer filer;
	Messager messager;
	TemporaryFiles temporaryFiles;
	String contextPath;

	private static final String PACKAGE = "package ";
	private static final String WEB_SERVLET = "; @javax.servlet.annotation.WebServlet({\"";
	private static final String CLASS = "\"}) public class ";
	private static final String EXTENDS = " extends com.github.pohtml.";
	private static final String VERSION = "{private static final long serialVersionUID = ";
	private static final String CONSTRUCTOR = ";public GetServletForm() {super(serialVersionUID);}";
	private static final String METHOD_DECLARATION = "@Override public ";
	private static final String METHOD_CODE = " get() {return new ";
	private static final String END = "();}}";

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		contextPath = processingEnv.getOptions().get("com.github.pohtml.context");
		types = processingEnv.getTypeUtils();
		elements = processingEnv.getElementUtils();
		filer = processingEnv.getFiler();
		messager = processingEnv.getMessager();
	}

	private void generate(Element element, String method) throws IOException {
		DynamicHtml annotation = element.getAnnotation(DynamicHtml.class);
		String model = annotation.model();
		if (model.isEmpty()) {
			model = annotation.value();
			if (model.isEmpty()) {
				String message = "You must specifiy the context relative URI using the 'model' attribute or the default (value) attribute";
				messager.printMessage(ERROR, message, element);
				return;
			}
		}
		if (model.contains("*")) {
			String message = "URI patterns not allowed. You must uniquely identify the annotated resource";
			messager.printMessage(ERROR, message, element);
		} else if (model.endsWith(".html")) {
			String message = "The URI of a dynamic resource cannot have the .html extension";
			messager.printMessage(ERROR, message, element);
		} else {
			String view = annotation.view();
			String version = "1";
			if (!view.isEmpty()) {
				if (!view.isEmpty() && !view.endsWith(".html")) {
					view = view + model + ".html";
				}
				if (temporaryFiles != null) {
					File temporary = new File(temporaryFiles.directory, view.substring(1));
					if (temporary.exists()) {
						version = String.valueOf(temporary.lastModified());	
					}
				}
			}
			version += 'L';
			String simpleName = element.getSimpleName().toString();
			String packageName = elements.getPackageOf(element).getQualifiedName().toString();
			String className = method + "Servlet" + simpleName;
			String name = packageName + '.' + className;
			JavaFileObject builderFile = filer.createSourceFile(name, element);
			StringBuilder out = new StringBuilder();
			try (PrintWriter pw = new PrintWriter(builderFile.openWriter())) {
				out.append(PACKAGE).append(packageName);
				out.append(WEB_SERVLET).append(model).append("\"");
				if (!view.isEmpty()) {
					out.append(", \"").append(view);	
				}
				out.append(CLASS).append(className);
				out.append(EXTENDS).append(method + "Servlet<").append(simpleName).append('>');
				out.append(VERSION).append(version);
				out.append(CONSTRUCTOR);
				out.append(METHOD_DECLARATION).append(simpleName);
				out.append(METHOD_CODE).append(simpleName);
				out.append(END);
				pw.append(out.toString());
			}
		}
	}

	void createContextServlet() {
		try {
			String pohtml = "com.github.pohtml";
			JavaFileObject context = filer.createSourceFile(pohtml + ".client.Context");
			StringBuilder out = new StringBuilder();
			long now = System.currentTimeMillis();
			try (PrintWriter pw = new PrintWriter(context.openWriter())) {
				out.append(PACKAGE).append(pohtml + ".client");
				out.append("/*" + contextPath + "*/");
				out.append(WEB_SERVLET).append(pohtml);
				out.append(CLASS).append("Context");
				out.append(EXTENDS).append("Static");
				out.append(" {private static final long serialVersionUID = 1L;public Context() {super(\"").append(now).append("\");}}");
				pw.append(out.toString());
			}
			String sourcePath = context.toUri().getPath();
			int index = sourcePath.indexOf("target/generated-sources");
			if (index == -1) {
				index = sourcePath.indexOf(".apt_generated");
			}
			if (index == -1) {
				throw new IllegalStateException("Unable to interpret the source file path: " + sourcePath);
			}
			File zip = new File(sourcePath.substring(0, index) + "src/main/resources/com.github.pohtml.zip");
			if (zip.exists()) {
				temporaryFiles = new TemporaryFiles(zip);
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		try {
			if (annotations.isEmpty()) {
				return false;
			}
			for (Element rootElement : roundEnv.getRootElements()) {
				if (rootElement.toString().equals("com.github.pohtml.client.Context")) {
					return false;
				}
			}
			createContextServlet();
			for (TypeElement annotation : annotations) {
				Set<? extends Element> types = roundEnv.getElementsAnnotatedWith(annotation);
				String name = annotation.getSimpleName().toString(); 
				if (name.equals("Get")) {
					for (Element type : types) {
						for (Element enclosed : ((TypeElement)type).getEnclosedElements()) {
							if (enclosed instanceof ExecutableElement) {
								ExecutableElement method = (ExecutableElement)enclosed;
								System.out.println(method.getSimpleName());
								System.out.println(method.getReturnType());
							}
						}
					}
				} else if (name.equals("DynamicHtml")) {
					for (Element element : types) {
						TypeMirror mirror = element.asType();
						List<String> ancestors = getDirectSupertypes(mirror);
						if (ancestors.contains("com.github.pohtml.Get")) {
							generate(element, "Get");
						} else if (ancestors.contains("com.github.pohtml.Post")) {
							generate(element, "Post");
						} else {
							String message = "@DynamicHtml annotation must be applied to an extension of Get or Post (child package classes)";
							messager.printMessage(ERROR, message, element);
						}
					}	
				}
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return true;
	}

	List<String> getDirectSupertypes(TypeMirror mirror) {
		List<String> result = new ArrayList<>();
		List<? extends TypeMirror> ancestors = types.directSupertypes(mirror);
		for (TypeMirror typeMirror : ancestors) {
			result.add(typeMirror.toString());
		}
		return result;
	}

}