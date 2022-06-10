package net.snowyhollows.bento;

import com.squareup.javapoet.*;
import net.snowyhollows.bento2.Bento;
import net.snowyhollows.bento2.BentoFactory;
import net.snowyhollows.bento2.BentoResettable;
import net.snowyhollows.bento2.annotation.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FactoryGenerator extends AbstractProcessor {

    private final static ClassName BENTO_FACTORY = ClassName.get(BentoFactory.class);
    private final static ClassName BENTO_RESETTABLE = ClassName.get(BentoResettable.class);
    private final static ClassName STRING = ClassName.get(String.class);
    private final static ClassName BENTO = ClassName.get(Bento.class);

    private Filer filer;
    private Messager messager;
    private Types types;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        types = processingEnvironment.getTypeUtils();
    }

    static ClassName factoryNameFor(ClassName type, String suffix) {
        return ClassName.get(type.packageName(), type.simpleName() + suffix);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> beans = roundEnv.getElementsAnnotatedWith(WithFactory.class);

        for (Element bean : beans) {
            messager.printMessage(Diagnostic.Kind.NOTE, "generating " + bean);
            ExecutableElement constructor = (ExecutableElement) bean;
            TypeElement beanClass = (TypeElement) constructor.getEnclosingElement();
	        ExecutableElement resetter = null;

	        for (Element enclosedElement : beanClass.getEnclosedElements()) {
		        if (enclosedElement.getAnnotation(Reset.class) != null) {
		        	resetter = (ExecutableElement) enclosedElement;
		        }
	        }

            ClassName beanClassName = ClassName.get(beanClass);

            String suffix = constructor.getAnnotation(WithFactory.class).value();

            ClassName factoryName = factoryNameFor(beanClassName, suffix);
            String packageName = beanClassName.packageName();

            ParameterizedTypeName bentoFactoryParametrized = ParameterizedTypeName.get(BENTO_FACTORY, beanClassName);
            ParameterizedTypeName bentoRessetableParametrized = ParameterizedTypeName.get(BENTO_RESETTABLE, beanClassName);

            MethodSpec.Builder createInContext = MethodSpec.methodBuilder("createInContext")
                    .addParameter(ParameterSpec.builder(Bento.class, "bento").build())
                    .addModifiers(Modifier.PUBLIC)
                    .returns(beanClassName)
                    .addCode("return new $T(", beanClassName);

	        List<? extends VariableElement> parameters = constructor.getParameters();
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
                        .addSuperinterface(bentoRessetableParametrized)
                        .addMethod(reset.build());
            }

            try {
                JavaFile.builder(packageName, factory.build()).build().writeTo(filer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

	private void writeGettingParametersFromBento(MethodSpec.Builder source, List<? extends VariableElement> parameters) {
		boolean first = true;
		for (VariableElement param : parameters) {
	        TypeMirror tm = param.asType();

	        if (!first) {
	            source.addCode(", ");
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

	            String call = null;
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
	                    source.addCode("bento.get($T.IT)", factoryNameFor((ClassName) ClassName.get(tm), "Factory"));
	                }
	            } else {
	                TypeMirror typeMirror = getT(byFactory);
	                TypeElement element = (TypeElement)types.asElement(typeMirror);
	                if (!element.getQualifiedName().toString().equals(DefaultFactory.class.getCanonicalName())) {
	                    source.addCode("bento.get($T.IT)", ClassName.get(element));
	                } else {
	                    source.addCode("bento.get($T.IT)", factoryNameFor((ClassName) ClassName.get(tm), "Factory"));
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
