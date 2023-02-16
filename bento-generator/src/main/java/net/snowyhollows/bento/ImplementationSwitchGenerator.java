package net.snowyhollows.bento;

import com.squareup.javapoet.*;
import net.snowyhollows.bento.annotation.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Set;

public class ImplementationSwitchGenerator extends AbstractProcessor {

    private Filer filer;

    private Messager messager;
    private Types types;
    private final static ClassName BENTO_FACTORY = ClassName.get(BentoFactory.class);
    private static final ClassName BENTO_EXCEPTION = ClassName.get(BentoException.class);
    private static final ClassName REFLECTIVE_EXCEPTION = ClassName.get(ReflectiveOperationException.class);

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        types = processingEnvironment.getTypeUtils();
    }

    private ClassName factoryNameFor(ClassName type, String suffix) {
        return ClassName.get(type.packageName(), type.simpleName() + suffix);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Set<? extends Element> pickerInterfaces =
                roundEnv.getElementsAnnotatedWith(ImplementationSwitch.class);

        for (Element element : pickerInterfaces) {
            TypeElement pickerInterface = (TypeElement) element;
            ClassName pickerClassName = ClassName.get(pickerInterface);

            ClassName factoryName = factoryNameFor(ClassName.get(pickerInterface), "Factory");
            String packageName = factoryName.packageName();
            ParameterizedTypeName bentoFactoryParametrized = ParameterizedTypeName.get(BENTO_FACTORY, pickerClassName);

            ImplementationSwitch implementationSwitch = element.getAnnotation(ImplementationSwitch.class);
            String configKey = implementationSwitch.configKey();

            CodeBlock.Builder code = CodeBlock.builder();

            if (configKey.equals(ImplementationSwitch.NO_CONFIG_KEY_DEFINED) && implementationSwitch.cases().length == 0) {
                code.addStatement("throw new $T($S)", BENTO_EXCEPTION, "Implementation of " + pickerClassName + " must be registered manually, e.g. by calling bento.register(" + factoryName.simpleName() + ".IT, someImplementation)");
            } else {
                code.addStatement("String configValue = bento.getString($S)", configKey);

                code.beginControlFlow("switch(configValue)");

                for (ImplementationSwitch.When when : implementationSwitch.cases()) {
                    try {
                        code.addStatement("case $S: return bento.get($T.IT)", when.name(), factoryNameFor((ClassName) ClassName.get(extractClassName(when)), "Factory"));
                    } catch (Exception e) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        PrintWriter writer = new PrintWriter(out);
                        writer.println(e.getMessage());
                        e.printStackTrace(writer);
                        writer.flush();
                        messager.printMessage(Diagnostic.Kind.ERROR, new String(out.toByteArray()));
                    }
                }
                String classNameRegexp = "([a-z][a-z0-9]*[.])+[A-Z][A-Za-z0-9]*";
                code.add("default: ")
                        .indent()
                        .beginControlFlow("if (configValue.matches($S))", classNameRegexp)
                        .beginControlFlow("try")
                        .addStatement("BentoFactory<?> it = (BentoFactory<?>) Class.forName(configValue + $S).getField($S).get(null)", "Factory", "IT")
                        .addStatement("return ($T)bento.get(it)", pickerClassName)
                        .endControlFlow()
                        .beginControlFlow("catch ($T e)", REFLECTIVE_EXCEPTION)
                        .addStatement("throw new $T($S + configValue + $S)", BENTO_EXCEPTION, "Cold not instantiate class ", " using a default BentoFactory")
                        .endControlFlow()
                        .endControlFlow()
                        .unindent();


                code.endControlFlow();
                code.addStatement("String message = $S + configValue + $S", "No case found for [", "]");
                code.addStatement("throw new $T(message)", BENTO_EXCEPTION);
            }


            MethodSpec.Builder createInContext = MethodSpec.methodBuilder("createInContext")
                    .addParameter(ParameterSpec.builder(Bento.class, "bento").build())
                    .addModifiers(Modifier.PUBLIC)
                    .addCode(code.build())
                    .returns(pickerClassName);

            TypeSpec.Builder factory = TypeSpec.enumBuilder(factoryName)
                    .addModifiers(Modifier.PUBLIC)
                    .addEnumConstant("IT")
                    .addSuperinterface(bentoFactoryParametrized)
                    .addMethod(createInContext.build());


            try {
                JavaFile.builder(packageName, factory.build()).build().writeTo(filer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    private TypeMirror extractClassName(ImplementationSwitch.When when) {
        try {
            when.implementation();
        } catch (MirroredTypeException mte) {
            TypeMirror typeMirror = mte.getTypeMirror();
            return typeMirror;
        }
        return null; // should never happen
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ImplementationSwitch.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
