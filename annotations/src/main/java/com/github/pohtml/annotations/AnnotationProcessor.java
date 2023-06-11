package com.github.pohtml.annotations;

import static javax.tools.Diagnostic.Kind.ERROR;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

@SupportedAnnotationTypes("com.softalks.pohtml.annotations.Resource")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AnnotationProcessor extends AbstractProcessor {

	Types types;
	Elements elements;
	Filer filer;
	Messager messager;
	File tmp;
	
	private static final String PACKAGE = "package "; 
	private static final String WEB_SERVLET = "; @javax.servlet.annotation.WebServlet({\"";
	private static final String CLASS = "\"}) public class ";
	private static final String EXTENDS = " extends com.softalks.pohtml."; 
	private static final String VERSION = "let {private static final long serialVersionUID = ";
	private static final String METHOD_DECLARATION = "; @Override public com.softalks.pohtml."; 
	private static final String METHOD_CODE = " call() throws Exception {return new ";
	private static final String END = "();}}";

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		types = processingEnv.getTypeUtils();
		elements = processingEnv.getElementUtils();
		filer = processingEnv.getFiler();
		messager = processingEnv.getMessager();
	}

	private void file(Element element, String method) throws IOException {
		Resource resource = element.getAnnotation(Resource.class);
		String servlet = resource.uri();
		if (servlet.isEmpty()) {
			servlet = resource.uri();
			if (servlet.isEmpty()) {
				messager.printMessage(ERROR,
						"You must specifiy the context relative URI using the servlet attribute or the default (value) attribute",
						element);
			}
		}
		if (servlet.contains("*")) {
			messager.printMessage(ERROR, "URI patterns not allowed. You must uniquely identify the annotated resource",
					element);
		}
		String packageName = elements.getPackageOf(element).getQualifiedName().toString();
		String className = "PlainOldHtml" + element.getSimpleName().toString();
		String name = packageName + '.' + className;
		JavaFileObject builderFile = filer.createSourceFile(name, element);
		StringBuilder out = new StringBuilder();
		try (PrintWriter pw = new PrintWriter(builderFile.openWriter())) {
			out.append(PACKAGE).append(packageName);
			out.append(WEB_SERVLET).append(servlet).append("\", \"").append(servlet + ".html");
			out.append(CLASS).append(className);
			out.append(EXTENDS).append(method);
			out.append(VERSION).append("com.github.pohtml.Context.VERSION");
			out.append(METHOD_DECLARATION).append(method);
			out.append(METHOD_CODE).append(element.getSimpleName());
			out.append(END);
			pw.append(out.toString());
		}
	}

	File createContextServlet() {
		try {
			JavaFileObject context = filer.createSourceFile("com.github.pohtml.Context");
			StringBuilder out = new StringBuilder();
			long now = System.currentTimeMillis();
            Properties props = new Properties();
            URL url = this.getClass().getClassLoader().getResource("velocity.properties");
            props.load(url.openStream());
            VelocityEngine ve = new VelocityEngine(props);
            ve.init();
            VelocityContext vc = new VelocityContext();
            Template vt = ve.getTemplate("beaninfo.vm");
            vt.merge(vc, context.openWriter());
//			try (PrintWriter pw = new PrintWriter(context.openWriter())) {
//				out.append(PACKAGE).append("com.github.pohtml");
//				out.append(WEB_SERVLET).append(String.valueOf(now)).append("\"");
//				out.append(CLASS).append("Context");
//				out.append(EXTENDS).append("AbstractContext");
//				out.append(" {private static final long serialVersionUID = " + now + " L;}");
//				pw.append(out.toString());
//			}
//			try (PrintWriter pw = new PrintWriter(context.openWriter())) {
//				pw.print(
//						"package com.github.pohtml;public class Context {public static final long VERSION = System.currentTimeMillis();}");
//			}
			String sourcePath = context.toUri().getPath();
			int index = sourcePath.indexOf("target/generated-sources");
			if (index == -1) {
				index = sourcePath.indexOf(".apt_generated");
			}
			if (index == -1) {
				throw new IllegalStateException("Unable to interpret the source file path: " + sourcePath);
			}
			File tmp = new File(sourcePath.substring(0, index) + "src/main/resources/pohtml-tmp.zip");
			return tmp.exists() ? tmp : null;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		try {
			tmp = createContextServlet();
			for (TypeElement annotation : annotations) {
				Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
				for (Element element : elements) {
					TypeMirror mirror = element.asType();
					List<String> ancestors = getDirectSupertypes(mirror);
					if (ancestors.contains("com.softalks.pohtml.Get")) {
						file(element, "Get");
					} else if (ancestors.contains("com.softalks.pohtml.Post")) {
						file(element, "Post");
					} else {
						messager.printMessage(ERROR,
								"@Resource must be applied to an extension of Get or Post (child package classes)",
								element);
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