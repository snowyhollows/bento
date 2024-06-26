package net.snowyhollows.bento;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.snowyhollows.bento.annotation.ByFactory;
import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.DefaultFactory;
import net.snowyhollows.bento.annotation.Reset;
import net.snowyhollows.bento.annotation.WithFactory;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FactoryGenerator extends AbstractProcessor {

    public final static ClassName BENTO_FACTORY = ClassName.get(BentoFactory.class);
    private final static ClassName BENTO_RESETTABLE = ClassName.get(BentoResettable.class);
    private final static ClassName STRING = ClassName.get(String.class);
	public final static ClassName BENTO = ClassName.get(Bento.class);

    private Filer filer;
    private Types types;
	private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        types = processingEnvironment.getTypeUtils();
		messager = processingEnvironment.getMessager();
    }

    static ClassName factoryNameFor(TypeName type, String suffix) {
		String baseName = baseName(type);
        return ClassName.get(rawClassName(type).packageName(), baseName + suffix);
    }

	private static String baseName(TypeName type) {
		String parameters = type instanceof ParameterizedTypeName
				? ((ParameterizedTypeName) type).typeArguments.stream().map(FactoryGenerator::baseName).collect(Collectors.joining("And", "Of", ""))
				: "";

		return rawClassName(type).simpleName() + parameters;
	}

	private static ClassName rawClassName(TypeName typeName) {
		boolean isParametrized = typeName instanceof ParameterizedTypeName;
		return isParametrized ? ((ParameterizedTypeName)typeName).rawType : (ClassName) typeName;
	}

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> beans = roundEnv.getElementsAnnotatedWith(WithFactory.class);

        for (Element bean : beans) {
			try {
				processSingleFactory(bean);
			} catch (Exception e) {
				messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
				for (StackTraceElement stackTraceElement : e.getStackTrace()) {
					messager.printMessage(Diagnostic.Kind.ERROR, stackTraceElement.toString());
				}
			}
		}

        return true;
    }

	private void processSingleFactory(Element bean) {
		ExecutableElement constructor = bean.getKind() == ElementKind.CONSTRUCTOR
				? (ExecutableElement) bean
				: null;
		TypeElement beanClass = constructor != null ? (TypeElement) constructor.getEnclosingElement() : (TypeElement) bean;

		ExecutableElement resetter = null;

		for (Element enclosedElement : beanClass.getEnclosedElements()) {
			if (enclosedElement.getAnnotation(Reset.class) != null) {
				resetter = (ExecutableElement) enclosedElement;
			}
		}

		ClassName beanClassName = ClassName.get(beanClass);

		WithFactory with = bean.getAnnotation(WithFactory.class);
		String suffix = with.value();
		String exactName = with.exactName();
		String packageName = beanClassName.packageName();

		ClassName factoryName = "##".equals(exactName)
				? factoryNameFor(beanClassName, suffix)
				: ClassName.get(packageName, exactName);

		ParameterizedTypeName bentoFactoryParametrized = ParameterizedTypeName.get(BENTO_FACTORY, beanClassName);
		ParameterizedTypeName bentoResetableParametrized = ParameterizedTypeName.get(BENTO_RESETTABLE, beanClassName);

		MethodSpec.Builder createInContext = MethodSpec.methodBuilder("createInContext")
				.addParameter(ParameterSpec.builder(Bento.class, "bento").build())
				.addModifiers(Modifier.PUBLIC)
				.returns(beanClassName)
				.addCode("return new $T(", beanClassName);

		List<? extends VariableElement> parameters = constructor != null ? constructor.getParameters() : Collections.emptyList();
		writeGettingParametersFromBento(createInContext, parameters);
		createInContext.addCode(");\n");

		MethodSpec.Builder reset = MethodSpec.methodBuilder("reset")
				.addParameter(ParameterSpec.builder(beanClassName, "t").build())
				.addParameter(ParameterSpec.builder(Bento.class, "bento").build())
				.addModifiers(Modifier.PUBLIC)
				.returns(TypeName.VOID);

		if (resetter != null) {
			List<? extends VariableElement> resetParameters = resetter.getParameters();
			reset.addCode("t." + resetter.getSimpleName() + "(");
			writeGettingParametersFromBento(reset, resetParameters );
			reset.addCode(");\n");
		}

		TypeSpec.Builder factory = TypeSpec.enumBuilder(factoryName)
				.addModifiers(Modifier.PUBLIC)
				.addEnumConstant("IT")
				.addSuperinterface(bentoFactoryParametrized)
				.addMethod(createInContext.build());

		if (resetter != null) {
			factory
					.addSuperinterface(bentoResetableParametrized)
					.addMethod(reset.build());
		}

		try {
			JavaFile.builder(packageName, factory.build()).build().writeTo(filer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeGettingParametersFromBento(MethodSpec.Builder source, List<? extends VariableElement> parameters) {


		boolean first = true;
		for (VariableElement param : parameters) {
	        TypeMirror tm = param.asType();

	        if (!first) {
	            source.addCode(",\n    ");
	        }

	        ByName byName = param.getAnnotation(ByName.class);
	        ByFactory byFactory = param.getAnnotation(ByFactory.class);


			TypeName typeName = TypeName.get(tm);
	        boolean isEnum = !typeName.isPrimitive() && types.asElement(tm).getKind() == ElementKind.ENUM;
	        boolean isByName = byName != null || typeName.isPrimitive() || typeName.equals(STRING) || isEnum;

	        if (isByName) {
	            String nameToGet = (byName == null || byName.value().equals("##")) ?
	                    param.getSimpleName().toString()
	                    : byName.value();

				boolean hasDefault = (byName != null && !byName.fallbackValue().equals("##"));

				if (hasDefault) {
					source.addCode("bento.get($S, $S).equals($S) ? ", nameToGet, "##", "##");
					if (typeName.equals(TypeName.FLOAT)) {
						source.addCode("$T.parseFloat($S)", Float.class, byName.fallbackValue());
					} else if (typeName.equals(TypeName.INT)) {
						source.addCode("$T.parseInt($S)", Integer.class, byName.fallbackValue());
					} else if (typeName.equals(TypeName.BOOLEAN)) {
						source.addCode("$T.parseBoolean($S)", Boolean.class, byName.fallbackValue());
					} else if (typeName.equals(STRING)) {
						source.addCode("$S", byName.fallbackValue());
					} else if (isEnum) {
						source.addCode("$T.$L", ClassName.get(tm), byName.fallbackValue());
					}
					source.addCode(" : ");
				}

	            String call;
	            if (typeName.equals(TypeName.FLOAT)) {
	                call = "bento.getFloat($S)";
	            } else if (typeName.equals(TypeName.INT)) {
	                call = "bento.getInt($S)";
	            } else if (typeName.equals(TypeName.BOOLEAN)) {
	                call = "bento.getBoolean($S)";
	            } else if (typeName.equals(STRING)) {
	                call = "bento.getString($S)";
	            } else if (isEnum) {
	                call = "bento.getEnum($T.class, $S)";
	            } else {
	                call = "bento.get($S)";
	            }
	            if (!isEnum) {
	                source.addCode(call, nameToGet);
	            } else {
	                source.addCode(call, ClassName.get(tm), nameToGet);
	            }

	        } else {
	            if (byFactory == null) {
	                if (typeName.equals(BENTO)) {
	                    source.addCode("bento");
	                } else {
	                    source.addCode("bento.get($T.IT)", factoryNameFor(ClassName.get(tm), "Factory"));
	                }
	            } else {
	                TypeMirror typeMirror = getT(byFactory);
	                TypeElement element = (TypeElement)types.asElement(typeMirror);
	                if (!element.getQualifiedName().toString().equals(DefaultFactory.class.getCanonicalName())) {
	                    source.addCode("bento.get($T.IT)", ClassName.get(element));
	                } else {
	                    source.addCode("bento.get($T.IT)", factoryNameFor(ClassName.get(tm), "Factory"));
	                }
	            }
	        }

	        first = false;
	    }
	}

	private static TypeMirror getT(ByFactory byFactory) {
        try
        {
            byFactory.value();
        }
        catch( MirroredTypeException mte )
        {
            return mte.getTypeMirror();
        }
        return null;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(WithFactory.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
